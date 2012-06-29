/*
 * Grunt.js is a tool for automated JavaScript development
 * https://github.com/cowboy/grunt
 *
 * To use it:
 * 1) install node.js - http://nodejs.org/#download. This will install 'npm' (Node Package Manager) too
 * 2) install phantomjs - http://code.google.com/p/phantomjs/downloads/list
 * 3) install grunt - 'npm -g install grunt'
 * 4) run it: grunt lint[:all] qunit[:index] qunit:local
 */

module.exports = function(grunt) {

	// Project configuration.
	grunt.initConfig({
		lint: {
			all: [
				'wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-event-jquery.js',
				'wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery-debug.js',
				'wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js'
			]
		},

		jshint: {
			options: {
				"boss": true,
				"browser": true,
				"curly": true,
				"eqnull": true,
				"eqeqeq": true,
				"expr": true,
				"jquery": true,
				"latedef": true,
				"noarg": true,
				"onevar": true,
				"smarttabs": true,
				"trailing": true,
				"undef": true,
				"predef": [
					"ActiveXObject"
				]
			}
		},

		qunit: {
			/*
			 * Runs all tests (w/ ajax).
			 * See ajax.js header for details how to setup it.
			 */
			index: ['http://localhost/ajax-tests/test/js/all.html'],

			/*
			 * Runs only local tests (w/o ajax ones).
			 */
			local: ['wicket-core/src/test/js/all.html']
		}
	});
};
