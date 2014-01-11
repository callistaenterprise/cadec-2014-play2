package providers

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import util.WithTiming._
import models.{LocationWithWeather, Location}

trait WeatherProviderStrategies {
  this: Providers =>

  def provider(name: String): Location => Future[LocationWithWeather] = { location =>
    providers(name).getLocationWithWeather(location) withTiming
  }

  val smhi = provider("smhi")

  val yr = provider("yr")

  val firstCompleted: Location => Future[LocationWithWeather] = { location =>
    val weatherF = providers.values.map(_.getLocationWithWeather(location) withTiming)
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
          .map(f => f.getLocationWithWeather(location) withTiming))
          .map(l => l.tail.fold[LocationWithWeather](l.head)(_ merge _)
      )
  }

}
