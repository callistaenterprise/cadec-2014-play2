package util

import play.api.{Logger, Play}
import scala.util.Random
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 *
 */
object FailureUtil {
  val failPercent = Play.current.configuration.getString("fail.percent").get.toInt

  def fail() = if(failPercent != 0 && Random.nextInt(100) < failPercent) {
    Logger.warn("Controlled fail")
    sys.error("Controlled Failure")
  }

  def failF[T](f: Future[T]) : Future[T] = f.map(s => { fail(); s } )
}