package interfaces.controllers

import izumi.reflect.Tags.Tag
import play.api.mvc.{ Action, AnyContent, AnyContentAsEmpty, BodyParser, BodyParsers, Request, Result, WrappedRequest }
import zio.{ Has, Runtime, UIO, ZIO }

package object actions {
  class RequestWithAttachments[+A, +AT <: Has[_]](request: Request[A], val attachments: AT = Has(()))
      extends WrappedRequest(request) {
    def attach[B: Tag](attachment: B): RequestWithAttachments[A, AT with Has[B]] = {
      new RequestWithAttachments[A, AT with Has[B]](request, this.attachments.add(attachment))
    }

    def attachNone: RequestWithAttachments[A, AT with Has[Unit]] = attach(())
  }

  trait ZioActionFunction[-Env, +AT <: Has[_]] { self =>
    def invokeBlock[A, Env1, AT1 <: Has[_]](
      request: RequestWithAttachments[A, AT1],
      block: RequestWithAttachments[A, AT with AT1] => ZIO[Env1, Throwable, Result]
    ): ZIO[Env with Env1, Throwable, Result]

    def andThen[Env1, AT1 <: Has[_]](
      other: ZioActionFunction[Env1, AT1]
    ): ZioActionFunction[Env with Env1, AT with AT1] = {
      new ZioActionFunction[Env with Env1, AT with AT1] {
        override def invokeBlock[A, Env2, AT2 <: Has[_]](
          request: RequestWithAttachments[A, AT2],
          block: RequestWithAttachments[A, AT with AT1 with AT2] => ZIO[Env2, Throwable, Result]
        ): ZIO[Env with Env1 with Env2, Throwable, Result] = {
          self.invokeBlock(
            request, { r: RequestWithAttachments[A, AT with AT2] =>
              other.invokeBlock(r, { rr: RequestWithAttachments[A, AT with AT1 with AT2] =>
                block(rr)
              })
            }
          )
        }
      }
    }
  }

  trait ZioActionBuilder[B, -Env, +AT <: Has[_]] extends ZioActionFunction[Env, AT] { self =>
    def parser: BodyParser[B]

    def unsafeRun[Env1](
      block: => ZIO[Env1, Throwable, Result]
    )(implicit runtime: Runtime[Env with Env1]): Action[AnyContent] =
      unsafeRun(BodyParsers.utils.ignore(AnyContentAsEmpty: AnyContent))(_ => block)

    def unsafeRun[Env1](
      block: RequestWithAttachments[B, AT] => ZIO[Env1, Throwable, Result]
    )(implicit runtime: Runtime[Env with Env1]): Action[B] =
      unsafeRun(parser)(block)

    def unsafeRun[A, Env1](bodyParser: BodyParser[A])(
      block: RequestWithAttachments[A, AT] => ZIO[Env1, Throwable, Result]
    )(implicit runtime: Runtime[Env with Env1]): Action[A] = {
      composeAction(new Action[A] {
        def executionContext = runtime.platform.executor.asEC

        def parser = composeParser(bodyParser)

        def apply(request: Request[A]) =
          try {
            runtime.unsafeRunToFuture(invokeBlock(new RequestWithAttachments(request), block))
          } catch {
            // NotImplementedError is not caught by NonFatal, wrap it
            case e: NotImplementedError => throw new RuntimeException(e)
            // LinkageError is similarly harmless in Play Framework, since automatic reloading could easily trigger it
            case e: LinkageError => throw new RuntimeException(e)
          }
      })
    }

    protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] = bodyParser

    protected def composeAction[A](action: Action[A]): Action[A] = action

    override def andThen[Env1, AT1 <: Has[_]](
      other: ZioActionFunction[Env1, AT1]
    ): ZioActionBuilder[B, Env with Env1, AT with AT1] =
      new ZioActionBuilder[B, Env with Env1, AT with AT1] {
        override def parser: BodyParser[B] = self.parser

        protected override def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] =
          self.composeParser(bodyParser)

        protected override def composeAction[A](action: Action[A]): Action[A] = self.composeAction(action)

        override def invokeBlock[A, Env2, AT2 <: Has[_]](
          request: RequestWithAttachments[A, AT2],
          block: RequestWithAttachments[A, AT with AT1 with AT2] => ZIO[Env2, Throwable, Result]
        ): ZIO[Env with Env1 with Env2, Throwable, Result] = {
          self.invokeBlock(
            request, { r: RequestWithAttachments[A, AT with AT2] =>
              other.invokeBlock(r, { rr: RequestWithAttachments[A, AT with AT1 with AT2] =>
                block(rr)
              })
            }
          )
        }
      }
  }

  trait ZioActionRefiner[-Env, +AT <: Has[_]] extends ZioActionFunction[Env, AT] {
    protected def refine[A, AT1 <: Has[_]](
      request: RequestWithAttachments[A, AT1]
    ): ZIO[Env, Result, RequestWithAttachments[A, AT with AT1]]

    override def invokeBlock[A, Env1, AT1 <: Has[_]](
      request: RequestWithAttachments[A, AT1],
      block: RequestWithAttachments[A, AT with AT1] => ZIO[Env1, Throwable, Result]
    ): ZIO[Env with Env1, Throwable, Result] = {
      refine(request).foldM(UIO(_), block)
    }
  }
}
