module.controller('PoolController',
		function($scope, DataService, $resource, $http) {
			$scope.strategies = DataService.strategies;
			// pool视图切换
			$scope.poolEditing = null;
			$scope.switchPool = function(index) {
				$scope.poolEditing = $scope.vs.pools[index];
			};
			$scope.switchPoolList = function() {
				$scope.poolEditing = null;
			}
			// pool增删
			$scope.openAddPoolModal = function() {
				$scope.poolToBeAdd = new Object();
				$('#addPoolModal').modal('show');
				$('#addPoolName').focus();
			};
			$scope.addPool = function() {
				var name = $scope.poolToBeAdd.name;
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
					$scope.vs.pools.push($scope.poolToBeAdd);
					$('#addPoolModal').modal('hide');
				}
			}
			$scope.affirmRemovePoolModal = function(index) {
				$scope.poolToBeRemove = $scope.vs.pools[index];
				$scope.poolIndexToBeRemove = index;
				$('#affirmRemovePoolModal').modal('show');
			}
			$scope.removePool = function() {
				$scope.vs.pools.splice($scope.poolIndexToBeRemove, 1);
				$('#affirmRemovePoolModal').modal('hide');
			}
			//存活的member
			$scope.getAliveMemberCount = function(){
				return '待咨询jinhua';
			}
			// member增删
			$scope.addMember = function() {
				var member = new Object();
				member.state = 'ENABLED';
				member.availability = 'AVAILABLE';
				var members = $scope.poolEditing.members;
				if (!members) {
					members = [];
					$scope.poolEditing.members = members;
				}
				members.push(member);
			}
			$scope.affirmRemoveMemberModal = function(index) {
				$scope.memberToBeRemove = $scope.poolEditing.members[index];
				$scope.memberIndexToBeRemove = index;
				$('#affirmRemoveMemberModal').modal('show');
			}
			$scope.removeMember = function() {
				$scope.poolEditing.members.splice($scope.memberIndexToBeRemove,
						1);
				$('#affirmRemoveMemberModal').modal('hide');
			}
		});
