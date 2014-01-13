package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc._
import providers.{LocationProvider, WeatherFetchStrategies, ConcreteProviders}
import scala.concurrent.Future
import util.EnumeratorUtil._
import views._
import models.JsonHelper._


object WebSocketApplication extends Controller
  with LocationProvider
  with WeatherFetchStrategies
  with ConcreteProviders {

  private def getLocationsWithWeatherFutures(locations: Seq[Location]): Seq[Future[LocationWithWeather]] =
    locations.map(location => all(location))


  /**
   * Method that returns a stream to be consumed by an HTML5 EventSource
   *
   * Try out with: curl -vN  http://localhost:9000/weatherstream/strandvagen
   *
   * @param address the address to search for
   * @return Chuncked stream
   */
  def getWeatherStream(address: String) = Action.async { request =>

    // Helper method that creates the enumerator
    val enumerator = locationWithWeatherEnumerator(getLocations(address), getLocationsWithWeatherFutures)

    Future(
      Ok.chunked(
        enumerator through locationWithWeatherToJson through EventSource()
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

    (iteratee, enumerator through addressToLocationWithWeatherEnumerator(getLocations, getLocationsWithWeatherFutures))
  }

}