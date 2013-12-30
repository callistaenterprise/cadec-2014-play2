package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{WS}
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.json.Json._
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
      "address" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )

  val getConfig = (key: String) => config.getString(key).get

  implicit val locationReads: Reads[Location] = (
    (JsPath \ "geometry" \ "location" \ "lng").read[Double].map(_.toString.substring(0,5)) and
    (JsPath \ "geometry" \ "location" \ "lat").read[Double].map(_.toString.substring(0,5)) and
    (JsPath \ "formatted_address").read[String]
  )(Location)

  implicit val weatherReads: Reads[Weather] = (
    (JsPath \\ "validTime").read[String].map(DateTime.parse(_, dateTimeNoMillis)) and
    (JsPath \\ "t").read[Double].map(_.toString)
  )(Weather)

  implicit val localWeatherWrites: Writes[models.LocalWeather] = (
    (JsPath \ "lng").write[String] and
    (JsPath \ "lat").write[String] and
    (JsPath \ "temp").lazyWrite(Writes.mapWrites[String])
  )(unlift(models.LocalWeather.unapply))


  def getLocations(address: String): Future[Seq[Location]] = {
    val response = WS.url(getConfig("maps.api")).withQueryString("address" -> address, "sensor" -> "false").get.filter (_.status == OK)
    response map (r =>(r.json \ "results").as[Seq[Location]])
  }

  def getWeatherFromSmhi(locations: Seq[Location]): Future[Seq[LocalWeather]] = {
    Future.sequence(
      locations.map {
        location =>
          val response = WS.url(getConfig("smhi.url").format(location.lat, location.lng)).get.filter(_.status == OK)
          response map (r => loadCurrentTempFromForecasts(r.json, location))
      }
    )
  }

  private def loadCurrentTempFromForecasts(json: JsValue, l: Location): LocalWeather = {
    val w = (json \ "timeseries").as[Seq[Weather]].filter(t => t.time.isAfterNow())(0)
    l.withWeather("smhi", w)
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
    getWeatherAsJson(address)
  }

  def getWeatherAsJson(address: String) = {
    println("address: " + address)
    getLocations(address) flatMap {
      getWeatherFromSmhi(_)
    } map (s => Ok(toJson(s)))
  }

  def index = Action.async {
    Future {
      Ok(html.main())
    }
  }
}