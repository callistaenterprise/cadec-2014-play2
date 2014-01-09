package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.libs.json.Json._
import play.api.data.Forms._
import models._
import views._
import play.api.data._
import JsonHelper._
import scala.util.Try
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.EventSource
import util.EnumeratorUtil._
import scala.util.Success
import models.Location
import models.LocationWithWeather
import models.Address
import play.api.libs.json.JsValue

object Application extends Controller with LocationProvider with WeatherProviderStrategies with ConcreteProviders {

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
  private def getLocationsWithWeatherFuture(locations: Seq[Location]): Future[Seq[LocationWithWeather]] =  Future.sequence(locations map all)

  private def getLocationsWithWeatherFutures(locations: Seq[Location]): Seq[Future[LocationWithWeather]] = locations map all

  private def getLocationsWithWeatherAsJson(address: String): Future[SimpleResult] = {
    // Get a locations future
    val locationsF: Future[Seq[Location]] = getLocations(address)

    // Get weather for each location i future
    val locationsWithWeatherF: Future[Seq[LocationWithWeather]] = locationsF.flatMap(getLocationsWithWeatherFuture)

    // Transform the locationWithWeatehr elements to json and return the future
    locationsWithWeatherF.map(s => Ok(toJson(s)))
  }
  /**
   * Method that returns a stream to be consumed by an HTML5 EventSource
   *
   * Try out with: curl -vN  http://localhost:9000/weatherstream/strandvägen
   *
   * @param address the address to search for
   * @return Chuncked stream
   */
  def getWeatherStream(address: String) = Action.async { request =>
    import util.EnumeratorUtil._


    // Helper method that creates the enumerator
    val enumerator = locationWithWeatherEnumerator(getLocations(address), getLocationsWithWeatherFutures)

    // Enumeratee that filters failures and formats the LocationWithWeather to a json object
    val formatMessage = Enumeratee.map[Try[LocationWithWeather]] {
      case Success(m) => toJson(m)
    }

    Future(Ok.chunked(enumerator through formatMessage through EventSource()).as(EVENT_STREAM))
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

    val formatMessage = Enumeratee.map[Try[LocationWithWeather]] {
       case Success(m) => toJson(m)
    }

    val f = Enumeratee.mapFlatten{
      address : JsValue =>
        val a = (address \ "address").toString()
        locationWithWeatherEnumerator(getLocations(a), getLocationsWithWeatherFutures) through formatMessage
    }

    (iteratee, enumerator through f)
  }

}