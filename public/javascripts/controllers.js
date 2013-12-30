var app = angular.module('playApp', []);
 
app.controller('MainCtrl', function ($scope, $http) {

	$scope.positions = [];

	$scope.getWeather = function(address){
        $scope.loading = true;

		var data = { 'address' : address };

		$http.post('/weather', data).success(function(response) {
            $scope.positions = response;
            $scope.loading = false;
            $scope.$broadcast('update');
		}).error(function(err){
            console.log('err');
            $scope.loading = false;
        });
	}

});
