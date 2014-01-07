package util

import scala.concurrent.Future
import play.Logger

import play.api.libs.concurrent.Execution.Implicits._

/**
 *
 */
object WithTiming {

  def apply[T](f: => Future[T]) = {
    val startTime = System.currentTimeMillis
    Logger.info(s"START $f")
    f.map { case r =>
      val latency = System.currentTimeMillis() - startTime
      Logger.info(s"Future $f took $latency ms to process")
      r
    }
  }

}
