var module = angular.module('MyApp', [ 'ngResource' ]);

module.config(function($locationProvider, $resourceProvider) {
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

module.controller('TaskController', function($scope, $resource, $http) {
	$scope.task = null;
	$scope.getTask = function(taskId) {
		// 获取task
		$http({
			method : 'GET',
			url : window.contextpath + '/deploy/task/' + taskId + '/get'
		}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				$scope.task = data.task;
			} else {
				app.alertError("获取失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	$scope.isContain = function(deployAgents, ip) {
		// var re = false;
		// $.each(deployAgents, function(i, deployAgent) {
		// if (deployAgent.ipAddress == ip) {
		// re = true;
		// return false;
		// }
		// });
		return deployAgents[ip] != null;
	}
	$scope.checkIp = function(deployAgents, ip) {
		if (deployAgents[ip] != null) {
			delete deployAgents[ip];
		} else {
			deployAgents[ip] = {
				"ipAddress" : ip
			};
		}
		// var contain = false;
		// $.each(deployAgents, function(i, deployAgent) {
		// if (deployAgent.ipAddress == ip) {
		// deployAgents.splice(i, 1);
		// contain = true;
		// return false;
		// }
		// });
		// if (!contain) {
		// deployAgents.push({
		// "ipAddress" : ip
		// });
		// }
	}
	$scope.isContainAll = function(deployAgents, instances) {
		var isContainAll = true;
		$.each(instances, function(j, instance) {
			// var contain = false;
			// $.each(deployAgents, function(i, deployAgent) {
			// if (deployAgent.ipAddress == instance.ip) {
			// contain = true;
			// return false;
			// }
			// });
			var contain = deployAgents[instance.ip] != null;
			if (!contain) {
				isContainAll = false;
				return false;
			}
		});
		return isContainAll;
	}
	$scope.checkAllIp = function(deployVsBo, e) {
		var elem = angular.element(e.srcElement);
		var checked = (elem.attr('checked'));
		if (checked) {
			deployVsBo.deployAgents = {};
			var instances = deployVsBo.vs.instances;
			$.each(instances, function(i, instance) {
				deployVsBo.deployAgents[instance.ip] = {
					"ipAddress" : instance.ip
				};
			});
		} else {
			deployVsBo.deployAgents = {};
		}
	}
	$scope.getStatus = function(deployAgents, ip) {
		var deployAgent = deployAgents[ip];
		if (deployAgent != null) {
			if (deployAgent.status != null) {
				return deployAgent.status;
			} else {
				return "CREATED";
			}
		}
		return null;
	}
	$scope.startTask = function() {
		$http(
				{
					method : 'GET',
					url : window.contextpath + '/deploy/task/' + $scope.task.id
							+ '/start'
				}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				// 修改button为不可点击

				// 开始ajax论询获取task的状态
			} else {
				app.alertError("获取失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	}
});
