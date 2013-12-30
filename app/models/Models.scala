package models

import org.joda.time.DateTime

case class Address(address: String)

case class Location(lng: String, lat: String, address: String) {
  def withWeather(provider: String, weather: Weather) = LocationWithWeather(this, Map(provider -> weather))
}

case class LocationWithWeather(location: Location, temperatures:Map[String, Weather])

case class Weather(time: DateTime, temp: String)
