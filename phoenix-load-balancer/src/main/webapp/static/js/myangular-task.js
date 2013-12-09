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

module.directive('booleanValue', function() {
	return function(scope, elm, attr) {
		attr.$set('value', attr.booleanValue === 'true');
	};
});

module
		.controller(
				'TaskController',
				function($scope, $resource, $http) {
					$scope.task = null;
					$scope.canUpdate = false;
					$scope.getTask = function(taskId) {
						// 获取task
						$http(
								{
									method : 'GET',
									url : window.contextpath + '/deploy/task/'
											+ taskId + '/get'
								})
								.success(
										function(data, status, headers, config) {
											if (data.errorCode == 0) {
												$scope.task = data.task;
												if ($scope.task.task.status == 'CREATED') {
													$scope.canUpdate = true;
												}
												console.log($scope.canUpdate);
											} else {
												app.alertError("获取失败: "
														+ data.errorMessage);
											}
										})
								.error(function(data, status, headers, config) {
									app.appError("响应错误", data);
								});
					};
					$scope.isContain = function(deployAgents, ip) {
						return deployAgents[ip] != null;
					}
					$scope.checkIp = function(deployAgents, ip) {
						if (!$scope.canUpdate) {
							return;
						}
						if (deployAgents[ip] != null) {
							delete deployAgents[ip];
						} else {
							deployAgents[ip] = {
								"ipAddress" : ip
							};
						}
					}
					$scope.isContainAll = function(deployAgents, instances) {
						var isContainAll = true;
						$.each(instances, function(j, instance) {
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
					// 更新Task
					$scope.updateDeployTask = function() {
						app.clearAlertMessage();
						if (!$scope.canUpdate) {// 如果不可修改，则直接返回
							console.log('Can not update task.');
							return;
						}
						$http(
								{
									method : 'POST',
									data : $scope.task,
									url : window.contextpath + '/deploy/task/'
											+ $scope.task.task.id + '/update'
								}).success(
								function(data, status, headers, config) {
									if (data.errorCode == 0) {
										// 设置为不可修改
										$scope.canUpdate = false;
										// 开始启动
										$scope.startTask();
									} else {
										app.alertError("保存失败: "
												+ data.errorMessage);
									}
								}).error(
								function(data, status, headers, config) {
									app.appError("响应错误", data);
								});
					};
					$scope.isStarting = false;
					$scope.updateAndStartTask = function() {
						if (!$scope.canUpdate) {// 如果不可修改，则直接启动
							$scope.startTask();
						} else {
							$scope.updateDeployTask();
						}
					}
					$scope.startTask = function() {
						$scope.isStarting = true;
						$http(
								{
									method : 'GET',
									url : window.contextpath + '/deploy/task/'
											+ $scope.task.task.id + '/start'
								}).success(
								function(data, status, headers, config) {
									if (data.errorCode == 0) {
										$scope.isStarting = true;
										// 开始ajax论询获取task的状态
										$scope.statusConsole();
									} else {
										$scope.isStarting = false;
										app.alertError("启动失败: "
												+ data.errorMessage);
									}
								}).error(
								function(data, status, headers, config) {
									$scope.isStarting = false;
									app.appError("响应错误", data);
								});
					}
					$scope.statusConsole = function() {
						$http(
								{
									method : 'GET',
									url : window.contextpath + '/deploy/task/'
											+ $scope.task.task.id + '/status'
								})
								.success(
										function(data, status, headers, config) {
											if (data.errorCode == 0) {
												$scope.task = data.task;
												if (!($scope.task.status == 'SUCCESS'
														&& $scope.task.status == 'FAILED' && $scope.task.status == 'DONE')) {
													// 等待0.5秒，继续获取
													setTimeout(
															$scope.statusConsole,
															1000);
												}
											} else {
												app.alertError("获取失败: "
														+ data.errorMessage);
											}
										})
								.error(function(data, status, headers, config) {
									app.appError("响应错误", data);
								});
					};
				});
