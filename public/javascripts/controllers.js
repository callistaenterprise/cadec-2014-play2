var app = angular.module('playApp', []);
 
app.controller('MainCtrl', function ($scope, $http, WsService) {

    WsService.connect();

	$scope.positions = [];

	$scope.getWeather = function(address){

        $scope.loading = true;
        var data = { 'address' : address };

        if(WsService.isConnected()){
            console.log('use web socket');

            $scope.positions = [];
            $scope.$broadcast('update');

            WsService.send(data);

        } else {
            console.log('do http post');
            $http.post('/weather', data).success(function(response) {
                $scope.positions = response;
                $scope.loading = false;
                $scope.$broadcast('update');

                angular.forEach(response, function(pos){
                    $scope.$broadcast('newPosition', pos);
                });
            }).error(function(err){
                console.log('err');
                $scope.loading = false;
            });
        }
	}

    $scope.$on('newPosition', function(e, p) {
        $scope.loading = false;
    });
});
