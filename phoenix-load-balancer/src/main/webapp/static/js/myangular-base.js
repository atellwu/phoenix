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

//	var PropertiesDefinedInputs = $resource(window.contextpath
//			+ '/base/propertiesDefinedInputs');
//	model.propertiesDefinedInputs = PropertiesDefinedInputs.get(function() {
//	});
	var PropertiesDefinedInputs = $resource(window.contextpath + '/base/propertiesDefinedInputs');
	model.getPropertiesDefinedInputs = function() {
		model.propertiesDefinedInputs = PropertiesDefinedInputs.get(function() {
		});
		return model.propertiesDefinedInputs;
	};

//	var DirectiveDefinedInputs = $resource(window.contextpath
//			+ '/base/directiveDefinedInputs');
//	model.directiveDefinedInputs = DirectiveDefinedInputs.get(function() {
//	});
	var DirectiveDefinedInputs = $resource(window.contextpath + '/base/directiveDefinedInputs');
	model.getDirectiveDefinedInputs = function() {
		model.directiveDefinedInputs = DirectiveDefinedInputs.get(function() {
		});
		return model.directiveDefinedInputs;
	};

//	var Strategies = $resource(window.contextpath + '/base/listStrategies');
//	model.strategies = Strategies.query(function() {
//	});
	var Strategies = $resource(window.contextpath + '/base/listStrategies');
	model.getStrategies = function() {
		model.strategies = Strategies.query(function() {
		});
		return model.strategies;
	};

//	var Pools = $resource(window.contextpath + '/base/listPools');
//	model.pools = Pools.query(function() {
//	});
	var Pools = $resource(window.contextpath + '/base/listPools');
	model.getPools = function() {
		model.pools = Pools.query(function() {
		});
		return model.pools;
	};

	var SlbPools = $resource(window.contextpath + '/base/listSlbPools');
	model.getSlbPools = function() {
		model.slbPools = SlbPools.query(function() {
		});
		return model.slbPools;
	};

//	var Aspects = $resource(window.contextpath + '/base/listAspects');
//	model.aspects = Aspects.query(function() {
//	});
	var Aspects = $resource(window.contextpath + '/base/listAspects');
	model.getAspects = function() {
		model.aspects = Aspects.query(function() {
		});
		return model.aspects;
	};

	// list tag的resource
	// model.Tags = $resource(window.contextpath + '/vs/:vsName0/tag/list');

	return model;
});
