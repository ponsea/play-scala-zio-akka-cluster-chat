package domain.utils

import zio._
import zio.logging.{ LogContext, LogLevel, Logging }

class Logger(underlying: Logging.Service) {
  def apply(line: => String): UIO[Unit] =
    underlying.logger.log(line)

  def apply(level: LogLevel)(line: => String): UIO[Unit] =
    underlying.logger.log(level)(line)

  def context: UIO[LogContext] =
    underlying.logger.logContext

  def debug(line: => String): UIO[Unit] =
    apply(LogLevel.Debug)(line)

  def error(line: => String): UIO[Unit] =
    apply(LogLevel.Error)(line)

  def error(line: => String, cause: Cause[Any]): UIO[Unit] =
    apply(LogLevel.Error)(line + System.lineSeparator() + cause.prettyPrint)

  def info(line: => String): UIO[Unit] =
    apply(LogLevel.Info)(line)

  def locally[R, E, A](fn: LogContext => LogContext)(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    underlying.logger.locally(fn)(zio)

  def locallyM[R, E, A](
    fn: LogContext => URIO[R, LogContext]
  )(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    underlying.logger.locallyM(fn)(zio)

  def throwable(line: => String, t: Throwable): UIO[Unit] =
    error(line, Cause.die(t))

  def trace(line: => String): UIO[Unit] =
    apply(LogLevel.Trace)(line)

  def warn(line: => String): UIO[Unit] =
    apply(LogLevel.Warn)(line)
}

object Logger {
  val layer = ZLayer.fromService[Logging.Service, Logger](logger => new Logger(logger))
}
