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
