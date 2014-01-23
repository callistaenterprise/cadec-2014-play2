Cadec Play 2 Tutorial 2014
====================
TODO: Beskrivning

Instruktioner för att sätta upp en utvecklingsmiljö finns [här](https://github.com/callistaenterprise/play2-cadec/wiki/Installationsanvisningar).

[Lathund för Scala och Play]() TODO?

Övning 1: Hämta koordinater för en adress
---------------------
I första övningen ska vi söka fram koordinater för en adress från Google's kart-API. En adress (t.ex "Storgatan") kan finnas flera gånger, därför får man tillbaks en lista med koordinater i objektet 'Location', dessa ska sedan returneras till användaren på URL:en http://localhost:9000/location/{adress}

Övningen är uppdelad i två delar. Först måste metoden 'getLocation' implementeras i [LocationProvider](https://github.com/callistaenterprise/play2-cadec/blob/master/app/providers/LocationProvider.scala). Vidare instruktioner finns i klassen. 

Sedan ska metoden 'getLocation' anropas från själva GET-metoden som finns definierad i controller-klassen [Application](https://github.com/callistaenterprise/play2-cadec/blob/master/app/controllers/Application.scala). Man behöver även ange en korrekt route mellan URL:en '/weather/:address' och metoden 'getLocationForAddress_GET' i [routes-konfigurations-filen](https://github.com/callistaenterprise/play2-cadec/blob/master/conf/routes).

Testkör i en browser med url:en http://localhost:9000/location/{address}, t.ex [localhost:9000/location/kungsgatan](http://localhost:9000/weather/kungsgatan)

Övning 2: Hämta väder för varje plats 
---------------------
Nu ska vi hämta väderinformation för varje plats som sökts upp i Övning 1. Detta görs genom att uppdatera metoden [getLocationsWithWeatherFuture](https://github.com/callistaenterprise/play2-cadec/blob/master/app/controllers/Application.scala#L93) i Application-controllern.

TODO: Beskriv alla moment

TODO: Hur kan man testa detta?


Övning 3: Presentera hämtad information för användaren
---------------------
I sista övningen ska vi presentera väderinformationen för användaren. Detta genom att implementera metoden [getLocationWithWeather_POST](https://github.com/callistaenterprise/play2-cadec/blob/master/app/controllers/Application.scala#L50) i Application-controllern.


---
*Tutorialen är skapad av Anders Asplund, Pär Wenåker och Albert Örwall för Cadec 2014 som arrangeras av [Callista Enterprise AB](http://callistaenterprise.se/).*
