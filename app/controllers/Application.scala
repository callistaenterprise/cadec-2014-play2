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
import scala.util.{Success, Try}
import play.api.libs.iteratee.{Enumerator, Concurrent, Enumeratee}
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


  def index = Action.async {
    Future {
      Ok(html.main())
    }
  }

  def weatherPost() = Action.async(parse.json) {
    implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest("Unable to parse form")
      },
      address => {
        getWeatherAsJson(address.address)
      })
  }

  def weatherGet(address: String) = Action.async {
    getLocations(address)
      .flatMap(getWeather)
      .map(s => Ok(toJson(s)))
  }

  private def getWeather(locations: Seq[Location]): Future[Seq[LocationWithWeather]] =  Future.sequence(locations map all)

  private def locationsToLocationsWithWeatherF(locations: Seq[Location]): Seq[Future[LocationWithWeather]] = locations map all

  private def getWeatherAsJson(address: String) =
    getLocations(address)
      .flatMap(getWeather)
      .map(s => Ok(toJson(s)))

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
    val enumerator = locationWithWeatherEnumerator(getLocations(address), locationsToLocationsWithWeatherF)

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
        locationWithWeatherEnumerator(getLocations(a), locationsToLocationsWithWeatherF) through formatMessage
    }

    (f(iteratee), enumerator)
  }

}