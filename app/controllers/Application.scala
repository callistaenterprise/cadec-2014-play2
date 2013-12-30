package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{WS}
import scala.concurrent.{Future}
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import play.api.data.Forms._
import models._
import views._
import play.api.data._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._


object Application extends Controller {

  val config = Play.current.configuration
  type LocalTemperature = Map[Location, Map[String, String]]

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

  implicit val localWeatherWrites: Writes[LocationWithWeather] = (
      (JsPath \ "location").lazyWrite[Location](locationWrites) and
      (JsPath \ "temperatures").lazyWrite(Writes.mapWrites[Weather](weatherWrites))
    )(unlift(LocationWithWeather.unapply))

  implicit val weatherWrites: Writes[Weather] = (
      (JsPath \ "time").write[DateTime](Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ssZZ")) and
      (JsPath \ "temp").write[String]
    )(unlift(Weather.unapply))

  implicit val locationWrites: Writes[Location] = (
      (JsPath \ "lng").write[String] and
      (JsPath \ "lat").write[String] and
      (JsPath \ "address").write[String]
    )(unlift(Location.unapply))


  def getLocations(address: String): Future[Seq[Location]] = {
    WS.url(getConfig("maps.api")).withQueryString("address" -> address, "sensor" -> "false").get map (r =>(r.json \ "results").as[Seq[Location]])
  }

  def getWeather(locations: Seq[Location]): Future[Seq[LocationWithWeather]] = {
    Future.sequence(
    locations.map {
        location => {
          val smhi = WS.url(getConfig("smhi.url").format(location.lat, location.lng)).get map (r => loadCurrentTempFromSmhiForecast(r.json)) map(weather => location.withWeather("smhi", weather))
          val yr = WS.url(getConfig("yr.url").format(location.lat, location.lng)).get map (r => loadCurrentTempFromSmhiForecast(r.json)) map(weather => location.withWeather("yr", weather))
//          smhi.recoverWith {case _ => yr}
//          Future.firstCompletedOf(List(smhi, yr))
          Future.sequence(List(smhi, yr)).map(l => l.tail.fold[LocationWithWeather](l.head)(_ merge _))
        }
      }
     )
  }

  private def loadCurrentTempFromSmhiForecast(json: JsValue): Weather = {
    (json \ "timeseries").as[Seq[Weather]].filter(t => t.time.isAfterNow())(0)
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
    getLocations(address) flatMap {
      getWeather(_)
    } map (s => Ok(toJson(s)))
  }

  def index = Action.async {
    Future {
      Ok(html.main())
    }
  }
}