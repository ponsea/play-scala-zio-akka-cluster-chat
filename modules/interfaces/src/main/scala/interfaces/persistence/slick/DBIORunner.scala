package interfaces.persistence.slick

import slick.dbio.{ DBIOAction, Effect, NoStream, Streaming }
import zio._
import zio.stream.ZStream

class DBIORunner(databasePair: DatabasePair) {
  private val masterDb   = databasePair.master
  private val readonlyDb = databasePair.readonly

  def run[R](action: DBIOAction[R, NoStream, Effect.All]): UIO[R] = {
    ZIO.fromFuture(_ => masterDb.run(action)).orDie
  }

  def runReadonly[R](action: DBIOAction[R, NoStream, Effect.Read]): UIO[R] = {
    ZIO.fromFuture(_ => readonlyDb.run(action)).orDie
  }

  def stream[T](action: DBIOAction[_, Streaming[T], Effect.Read]): ZStream[Any, Throwable, T] = {
    import zio.interop.reactivestreams._
    readonlyDb.stream(action).toStream()
  }
}

object DBIORunner {
  val layer = ZLayer.fromService[DatabasePair, DBIORunner](new DBIORunner(_))
}
