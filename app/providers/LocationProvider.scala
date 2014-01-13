package providers

import models.JsonHelper._
import models.Location
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future


trait LocationProvider {
  def getLocations(address: String): Future[Seq[Location]] = {
    ??? //Todo add instructions for exercise 4
  }

  private def responseToLocations(response: Response): Seq[Location] = (response.json \ "results").as[Seq[Location]]
}
