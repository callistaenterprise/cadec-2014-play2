package util

import scala.concurrent.Future
import models.{LocationWithWeather, Location}
import play.api.libs.iteratee.{Concurrent, Enumerator}
import scala.util.{Success, Try}

import play.api.libs.concurrent.Execution.Implicits._

/**
 *
 */
object EnumeratorUtil {

  /**
   * Ugly method for converting from Future[ Seq[T] ] to Enumerator[Try[S]]
   *
   * @param locationsF Future[ Seq[T] ]
   * @param f closure that transforms a Seq[T] to a Seq[ Future[S] ]
   *
   * @return a Enumerator[ Try[S] ]
   */
  def locationWithWeatherEnumerator[T,S](locationsF: Future[Seq[T]], f: => Seq[T] => Seq[Future[S]])
  : Enumerator[Try[S]] =

    Concurrent.unicast[Try[S]] {
      channel =>
        locationsF onComplete {
          case Success(locations) => {
            val locationsWithWeatherF = f(locations)
            locationsWithWeatherF foreach {
              locationF =>
                locationF onComplete {
                  case t => {
                    channel.push(t)
                    if (locationsWithWeatherF.forall(_.isCompleted)) channel.end()
                  }
                }

            }
          }
          case scala.util.Failure(t) => ???  // TODO:
        }
    }
}
