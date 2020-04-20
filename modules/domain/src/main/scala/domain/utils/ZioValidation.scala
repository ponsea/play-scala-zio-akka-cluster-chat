package domain.utils

import zio.{ UIO, ZIO }

object ZioValidation {
  // same as ZIO.mapN except that await all results and combine errors
  def mapN[R, E, A, B, C](zio1: ZIO[R, E, A], zio2: ZIO[R, E, B])(f: (A, B) => C): ZIO[R, E, C] = {
    zio1.conzipWith(zio2)(f)
  }

  def mapN[R, E, A, B, C, D](zio1: ZIO[R, E, A], zio2: ZIO[R, E, B], zio3: ZIO[R, E, C])(
    f: (A, B, C) => D
  ): ZIO[R, E, D] = {
    (zio1 <+> zio2 <+> zio3).map {
      case ((a, b), c) => f(a, b, c)
    }
  }

  def mapN[R, E, A, B, C, D, F](
    zio1: ZIO[R, E, A],
    zio2: ZIO[R, E, B],
    zio3: ZIO[R, E, C],
    zio4: ZIO[R, E, D]
  )(f: (A, B, C, D) => F): ZIO[R, E, F] = {
    (zio1 <+> zio2 <+> zio3 <+> zio4).map {
      case (((a, b), c), d) => f(a, b, c, d)
    }
  }

  // same as ZIO.mapParN except that await all results and combine errors
  def mapParN[R, E, A, B, C](zio1: ZIO[R, E, A], zio2: ZIO[R, E, B])(f: (A, B) => C): ZIO[R, E, C] = {
    zio1.conzipWithPar(zio2)(f)
  }

  def mapParN[R, E, A, B, C, D](zio1: ZIO[R, E, A], zio2: ZIO[R, E, B], zio3: ZIO[R, E, C])(
    f: (A, B, C) => D
  ): ZIO[R, E, D] = {
    (zio1 <|> zio2 <|> zio3).map {
      case ((a, b), c) => f(a, b, c)
    }
  }

  def mapParN[R, E, A, B, C, D, F](
    zio1: ZIO[R, E, A],
    zio2: ZIO[R, E, B],
    zio3: ZIO[R, E, C],
    zio4: ZIO[R, E, D]
  )(f: (A, B, C, D) => F): ZIO[R, E, F] = {
    (zio1 <|> zio2 <|> zio3 <|> zio4).map {
      case (((a, b), c), d) => f(a, b, c, d)
    }
  }

  implicit class ZIOValidationOps[-R, +E, +A](self: ZIO[R, E, A]) {
    // same as ZIO#zipWith except that await all results and combine errors
    def conzipWith[R1 <: R, E1 >: E, B, C](that: ZIO[R1, E1, B])(f: (A, B) => C): ZIO[R1, E1, C] = {
      self.foldCauseM({ cause1 =>
        that.foldCauseM(
          cause2 => ZIO.halt(cause1 ++ cause2),
          _ => ZIO.halt(cause1)
        )
      }, { a =>
        that.foldCauseM(
          cause => ZIO.halt(cause),
          b => UIO(f(a, b))
        )
      })
    }

    // same as ZIO#zip except that await all results and combine errors
    def conzip[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, (A, B)] = {
      conzipWith(that)((a, b) => (a, b))
    }

    // alias for `conzip`
    def <+>[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, (A, B)] = {
      conzip(that)
    }

    // same as ZIO#zipRight except that await all results and combine errors
    def conzipRight[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, B] = {
      conzipWith(that)((_, b) => b)
    }

    // alias for `conzipRight`
    def +>[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, B] = {
      conzipRight(that)
    }

    // same as ZIO#zipLeft except that await all results and combine errors
    def conzipLeft[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, A] = {
      conzipWith(that)((a, _) => a)
    }

    // alias for `conzipLeft`
    def <+[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, A] = {
      conzipLeft(that)
    }

    // same as ZIO#zipWithPar except that await all results and combine errors
    def conzipWithPar[R1 <: R, E1 >: E, B, C](that: ZIO[R1, E1, B])(f: (A, B) => C): ZIO[R1, E1, C] = {
      for {
        fiber1 <- self.fork
        fiber2 <- that.fork
        exit1  <- fiber1.await
        exit2  <- fiber2.await
        result <- ZIO.done(exit1 <&> exit2)
      } yield f.tupled(result)
    }

    // same as ZIO#zipPar except that await all results and combine errors
    def conzipPar[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, (A, B)] = {
      conzipWithPar(that)((a, b) => (a, b))
    }

    // alias for `conzipPar`
    def <|>[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, (A, B)] = {
      self.conzipPar(that)
    }

    // same as ZIO#zipParRight except that await all results and combine errors
    def conzipParRight[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, B] = {
      conzipWithPar(that)((_, b) => b)
    }

    // alias for `conzipParRight`
    def |>[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, B] = {
      conzipParRight(that)
    }

    // same as ZIO#zipParLeft except that await all results and combine errors
    def conzipParLeft[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, A] = {
      conzipWithPar(that)((a, _) => a)
    }

    // alias for `conzipParLeft`
    def <|[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, A] = {
      conzipParLeft(that)
    }
  }
}
