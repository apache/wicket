/*
 * Grunt.js is a tool for automated JavaScript development
 * https://github.com/cowboy/grunt
 *
 * To use it:
 * 1) install node.js - http://nodejs.org/#download. This will install 'npm' (Node Package Manager) too
 * 2) install grunt - 'npm -g install grunt-cli'
 * 3) run: npm install (This will use package.json and install all dependencies)
 * 4.1) grunt jshint - checks all JavaScript files with JSHint
 * 4.2) grunt jshint:core - checks only the files in wicket-core
 * 4.3) grunt test - starts a web server and runs all tests (Ajax, non-Ajax and AMD)
 */

 /*global module: true */

module.exports = function(grunt) {
	"use strict";

	var
		coreJs = [
			'../../wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-event-jquery.js',
			'../../wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery-debug.js',
			'../../wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js',
			"../../wicket-core/src/main/java/org/apache/wicket/markup/html/form/CheckSelector.js",
			"../../wicket-core/src/main/java/org/apache/wicket/markup/html/form/upload/MultiFileUploadField.js",
			"../../wicket-core/src/main/java/org/apache/wicket/ajax/form/AjaxFormChoiceComponentUpdatingBehavior.js",
			"../../wicket-core/src/main/java/org/apache/wicket/markup/html/pages/wicket-browser-info.js"
		],
		extensionsJs = [
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/form/upload/progressbar.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/form/palette/palette.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/tree/res/tree.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/autocomplete/wicket-autocomplete.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/modal/res/modal.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/repeater/data/table/filter/wicket-filterform.js"
		],
		datetimeJs = [
			"../../wicket-datetime/src/main/java/org/apache/wicket/extensions/yui/calendar/wicket-date.js"
		],
		nativeWebSocketJs = [
			"../../wicket-native-websocket/wicket-native-websocket-core/src/main/java/org/apache/wicket/protocol/ws/api/res/js/wicket-websocket-jquery.js"
		],
		atmosphereJs = [
			"../../wicket-experimental/wicket-atmosphere/src/main/java/org/apache/wicket/atmosphere/jquery.wicketatmosphere.js"
		],
		testsJs = [
			"../../wicket-core/src/test/js/ajax.js",
			"../../wicket-core/src/test/js/head.js",
			"../../wicket-core/src/test/js/form.js",
			"../../wicket-core/src/test/js/dom.js",
			"../../wicket-core/src/test/js/channels.js",
			"../../wicket-core/src/test/js/event.js",
			"../../wicket-core/src/test/js/timer.js",
			"../../wicket-core/src/test/js/amd.js"
		],
		gruntJs = [
			"Gruntfile.js"
		];

	// Project configuration.
	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),

		jshint: {
			core: coreJs,
			extensions: extensionsJs,
			datetime: datetimeJs,
			nativeWebSocket: nativeWebSocketJs,
			atmosphere: atmosphereJs,
			testsJs: testsJs,
			grunt: gruntJs,

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
			all: {
				options: {
			        urls: ['http://localhost:38888/test/js/all.html']
			    }
			},

			/**
			 * Run Asynchronous module definition tests
			 */
			amd: {
				options: {
					urls: ['http://localhost:38888/test/js/amd.html']
				}
			},

			/*
			 * Runs only local tests (w/o ajax ones).
			 */
			local: ['../../wicket-core/src/test/js/all.html']
		},

		connect: {
			server: {
				options: {
					port: 38888,
//					debug: true,
					middleware: function(connect, options, middlewares) {
						middlewares.unshift(function(req, res, next) {
							if (req.url.indexOf('submitNestedForm') > 0) {
								// WICKET-5631
								req.method = 'GET';
							}
							return next();
						});

						return middlewares;
					  },
					base: '../../wicket-core/src'
				}
			}
		}
	});

	grunt.loadNpmTasks('grunt-contrib-qunit');
	grunt.loadNpmTasks('grunt-contrib-jshint');

	// This plugin provides the "connect" task - starts a web server for the Ajax tests.
	grunt.loadNpmTasks('grunt-contrib-connect');

	grunt.registerTask('default', ['jshint', 'connect', 'qunit']);
};
