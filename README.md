Cadec Play 2 Tutorial 2014
====================
TODO: Beskrivning


Instruktioner för att sätta upp en utvecklingsmiljö finns [här](https://github.com/callistaenterprise/play2-cadec/wiki/Installationsanvisningar).

[Lathund för Scala och Akka]() TODO

Uppgift 1: Vy som visar formulär
---------------------
I första uppgiften ska vi presentera ett formulär för användaren där denne kan fylla i en adress för vart väder ska visas.

Vi behöver därför implementera en ny index Action som returnerar ett formulär.
   
  * Använd vyn `simpleform.scala.html`, glöm inte att fixa en route till din Action.

TODO: Tydligare instruktioner

Uppgift 2: Hantera ifyllt formulär
---------------------
För att hantera det användaren skickar in i formuläret behövs en ny metod som hanterar POST-anrop. Implementera en Action som hanterar en POST från simpleform.scala.html

   * Lägg till en route från /location till din Action

TODO: Tydligare instruktioner

Uppgift 3: Hantera JSON istället för formulärdata
---------------------
I fortsättningen vill vi hantera data som kommer in som JSON. Därför behöver den Action som du skapade i Övning 2 nu hantera json.

TODO: Tydligare instruktioner

Uppgift 4: Hämta koordinater för adress
---------------------
Nu ska vi med hjälp av Google's Map API ta hem koordinater för den adress som fylls i. Då en adress kan finnas på flera ställen kommer en lista med flera `locations` returneras. 

Hämta locations utifrån en adress. Använd getLocations i LocationProvider.
       * OBS! Glöm inte att du måste implementera getLocations själv

TODO: Tydligare instruktioner


Uppgift 5: Hämta väderinformation
---------------------
Nu är det äntligen dags att hämta väderinformationen för varje `location`. Använd getLocation som du implementerade i övning 4 för att hämta locations. Använd sedan en WeatherProvider (smhi eller yr)
     * för att hämta vädret för samtliga locations.

---
*Tutorialen är skapad av Anders Asplund, Pär Wenåker och Albert Örwall för Cadec 2014 som arrangeras av [Callista Enterprise AB](http://callistaenterprise.se/).*
