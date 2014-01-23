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
  /**
   * Övning 5
   * Returnera svaret från den vädertjänst(smhi eller yr) som är först klar. Använd metoden
   * firstCompletedOf som finns i Future-apit.
   *
   * För att testa måste du byta ut den strategin vi använder (smhi) i
   * getLocationsWithWeatherFuture i Application.scala.
   *
   * Länk till wiki:
   * https://github.com/callistaenterprise/cadec-2014-play2/blob/master/README.md#%C3%96vning-5-first-completed
   */

    ???
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
