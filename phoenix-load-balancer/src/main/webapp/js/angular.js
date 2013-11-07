var module = angular.module('MyApp', [ 'ngResource', 'ngRoute' ]);

module.config(function($routeProvider, $locationProvider, $resourceProvider) {
	$routeProvider.when('#/:vsName/', {
		controller : '{{ controller }}'
	});
	// $routeProvider.when('/Book/:bookId/ch/:chapterId', {
	// templateUrl : 'chapter.html',
	// controller : ChapterCntl
	// });

	// configure html5 to get links working on jsfiddle
	$locationProvider.html5Mode(true);
});

module.factory('DataService', function($resource) {
	var model = {};
	var VsNameList = $resource('/vsNamelist');
	model.vsNameList = VsNameList.query(function() {
		if (model.vsNameList.length > 0) {
			// $scope.vsName = $scope.vsNameList[0].name;
		}
	});

	var DefinedParamMap = $resource('/definedParamMap');
	model.definedParamMap = DefinedParamMap.get(function() {
	});
	return model;
});

module.controller('VsNameListController', function($scope, DataService, $route,
		$resource) {
	$scope.isActive = function(tabName) {
		var clazz = ($scope.vsName == tabName) ? 'active' : '';
		return clazz;
	};

	$scope.setVsName = function(tabName) {
		$scope.vsName = tabName;
	};

	$scope.vsNameList = DataService.vsNameList;

	// $scope.selectedVaName = 'profile';

	// $route.when('/book/:title', {
	// template : '{{ title }}',
	// controller : function($scope, $routeParams) {
	// $scope.title = $routeParams.title;
	// }
	// });
});

module.controller('VsController', function($scope, DataService, $route,
		$resource) {
	$scope.selectedTab = 'profile';
	$scope.isActive = function(tabName) {
		var clazz = ($scope.selectedTab == tabName) ? 'active' : '';
		return clazz;
	};
	var Vs = $resource('/vs/:name', {
		name : '@name'
	}, {
		caches : 'dds'
	});
	$scope.getVs = function(vsName) {
		console.log("getVs:" + vsName);
		$scope.vs = Vs.get({
			name : vsName
		}, function() {
			console.log($scope.vs);
			console.log($scope.vs.name);
		});
	};
	$scope.definedParamMap = DataService.definedParamMap;

//	$scope.addedItems = [];
//	$scope.addItem = function(index) {
//		var newItem = $scope.definedParamList[index];
//		$scope.addedItems.push(newItem);
//		console.log($scope.addedItems);
//	}
	
	$scope.getInputType = function(key) {
//		console.log(key);
//		console.log($scope.definedParamMap);
		var definedParam = $scope.definedParamMap[key];
		var inputType = definedParam.inputType;
//		console.log(inputType);
		return inputType;
	}
	
	$scope.getValueList = function(key) {
		var definedParam = $scope.definedParamMap[key];
		console.log(key);
		return definedParam.valueList;
	}
});
