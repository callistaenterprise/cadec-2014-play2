package controllers

import scala.concurrent.Future
import play.api.libs.ws.WS
import models.JsonHelper._
import models.Location
import play.api.libs.concurrent.Execution.Implicits._


trait LocationProvider {
  def getLocations(address: String): Future[Seq[Location]] = {
    WS.url(config("maps.api"))
      .withQueryString("address" -> address, "sensor" -> "false")
      .get()
      .map(r => (r.json \ "results").as[Seq[Location]])
  }

}
