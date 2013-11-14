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

	var DefinedParamMap = $resource('/definedParamMap');
	model.definedParamMap = DefinedParamMap.get(function() {
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

	$scope.definedParamMap = DataService.definedParamMap;

});

module.controller('PoolController',
		function($scope, DataService, $route, $resource, $http) {
			$scope.strategies = DataService.strategies;
			// pool视图切换
			$scope.showPoolList = true;
			$scope.switchPool = function(name) {
				$scope.showPoolList = false;
				$('div[pool]').hide();
				$("div[pool='" + name + "']").show();
				$scope.curPoolName = name;
			}
			var getPool = function(name) {
				var re = null;
				$.each($scope.vs.pools, function(i, pool) {
					if (pool.name == name) {
						re = pool;
						return false;
					}
				});
				return re;
			}
			$scope.switchPoolList = function() {
				$scope.showPoolList = true;
				$('div[pool]').hide();
				$scope.curPoolName = '';
			}
			// pool增删
			$scope.addPool = function() {
				console.log('das');
				var name = $('#addPoolName').val();
				if (name == null || name.trim() == '') {
					app.alertError("集群名不能为空！", "addPoolAlertDiv");
					return;
				}
				name = name.trim();
				var exist = false;
				$.each($scope.vs.pools, function(i, pool) {
					if (pool.name == name) {
						exist = true;
						return false;
					}
				});
				if (exist) {
					app.alertError("该集群名( " + name + " )已经存在，不能添加！",
							"addPoolAlertDiv");
				} else {
					var pool = new Object();
					pool.name = name;
					$scope.vs.pools.push(pool);
					$('#addPoolModal').modal('hide');
				}
			}
			$scope.removePool = function() {
				var affirmRemovePoolId = $('#affirmRemovePoolId').val();
				$scope.vs.pools.splice(affirmRemovePoolId, 1);
				$('#affirmRemovePoolModal').modal('hide');
			}
			$scope.affirmRemovePoolModal = function(affirmRemovePoolId,
					affirmText) {
				$('#affirmRemovePoolId').val(affirmRemovePoolId);
				$('#affirmRemovePoolText').text(affirmText);
				$('#affirmRemovePoolModal').modal('show');
			}
			// member增删
			$scope.addMember = function() {
				var member = new Object();
				var members = getPool($scope.curPoolName).members;
				if(!members){
					members = [];
					getPool($scope.curPoolName).members = members;
				}
				members.push(member);
			}
			$scope.removeMember = function() {
				var affirmRemoveMemberId = $('#affirmRemoveMemberId').val();
				console.log($scope.curPoolName);
				var pool = getPool($scope.curPoolName);
				pool.members.splice(affirmRemoveMemberId, 1);
				$('#affirmRemoveMemberModal').modal('hide');
			}
			$scope.affirmRemoveMemberModal = function(affirmRemoveMemberId,
					affirmRemoveMemberText) {
				$('#affirmRemoveMemberId').val(affirmRemoveMemberId);
				$('#affirmRemoveMemberText').text(affirmRemoveMemberText);
				$('#affirmRemoveMemberModal').modal('show');
			}
		});

module.controller('ProfileController', function($scope, DataService, $route,
		$resource, $http) {
	// 动态参数的管理
	$scope.addDynamicAttribute = function(key, value) {
		if (key == null || key.trim() == '') {
			app.alertError("参数名不能为空！", "addParamAlertDiv");
			return;
		}
		key = key.trim();
		if ($scope.vs.dynamicAttributes[key] != null) {
			app.appError('通知', "该参数名( " + key + " )已经存在，不能添加！");
		} else {
			if (value != null) {
				$scope.vs.dynamicAttributes[key] = value;
			} else {
				$scope.vs.dynamicAttributes[key] = '';
			}
		}
		$('#addParamModal').modal('hide');
	}
	$scope.addNewDynamicAttribute = function() {
		$scope.addDynamicAttribute($('#addParamKey').val(), $('#addParamValue')
				.val());
	}
	$scope.removeDynamicAttribute = function(key) {
		delete $scope.vs.dynamicAttributes[key];
	}
	$scope.getInputType = function(key) {
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

	// instance
	$scope.removeInstance = function(index) {
		$scope.vs.instances.splice(index, 1);
	}
	$scope.addInstance = function() {
		var instance = new Object();
		instance.ip = '';
		$scope.vs.instances.push(instance);
	}
});
