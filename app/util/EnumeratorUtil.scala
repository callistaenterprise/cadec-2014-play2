package util

import scala.concurrent.Future
import play.api.libs.iteratee.{Enumeratee, Concurrent, Enumerator}
import scala.util.{Failure, Try, Success}

import play.api.libs.concurrent.Execution.Implicits._
import models.{Location, LocationWithWeather}
import play.api.libs.json.Json._
import play.api.libs.json.JsValue

import models.JsonHelper._

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

  def locationWithWeatherToJson = Enumeratee.map[Try[LocationWithWeather]] {
    case Success(m) => toJson(m)
    case Failure(_) => ???
  }

  def addressToLocationWithWeatherEnumerator(locations: (String) => Future[Seq[Location]], f: => Seq[Location] => Seq[Future[LocationWithWeather]]) = Enumeratee.mapFlatten{
    address : JsValue =>
      val a = (address \ "address").toString()
      locationWithWeatherEnumerator(locations(a), f) through locationWithWeatherToJson
  }

}
