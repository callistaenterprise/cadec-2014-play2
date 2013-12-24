package models

import org.joda.time.DateTime

case class Address(name: String)

case class Location(lng: Double, lat: Double, address: String) {
  def withWeather(weather: Weather) = LocalWeather(this, weather)
}

case class LocalWeather(location: Location, weather: Weather)

case class Weather(time: DateTime, temp: String)
