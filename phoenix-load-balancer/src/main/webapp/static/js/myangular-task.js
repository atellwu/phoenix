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
	$scope.isContain = function(deploymentDetails, ip) {
		// var re = false;
		// $.each(deploymentDetails, function(i, deploymentDetail) {
		// if (deploymentDetail.ipAddress == ip) {
		// re = true;
		// return false;
		// }
		// });
		return deploymentDetails[ip] != null;
	}
	$scope.checkIp = function(deploymentDetails, ip) {
		if (deploymentDetails[ip] != null) {
			delete deploymentDetails[ip];
		} else {
			deploymentDetails[ip] = {
				"ipAddress" : ip
			};
		}
		// var contain = false;
		// $.each(deploymentDetails, function(i, deploymentDetail) {
		// if (deploymentDetail.ipAddress == ip) {
		// deploymentDetails.splice(i, 1);
		// contain = true;
		// return false;
		// }
		// });
		// if (!contain) {
		// deploymentDetails.push({
		// "ipAddress" : ip
		// });
		// }
	}
	$scope.isContainAll = function(deploymentDetails, instances) {
		var isContainAll = true;
		$.each(instances, function(j, instance) {
			// var contain = false;
			// $.each(deploymentDetails, function(i, deploymentDetail) {
			// if (deploymentDetail.ipAddress == instance.ip) {
			// contain = true;
			// return false;
			// }
			// });
			var contain = deploymentDetails[instance.ip] != null;
			if (!contain) {
				isContainAll = false;
				return false;
			}
		});
		return isContainAll;
	}
	$scope.checkAllIp = function(deploymentBo, e) {
		var elem = angular.element(e.srcElement);
		var checked = (elem.attr('checked'));
		if (checked) {
			deploymentBo.deploymentDetails = {};
			var instances = deploymentBo.vs.instances;
			$.each(instances, function(i, instance) {
				deploymentBo.deploymentDetails[instance.ip] = {
					"ipAddress" : instance.ip
				};
			});
		} else {
			deploymentBo.deploymentDetails = {};
		}
	}
	$scope.getStatus = function(deploymentDetails,ip){
		var deploymentDetail = deploymentDetails[ip];
		if(deploymentDetail!=null){
			if(deploymentDetail.status!=null){
				return deploymentDetail.status;
			}else{
				return "CREATED";
			}
		}
		return null;
	}
});
