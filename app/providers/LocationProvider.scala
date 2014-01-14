package providers

import models.JsonHelper._
import models.Location
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future


trait LocationProvider {
  def getLocations(address: String): Future[Seq[Location]] = {
    /**
     * Övning 4
     * Hämta koordinater mha googles map service. Url:en finns som en property med namnet maps.api.
     * Använd metoden responseToLocations för att mappa svaret(json) till modell-objektet Location. Mappningen görs
     * då med den Json-Writer för Location som finns definierad i JsonHelper.
     *
     * Tips: Använd funktionen config som finns i JsonHelper för att hämta ut properties från application.conf
     */
    ???
  }

  private def responseToLocations(response: Response): Seq[Location] = (response.json \ "results").as[Seq[Location]]
}
