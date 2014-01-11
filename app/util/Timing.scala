package util

import scala.concurrent.Future
import play.Logger

import play.api.libs.concurrent.Execution.Implicits._

object WithTiming {

  implicit def futureWithTiming[T](f: Future[T]) = new {
    def withTiming = WithTiming(f)
  }

  implicit def apply[T](f: => Future[T]): Future[T] = {
    val startTime = System.currentTimeMillis
    Logger.info(s"START $f")
    f.map { case r =>
      val latency = System.currentTimeMillis() - startTime
      Logger.info(s"Future $f took $latency ms to process")
      r
    }
  }

}
