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

	//动态参数的管理
	$scope.addDynamicAttribute = function(key) {
		if ($scope.vs.dynamicAttributes[key] != null) {
			app.alertWarn('Param Already Exist.');
		} else {
			$scope.vs.dynamicAttributes[key] = '';
		}
	}
	$scope.removeDynamicAttribute = function(key) {
		delete $scope.vs.dynamicAttributes[key];
	}
	$scope.getInputType = function(key) {
		console.log(key);
		// console.log($scope.definedParamMap);
		var definedParam = $scope.definedParamMap[key];
		if (definedParam == null) {
			return 'TEXT';
		}
		var inputType = definedParam.inputType;
		return inputType;
	}
	$scope.valueList = [];
	$scope.initValueList = function(key) {
		var definedParam = $scope.definedParamMap[key];
		if (definedParam) {
			$scope.valueList = definedParam.valueList;
		}
	}
	
});
