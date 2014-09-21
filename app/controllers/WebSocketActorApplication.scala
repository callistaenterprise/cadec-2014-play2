package controllers

import akka.actor.{PoisonPill, Actor, ActorRef, Props}
import akka.pattern.pipe
import models.JsonHelper._
import models.{Location, LocationWithWeather}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc.{Controller, WebSocket}
import providers.{ConcreteProviders, LocationProvider, WeatherFetchStrategies}


/**
 *
 */
object WebSocketActorApplication extends Controller
  with LocationProvider
  with WeatherFetchStrategies
  with ConcreteProviders{

  case class LocationsAnswer(locations: Seq[Location])

  /**
   * Actor that fetches weather for each location and pipes the result to it self.
   * This actor keeps track of the number of responses received.
   *
   * @param out actor to send the response to
   */
  class LocationWeatherActor(out: ActorRef) extends Actor {
    var num = 0

    def receive = {
      case LocationsAnswer(locations) =>
        num = locations.size
        locations.foreach{ location =>
          all(location) pipeTo self
        }
      case locationWithWeather : LocationWithWeather =>
        out ! toJson(locationWithWeather)
        num -= 1
        if(num < 1) {
          out ! toJson(Map("status" -> "end"))
          self ! PoisonPill
        }
    }
  }
  object LocationWeatherActor {
    def props(out: ActorRef) = Props(new LocationWeatherActor(out))
  }

  /**
   * Websocket Actor
   */
  class WeatherWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: JsValue =>
        val address = (msg \ "address").as[String]
        getLocations(address).map(LocationsAnswer) pipeTo context.actorOf(LocationWeatherActor.props(out))
    }
  }
  object WeatherWebSocketActor {
    def props(out: ActorRef) = Props(new WeatherWebSocketActor(out))
  }

  /**
   * WebSocket Action
   *
   * @return Props object fro creating actor that listens and sends messages on web socket.
   */
  def getWeatherWsActor = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WeatherWebSocketActor.props(out)
  }

}
