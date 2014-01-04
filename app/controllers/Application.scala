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

object Application extends Controller with LocationProvider with WeatherProviderStrategies with ConcreteProviders {

  /**
   * Describe the computer form (used in both edit and create screens).
   */
  val addressForm = Form(
    mapping(
      "address" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )


  def getWeather(locations: Seq[Location]): Future[Seq[LocationWithWeather]] =  Future.sequence(locations map smhi)


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
    getWeatherAsJson(address)
  }

  def getWeatherAsJson(address: String) = {
    getLocations(address)
      .flatMap(getWeather(_))
      .map(s => Ok(toJson(s)))
  }

  def index = Action.async {
    Future {
      Ok(html.main())
    }
  }
}