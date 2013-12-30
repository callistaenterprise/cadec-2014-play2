package models

import org.joda.time.DateTime

case class Address(name: String)

case class Location(lng: String, lat: String, address: String) {
  def withWeather(weatherService: String, weather: Weather) = LocalWeather(lng, lat, Map(weatherService -> weather.temp))
}

case class LocalWeather(lng: String, lat: String, temp:Map[String, String])

case class Weather(time: DateTime, temp: String)

