package controllers

import models.JsonHelper._
import models.Location
import models.LocationWithWeather
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumeratee, Enumerator, Concurrent}
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc._
import providers.{LocationProvider, WeatherFetchStrategies, ConcreteProviders}
import scala.concurrent.Future


object WebSocketApplication extends Controller
  with LocationProvider
  with WeatherFetchStrategies
  with ConcreteProviders {

  private def getLocationsWithWeatherFutures(locations: Seq[Location]): Seq[Future[LocationWithWeather]] =
    locations.map(location => all(location))

  private def toEnumerator[T](f: Future[T]) = Enumerator.flatten(f.map(v => Enumerator(v)))

  private val locationToLocationWithWeather = Enumeratee.mapFlatten[Seq[Location]]{
    locations => Enumerator.interleave(
      getLocationsWithWeatherFutures(locations).map { locationWithWeatherF =>
        toEnumerator(locationWithWeatherF)
      }
    )
  }

  private val locationWithWeatherToJson = Enumeratee.map[LocationWithWeather] {
    case m => toJson(m)
  }

  /**
   * Method that returns a stream to be consumed by an HTML5 EventSource
   *
   * Try out with: curl -vN  http://localhost:9000/weatherstream/strandvagen
   *
   * @param address the address to search for
   * @return Chuncked stream
   */
  def getWeatherStream(address: String) = Action.async { request =>

    val locationEnumerator = toEnumerator(getLocations(address))

    Future(
      Ok.chunked(
        locationEnumerator through locationToLocationWithWeather through locationWithWeatherToJson through EventSource()
      ).as(EVENT_STREAM))
  }

  /**
   * WebSocket method that takes an address object { "address": "stockholm"} as input
   * and returns weather messages on the output WebSocket channel
   *
   * Try it out at http://www.websocket.org
   *
   * @return
   */
  def getWeatherWs = WebSocket.using[JsValue] { request =>

    val (iteratee, enumerator) = Concurrent.joined[JsValue]

    val addressJsToAddress = Enumeratee.map[JsValue]{
      addressJs => (addressJs \ "address").toString()
    }

    val addressToLocation = Enumeratee.mapFlatten[String]{
      address => toEnumerator(getLocations(address))
    }

    (iteratee, enumerator &> addressJsToAddress &> addressToLocation &> locationToLocationWithWeather &> locationWithWeatherToJson)
  }

}