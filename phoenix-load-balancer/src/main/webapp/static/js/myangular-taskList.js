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

module.controller('TaskListController', function($scope, $resource, $http) {
	$scope.newTask = {};
	$scope.newTask.selectedVsAndTags = [ {
		"vsName" : "",
		"tag" : ""
	} ];
	$scope.vsList = [];
	$http({
		method : 'GET',
		url : window.contextpath + '/vs/list'
	}).success(function(data, status, headers, config) {
		$scope.vsList = data;
	}).error(function(data, status, headers, config) {
		app.appError("响应错误", data);
	});
	$scope.vs2Tags = {};// vs和tag的cache
	$scope.getTags = function(vsName) {
		console.log(vsName);
		if (vsName == null || vsName.trim() == ""
				|| $scope.vs2Tags[vsName] != null) {
			return;
		}
		$http({
			method : 'GET',
			url : window.contextpath + '/vs/' + vsName + '/tag/list'
		}).success(function(data, status, headers, config) {
			$scope.vs2Tags[vsName] = data;
		}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	}
	$scope.addVsAndTag = function() {
		$scope.newTask.selectedVsAndTags.push({
			"vsName" : "",
			"tag" : ""
		});
	}
	$scope.addTaskModal = function() {
		// 显示modal
		var width = $(window).width();
		var height = $(window).height();
		var left = (width - 700) / 2;
		var modal = $('#addTaskModal');
		modal.css('height', height - 20);
		modal.css('width', 700);
		modal.css('top', 10);
		if (left > 0) {
			modal.css('left', left);
			$('#addTaskModal>.modal-footer').css('left', left);
		}
		var modalBody = $('#addTaskModal>.modal-body');
		modalBody.css('height', height - 170);
		modalBody.css('max-height', height - 145);
		$('#addTaskModal').modal('show');
	}
	$scope.addTask = function() {
		$http({
			method : 'POST',
			data : $scope.newTask,
			url : window.contextpath + '/deploy/task/add'
		}).success(
				function(data, status, headers, config) {
					if (data.errorCode == 0) {
						app.alertSuccess("保存成功！ 即将刷新页面...","addTaskAlertDiv");
						vsChanged = false;// 保存成功，修改标识重置
						setTimeout(function() {
							app.refresh();
						}, 700);
					} else {
						app.alertError("保存失败: " + data.errorMessage,"addTaskAlertDiv");
					}
				}).error(function(data, status, headers, config) {
			app.appError("响应错误", data);
		});
	}

});
