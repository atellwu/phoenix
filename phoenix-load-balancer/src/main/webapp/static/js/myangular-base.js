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

module.factory('DataService', function($resource) {
	var model = {};

	var PropertiesDefinedInputs = $resource(window.contextpath
			+ '/base/propertiesDefinedInputs');
	model.propertiesDefinedInputs = PropertiesDefinedInputs.get(function() {
	});

	var DirectiveDefinedInputs = $resource(window.contextpath
			+ '/base/directiveDefinedInputs');
	model.directiveDefinedInputs = DirectiveDefinedInputs.get(function() {
	});

	var Strategies = $resource(window.contextpath + '/base/strategies');
	model.strategies = Strategies.query(function() {
	});

	return model;
});
