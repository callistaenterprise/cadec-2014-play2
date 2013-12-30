var app = angular.module('playApp', []);
 
app.controller('MainCtrl', function ($scope, $http) {

	$scope.positions = [{"lng":"17.57","lat":"65.35","temp":{"smhi":"-2.6"}},{"lng":"21.25","lat":"62.81","temp":{"smhi":"2.4"}},{"lng":"12.39","lat":"59.76","temp":{"smhi":"-1.4"}},{"lng":"22.97","lat":"63.81","temp":{"smhi":"2.9"}},{"lng":"15.17","lat":"61.17","temp":{"smhi":"-2.9"}},{"lng":"21.65","lat":"63.08","temp":{"smhi":"2.4"}},{"lng":"18.08","lat":"59.33","temp":{"smhi":"3.3"}},{"lng":"13.38","lat":"57.67","temp":{"smhi":"3.8"}},{"lng":"12.52","lat":"56.87","temp":{"smhi":"5.9"}},{"lng":"17.52","lat":"65.53","temp":{"smhi":"-2.7"}},{"lng":"11.25","lat":"58.68","temp":{"smhi":"5.7"}},{"lng":"15.66","lat":"60.59","temp":{"smhi":"-1.4"}},{"lng":"14.08","lat":"59.33","temp":{"smhi":"1.5"}},{"lng":"19.06","lat":"57.84","temp":{"smhi":"4.9"}},{"lng":"15.06","lat":"58.53","temp":{"smhi":"3.2"}},{"lng":"22.30","lat":"60.30","temp":{"smhi":"2.1"}},{"lng":"17.95","lat":"59.43","temp":{"smhi":"3.3"}},{"lng":"12.20","lat":"59.03","temp":{"smhi":"1.0"}},{"lng":"12.57","lat":"59.65","temp":{"smhi":"-1.3"}},{"lng":"13.51","lat":"59.37","temp":{"smhi":"-0.1"}},{"lng":"23.98","lat":"66.82","temp":{"smhi":"1.1"}},{"lng":"14.35","lat":"55.55","temp":{"smhi":"5.3"}},{"lng":"12.68","lat":"56.06","temp":{"smhi":"5.2"}}];

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
