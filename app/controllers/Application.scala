package controllers

import models._
import models.JsonHelper._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import providers.{LocationProvider, WeatherFetchStrategies, ConcreteProviders}
import scala.concurrent.Future
import play.api.libs.json.Json._


object Application extends Controller
with LocationProvider
with WeatherFetchStrategies
with ConcreteProviders {

  /**
   * Describe the computer form (used in both edit and create screens).
   */
  val addressForm = Form(
    mapping(
      "address" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )

  /**
   * Simple action that displays the index page.
   * @return
   */
  def index = Action {
      Ok(views.html.main())
  }
  
  /**
   * Method that returns the location for an address as JSON.
   * Mapped to the GET verb in routes.
   *
   * @param address
   * @return
   */
  def getLocationForAddress_GET(address: String) = Action.async {
    getLocations(address).map(s => Ok(toJson(s)))
  }


  /**
   * Method that binds an address from the request. Looks up the
   * locations for that address and then gets the weather for the
   * locations according to a defined strategy.
   * Mapped to the POST verb in routes
   *
   * @return
   */
  def getLocationWithWeather_POST = Action.async(parse.json) {
    implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest("Unable to parse form")
      },
        address => {
          /**
           * Övning 4
           * Hämta alla WeatherWithLocations för aktuell adress och sätt
           * upp en route hit i routes-filen. Det skall vara en POST och
           * ligga på /weather.
           *
           * Länk till wiki:
           * https://github.com/callistaenterprise/cadec-2014-play2/wiki/Övningar#wiki-Övning-4-presentera-hämtad-information-för-användaren
           */
          ???
        })
  }


  /**
   * Method that returns a location with weather for an address.
   * Mapped to the GET verb and the path /weather/:address in routes
   *
   * @param address
   * @return
   */
  def getLocationsWithWeather_GET(address: String) = Action.async {
    getLocationsWithWeatherAsJson(address)
  }


  /**
   * Övning 3
   * I denna övning skall vi hämta alla locations för en given adress och slå ihop
   * med vädret (model.Weather) varje enskild location (model.Location).
   *
   * Vi kommer då få en Future av en lista med WeatherWithLocation som vi kan
   * returenra som Json.
   *
   * Gör klart de två nedanstående privata metoderna.
   *
   * Lägg till en route i routes-filen från /weather/:address till
   * getLocationsWithWeather_GET ovan. Testkör t.ex. från en browser.
   *
   * Länk till wiki:
   * https://github.com/callistaenterprise/cadec-2014-play2/wiki/Övningar#wiki-Övning-3-hämta-väder-för-varje-plats-
   */

  private def getLocationsWithWeatherFuture(locations: Seq[Location]): Future[Seq[LocationWithWeather]] = {
    /**

      Future.sequence(
         Ersätt innehållet i detta block med kod som hämtar vädret
         från smhi, detta skall göras för samtliga location i locations.

         Använd funktionen <smhi: (Location) => Future[LocationWithWeather]>
         som finns tillgänglig i scopet via trait:en WeatherFetchStrategies
      )

    */

    ??? //Ta bort denna rad
  }

  private def getLocationsWithWeatherAsJson(address: String): Future[Result] = {
    /**

      Fyll i det som saknas vid pilarna.

        for {
          locations <- ??? Hämta alla locations för en adress
          locationsWithWeather <- ???  H
        } yield Ok(toJson(locationsWithWeather))

    */

    ??? //Ta bort denna rad

  }

}