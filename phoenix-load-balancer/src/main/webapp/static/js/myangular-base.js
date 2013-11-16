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

module.directive('ngEnter', function() {
	return function(scope, element, attrs) {
		element.bind("keydown keypress", function(event) {
			if (event.which === 13) {
				scope.$apply(function() {
					scope.$eval(attrs.ngEnter);
				});
				event.preventDefault();
			}
		});
	};
});

module.factory('DataService', function($resource) {
	var model = {};
	var VsNameList = $resource('/vsNamelist');
	model.vsNameList = VsNameList.query(function() {
		if (model.vsNameList.length > 0) {
			// $scope.vsName = $scope.vsNameList[0].name;
		}
	});

	var PropertiesDefinedInputs = $resource('/propertiesDefinedInputs');
	model.propertiesDefinedInputs = PropertiesDefinedInputs.get(function() {
	});

	var DirectiveDefinedInputs = $resource('/directiveDefinedInputs');
	model.directiveDefinedInputs = DirectiveDefinedInputs.get(function() {
	});

	var Strategies = $resource('/strategies');
	model.strategies = Strategies.query(function() {
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
		$resource, $http) {
	$scope.selectedTab = 'profile';
	$scope.isActive = function(tabName) {
		var clazz = ($scope.selectedTab == tabName) ? 'active' : '';
		return clazz;
	};
	// var Vs = $resource('/vs/:name', {
	// name : '@name'
	// }, {
	// caches : 'dds'
	// });

	$scope.getVs = function(vsName) {
		$http({
			method : 'GET',
			url : '/vs/' + vsName
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						$scope.vs = data.virtualServer;
						if ($scope.vs == null) {// 新建vs
							$scope.vs = new Object();
							$scope.vs.name = vsName;
							$scope.vs.dynamicAttributes = new Object();
							$scope.vs.instances = [];
							$scope.vs.instances.push(new Object());
							$scope.vs.pools = [];
							$scope.newVs = true;
						} else {
							$scope.newVs = false;
						}
					} else {
						app.alertError("获取失败[errorCode=" + data.errorCode
								+ "]: " + data.errorMessage);
					}
				}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
		// $scope.vs = Vs.get({
		// name : vsName
		// }, function() {
		// console.log($scope.vs);
		// console.log($scope.vs.name);
		// });
	};
	// 保存
	// $scope.save = function() {
	// $scope.vs.$save();
	// }
	$scope.save = function() {
		$http({
			method : 'POST',
			data : $scope.vs,
			url : '/vs/' + $scope.vs.name
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						app.alertSuccess("保存成功！ 即将刷新页面...");
						setTimeout(app.refresh, 700);
					} else {
						app.alertError("保存失败[errorCode=" + data.errorCode
								+ "]: " + data.errorMessage);
					}
				}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
		// $scope.vs = Vs.get({
		// name : vsName
		// }, function() {
		// console.log($scope.vs);
		// console.log($scope.vs.name);
		// });
	};

});