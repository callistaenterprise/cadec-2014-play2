package models

import org.joda.time.DateTime

case class Address(name: String)

case class Location(lng: Double, lat: Double, address: String)

case class Weather(time: DateTime, temp: String)
