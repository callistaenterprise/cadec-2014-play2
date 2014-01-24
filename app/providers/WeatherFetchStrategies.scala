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
    Future.firstCompletedOf(Seq(smhi(location), yr(location)))
  }

  val withRecovery: Location => Future[LocationWithWeather] = { location =>

  /**
   * Övning 6
   * En future har en funktion, recoverWith, som kan köras om den fallerar.
   * Använd denna för att slå mot yr om smhi skulle råka vara nere.
   *
   * Testa genom att byta ut strategin vi använder i getLocationsWithWeatherFuture
   * i Application.scala. För att testa felhanteringen kan du gå in i
   * application.conf och ändra till en felaktig url för smhi (smhi.url).
   *
   * Länk till wiki:
   * https://github.com/callistaenterprise/cadec-2014-play2/wiki/Övningar#wiki-Övning-6-futures-med-felhantering
   */
    ???
  }

  /**
   * Collects weather from all providers and merge the result into a
   * LocationWithWeather.
   */
  val all: Location => Future[LocationWithWeather] = { location =>
    Future.sequence(
      providers.values
        .map(f => f.getLocationWithWeather(location)))
        .map(l => l.tail.fold[LocationWithWeather](l.head)(_ merge _)
      )
  }

}
