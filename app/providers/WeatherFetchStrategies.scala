package providers

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import util.WithTiming._
import models.{LocationWithWeather, Location}

trait WeatherFetchStrategies {
  this: Providers =>

  def provider(name: String): Location => Future[LocationWithWeather] = { location =>
    providers(name).getLocationWithWeather(location) withTiming
  }

  val smhi: (Location) => Future[LocationWithWeather] = provider("smhi")

  val yr: (Location) => Future[LocationWithWeather] = provider("yr")

  val firstCompleted: Location => Future[LocationWithWeather] = { location =>
    ??? //Todo add instructions
  }

  val withRecovery: Location => Future[LocationWithWeather] = { location =>
    ??? //Todo add instructions
  }

  val all: Location => Future[LocationWithWeather] = { location =>
    ??? //Todo add instructions
  }

}
