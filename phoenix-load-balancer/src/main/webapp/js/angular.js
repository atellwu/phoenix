var module = angular.module('MyApp', [ 'ngResource' ]);

module.config(function($routeProvider, $locationProvider, $resourceProvider) {
	$routeProvider.when('#/:vsName/:controller', {
		controller : '{{ controller }}'
	});
	// $routeProvider.when('/Book/:bookId/ch/:chapterId', {
	// templateUrl : 'chapter.html',
	// controller : ChapterCntl
	// });

	// configure html5 to get links working on jsfiddle
	$locationProvider.html5Mode(true);
});

module.factory('DataService', function() {
	var model = {};
	return model;
});

module.controller('VsNameListController', function($scope, DataService, $route,
		$resource) {
	$scope.vsNameList = [ {
		'name' : 'Nexus S',
		'snippet' : 'Fast just got faster with Nexus S.'
	}, {
		'name' : 'Motorola',
		'snippet' : 'The Next, Next Generation tablet.'
	}, {
		'name' : 'MOTOROLA',
		'snippet' : 'The Next, Next Generation tablet.'
	} ];
	// alert('ProfileController');
	// var CreditCard = $resource('/user/:userId/card/:cardId',
	// {userId:123}, {
	// charge: {method:'POST', params:{charge:true}}
	// });
	//	
	// CreditCard.get();

	// $route.when('/book/:title', {
	// template : '{{ title }}',
	// controller : function($scope, $routeParams) {
	// $scope.title = $routeParams.title;
	// }
	// });
});

module.controller('"ProfileController"', function($scope, DataService, $route,
		$resource) {
	$scope.vsNameList = [ {
		'name' : 'Nexus S',
		'snippet' : 'Fast just got faster with Nexus S.'
	}, {
		'name' : 'Motorola',
		'snippet' : 'The Next, Next Generation tablet.'
	}, {
		'name' : 'MOTOROLA',
		'snippet' : 'The Next, Next Generation tablet.'
	} ];
});
