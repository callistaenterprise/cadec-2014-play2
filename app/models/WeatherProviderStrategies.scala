package models

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

trait WeatherProviderStrategies {
  this: Providers =>

  val smhi: Location => Future[LocationWithWeather] = { location =>
    providers("smhi").getLocationWithWeather(location)
  }

  val yr: Location => Future[LocationWithWeather] = { location =>
    providers("yr").getLocationWithWeather(location)
  }

  val firstCompleted: Location => Future[LocationWithWeather] = { location =>
    val weatherF = providers.values.map(_.getLocationWithWeather(location))
    Future.firstCompletedOf(weatherF)
  }

  val withRecovery: Location => Future[LocationWithWeather] = { location =>
    smhi(location).recoverWith {
       case _ => yr(location)
    }
  }

  val all: Location => Future[LocationWithWeather] = { location =>
      Future.sequence(
        providers.values
          .map(_.getLocationWithWeather(location)))
          .map(l => l.tail.fold[LocationWithWeather](l.head)(_ merge _)
      )
  }

}
