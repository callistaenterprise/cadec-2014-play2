app.directive('map', function () {
  return {
    link: function (scope, elem, attrs) {

      function initialize() {
        var mapOptions = {
          mapTypeId: google.maps.MapTypeId.ROADMAP,
          streetViewControl: false,
          zoomControl: true,
          zoomControlOptions: {
            style: google.maps.ZoomControlStyle.LARGE,
            position: google.maps.ControlPosition.LEFT_CENTER
          },
          panControl: true,
          panControlOptions: {
            position: google.maps.ControlPosition.TOP_RIGHT
          }
        };
        var bounds = new google.maps.LatLngBounds();
        var map = new google.maps.Map(elem[0], mapOptions);

        angular.forEach(scope.positions, function(p){
          var position = new google.maps.LatLng(p.lat, p.lng)
          bounds.extend(position);

          var marker = new google.maps.Marker({
              position: position,
              map: map,
              title: "\"<h3>" + p.temp['smhi'] + "</h3>\""
          });

          var infoWindow = new google.maps.InfoWindow({
              content: "<h3>" + p.temp['smhi'] + " grader</h3>"
          });

          // Allow each marker to have an info window
          google.maps.event.addListener(marker, 'mouseover', function() {
            infoWindow.open(map, marker);
          });

          google.maps.event.addListener(marker, 'mouseout', function() {
            infoWindow.close();
          });
          map.fitBounds(bounds);

          var boundsListener = google.maps.event.addListener((map), 'bounds_changed', function(event) {
            this.setZoom(5);
            google.maps.event.removeListener(boundsListener);
          });
        });

        scope.$on('update', function() {
          console.log('changed');
          initialize();
        })

      }
      google.maps.event.addDomListener(window, 'load', initialize);
    }
  };
});
