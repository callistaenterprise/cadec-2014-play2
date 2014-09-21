package providers

import play.api.libs.ws.{WSResponse, WS}
import models.JsonHelper._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import models._
import org.joda.time.DateTime
import scala.xml.{NodeSeq, XML}
import org.joda.time.format.ISODateTimeFormat._
import models.Location
import models.LocationWithWeather
import util.FailureUtil._
import play.api.Play.current

trait WeatherProvider {
  def getLocationWithWeather(location: Location):Future[LocationWithWeather]
}

trait WeatherProviderImpl extends WeatherProvider {

  val providerName : String

  val parser : WSResponse => Weather

  lazy val providerUrl = config(s"$providerName.url")

  protected def format(baseUrl: String, location: Location): String

  protected def params(location: Location) : Seq[(String,String)]

  def getLocationWithWeather(location: Location) : Future[LocationWithWeather]  = {
    val serviceUrl = WS.url(
      format(providerUrl, location)
    ).withQueryString(params(location):_*)

    val responseF = serviceUrl.get()
    val weatherF = responseF map parser
    val weatherWithLocationF =  weatherF.map(weather => location.withWeather(providerName, weather))

    failF(weatherWithLocationF)
  }

}

/**
 * Provider for YR
 */
class YrProvider extends WeatherProviderImpl {

  val providerName = "yr"

  protected def format(baseUrl: String, location: Location) = baseUrl.format(location.lat, location.lng)

  protected def params(location: Location) = Seq("lat" -> location.lat, "lon" -> location.lng)

  val parser : WSResponse => Weather = { response =>
    val xml = XML.loadString(response.body)
    val v: (String, Seq[NodeSeq]) = (xml \\ "product" \ "time")
      .map(t => (
          (t \ "@from").text,
          (t \ "location" \ "temperature").map(_ \ "@value")
        )
      ).head
    Weather(DateTime.parse(v._1, dateTimeNoMillis), v._2.head.text)
  }
}

/**
 * Provider for SMHI
 */
class SmhiProvider extends WeatherProviderImpl {

  val providerName = "smhi"

  protected def format(baseUrl: String, location: Location) = baseUrl.format(location.lat, location.lng)

  protected def params(location: Location) = Seq.empty

  val parser: WSResponse => Weather = { response =>
    (response.json \ "timeseries").as[Seq[Weather]].filter(_.time.isAfterNow)(0)
  }

}

trait Providers {
  val providers: Map[String, WeatherProvider]
}

trait ConcreteProviders extends Providers {
  val providers = Map("yr" -> new YrProvider, "smhi" -> new SmhiProvider)
}
