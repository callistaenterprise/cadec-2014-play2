package providers

import models.JsonHelper._
import models.Location
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS
import scala.concurrent.Future


trait LocationProvider {
  def getLocations(address: String): Future[Seq[Location]] = {
    WS.url(config("maps.api"))
      .withQueryString("address" -> address, "sensor" -> "false")
      .get()
      .map(r => (r.json \ "results").as[Seq[Location]])
  }

}
