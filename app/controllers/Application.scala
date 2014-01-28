package controllers

import models._
import models.JsonHelper._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import providers.{LocationProvider, WeatherFetchStrategies, ConcreteProviders}
import scala.concurrent.Future
import play.api.libs.json.Json._


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
  def getLocationForAddress_GET(address: String) = Action.async {
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
  def getLocationWithWeather_POST = Action.async(parse.json) {
    implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest("Unable to parse form")
      },
        address => {
          getLocationsWithWeatherAsJson(address.address)
        })
  }


  /**
   * Method that returns a location with weather for an address.
   * Mapped to the GET verb and the path /locations/:address in routes
   *
   * @param address
   * @return
   */
  def getLocationsWithWeather_GET(address: String) = Action.async {
    getLocationsWithWeatherAsJson(address)
  }


  private def getLocationsWithWeatherFuture(locations: Seq[Location]): Future[Seq[LocationWithWeather]] = {
    Future.sequence(locations.map(location => smhi(location)))
  }

  private def getLocationsWithWeatherAsJson(address: String): Future[SimpleResult] = {
    for {
      locations <- getLocations(address)
      locationsWithWeather <- getLocationsWithWeatherFuture(locations)
    } yield Ok(toJson(locationsWithWeather))
  }

}