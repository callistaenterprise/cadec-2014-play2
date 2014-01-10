package providers

import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._
import org.joda.time.DateTime

import scala.concurrent._
import scala.concurrent.duration.Duration

import ExecutionContext.Implicits.global

import models._
/**
 *
 */
@RunWith(classOf[JUnitRunner])
class WeatherFetchStrategiesSpec extends Specification {

  "WeatherProviderStrategies.smhi" should {

    "return 10 degress" in {
      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = ""; val sleep = ""}
      val locationWithWeatherF = strategy.smhi(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(1)
      locationWithWeather.temperatures("smhi").temp must equalTo("10")
    }
  }

  "WeatherProviderStrategies.yr" should {

    "return 11 degress" in {
      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = ""; val sleep = ""}
      val locationWithWeatherF = strategy.yr(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(1)
      locationWithWeather.temperatures("yr").temp must equalTo("11")
    }
  }

  "WeatherProviderStrategies.firstCompleted" should {

    "return 10 degress if yr is delayed" in {
      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = ""; val sleep = "yr"}
      val locationWithWeatherF = strategy.firstCompleted(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(1)
      locationWithWeather.temperatures("smhi").temp must equalTo("10")
    }
    "return 11 degress if smhi is delayed" in {
       val strategy = new WeatherFetchStrategies with DummyProviders { val fail = ""; val sleep = "smhi"}
       val locationWithWeatherF = strategy.firstCompleted(Location("1.0","2.0", "addr1"))

       val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

       locationWithWeather.temperatures.size must equalTo(1)
       locationWithWeather.temperatures("yr").temp must equalTo("11")
     }
  }

  "WeatherProviderStrategies.withRecovery" should {

    "return 10 degress if both smhi and yr succeeds" in {
      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = ""; val sleep = ""}
      val locationWithWeatherF = strategy.withRecovery(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(1)
      locationWithWeather.temperatures("smhi").temp must equalTo("10")
    }
    "return 11 degress if smhi fails" in {
      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = "smhi"; val sleep = ""}
      val locationWithWeatherF = strategy.withRecovery(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(1)
      locationWithWeather.temperatures("yr").temp must equalTo("11")
    }
    "return 10 degress if yr fails" in {
      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = "yr"; val sleep = ""}
      val locationWithWeatherF = strategy.withRecovery(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(1)
      locationWithWeather.temperatures("smhi").temp must equalTo("10")
    }
  }

  "WeatherProviderStrategies.all" should {

    "return two locations with weather" in {

      val strategy = new WeatherFetchStrategies with DummyProviders { val fail = ""; val sleep = ""}
      val locationWithWeatherF = strategy.all(Location("1.0","2.0", "addr1"))

      val locationWithWeather = Await.result(locationWithWeatherF, Duration(2, "s"))

      locationWithWeather.temperatures.size must equalTo(2)
    }
  }


}

trait DummyProviders extends Providers {
  val fail: String
  val sleep: String


  def provider(name: String, degrees: String) = {
    name -> new WeatherProvider {
      def getLocationWithWeather(location: Location) = {

        val promise = Promise[LocationWithWeather]()

        def success = promise.success(
          LocationWithWeather(location, Map( name -> Weather(new DateTime(), degrees)))
        )

        if (fail == name)
          promise.failure(new RuntimeException)
        else if (sleep == name)
          future {
            Thread.sleep(1000)
          } onComplete {
            case _ => success
          }
        else success

        promise.future
      }
    }
  }

  val providers: Map[String, WeatherProvider] =
    Map(
      provider("smhi", "10"),
      provider("yr", "11")
    )
}