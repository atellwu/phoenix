module.controller('PoolController', function($scope, DataService, $resource,
		$http) {
	$scope.strategies = DataService.strategies;
	var poolChanged = false;
	$scope.pool = null;
	$scope.getPool = function(poolName) {
		$http({
			method : 'GET',
			url : window.contextpath + '/pool/' + poolName + '/get'
		}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				if (data.pool == null) {// 新建pool
					$scope.pool = new Object();
					$scope.pool.name = poolName;
					$scope.newPool = true;
				} else {
					$scope.pool = data.pool;
					$scope.newPool = false;
				}
				// 开始监听pool的修改
				$scope.$watch('pool', function(newValue, oldValue) {
					if (newValue != oldValue) {
						poolChanged = true;
					}
				}, true);
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
			data : $scope.pool,
			url : window.contextpath + '/pool/' + $scope.pool.name + '/save'
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						app.alertSuccess("保存成功！ 即将刷新页面...");
						poolChanged = false;// 保存成功，修改标识重置
						setTimeout(function() {
							window.location = window.contextpath + "/pool/"
									+ $scope.pool.name;
						}, 700);
					} else {
						app.alertError("保存失败: " + data.errorMessage);
					}
				}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	// 删除
	$scope.removePool = function() {
		$http({
			method : 'POST',
			data : $scope.pool,
			url : window.contextpath + '/pool/' + $scope.pool.name + '/remove'
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						app.alertSuccess("删除成功！ 即将刷新页面...",
								"removePoolAlertDiv");
						setTimeout(function() {
							window.location = window.contextpath + "/";
						}, 700);
					} else {
						app.alertError("删除失败: " + data.errorMessage,
								"removePoolAlertDiv");
					}
				}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	// 用於select
	$scope.getInitSelectValue = function(curValue, valueList, propertyName) {
		var re = curValue;
		if (propertyName) {
			if ((curValue == null || curValue == '') && valueList
					&& valueList.length > 0)
				re = valueList[0][propertyName];
		} else {
			if ((curValue == null || curValue == '') && valueList
					&& valueList.length > 0) {
				re = valueList[0];
			}
		}
		return re;
	}
	$scope.edit = function() {
		window.location = window.contextpath + '/pool/' + $scope.pool.name
				+ '/edit' + window.location.hash;
	}
	$scope.cancleEdit = function() {
		window.location = window.contextpath + '/pool/' + $scope.pool.name
				+ window.location.hash;
	}
	// 存活的member
	$scope.getAliveMemberCount = function() {
		return '待咨询jinhua';
	}
	// member增删
	$scope.addMember = function() {
		var member = new Object();
		member.state = 'ENABLED';
		member.availability = 'AVAILABLE';
		var members = $scope.pool.members;
		if (!members) {
			members = [];
			$scope.pool.members = members;
		}
		members.push(member);
	}
	$scope.affirmRemoveMemberModal = function(index) {
		$scope.memberToBeRemove = $scope.pool.members[index];
		$scope.memberIndexToBeRemove = index;
		$('#affirmRemoveMemberModal').modal('show');
	}
	$scope.removeMember = function() {
		$scope.pool.members.splice($scope.memberIndexToBeRemove, 1);
		$('#affirmRemoveMemberModal').modal('hide');
	}
	// 离开页面时，对比一下pool是否发生了修改
	var onunload = function() {
		if (poolChanged) {
			return "您的修改尚未保存，现在离开将丢失所有修改";
		}
	}
	window.onbeforeunload = onunload;

});