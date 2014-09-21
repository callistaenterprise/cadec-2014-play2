package providers

import models.JsonHelper._
import models.Location
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{WSResponse, WS}
import scala.concurrent.Future


trait LocationProvider {
  def getLocations(address: String): Future[Seq[Location]] = {
    /**
     * Övning 2
     * Hämta koordinater mha googles map service. Url:en finns som en property med namnet maps.api
     * i application.conf.
     * Använd hjälp-funktionen config som vi har definierat i JsonHelper för att hämta ut properties
     * från application.conf
     *
     * Google maps api kräver två request parametrar:
     *
     * 1. address: Den adress som vi vill ha geokoordinaterna för
     * 2. sensor: Används för att ange om man använder sig av en GPS-sensor för att avgöra applikationens
     *            poition. I vårt fall skall vi alltid sätta den till false
     *
     * Använd metoden withQueryString som finns på Plays WS-client för detta.
     *
     * M.h.a metoden responseToLocations nedan skall du sedan mappa svaret från json till modell-objektet
     * Location (model.Location).
     *
     * (Mappningen görs implicit med hjälp av en Json-Writer för Location som
     * finns definierad i models.JsonHelper)
     *
     */
    ???
  }

  private def responseToLocations(response: WSResponse): Seq[Location] = (response.json \ "results").as[Seq[Location]]
}
