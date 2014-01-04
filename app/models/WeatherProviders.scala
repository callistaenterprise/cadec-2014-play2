package models

import play.api.libs.ws.WS
import models.JsonHelper._
import scala.concurrent.Future
import play.api.libs.ws.Response
import play.api.libs.concurrent.Execution.Implicits._


trait WeatherProvider {
  def getLocationWithWeather(location: Location):Future[LocationWithWeather]
}


trait WeatherProviderImpl extends WeatherProvider {

  val providerName : String

  val parser : Response => Weather

  lazy val providerUrl = config(s"$providerName.url")

  protected def format(baseUrl: String, location: Location): String

  def getLocationWithWeather(location: Location) : Future[LocationWithWeather]  = {
    val serviceUrl = WS.url(format(providerUrl, location))
    val responseF = serviceUrl.get()
    val weatherF = responseF map parser
    val weatherWithLocationF =  weatherF.map(weather => location.withWeather(providerName, weather))

    weatherWithLocationF
  }

}

class YrProvider extends WeatherProviderImpl {

  val providerName = "yr"

  protected def format(baseUrl: String, location: Location) = baseUrl.format(location.lat, location.lng)

  val parser : Response => Weather = { response =>
    (response.json \ "timeseries").as[Seq[Weather]].filter(_.time.isAfterNow)(0)
  }

}

class SmhiProvider extends WeatherProviderImpl {

  val providerName = "smhi"

  protected def format(baseUrl: String, location: Location) = baseUrl.format(location.lat, location.lng)

  val parser: Response => Weather = { response =>
    (response.json \ "timeseries").as[Seq[Weather]].filter(_.time.isAfterNow)(0)
  }

}

trait Providers {
  val providers: Map[String, WeatherProvider]
}

trait ConcreteProviders extends Providers {
  val providers = Map("yr" -> new YrProvider, "smhi" -> new SmhiProvider)
}