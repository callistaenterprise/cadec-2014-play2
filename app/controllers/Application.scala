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



  /**
   * Simple action that displays the index page.
   * @return
   */
  def index = Action.async {
    Future {
      Ok(html.hello())
    }
  }


  def getAddress = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(formWithErrors => Future {
      BadRequest("Unable to parse form")
    },
      address => Future {
        Ok(address.toString)
      })
  }

  /**
   * Method that returns the location for an address as JSON.
   * Mapped to the GET verb in routes.
   *
   * @param address
   * @return
   */
  def getLocationForAddress_GET(address: String) = Action.async {
    Future(???) //Todo add instructions for exercise 4
  }




  /**
   * Method that binds an address from the request. Looks up the
   * locations for that address and then gets the weather for the
   * locations according to a defined strategy.
   * Mapped to the POST verb in routes
   *
   * @return
   */
  def getLocationWithWeather_POST = Action.async(parse.json) { implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest("Unable to parse form")
      },
      address => {
        ??? //Todo add instructions for exercise 6
      })
  }




  /**
   * Method that returns a location with weather for an address.
   * Mapped to the GET verb in routes
   *
   * @param address
   * @return
   */
  def getLocationsWithWeather_GET(address: String) = Action.async {
    getLocationsWithWeatherAsJson(address)
  }





  private def getLocationsWithWeatherAsJson(address: String): Future[SimpleResult] = {

    // Get a locations future
    val locationsF: Future[Seq[Location]] =
      ??? //Todo add instructions for exercise 5

    def getLocationsWithWeatherFuture(locations: Seq[Location]): Future[Seq[LocationWithWeather]] =
      ??? //Todo add instructions for exercise 5

    // Get weather for each location i future
    val locationsWithWeatherF: Future[Seq[LocationWithWeather]] =
      ??? //Todo add instructions for exercise 5

    // Transform the locationWithWeather elements to json and return the future
    ??? //Todo add instructions for exercise 5

  }


}