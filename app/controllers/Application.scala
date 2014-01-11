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


object Application extends Controller
  with LocationProvider
  with WeatherFetchStrategies
  with ConcreteProviders {

  /**
   * Describe the computer form (used in both edit and create screens).
   */
  val addressForm = Form(
    mapping(
      "address" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )

//  def index = Action.async {
//    Future {
//      Ok(html.simpleform(addressForm))
//    }
//  }
  /**
   * Simple action that displays the index page.
   * @return
   */
  def index = Action.async {
    Future {
      Ok(html.main())
    }
  }

  /**
   * Method that returns the location for an address as JSON.
   * Mapped to the GET verb in routes.
   *
   * @param address
   * @return
   */
  def getLocationForAddressGet(address: String) = Action.async {
    getLocations(address).map(s => Ok(toJson(s)))
  }

  /**
   * Method that binds an address from the request. Looks up the
   * locations for that address and then gets the weather for the
   * locations according to a defined strategy.
   * Mapped to the POST verb in routes
   *
   * @return
   */
//  def getLocationWithWeatherPost = Action.async /*(parse.json)*/ {
  def getLocationWithWeatherPost = Action.async(parse.json) {
    implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest("Unable to parse form")
      },
      address => {
        // Future(Ok(address.toString))
        getLocationsWithWeatherAsJson(address.address)
      })
  }

  /**
   * Method that returns a location with weather for an address.
   * Mapped to the GET verb in routes
   *
   * @param address
   * @return
   */
  def getLocationsWithWeatherGet(address: String) = Action.async {
    getLocationsWithWeatherAsJson(address)
  }

  // TODO: Explain helpers
  //
  private def getLocationsWithWeatherFuture(locations: Seq[Location]): Future[Seq[LocationWithWeather]] =
    Future.sequence(locations.map(location =>  all(location)))

  private def getLocationsWithWeatherFutures(locations: Seq[Location]): Seq[Future[LocationWithWeather]] =
    locations.map(location => all(location))

  private def getLocationsWithWeatherAsJson(address: String): Future[SimpleResult] = {
    // Get a locations future
    val locationsF: Future[Seq[Location]] = getLocations(address)

    // Get weather for each location i future
    val locationsWithWeatherF: Future[Seq[LocationWithWeather]] = locationsF.flatMap(getLocationsWithWeatherFuture)

    // Transform the locationWithWeather elements to json and return the future
    locationsWithWeatherF.map(s => Ok(toJson(s)))
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