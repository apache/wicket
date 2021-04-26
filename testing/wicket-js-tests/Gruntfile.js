/*
 * Grunt.js is a task runner for JavaScript development.
 * Grunt and its plugins are installed and managed via npm, the Node.js package manager.
 * http://gruntjs.com/
 *
 * To use it:
 * 1) install node.js - http://nodejs.org/#download. This will install 'npm' (Node Package Manager) too.
 * 3) run: npm install (This will use package.json and install grunt and all dependencies)
 * 4.1) grunt jshint - checks all JavaScript files with JSHint
 * 4.2) grunt jshint:core - checks only the files in wicket-core
 * 4.3) grunt - starts the registered tasks: starting a web server and running all tests (Ajax, non-Ajax and AMD)
 */

 /*global module: true */

module.exports = function(grunt) {
	"use strict";

	var
		coreJs = [
			'../../wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery-debug.js',
			'../../wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js',
			"../../wicket-core/src/main/java/org/apache/wicket/markup/html/form/CheckSelector.js",
			"../../wicket-core/src/main/java/org/apache/wicket/markup/html/form/upload/MultiFileUploadField.js",
			"../../wicket-core/src/main/java/org/apache/wicket/ajax/form/AjaxFormChoiceComponentUpdatingBehavior.js",
			"../../wicket-core/src/main/java/org/apache/wicket/markup/html/pages/wicket-browser-info.js"
		],
		extensionsJs = [
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/form/upload/progressbar.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/wicket-ajaxdownload.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/form/palette/palette.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/autocomplete/wicket-autocomplete.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/ajax/markup/html/modal/res/modal.js",
			"../../wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/repeater/data/table/filter/wicket-filterform.js"
		],
		nativeWebSocketJs = [
			"../../wicket-native-websocket/wicket-native-websocket-core/src/main/java/org/apache/wicket/protocol/ws/api/res/js/wicket-websocket-jquery.js"
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
		gymTestsJs = [
			"../../wicket-examples/src/main/webapp/js-test/tests/ajax/form.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/bean-validation/birthdate.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/bean-validation/email.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/bean-validation/name.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/bean-validation/phone.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/cdi/auto-conversation.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/cdi/conversation.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/cdi/injection.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/echo.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/forminput.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/helloworld.js",
			"../../wicket-examples/src/main/webapp/js-test/tests/mailtemplate.js"
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
			nativeWebSocket: nativeWebSocketJs,
			testsJs: testsJs,
			gymTestsJs: gymTestsJs,
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
					urls: [
						'http://localhost:38887/test/js/all.html?1.12.4',
						'http://localhost:38887/test/js/all.html?2.2.4',
						'http://localhost:38887/test/js/all.html?3.6.0'
					]
				}
			},

			/**
			 * Run Asynchronous module definition tests
			 */
			amd: {
				options: {
					urls: [
						'http://localhost:38887/test/js/amd.html?1.12.4',
						'http://localhost:38887/test/js/amd.html?2.2.4',
						'http://localhost:38887/test/js/amd.html?3.6.0'
					]
				}
			}
		},

		connect: {
			server: {
				options: {
					port: 38887,
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
