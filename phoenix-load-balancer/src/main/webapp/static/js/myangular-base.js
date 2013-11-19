var module = angular.module('MyApp', [ 'ngResource', 'ngRoute' ]);

module.config(function($routeProvider, $locationProvider, $resourceProvider) {
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

	var PropertiesDefinedInputs = $resource(window.contextpath
			+ '/base/propertiesDefinedInputs');
	model.propertiesDefinedInputs = PropertiesDefinedInputs.get(function() {
	});

	var DirectiveDefinedInputs = $resource(window.contextpath
			+ '/base/directiveDefinedInputs');
	model.directiveDefinedInputs = DirectiveDefinedInputs.get(function() {
	});

	var Strategies = $resource(window.contextpath + '/base/strategies');
	model.strategies = Strategies.query(function() {
	});

	return model;
});

module.controller('VsController', function($scope, DataService, $route,
		$resource, $http) {
	$scope.selectedTab = 'profile';
	$scope.isActive = function(tabName) {
		var clazz = ($scope.selectedTab == tabName) ? 'active' : '';
		return clazz;
	};
	$scope.getVs = function(vsName) {
		$http({
			method : 'GET',
			url : window.contextpath + '/' + vsName + '/get'
		}).success(function(data, status, headers, config) {
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
				app.alertError("获取失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	// 保存
	$scope.save = function() {
		$http({
			method : 'POST',
			data : $scope.vs,
			url : window.contextpath + '/' + $scope.vs.name + '/save'
		}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				app.alertSuccess("保存成功！ 即将刷新页面...");
				setTimeout(app.refresh, 700);
			} else {
				app.alertError("保存失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	// 删除
	$scope.removeVirtualServer = function() {
		$http({
			method : 'POST',
			data : $scope.vs,
			url : window.contextpath + '/' + $scope.vs.name + '/remove'
		}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				app.alertSuccess("删除成功！ 即将刷新页面...","removeVirtualServerAlertDiv");
				setTimeout(function(){
					window.location = window.contextpath + "/";
				}, 700);
			} else {
				app.alertError("删除失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};

});