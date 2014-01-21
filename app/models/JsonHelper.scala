package models

import play.api.libs.json.{Writes, JsPath, Reads}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._
import play.api.libs.functional.syntax._
import play.api.Play

object JsonHelper {
  val config = (key: String) => Play.current.configuration.getString(key).get

  implicit val locationReads: Reads[Location] = (
     (JsPath \ "geometry" \ "location" \ "lng").read[Double].map(_.toString.substring(0, 5)) and
      (JsPath \ "geometry" \ "location" \ "lat").read[Double].map(_.toString.substring(0, 5)) and
      (JsPath \ "formatted_address").read[String]
    )(Location)

  implicit val locationWrites: Writes[Location] = (
    (JsPath \ "lng").write[String] and
      (JsPath \ "lat").write[String] and
      (JsPath \ "address").write[String]
    )(unlift(Location.unapply))

  implicit val weatherReads: Reads[Weather] = (
      (JsPath \\ "validTime").read[String].map(DateTime.parse(_, dateTimeNoMillis)) and
      (JsPath \\ "t").read[Double].map(_.toString)
    )(Weather)

  implicit val weatherWrites: Writes[Weather] = (
      (JsPath \ "time").write[DateTime](Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ssZZ")) and
      (JsPath \ "temp").write[String]
    )(unlift(Weather.unapply))

  implicit val localWeatherWrites: Writes[LocationWithWeather] = (
      (JsPath \ "location").lazyWrite[Location](locationWrites) and
      (JsPath \ "temperatures").lazyWrite(Writes.mapWrites[Weather](weatherWrites))
    )(unlift(LocationWithWeather.unapply))

}
