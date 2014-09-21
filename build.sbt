import sbt.Keys._
import sbt._

name := "weather"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies += ws

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)
