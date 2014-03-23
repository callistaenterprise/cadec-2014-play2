package controllers

import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumeratee, Enumerator, Concurrent}
import play.api.libs.json.JsValue
import play.api.mvc._
import providers.{LocationProvider, WeatherFetchStrategies, ConcreteProviders}
import scala.concurrent.Future
import play.api.libs.json.Json._
import models.Location
import models.LocationWithWeather
import models.JsonHelper._

object WebSocketApplication extends Controller
  with LocationProvider
  with WeatherFetchStrategies
  with ConcreteProviders {

  private def getLocationsWithWeatherFutures(locations: Seq[Location]): Seq[Future[LocationWithWeather]] =
    locations.map(location => all(location))

  private val locationToLocationWithWeather: Enumeratee[Seq[Location], Option[LocationWithWeather]] = Enumeratee.mapFlatten {
    locations => Enumerator.interleave(
      getLocationsWithWeatherFutures(locations).map { locationWithWeatherF =>
        Enumerator.flatten(locationWithWeatherF.map(v => Enumerator(Option(v))))
      }
    ) andThen Enumerator(None)
  }

  private val locationWithWeatherToJson: Enumeratee[Option[LocationWithWeather], JsValue] = Enumeratee.map {
    case Some(locationWithWeather) => toJson(locationWithWeather)
    case None => toJson(Map("status" -> "end"))
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

    val locationsEnumerator = Enumerator.flatten(getLocations(address).map(v => Enumerator(v)))

    Future(
      Ok.chunked(
        locationsEnumerator through locationToLocationWithWeather through locationWithWeatherToJson through EventSource()
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

    val addressJsToAddress: Enumeratee[JsValue, String] = Enumeratee.map {
      addressJs => (addressJs \ "address").toString()
    }

    val locations: Enumeratee[String, Seq[Location]] = Enumeratee.mapFlatten {
      address => Enumerator.flatten(getLocations(address).map(v => Enumerator(v)))
    }

    (iteratee, enumerator &> addressJsToAddress &> locations &> locationToLocationWithWeather &> locationWithWeatherToJson)
  }

}