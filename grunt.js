/*
 * Grunt.js is a tool for automated JavaScript development
 * https://github.com/cowboy/grunt
 *
 * To use it:
 * 1) install node.js - http://nodejs.org/#download. This will install 'npm' (Node Package Manager) too
 * 2) install phantomjs - http://code.google.com/p/phantomjs/downloads/list
 * 3) install grunt - 'npm -g install grunt'
 * 4) run it: grunt lint, grunt lint:core, grunt qunit, grunt qunit:local
 */

 /*global module: true */

module.exports = function(grunt) {
	"use strict";

	var
		coreJs = [
			'wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-event-jquery.js',
			'wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery-debug.js',
			'wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js',
			"wicket-core/src/main/java/org/apache/wicket/markup/html/form/CheckGroupSelector.js",
			"wicket-core/src/main/java/org/apache/wicket/markup/html/form/CheckboxMultipleChoiceSelector.js",
			"wicket-core/src/main/java/org/apache/wicket/markup/html/form/AbstractCheckSelector.js",
			"wicket-core/src/main/java/org/apache/wicket/markup/html/form/upload/MultiFileUploadField.js",
			"wicket-core/src/main/java/org/apache/wicket/markup/html/form/CheckBoxSelector.js"
		],
		extensionsJs = [
			"wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/form/upload/progressbar.js",
			"wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/form/palette/palette.js",
			"wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/tree/res/tree.js",
			"wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/autocomplete/wicket-autocomplete.js",
			"wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/modal/res/modal.js"
		],
		datetimeJs = [
			"wicket-datetime/src/main/java/org/apache/wicket/extensions/yui/calendar/wicket-date.js"
		],
		nativeWebSocketJs = [
		"wicket-experimental/wicket-native-websocket/wicket-native-websocket-core/src/main/java/org/apache/wicket/protocol/ws/api/res/js/wicket-websocket-jquery.js"
		],
		atmosphereJs = [
			"wicket-experimental/wicket-atmosphere/src/main/java/org/apache/wicket/atmosphere/jquery.wicketatmosphere.js"
		],
		gruntJs = [
			"grunt.js"
		];

	// Project configuration.
	grunt.initConfig({
		lint: {
			core: coreJs,
			extensions: extensionsJs,
			datetime: datetimeJs,
			nativeWebSocket: nativeWebSocketJs,
			atmosphere: atmosphereJs,
			grunt: gruntJs
		},

		jshint: {
			options: {
				"boss": true,
				"browser": true,
				"curly": true,
				"eqnull": true,
				"eqeqeq": true,
				"expr": true,
				"evil": true,
				"jquery": true,
				"latedef": true,
				"noarg": true,
				"onevar": false,
				"smarttabs": true,
				"trailing": true,
				"undef": true,
				"strict": true,
				"predef": [
					"Wicket"
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
