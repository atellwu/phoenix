module.controller('VsController', function($scope, DataService, $resource,
		$http) {
	$scope.selectedTab = 'profile';
	// list tag的resource
	var Tags = $resource(window.contextpath + '/vs/:vsName/tag/list', {
		vsName : '@vsName'
	});
	// 获取hash，设置给selectedTab
	var hash = window.location.hash;
	if (hash.length > 1) {// 去掉#号
		hash = hash.substring(1);
		if (hash == 'profile' || hash == 'pool' || hash == 'location') {
			$scope.selectedTab = hash;
		}
	}
	$scope.isActive = function(tabName) {
		var clazz = ($scope.selectedTab == tabName) ? 'active' : '';
		return clazz;
	};
	var vsChanged = false;
	$scope.vs = null;
	$scope.getVs = function(vsName) {
		$http({
			method : 'GET',
			url : window.contextpath + '/vs/' + vsName + '/get'
		}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				if (data.virtualServer == null) {// 新建vs
					$scope.vs = new Object();
					$scope.vs.name = vsName;
					$scope.vs.dynamicAttributes = new Object();
					$scope.vs.instances = [];
					$scope.vs.instances.push(new Object());
					$scope.vs.pools = [];
					$scope.vs.state = 'ENABLED';
					$scope.vs.availability = 'AVAILABLE';
					$scope.vs.locations = [];
					$scope.newVs = true;
				} else {
					$scope.vs = data.virtualServer;
					$scope.newVs = false;
				}
				// 开始监听vs的修改
				$scope.$watch('vs', function(newValue, oldValue) {
					if (newValue != oldValue) {
						vsChanged = true;
					}
				}, true);
			} else {
				app.alertError("获取失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
		// 获取tag list
		$scope.tags = Tags.get({
			vsName : vsName
		}, function() {
		});
	};
	// 保存
	$scope.save = function() {
		$http({
			method : 'POST',
			data : $scope.vs,
			url : window.contextpath + '/vs/' + $scope.vs.name + '/save'
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						app.alertSuccess("保存成功！ 即将刷新页面...");
						vsChanged = false;// 保存成功，修改标识重置
						setTimeout(function() {
							window.location = window.contextpath + "/vs/"
									+ $scope.vs.name + window.location.hash;
						}, 700);
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
			url : window.contextpath + '/vs/' + $scope.vs.name + '/remove'
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						app.alertSuccess("删除成功！ 即将刷新页面...",
								"removeVirtualServerAlertDiv");
						setTimeout(function() {
							window.location = window.contextpath + "/";
						}, 700);
					} else {
						app.alertError("删除失败: " + data.errorMessage,
								"removeVirtualServerAlertDiv");
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
		window.location = window.contextpath + '/vs/' + $scope.vs.name
				+ '/edit' + window.location.hash;
	}
	$scope.cancleEdit = function() {
		window.location = window.contextpath + '/vs/' + $scope.vs.name
				+ window.location.hash;
	}
	$scope.preview = function() {
		// 显示modal
		var width = $(window).width();
		var height = $(window).height();
		var left = (width - 900) / 2;
		var modal = $('#previewVirtualServerModal');
		modal.css('height', height - 20);
		modal.css('width', 900);
		modal.css('top', 10);
		if (left > 0) {
			modal.css('left', left);
			$('#previewVirtualServerModal>.modal-footer').css('left', left);
		}
		var modalBody = $('#previewVirtualServerModal>.modal-body');
		modalBody.css('height', height - 170);
		modalBody.css('max-height', height - 145);
		window.nginxConfigEditor.setValue("");
		$('#nginxConfigEditor').show();
		$('#previewVirtualServerModal').modal('show');
		$http({
			method : 'POST',
			data : $scope.vs,
			url : window.contextpath + '/vs/' + $scope.vs.name + '/preview'
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						window.nginxConfigEditor.setValue(data.nginxConfig);
						window.nginxConfigEditor.moveCursorTo(0, 0);
					} else {
						$('#nginxConfigEditor').hide();
						app.alertError("预览失败: " + data.errorMessage,
								"previewVirtualServerAlertDiv");
					}
				}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	}

	// addTag
	$scope.addTag = function() {
		var param = new Object();
		// param.virtualServerName = $scope.vs.name;
		param.version = $scope.vs.version;
		$http({
			method : 'POST',
			data : $.param(param),
			headers : {
				'Content-Type' : 'application/x-www-form-urlencoded'
			},
			url : window.contextpath + '/vs/' + $scope.vs.name + '/tag/add'
		}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				app.alertSuccess("保存成功！ 即将刷新页面...");
				// vsChanged = false;// 保存成功，修改标识重置
				// setTimeout(function() {
				// window.location = window.contextpath + "/vs/"
				// + $scope.vs.name + window.location.hash;
				// }, 700);
			} else {
				app.alertError("保存失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	$scope.viewTag = function(tagId) {
		$http(
				{
					method : 'GET',
					url : window.contextpath + '/vs/' + $scope.vs.name
							+ '/tag/' + tagId
				}).success(function(data, status, headers, config) {
			if (data.errorCode == 0) {
				app.alertSuccess("保存成功！ 即将刷新页面...");
				// vsChanged = false;// 保存成功，修改标识重置
				// setTimeout(function() {
				// window.location = window.contextpath + "/vs/"
				// + $scope.vs.name + window.location.hash;
				// }, 700);
			} else {
				app.alertError("保存失败: " + data.errorMessage);
			}
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	};
	// 离开页面时，对比一下vs是否发生了修改
	var onunload = function() {
		if (vsChanged) {
			return "您的修改尚未保存，现在离开将丢失所有修改";
		}
	}
	window.onbeforeunload = onunload;

});