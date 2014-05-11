package providers

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import util.WithTiming._
import models.{LocationWithWeather, Location}

trait WeatherFetchStrategies {
  this: Providers =>

  def provider(name: String): Location => Future[LocationWithWeather] = { location =>
    providers(name).getLocationWithWeather(location).withTiming
  }

  val smhi: (Location) => Future[LocationWithWeather] = provider("smhi")

  val yr: (Location) => Future[LocationWithWeather] = provider("yr")

  val firstCompleted: Location => Future[LocationWithWeather] = { location =>
    Future.firstCompletedOf(Seq(smhi(location), yr(location)))
  }

  val withRecovery: Location => Future[LocationWithWeather] = { location =>
    smhi(location).recoverWith {
       case _ => yr(location)
    }
  }

  /**
   * Collects weather from all providers and merge the result into a
   * LocationWithWeather.
   */
  val all: Location => Future[LocationWithWeather] = { location =>

    Future.sequence(
        providers.values
          .map(f => f.getLocationWithWeather(location).withTiming))
          .map(l => l.reduce(_ merge _)
      )
  }

}
