package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{Response, WS}
import scala.concurrent.{Await, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.Forms._
import views._
import models._
import play.api.data._

object Application extends Controller {

  /**
   * Describe the computer form (used in both edit and create screens).
   */
  val addressForm = Form (
    mapping(
      "name" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )


  implicit val locationReads: Reads[Location] = (
    (__ \ "geometry" \ "location" \ "lng").read[Double] and
      (__ \ "geometry" \ "location" \ "lat").read[Double]
    )(Location)

  implicit val weatherReads: Reads[Weather] = (
    (__ \\ "validTime").read[String] and
      (__ \\ "t").read[Double]
    )(Weather)


  def getGeoCodes(address: String): Future[Response] = {
    WS.url(Play.current.configuration.getString("maps.api").get)
      .withQueryString("address" -> address, "sensor" -> "false")
      .get
  }

  def getWeather(location: Location): Future[Response] = {
    val url = WS.url(Play.current.configuration.getString("smhi.url").get.format(location.lat.toString.substring(0,5), location.lng.toString.substring(0,5)))
    println(url)
    url get
  }

  def loadLocationsFromGeoCodes(json: JsValue): Seq[Future[Response]] = {
    (json \ "results").as[Seq[Location]] map (location => getWeather(location))
  }

  def loadTempFromForecasts(json: JsValue):Future[Seq[Weather]] = {
    Future{(json \ "timeseries").as[Seq[Weather]]}
  }

  def weather() = Action.async { implicit request =>
    Ok("asdfadf")
      addressForm.bindFromRequest.fold(formWithErrors => Future{BadRequest(html.index(formWithErrors))},
        address => {
          for {
            geoCodes <- getGeoCodes(address.name)
            futureLocations = loadLocationsFromGeoCodes(geoCodes.json)
            forecasts <- Future.sequence(futureLocations)
            futureTemperatures = forecasts.map(f => loadTempFromForecasts(f.json))
            temperatures <- Future.sequence(futureTemperatures.map(x => x))
          } yield Ok(html.temp(temperatures(0)(0)))
        })
  }

  /**
   * Handle default path requests, redirect to computers list
   */
  def index = Action.async {
    Future{Ok(html.index(addressForm))}
  }

}