package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future
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
  val addressForm = Form(
    mapping(
      "name" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )

  val getConfig = (key: String) => config.getString(key).get

  implicit val locationReads: Reads[Location] = (
    (JsPath \ "geometry" \ "location" \ "lng").read[Double] and
      (JsPath \ "geometry" \ "location" \ "lat").read[Double] and
      (JsPath \ "formatted_address").read[String]
    )(Location)

  implicit val weatherReads: Reads[Weather] = (
    (JsPath \\ "validTime").read[String].map(DateTime.parse(_, dateTimeNoMillis)) and
      (JsPath \\ "t").read[Double].map(_.toString)
    )(Weather)


  def getLocations(address: String): Future[Seq[Location]] = {
    val response = WS.url(getConfig("maps.api")).withQueryString("address" -> address, "sensor" -> "false")
      .get
    response map (r => (r.json \ "results").as[Seq[Location]])
  }

  def getWeather(locations: Seq[Location]): Future[Seq[LocalWeather]] = {
    Future.sequence(
      locations.map {
        location =>
          val response = WS.url(getConfig("smhi.url").format(location.lat.toString.substring(0, 5), location.lng.toString.substring(0, 5))).get
          response map (r => loadCurrentTempFromForecasts(r.json, location))
      }
    )
  }

  private def loadCurrentTempFromForecasts(json: JsValue, l: Location): LocalWeather = {
    val w =(json \ "timeseries").as[Seq[Weather]].filter(t => t.time.isAfterNow())(0)
    l.withWeather(w)
  }


  def weather() = Action.async {
    implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest(html.index(formWithErrors))
      },
        address => {
          getLocations(address.name) flatMap {
            getWeather(_)
          } map (s => Ok(html.temp(s)))
        })
  }

  /**
   * Handle default path requests, redirect to computers list
   */
  def index = Action.async {
    Future {
      Ok(html.index(addressForm))
    }
  }

}