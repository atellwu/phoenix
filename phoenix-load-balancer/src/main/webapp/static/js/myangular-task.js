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
												// 展现出来
												$(
														'#TaskController > div.main-content')
														.show();

												if ($scope.task.task.status == 'CREATED') {
													$scope.canUpdate = true;
												}
												// 开始ajax论询获取task的状态
												if (!$scope.canUpdate) {
													$scope.needGetStatus = true;
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
					$scope.isContain = function(deployAgentBos, ip) {
						return deployAgentBos[ip] != null;
					}
					$scope.checkIp = function(deployAgentBos, ip) {
						if (!$scope.canUpdate) {
							return;
						}
						if (deployAgentBos[ip] != null) {
							delete deployAgentBos[ip];
						} else {
							deployAgentBos[ip] = {
								"deployAgent" : {
									"ipAddress" : ip
								}
							};
						}
					}
					$scope.isContainAll = function(deployAgentBos, instances) {
						var isContainAll = true;
						$.each(instances, function(j, instance) {
							var contain = deployAgentBos[instance.ip] != null;
							if (!contain) {
								isContainAll = false;
								return false;
							}
						});
						return isContainAll;
					}
					$scope.checkAllIp = function(deployVsBo) {
						deployVsBo.deployAgentBos = {};
						var instances = deployVsBo.slbPool.instances;
						$.each(instances, function(i, instance) {
							deployVsBo.deployAgentBos[instance.ip] = {
								"deployAgent" : {
									"ipAddress" : instance.ip
								}
							};
						});
					}
					$scope.uncheckAllIp = function(deployVsBo) {
						deployVsBo.deployAgentBos = {};
					}
					$scope.batchCheckAllIp = function() {
						$
								.each(
										$scope.task.deployVsBos,
										function(i, deployVsBo) {
											deployVsBo.deployAgentBos = {};
											var instances = deployVsBo.slbPool.instances;
											$
													.each(
															instances,
															function(i,
																	instance) {
																deployVsBo.deployAgentBos[instance.ip] = {
																	"deployAgent" : {
																		"ipAddress" : instance.ip
																	}
																};
															});
										});
					}
					$scope.batchUncheckAllIp = function() {
						$.each($scope.task.deployVsBos,
								function(i, deployVsBo) {
									deployVsBo.deployAgentBos = {};
								});
					}

					$scope.getAgent = function(deployAgentBos, ip) {
						return deployAgentBos[ip];
					}
					$scope.getStatus = function(deployAgentBos, ip) {
						var deployAgentBo = deployAgentBos[ip];
						if (deployAgentBo != null) {
							if (deployAgentBo.deployAgent.status != null) {
								return deployAgentBo.deployAgent.status;
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
					$scope.updateAndStartTask = function() {
						if (!$scope.canUpdate) {// 如果不可修改，则直接启动
							$scope.startTask();
						} else {
							$scope.updateDeployTask();
						}
					}
					$scope.startTask = function() {
						$scope.needGetStatus = true;
						$http(
								{
									method : 'GET',
									url : window.contextpath + '/deploy/task/'
											+ $scope.task.task.id + '/start'
								}).success(
								function(data, status, headers, config) {
									if (data.errorCode == 0) {
									} else {
										app.alertError("操作失败: "
												+ data.errorMessage);
									}
								}).error(
								function(data, status, headers, config) {
									app.appError("响应错误", data);
								});
					}
					$scope.stopTask = function() {
						$http(
								{
									method : 'GET',
									url : window.contextpath + '/deploy/task/'
											+ $scope.task.task.id + '/stop'
								}).success(
								function(data, status, headers, config) {
									if (data.errorCode == 0) {
									} else {
										app.alertError("操作失败: "
												+ data.errorMessage);
									}
								}).error(
								function(data, status, headers, config) {
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
												// if
												// ($scope.needGetStatus($scope.task.status))
												// {
												// // 等待1秒，继续获取
												// setTimeout($scope.statusConsole,
												// 1000);
												// }
												//
												// 如果有agent处于'PROCESSING'状态，则console显示它。
												$
														.each(
																$scope.task.deployVsBos,
																function(
																		vsName,
																		deployVsBo) {
																	$
																			.each(
																					deployVsBo.deployAgentBos,
																					function(
																							ip,
																							deployAgentBo) {
																						console
																								.log(deployAgentBo);
																						if (deployAgentBo.deployAgent.status == 'PROCESSING') {
																							$scope.currentAgentOrVsOfLogView = deployAgentBo.deployAgent;
																							return false;
																						}
																					});
																});

												$scope.showLog();

												// 状态是成功，或停止，则不再获取状态
												if ($scope.task.task.status == 'SUCCESS'
														|| $scope.task.task.stateAction == 'STOP') {
													$scope.needGetStatus = false;
												}
											} else {
												app.alertError("获取失败: "
														+ data.errorMessage);
											}
										})
								.error(function(data, status, headers, config) {
									$scope.needGetStatus = false;
								});
					};
					$scope.showVsLog = function(deployVsBo) {
						if (deployVsBo) {
							$scope.currentAgentOrVsOfLogView = deployVsBo.deployVs;
							$scope.showLog();
						}
					}
					$scope.showAgentLog = function(deployAgentBo) {
						if (deployAgentBo) {
							$scope.currentAgentOrVsOfLogView = deployAgentBo.deployAgent;
							$scope.showLog();
						}
					}
					$scope.showLog = function() {
						if ($scope.currentAgentOrVsOfLogView) {
							if ($scope.currentAgentOrVsOfLogView.rawLog) {
								$('#console')
										.text(
												$scope.currentAgentOrVsOfLogView.rawLog);
							} else if ($scope.currentAgentOrVsOfLogView.summaryLog) {
								$('#console')
										.text(
												$scope.currentAgentOrVsOfLogView.summaryLog);
							}
						}

					}
					setInterval(function() {
						if ($scope.needGetStatus) {
							$scope.statusConsole();
						}
					}, 600);
				});
