module.controller('LocationController', function($scope, DataService, $route,
		$resource, $http) {
	// location视图切换
	$scope.locationEditing = null;
	$scope.switchLocation = function(index) {
		$scope.locationEditing = $scope.vs.locations[index];
	};
	$scope.switchLocationList = function() {
		$scope.locationEditing = null;
	}
	// location增删
	$scope.openAddLocationModal = function() {
		$scope.locationToBeAdd = new Object();
		$scope.locationToBeAdd.caseSensitive = true;
		$('#addLocationModal').modal('show');
		$('#addLocationName').focus();
	};
	$scope.addLocation = function() {
		var domain = $scope.locationToBeAdd.domain;
		if (domain == null || domain.trim() == '') {
			app.alertError("集群名必选！", "addLocationAlertDiv");
			return;
		}
		$scope.vs.locations.push($scope.locationToBeAdd);
		$('#addLocationModal').modal('hide');
	}
	$scope.affirmRemoveLocationModal = function(index) {
		$scope.locationToBeRemove = $scope.vs.locations[index];
		$scope.locationIndexToBeRemove = index;
		$('#affirmRemoveLocationModal').modal('show');
	}
	$scope.removeLocation = function() {
		$scope.vs.locations.splice($scope.locationIndexToBeRemove, 1);
		$('#affirmRemoveLocationModal').modal('hide');
	}
	// directive增删
	$scope.addDirective = function() {
		var directive = new Object();
		var directives = $scope.locationEditing.directives;
		if (!directives) {
			directives = [];
			$scope.locationEditing.directives = directives;
		}
		directives.push(directive);
	}
	$scope.affirmRemoveDirectiveModal = function(index) {
		$scope.directiveToBeRemove = $scope.locationEditing.directives[index];
		$scope.directiveIndexToBeRemove = index;
		$('#affirmRemoveDirectiveModal').modal('show');
	}
	$scope.removeDirective = function() {
		$scope.locationEditing.directives.splice(
				$scope.directiveIndexToBeRemove, 1);
		$('#affirmRemoveDirectiveModal').modal('hide');
	}
});
