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
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._


object Application extends Controller {

  val config = Play.current.configuration

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
      (__ \ "geometry" \ "location" \ "lat").read[Double] and
      (__ \ "formatted_address").read[String]
    )(Location)

  implicit val weatherReads: Reads[Weather] = (
      (__ \\ "validTime").read[String].map(DateTime.parse(_, dateTimeNoMillis)) and
      (__ \\ "t").read[Double].map(_.toString)
    )(Weather)


  def getGeoCodes(address: String): Future[Response] = {
    WS.url(config.getString("maps.api").get)
      .withQueryString("address" -> address, "sensor" -> "false")
      .get
  }

  def getWeather(location: Location): Future[Response] = {
    WS.url(config.getString("smhi.url").get.format(location.lat.toString.substring(0,5), location.lng.toString.substring(0,5)))
    .get
  }

  def loadLocationsFromGeoCodes(json: JsValue): Seq[(Location)] = {
    (json \ "results").as[Seq[Location]]
  }

  def loadCurrentTempFromForecasts(json: JsValue):Weather = {
    (json \ "timeseries").as[Seq[Weather]].filter(t => t.time.isAfterNow())(0)
  }

  def weather() = Action.async { implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future{BadRequest(html.index(formWithErrors))},
        address => {
          for {
            geoCodes <- getGeoCodes(address.name)
            locations = loadLocationsFromGeoCodes(geoCodes.json)
            futureForecasts = locations.map(location => getWeather(location))
            forecasts <- Future.sequence(futureForecasts)
            temperatures = forecasts map (x => loadCurrentTempFromForecasts(x.json))
          } yield Ok(html.temp(locations zip temperatures))
        })
  }

  /**
   * Handle default path requests, redirect to computers list
   */
  def index = Action.async {
    Future{Ok(html.index(addressForm))}
  }

}