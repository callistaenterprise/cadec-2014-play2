package models

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

trait WeatherProviderStrategies {
  this: SmhiProvider with YrProvider =>

  val smhi: Location => Future[LocationWithWeather] = { location =>
    getLocationWithSmhiWeather(location)
  }

  val firstCompleted: Location => Future[LocationWithWeather] = { location =>
      Future.firstCompletedOf(Seq(getLocationWithSmhiWeather(location), getLocationWithYrWeather(location)))
  }

  val withRecovery: Location => Future[LocationWithWeather] = { location =>
      getLocationWithSmhiWeather(location).recoverWith {
        case _ => getLocationWithYrWeather(location)
      }
  }

  val all: Location => Future[LocationWithWeather] = { location =>
      Future.sequence(
        Seq(getLocationWithSmhiWeather(location),
            getLocationWithYrWeather(location)))
      .map(xs => xs.tail
        .fold(xs.head)(_ merge _))
  }
}
