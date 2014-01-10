package providers

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import util.WithTiming
import providers.Providers
import models.{LocationWithWeather, Location}

trait WeatherFetchStrategies {
  this: Providers =>

  def provider(name: String): Location => Future[LocationWithWeather] = { location =>
    providers(name).getLocationWithWeather(location)
  }

  val smhi = provider("smhi")

  val yr = provider("yr")

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
          .map(f => WithTiming(f.getLocationWithWeather(location))))
          .map(l => l.tail.fold[LocationWithWeather](l.head)(_ merge _)
      )
  }

}
