package util

import scala.concurrent.Future
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
   * @param elememtsInF Future[ Seq[T] ]
   * @param f closure that transforms a Seq[T] to a Seq[ Future[S] ]
   *
   * @return a Enumerator[ Try[S] ]
   */
  def locationWithWeatherEnumerator[T, S](elememtsInF: Future[Seq[T]], f: => Seq[T] => Seq[Future[S]])
  : Enumerator[Try[S]] =

    Concurrent.unicast[Try[S]] {
      channel =>
        elememtsInF onComplete {
          case Success(elememtsIn) => {
            val elementsOut = f(elememtsIn)
            elementsOut foreach {
              _ onComplete {
                case t => {
                  channel.push(t)
                  if (elementsOut.forall(_.isCompleted)) channel.eofAndEnd()
                }
              }

            }
          }
          case scala.util.Failure(t) => channel.end(t)
        }
    }
}
