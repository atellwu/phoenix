module
		.controller(
				'LocationController',
				function($scope, DataService, $route, $resource, $http) {
					// location视图切换
					$scope.locationEditing = null;
					$scope.switchLocation = function(index) {
						$scope.locationEditing = $scope.vs.locations[index];
					};
					$scope.switchLocationList = function() {
						$scope.locationEditing = null;
					}
					// location增删
					$scope.openAddLocationModal = function() {
						$scope.locationToBeAdd = new Object();
						$scope.locationToBeAdd.caseSensitive = true;
						$('#addLocationModal').modal('show');
						$('#addLocationName').focus();
					};
					$scope.addLocation = function() {
						var domain = $scope.locationToBeAdd.domain;
						if (domain == null || domain.trim() == '') {
							app.alertError("集群名必选！", "addLocationAlertDiv");
							return;
						}
						$scope.vs.locations.push($scope.locationToBeAdd);
						$('#addLocationModal').modal('hide');
					}
					$scope.affirmRemoveLocationModal = function(index) {
						$scope.locationToBeRemove = $scope.vs.locations[index];
						$scope.locationIndexToBeRemove = index;
						$('#affirmRemoveLocationModal').modal('show');
					}
					$scope.removeLocation = function() {
						$scope.vs.locations.splice(
								$scope.locationIndexToBeRemove, 1);
						$('#affirmRemoveLocationModal').modal('hide');
					}
					// directive增删
					$scope.directiveDefinedInputs = DataService.directiveDefinedInputs;
					$scope.getInputs = function(type) {
						return $scope.directiveDefinedInputs[type];
					}
					$scope.getValueList = function(inputs, name) {
						return inputs[name];
						return input.valueList;
					}
					$scope.openAddDirectiveModal = function() {
						$scope.directiveToBeAdd = new Object();
						$scope.directiveToBeAdd.dynamicAttributes = {};
						$('#addDirectiveModal').modal('show');
						$('#addDirectiveType').focus();
					};
					$scope.addDirective = function() {
						var directives = $scope.locationEditing.directives;
						if (!directives) {
							directives = [];
							$scope.locationEditing.directives = directives;
						}
						// 根据勾选的type，找到inputs模板
						var inputs = $scope.directiveDefinedInputs[$scope.directiveToBeAdd.type];
						// 给$scope.directiveToBeAdd赋予空的inputs模板的键值对
						for ( var name in inputs) {
							$scope.directiveToBeAdd.dynamicAttributes[name] = "";
						}
						directives.push($scope.directiveToBeAdd);
						$('#addDirectiveModal').modal('hide');
					}
					$scope.affirmRemoveDirectiveModal = function(index) {
						$scope.directiveToBeRemove = $scope.locationEditing.directives[index];
						$scope.directiveIndexToBeRemove = index;
						$('#affirmRemoveDirectiveModal').modal('show');
					}
					$scope.removeDirective = function() {
						$scope.locationEditing.directives.splice(
								$scope.directiveIndexToBeRemove, 1);
						$('#affirmRemoveDirectiveModal').modal('hide');
					}
					//指令下的属性的增删
					$scope.addDynamicAttribute = function(directive,name){
						if (directive.dynamicAttributes[name] != null) {
							app.appError('通知', "该参数名( " + name + " )已经存在，不能添加！");
						} else {
							directive.dynamicAttributes[name] = '';
						}
					}
					$scope.removeDynamicAttribute = function(directive,name) {
						delete directive.dynamicAttributes[name];
					}
					
				});
