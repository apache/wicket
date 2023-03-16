/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
	Note: these tests run only through Web Server.
	Here is a possible setup for Apache HTTPD:

		 Alias /ajax-tests "/path/to/wicket/wicket-core/src"

		 <Directory "/path/to/wicket/wicket-core/src">

		 Options Indexes
		 AllowOverride None AuthConfig

		 Order allow,deny
		 Allow from all

		 </Directory>

	Or start StartJavaScriptTests.java in project wicket-js-tests. 
 */

/*global ok: true, start: true, test: true, equal: true, deepEqual: true,
 QUnit: true, expect: true, console: true  */

jQuery(document).ready(function() {
	"use strict";

	const { module, test } = QUnit;

	var execute = function (attributes, assert, done) {
		const done2 = done || assert.async();
		Wicket.testDone = done2;

		var defaults = {
				fh: [
					function () {
						done2();
						assert.ok(false, 'Failure handler should not be executed!');
					}
				],
				ch: '0|s',
				sh: [
					function () {
						assert.ok(true, 'Success handler is executed');
					}
				]
		};
		var attrs = jQuery.extend({}, defaults, attributes);
		var call = new Wicket.Ajax.Call();
		call.ajax(attrs);

	};

	// Ajax tests are executed only when run with Web Server
	if ( !QUnit.isLocal ) {

		module('Wicket.Ajax', {
			beforeEach: function() {
				// unsubscribe all global listeners
				Wicket.Event.unsubscribe();
			}
		});

		test('processEvaluation with mock data.', assert => {
			Wicket.assert = assert;
			assert.expect(2);

			var attrs = {
				u: 'data/ajax/evaluationId.xml',
				c: 'evaluationId'
			};
			execute(attrs, assert);
		});

		/**
		 * Suspends execution.
		 */
		test('processEvaluation with suspend.', assert => {
			Wicket.assert = assert;
			assert.expect(2);

			var attrs = {
				u: 'data/ajax/evaluationIdentifierAndCodeId.xml',
				c: 'evaluationIdentifierAndCodeId'
			};
			execute(attrs, assert);
		});

		/**
		 * Suspends executions.
		 */
		test('processEvaluation*s* with suspend.', assert => {
			Wicket.assert = assert;
			assert.expect(4);

			var attrs = {
				u: 'data/ajax/multipleEvaluationsWithIdentifier.xml',
				c: 'multipleEvaluationsWithIdentifier'
			};
			execute(attrs, assert);
		});

		test('processComponent, normal case.', assert => {
			const done = assert.async();
			assert.expect(2);

			assert.equal(jQuery('#componentToReplace').text(), 'old body', 'The component is existing and has the old innerHTML');

			var attrs = {
				u: 'data/ajax/componentId.xml',
				c: 'componentId',
				sh: [
					function() {
						done();
						assert.equal(jQuery('#componentToReplace').text(), 'new body', 'The component must be replaced');
					}
				]
			};
			execute(attrs, assert, done);
		});


		test('processComponent() but the old component doesn\'t exist.', assert => {
			const done = assert.async();
			assert.expect(2);

			var oldWicketLogError = Wicket.Log.error;

			Wicket.Log.error = function() {
				assert.equal(arguments[1], "componentToReplaceDoesNotExist");

				// restore the original method
				Wicket.Log.error = oldWicketLogError;
			};

			var attrs = {
				u: 'data/ajax/componentDoesNotExistsId.xml',
				c: 'componentDoesNotExistsId',
				sh: [
					function() {
						done();
						assert.equal(jQuery('#componentToReplaceDoesNotExist').length, 0, 'A component with id \'componentToReplaceDoesNotExist\' must not exist!');
					}
				]
			};
			execute(attrs, assert, done);
		});

		test('processComponent() replace a component with a table with scripts inside.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			Wicket.assert = assert;
			assert.expect(4);

			var attrs = {
				u: 'data/ajax/complexComponentId.xml',
				c: 'complexComponentId',
				sh: [
					function() {
						done();
						assert.equal(jQuery('#componentToReplace')[0].tagName.toLowerCase(), 'table', 'A component with id \'componentToReplace\' must be a table now!');
					}
				]
			};
			execute(attrs, assert, done);

		});


		test('processComponent() replace title\'s text.', assert => {
			const done = assert.async();
			assert.expect(1);

			var oldTitle = jQuery('title').text();

			var attrs = {
				u: 'data/ajax/componentToReplaceTitle.xml',
				c: 'componentToReplaceTitle',
				sh: [
					function() {
						done();
						var $title = jQuery('title');
						assert.equal($title.text(), 'new title', 'The title text should be updated!');
						$title.text(oldTitle);
					}
				]
			};
			execute(attrs, assert, done);
		});

		test('non-wicket response.', assert => {
			const done = assert.async();
			assert.expect(2);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						done();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						assert.deepEqual(data, expected);
						assert.equal('success', textStatus);
					}
				]
			};
			execute(attrs, assert, done);
		});

		test('listen on several events.', assert => {
			const done = assert.async();
			assert.expect(4);

			var calls = 0;

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: [ 'event1', 'event2' ],
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						assert.deepEqual(data, expected);
						assert.equal('success', textStatus);

						if (++calls === 2) {
							done();
							jQuery(window).off("event1 event2");
						}
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.triggerHandler("event2");
		});


		test('throttle execution.', assert => {
			const done = assert.async();
			assert.expect(2);

			var attrs = {
				tr: {
					id: "someId",
					d: 100, // in millis
					p: false
				},
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						done();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						assert.deepEqual(data, expected);
						assert.equal('success', textStatus);
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);

			// this one will be throttled
			target.triggerHandler("event1");

			// this one will override the previous and will be throttled too
			target.triggerHandler("event1");

			target.off("event1");
		});

		test('verify arguments to IAjaxCallListener handlers. Success scenario.', assert => {
			const done = assert.async();
			assert.expect(13);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				ih: [function() {assert.ok('Init handler should be called');}],
				dh: [function() {assert.ok('Done handler should be called');}],
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						done();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						assert.deepEqual(data, expected, 'Success: data deep equal');
						assert.equal('success', textStatus, 'Success: textStatus');
						assert.equal(attrs.u, attributes.u, 'Success: attributes equal');
						assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Success: Assert that jqXHR is a XMLHttpRequest');
					}
				],
				fh: [
					function(attributes, errorMessage) {
						assert.ok(false, 'Should not be called');
					}
				],
				bsh: [
					function(attributes, jqXHR, settings) {
						assert.equal(attrs.u, attributes.u, 'Before: attributes equal');
						assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
						assert.ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
					}
				],
				ah: [
					function(attributes) {
						assert.equal(attrs.u, attributes.u, 'After: attributes equal');
					}
				],
				coh: [
					function(attributes, jqXHR, textStatus) {
						assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Complete: Assert that jqXHR is a XMLHttpRequest');
						assert.equal('success', textStatus, 'Complete: textStatus');
						assert.equal(attrs.u, attributes.u, 'Complete: attributes equal');
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		test('verify arguments to IAjaxCallListener handlers. Failure scenario.', assert => {
			const done = assert.async();
			assert.expect(13);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				ih: [function() {assert.ok('Init handler should be called');}],
				dh: [function() {assert.ok('Done handler should be called');}],
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						assert.ok(false, 'Should not be called');
					}
				],
				fh: [
					function(attributes, jqXHR, errorMessage, textStatus) {
						done();
						assert.equal(attrs.u, attributes.u);
						assert.ok(typeof(jqXHR) === "object", "jqXHR should be passed");
						assert.equal(errorMessage, "Not Found", "Error message should be passed");
						assert.equal(textStatus, "error", "Text status should be passed");
					}
				],
				bsh: [
					function(attributes, jqXHR, settings) {
						assert.equal(attrs.u, attributes.u);
						assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						assert.ok(jQuery.isFunction(settings.beforeSend), 'Assert that settings is the object passed to jQuery.ajax()');
					}
				],
				ah: [
					function(attributes) {
						assert.equal(attrs.u, attributes.u);
					}
				],
				coh: [
					function(attributes, jqXHR, textStatus) {
						assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						assert.equal('error', textStatus);
						assert.equal(attrs.u, attributes.u);
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * Only attributes with non-default values are transferred to the client side.
		 * All defaults are initialized at the client side.
		 */
		test('verify default attributes.', assert => {
			const done = assert.async();
			assert.expect(26);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				coh: [
					function(attributes, jqXHR, textStatus) {
						done();
						var jQueryVersion = jQuery.fn.jquery;
						if (
							(!!window._phantom) &&
							(jQueryVersion.indexOf("3") === 0 || jQueryVersion.indexOf("2") === 0 )
						) {
							assert.equal(textStatus, "success", "textStatus");
						} else {
							assert.equal(textStatus, "parsererror", "textStatus");
						}
						assert.equal(attributes.u, attrs.u, "url");
						assert.deepEqual(attributes.e, [ "domready" ], "events");
						assert.equal(attributes.event, null, "No event for 'domready'");
						assert.equal(attributes.ch, '0|s', 'channel');
						assert.equal(attributes.dt, 'xml', 'data type');
						assert.equal(attributes.wr, true, 'wicket ajax response');
						assert.equal(attributes.m, 'GET', 'method');
						assert.ok(jQuery.isWindow(attributes.c), 'component');
						assert.ok(attributes.f === undefined, 'form');
						assert.ok(attributes.mp === undefined, 'multipart');
						assert.ok(attributes.sc === undefined, 'submitting component');
						assert.ok(attributes.i === undefined, 'indicator');
						assert.ok(attributes.pre === undefined, 'preconditions');
						assert.ok(attributes.ih === undefined, 'init handlers');
						assert.ok(attributes.bh === undefined, 'before handlers');
						assert.ok(attributes.ah === undefined, 'after handler');
						assert.ok(attributes.sh === undefined, 'success handlers');
						assert.ok(attributes.fh === undefined, 'failure handlers');
						assert.deepEqual(attrs.coh, attributes.coh, 'complete handlers');
						assert.ok(attributes.dh === undefined, 'done handlers');
						assert.ok(attributes.ep === undefined, 'extra parameters');
						assert.ok(attributes.dep === undefined, 'dynamic extra parameters');
						assert.equal(attributes.async, true, 'asynchronous');
						assert.equal(attributes.rt, 0, 'request timeout');
						assert.equal(attributes.pd, false, 'prevent default');
					}
				]
			};

			Wicket.Ajax.ajax(attrs);
		});

		test('verify arguments to global listeners. Success scenario.', assert => {
			const done = assert.async();
			assert.expect(14);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false // not Wicket's <ajax-response>
			};

			Wicket.Event.subscribe('/ajax/call/init', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'Complete: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, attributes, jqXHR, data, textStatus) {
				done();
				var expected = {
					one: 1,
					two: '2',
					three: true
				};
				assert.ok(attributes.event instanceof jQuery.Event, "There must be an event for non-'domready' events");
				assert.deepEqual(data, expected, 'Success: data');
				assert.equal('success', textStatus, 'Success: textStatus');
				assert.equal(attrs.u, attributes.u, 'Success: attrs');
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Success: Assert that jqXHR is a XMLHttpRequest');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes) {
				assert.ok(false, 'Failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				assert.equal(attrs.u, attributes.u, 'Before: attrs');
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				assert.ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'After: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, textStatus) {
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Complete: Assert that jqXHR is a XMLHttpRequest');
				assert.equal('success', textStatus, 'Complete: textStatus');
				assert.equal(attrs.u, attributes.u, 'Complete: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/done', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'Done: attrs');

				// unregister all subscribers
				Wicket.Event.unsubscribe();
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		test('verify arguments to global listeners. Failure scenario.', assert => {
			const done = assert.async();
			assert.expect(13);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false // not Wicket's <ajax-response>
			};

			Wicket.Event.subscribe('/ajax/call/init', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'Complete: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, attributes, jqXHR, data, textStatus) {
				assert.ok(false, 'Success handles should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
				done();
				assert.equal('Not Found', errorThrown, 'Failure: errorThrown');
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Failure: Assert that jqXHR is a XMLHttpRequest');
				assert.equal('error', textStatus, 'Failure: textStatus');
				assert.equal(attrs.u, attributes.u, 'Failure: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				assert.equal(attrs.u, attributes.u, 'Before: attrs');
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				assert.ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'After: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, textStatus) {
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Complete: Assert that jqXHR is a XMLHttpRequest');
				assert.equal('error', textStatus, 'Complete: textStatus');
				assert.equal(attrs.u, attributes.u, 'Complete: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/done', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'Complete: attrs');

				// unregister all subscribers
				Wicket.Event.unsubscribe();
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");

		});

		test('show/hide incrementally (WICKET-4364)', assert => {
			const done = assert.async();
			assert.expect(6);

			var $indicator = jQuery('<div id="indicator"></div>');
			var $el = jQuery('<div id="elementId"></div>');
			jQuery('#qunit-fixture')
				.append($indicator)
				.append($el);

			// returns the number of 'shows' of the indicator
			var getCurrentCount = function () {
				var count = $indicator.attr('showIncrementallyCount');
				return count ? parseInt(count, 10) : 0;
			};

			// called as 'success' for requestOne and as 'failure' for requestTwo
			var successFailureHandler = function () {
				var count = getCurrentCount();
				assert.ok(count === 1 || count === 2, "'showIncrementallyCount' must be 1 or 2. Value is: " + count);
				assert.equal('block', $indicator.css('display'), "Indicator's display must be 'block'");
			};

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				i: $indicator.attr('id'),
				c: $el.attr('id'),
				dt: 'json', // datatype
				sh: [ successFailureHandler ],
				fh: [ successFailureHandler ],
				wr: false // not Wicket's <ajax-response>
			};

			// binds requestOne (success)
			Wicket.Ajax.ajax(attrs);

			var attrsTwo = jQuery.extend({}, attrs, {
				u: 'data/ajax/nonExisting.json'
			});
			// binds requestTwo (failure - non-existing URL => error 404)
			Wicket.Ajax.ajax(attrsTwo);

			var attrsThree = jQuery.extend({}, attrs, {
				pre: [
					function () {
						done();
						assert.ok(true, 'Request 3: Precondition called.');

						var count = getCurrentCount();
						assert.equal(0, count, "'showIncrementallyCount' must be 0 after the executions but is: " + count);
						$indicator.remove();
						$el.off().remove();

						return false;
					}
				]
			});
			// binds requestThree - not executed due to precondition
			Wicket.Ajax.ajax(attrsThree);

			// fire all requests
			$el.triggerHandler("event1");
		});

		/**
		 * When using GET method the parameters should be added to 'settings.url'
		 * WICKET-4606
		 */
		test('verify dynamic parameters are appended to the Ajax GET params.', assert => {
			const done = assert.async();
			assert.expect(5);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				m: 'get',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				dep: [ function() {return { "one": 1, "two": 2 }; } ]
			};

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				assert.equal(attrs.u, attributes.u, 'Before: attrs');
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				assert.ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
				assert.ok(settings.url.indexOf('one=1') > 0, 'Parameter "one" with value "1" is found');
				assert.ok(settings.url.indexOf('two=2') > 0, 'Parameter "two" with value "2" is found');
				done();

				Wicket.Event.unsubscribe();
		});

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * When using POST method the parameters should be added to 'settings.data'
		 * WICKET-4606
		 */
		test('verify dynamic parameters are appended to the Ajax POST params.', assert => {
			const done = assert.async();
			assert.expect(7);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				m: 'post',
				ep: [ {name: 'one', value: 'static1'}, {name: 'one', value: 'static2'} ],
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				dep: [ function() {return [ {name: "one", value: 'dynamic1'}, {name: "one", value: 'dynamic2'} ]; } ]
			};

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				assert.equal(attrs.u, attributes.u, 'Before: attrs');
				assert.ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				assert.ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
				assert.ok(settings.data.indexOf('one=static1') > -1, 'Parameter "one" with value "static1" is found');
				assert.ok(settings.data.indexOf('one=static2') > -1, 'Parameter "one" with value "static2" is found');
				assert.ok(settings.data.indexOf('one=dynamic1') > -1, 'Parameter "one" with value "dynamic1" is found');
				assert.ok(settings.data.indexOf('one=dynamic2') > -1, 'Parameter "one" with value "dynamic2" is found');
				done();

				Wicket.Event.unsubscribe();
			});

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * 'before' handlers are called even before preconditions
		 * WICKET-4649
		 */
		test('before handler.', assert => {
			const done = assert.async();
			assert.expect(3);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				bh: [function(attributes) {
					assert.equal(attrs.u, attributes.u, 'Before: attrs');
				}],
				pre: [function() {
					assert.ok(true, "Precondition is called!");
					// do not allow calling of beforeSend handlers
					return false;
				}],
				bsh: [function() {
					assert.ok(false, 'beforeSend handles should not be called');
				}]
			};

			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attributes) {
				assert.equal(attrs.u, attributes.u, 'Global before: attrs');
				done();
			});

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * Verifies the order of execution of the callbacks.
		 * The order must be: before, precondition, beforeSend, after, success, complete, done.
		 * Three consecutive executions are made on the same Ajax channel validating
		 * that they do not overlap.
		 */
		test('callbacks order - success scenario.', assert => {
			const done = assert.async();
			assert.expect(42);

			var order = 0,

			// the number of assertions per iteration
			numberOfTests = 14,

			// calculates the offset for the order depending on the execution number
			offset = function(extraData) {
				return numberOfTests * extraData.round;
			};

			var attrs = {
				u: 'data/ajax/emptyAjaxResponse.xml',
				e: 'event1',
				bh: [
					function(attrs) {
						assert.equal((1 + offset(attrs.event.extraData)), ++order, "Before handler");
					}
				],
				pre: [
					function(attrs) {
						assert.equal((3 + offset(attrs.event.extraData)), ++order, "Precondition");
						return true;
					}
				],
				bsh: [
					function(attrs) {
						assert.equal((5 + offset(attrs.event.extraData)), ++order, "BeforeSend handler");
					}
				],
				ah: [
					function(attrs) {
						assert.equal((7 + offset(attrs.event.extraData)), ++order, "After handler");
					}
				],
				sh: [
					function(attrs) {
						assert.equal((9 + offset(attrs.event.extraData)), ++order, "Success handler");
					}
				],
				fh: [
					function() {
						assert.ok(false, 'Should not be called');
					}
				],
				coh: [
					function(attrs) {
						assert.equal((11 + offset(attrs.event.extraData)), ++order, "Complete handler");
					}
				],
				dh: [
					function(attrs) {
						assert.equal((13 + offset(attrs.event.extraData)), ++order, "Done handler");
					}
				]
			};


			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attrs) {
				assert.equal((2 + offset(attrs.event.extraData)), ++order, "Global before handler");
			});

			Wicket.Event.subscribe('/ajax/call/precondition', function(jqEvent, attrs) {
				assert.equal((4 + offset(attrs.event.extraData)), ++order, "Global precondition");
				return true;
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attrs) {
				assert.equal((6 + offset(attrs.event.extraData)), ++order, "Global beforeSend handler");
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attrs) {
				assert.equal((8 + offset(attrs.event.extraData)), ++order, "Global after handler");
			});

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, attrs) {
				assert.equal((10 + offset(attrs.event.extraData)), ++order, "Global success handler");
			});

			Wicket.Event.subscribe('/ajax/call/failure', function() {
				assert.ok(false, 'Global failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attrs) {
				assert.equal((12 + offset(attrs.event.extraData)), ++order, "Global complete handler");
			});

			Wicket.Event.subscribe('/ajax/call/done', function(jqEvent, attrs) {
				assert.equal((14 + offset(attrs.event.extraData)), ++order, "Global done handler");

				if (attrs.event.extraData.round === 2) {
					// unregister all global subscribers
					Wicket.Event.unsubscribe();
					jQuery(window).off("event1");

					done();
				}
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1", {"round": 0}); // execution No1
			target.triggerHandler("event1", {"round": 1}); // execution No2
			target.triggerHandler("event1", {"round": 2}); // execution No3
		});

		/**
		 * Verifies the order of execution of the callbacks.
		 * The order must be: before, beforeSend, after, failure, complete.
		 * Three consecutive executions are made on the same Ajax channel validating
		 * that they do not overlap.
		 */
		test('callbacks order - failure scenario.', assert => {
			const done = assert.async();
			assert.expect(42);

			var order = 0,

			// the number of assertions per iteration
			numberOfTests = 14,

			// calculates the offset for the order depending on the execution number
			offset = function(extraData) {
				return numberOfTests * extraData.round;
			};

			var attrs = {
				u: 'data/ajax/nonExistingResponse.xml',
				e: 'event1',
				bh: [
					function(attrs) {
						assert.equal((1 + offset(attrs.event.extraData)), ++order, "Before handler");
					}
				],
				pre: [
					function(attrs) {
						assert.equal((3 + offset(attrs.event.extraData)), ++order, "Precondition");
						return true;
					}
				],
				bsh: [
					function(attrs) {
						assert.equal((5 + offset(attrs.event.extraData)), ++order, "BeforeSend handler");
					}
				],
				ah: [
					function(attrs) {
						assert.equal((7 + offset(attrs.event.extraData)), ++order, "After handler");
					}
				],
				sh: [
					function() {
						assert.ok(false, 'Should not be called');
					}
				],
				fh: [
					function(attrs) {
						assert.equal((9 + offset(attrs.event.extraData)), ++order, "Failure handler");
					}
				],
				coh: [
					function(attrs) {
						assert.equal((11 + offset(attrs.event.extraData)), ++order, "Complete handler");
					}
				],
				dh: [
					function(attrs) {
						assert.equal((13 + offset(attrs.event.extraData)), ++order, "Done handler");
					}
				]
			};


			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attrs) {
				assert.equal((2 + offset(attrs.event.extraData)), ++order, "Global before handler");
			});

			Wicket.Event.subscribe('/ajax/call/precondition', function(jqEvent, attrs) {
				assert.equal((4 + offset(attrs.event.extraData)), ++order, "Global precondition");
				return true;
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attrs) {
				assert.equal((6 + offset(attrs.event.extraData)), ++order, "Global beforeSend handler");
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attrs) {
				assert.equal((8 + offset(attrs.event.extraData)), ++order, "Global after handler");
			});

			Wicket.Event.subscribe('/ajax/call/success', function() {
				assert.ok(false, 'Global failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attrs) {
				assert.equal((10 + offset(attrs.event.extraData)), ++order, "Global failure handler");
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attrs) {
				assert.equal((12 + offset(attrs.event.extraData)), ++order, "Global complete handler");
			});

			Wicket.Event.subscribe('/ajax/call/done', function(jqEvent, attrs) {
				assert.equal((14 + offset(attrs.event.extraData)), ++order, "Global done handler");

				if (attrs.event.extraData.round === 2) {
					// unregister all global subscribers
					Wicket.Event.unsubscribe();

					jQuery(window).off("event1");

					done();
				}
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1", {"round": 0}); // execution No1
			target.triggerHandler("event1", {"round": 1}); // execution No2
			target.triggerHandler("event1", {"round": 2}); // execution No3
		});

		/**
		 * Submits a nested multipart form (represented with <div>).
		 *
		 * https://issues.apache.org/jira/browse/WICKET-4673
		 */
		test('Submit nested form.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			Wicket.assert = assert;
			assert.expect(1);

			var attrs = {
				f:  "innerForm", // the id of the form to submit
				mp: true,  // multipart
				u:  "data/ajax/submitNestedForm.xml", // the mock response
				e:  "nestedFormSubmit", // the event
				c:  "innerSubmitButton", // the component that submits the form
				m:  "POST" // submit method
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#'+ attrs.c).triggerHandler("nestedFormSubmit");
		});


		/**
		 * Tests that Ajax call handlers are called when a (nested) multipart form
		 * is submitted with Ajax.
		 * The url points to Ajax response that contains an evaluation that starts the test.
		 */
		test('Submit nested form - success scenario.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			Wicket.assert = assert;
			assert.expect(9);

			var attrs = {
				f:  "innerForm", // the id of the form to submit
				mp: true,  // multipart
				u:  "data/ajax/submitNestedForm.xml", // the mock response
				e:  "nestedFormSubmit", // the event
				c:  "innerSubmitButton", // the component that submits the form
				m:  "POST", // submit method,
				ad: true, // do not allow default behavior
				bh: [ function(attrs) { assert.ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {assert.ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) {
					assert.ok(true, "BeforeSend handler executed");
				} ],
				ah: [ function(attrs) { assert.ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { assert.ok(true, "Success handler executed"); } ],
				fh: [ function(attrs) { assert.ok(false, "Failure handler should not be executed"); } ],
				coh: [
					function(attrs) {
						assert.ok(true, "Complete handler executed");
						assert.equal(attrs.event.isDefaultPrevented(), false, "default behavior is allowed");
					}
				],
				dep: [
					function(attrs) {
						assert.ok(true, "Dynamic parameters are collected in success scenario!");
						return { 'dynamicEPName': 'dynamicEPValue' };
					}
				],
				ep: {
					'extraParamName': 'extraParamValue'
				}
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#'+ attrs.c).triggerHandler("nestedFormSubmit");
		});

		/**
		 * Tests that Ajax call handlers are called when a (nested) multipart form
		 * is submitted with Ajax.
		 * Since the url points to not existing resource the final result is a failure.
		 */
		test('Submit nested form - failure scenario.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			assert.expect(8);

			var attrs = {
				f:  "innerForm", // the id of the form to submit
				mp: true,  // multipart
				u:  "data/ajax/submitNestedForm-NotExist.xml", // the mock response
				e:  "nestedFormSubmit", // the event
				c:  "innerSubmitButton", // the component that submits the form
				m:  "POST", // submit method,
				ad: false,
				bh: [ function(attrs) { assert.ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {assert.ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) {
					assert.ok(true, "BeforeSend handler executed");
				} ],
				ah: [ function(attrs) { assert.ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { assert.ok(false, "Success handler should not be executed"); } ],
				fh: [ function(attrs) { assert.ok(true, "Failure handler executed"); done(); } ],
				coh: [
					function(attrs) {
						assert.ok(true, "Complete handler executed");
						assert.equal(attrs.event.isDefaultPrevented(), false, "default behavior is not prevented");
					}
				],
				dep: [
					function(attrs) {
						assert.ok(true, "Dynamic parameters are collected in success scenario!");
						return { 'dynamicEPName': 'dynamicEPValue' };
					}
				],
				ep: {
					'extraParamName': 'extraParamValue'
				}
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#'+ attrs.c).triggerHandler("nestedFormSubmit");
		});


		/**
		 * Tests that submitting a of multipart form calls failure and complete handlers
		 * when the server is not reachable.
		 */
		test('Submit multipart form (server down).', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			assert.expect(6);

			var attrs = {
				f:  "multipartForm", // the id of the form to submit
				mp: true,  // multipart
				u:  "http://non-existing.tld/some.xml", // emulate server down by using non-existing address
				e:  "multipartFormSubmitEvent", // the event
				c:  "multipartFormSubmit", // the component that submits the form
				m:  "POST", // submit method,
				rt: 100, // 100ms request timeout
				bh: [ function(attrs) { assert.ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {assert.ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) { assert.ok(true, "BeforeSend handler executed"); } ],
				ah: [ function(attrs) { assert.ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { assert.ok(false, "Success handler should not be executed"); } ],
				fh: [ function(attrs) { done(); assert.ok(true, "Failure handler executed"); } ],
				coh: [ function(attrs) { assert.ok(true, "Complete handler executed"); } ]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#'+ attrs.c).triggerHandler("multipartFormSubmitEvent");
		});

		/**
		 * Tests that a huge response with more than 1000 evaluations is properly executed.
		 * FunctionsExecuter can execute at most 1000 functions in one go, the rest are executed
		 * in setTimeout() to prevent stack size exceeds.
		 * WICKET-4675
		 */
		test('Process response with 2k+ evaluations.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			Wicket.assert = assert;
			assert.expect(2133);

			var attrs = {
				u:  "data/ajax/manyEvaluationsResponse.xml", // the mock response
				e:  "manyEvaluations", // the event
				bh: [ function(attrs) { assert.ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {assert.ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) { assert.ok(true, "BeforeSend handler executed"); } ],
				ah: [ function(attrs) { assert.ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { assert.ok(true, "Success handler executed"); } ],
				fh: [ function(attrs) { assert.ok(false, "Failure handler should not be executed"); } ],
				coh: [ function(attrs) { assert.ok(true, "Complete handler executed"); } ]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery(window).triggerHandler("manyEvaluations");
		});

		/**
		 * The DOM elememt of the HTML element is used as a context (this)
		 * in the callbacks.
		 * https://issues.apache.org/jira/browse/WICKET-5025
		 */
		test('The HTML DOM element should be the context in the callbacks - success case.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			assert.expect(6);

			var attrs = {
				u: 'data/ajax/emptyAjaxResponse.xml',
				c: 'usedAsContextWicket5025',
				e: 'asContextSuccess',
				bh: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "Before handler executed"); } ],
				pre: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "Precondition executed"); return true; } ],
				bsh: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "BeforeSend handler executed"); } ],
				ah: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "After handler executed"); } ],
				sh: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "Success handler executed"); } ],
				fh: [ function() { assert.ok(false, "Failure handler should not be executed"); } ],
				coh: [
					function() {
						assert.equal(this.id, 'usedAsContextWicket5025', "Complete handler executed");
						jQuery('#usedAsContextWicket5025').off();
						done();
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#usedAsContextWicket5025').triggerHandler("asContextSuccess");
		});

		/**
		 * The DOM elememt of the HTML element is used as a context (this)
		 * in the callbacks.
		 * https://issues.apache.org/jira/browse/WICKET-5025
		 */
		test('The HTML DOM element should be the context in the callbacks - failure case.', assert => {
			const done = assert.async();
			assert.expect(6);

			var attrs = {
				u: 'data/ajax/nonExisting.xml',
				c: 'usedAsContextWicket5025',
				e: 'asContextFailure',
				bh: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "Before handler executed"); } ],
				pre: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "Precondition executed"); return true; } ],
				bsh: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "BeforeSend handler executed"); } ],
				ah: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "After handler executed"); } ],
				sh: [ function() { assert.ok(false, "Success handler should not be executed"); } ],
				fh: [ function() { assert.equal(this.id, 'usedAsContextWicket5025', "Failure handler should not be executed"); } ],
				coh: [
					function() {
						assert.equal(this.id, 'usedAsContextWicket5025', "Complete handler executed");
						jQuery('#usedAsContextWicket5025').off();
						done();
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#usedAsContextWicket5025').triggerHandler("asContextFailure");
		});

		/**
		 * 'null' values passed to _asParamArray() should be spliced
		 * See http://markmail.org/message/khuc2v37aakzyfth
		 * WICKET-5759
		 */
		test('_asParamArray() should drop nulls.', assert => {
			const done = assert.async();
			assert.expect(1);

			var attrs = {
				u: 'data/ajax/componentId.xml',
				e: 'event1',
				ep: [null, {name: "name", value: "value"}, null, null],
				bsh: [function(attributes) {
					var ep = attributes.ep;
					assert.equal(1, ep.length, 'The null values in the extra parameters should be dropped');
					done();
				}]
			};

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		test('Do not hide the indicator if redirecting.', assert => {
			const done = assert.async();
			assert.expect(2);

			var oldRedirect = Wicket.Ajax.redirect;
			Wicket.Ajax.redirect = function() {};

			var attrs = {
				u: 'data/ajax/redirectAjaxResponse.xml',
				e: 'event1',
				i: 'ajaxIndicator',
				sh: [function(attrs, jqXHR, data, textStatus) {
					var indicatorEl = Wicket.$(attrs.i);
					assert.equal("1", indicatorEl.getAttribute("showIncrementallyCount"));
				}],
				coh: [function(attrs, jqXHR, textStatus) {
					var indicatorEl = Wicket.$(attrs.i);
					assert.equal("1", indicatorEl.getAttribute("showIncrementallyCount"));
					Wicket.Ajax.redirect = oldRedirect;
					done();
				}]
			};

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		test('Do hide the indicator if not redirecting.', assert => {
			const done = assert.async();
			assert.expect(2);

			var attrs = {
				u: 'data/ajax/emptyAjaxResponse.xml',
				e: 'event1',
				i: 'ajaxIndicator',
				sh: [function(attrs, jqXHR, data, textStatus) {
					var indicatorEl = Wicket.$(attrs.i);
					assert.equal("1", indicatorEl.getAttribute("showIncrementallyCount"));
				}],
				coh: [function(attrs, jqXHR, textStatus) {
					var indicatorEl = Wicket.$(attrs.i);
					assert.equal("0", indicatorEl.getAttribute("showIncrementallyCount"));
					done();
				}]
			};

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		test('processAjaxResponse, normal HTTP case.', assert => {
			const done = assert.async();
			assert.expect(2);

			var originalProcessAjaxResponse = Wicket.Ajax.Call.prototype.processAjaxResponse,
				originalRedirect = Wicket.Ajax.redirect;

			Wicket.Ajax.Call.prototype.processAjaxResponse = function(data, textStatus, jqXHR, context) {
				var mockJqXHR = {
					"readyState": 4,
					getResponseHeader: function (headerName) {
						if ('Ajax-Location' === headerName) {
							return 'http://a.b.c';
						}
						return jqXHR.getResponseHeader(headerName);
					}
				};
				originalProcessAjaxResponse.call(Wicket.Ajax.Call.prototype, data, textStatus, mockJqXHR, context);
			};

			Wicket.Ajax.redirect = function(location) {
				Wicket.Ajax.Call.prototype.processAjaxResponse = originalProcessAjaxResponse;
				Wicket.Ajax.redirect = originalRedirect;
				done();
				assert.equal(location, 'http://a.b.c', 'Custom HTTP address is properly handled');
			};


			var attrs = {
				u: 'data/ajax/componentId.xml',
				c: 'componentId'
			};

			execute(attrs, assert, done);
		});

		test('Ajax 301 with Ajax-Location response header.', assert => {
			const done = assert.async();
			assert.expect(2);

			var redirectUrl = 'http://www.example.com/ajax/location';
			var componentUrl = 'data/ajax/componentId.xml';

			$.mockjax({
				url: componentUrl,
				status: 301,
				headers: {
					'Ajax-Location': redirectUrl
				}
			});

			var originalRedirect = Wicket.Ajax.redirect;

			Wicket.Ajax.redirect = function(location) {
				Wicket.Ajax.redirect = originalRedirect;
				done();
				assert.equal(location, redirectUrl, 'Ajax redirect in 301 response is properly handled');
			};

			var attrs = {
				u: componentUrl,
				c: 'componentId'
			};

			execute(attrs, assert, done);
		});

		test('processAjaxResponse, chrome-extensions case.', assert => {
			const done = assert.async();
			assert.expect(2);

			var originalProcessAjaxResponse = Wicket.Ajax.Call.prototype.processAjaxResponse,
				originalRedirect = Wicket.Ajax.redirect;

			Wicket.Ajax.Call.prototype.processAjaxResponse = function(data, textStatus, jqXHR, context) {
				var mockJqXHR = {
					"readyState": 4,
					getResponseHeader: function (headerName) {
						if ('Ajax-Location' === headerName) {
							return 'chrome-extensions://a.b.c';
						}
						return jqXHR.getResponseHeader(headerName);
					}
				};
				originalProcessAjaxResponse.call(Wicket.Ajax.Call.prototype, data, textStatus, mockJqXHR, context);
			};

			Wicket.Ajax.redirect = function(location) {
				Wicket.Ajax.Call.prototype.processAjaxResponse = originalProcessAjaxResponse;
				Wicket.Ajax.redirect = originalRedirect;
				done();
				assert.equal(location, 'chrome-extensions://a.b.c', 'Custom chrome-extensions address is properly handled');
			};

			var attrs = {
				u: 'data/ajax/componentId.xml',
				c: 'componentId'
			};

			execute(attrs, assert, done);
		});

		test('processAjaxResponse, no scheme case.', assert => {
			const done = assert.async();
			assert.expect(2);

			var originalProcessAjaxResponse = Wicket.Ajax.Call.prototype.processAjaxResponse,
				originalRedirect = Wicket.Ajax.redirect;

			Wicket.Ajax.Call.prototype.processAjaxResponse = function(data, textStatus, jqXHR, context) {
				var mockJqXHR = {
					"readyState": 4,
					getResponseHeader: function (headerName) {
						if ('Ajax-Location' === headerName) {
							return 'location-without-scheme';
						}
						return jqXHR.getResponseHeader(headerName);
					}
				};
				originalProcessAjaxResponse.call(Wicket.Ajax.Call.prototype, data, textStatus, mockJqXHR, context);
			};

			Wicket.Ajax.redirect = function(location) {
				Wicket.Ajax.Call.prototype.processAjaxResponse = originalProcessAjaxResponse;
				Wicket.Ajax.redirect = originalRedirect;
				done();
				assert.ok(location.indexOf('location-without-scheme') > 0, 'Custom address without scheme is properly handled');
			};

			var attrs = {
				u: 'data/ajax/componentId.xml',
				c: 'componentId'
			};

			execute(attrs, assert, done);
		});
		
		var metaByName = function(name) {
			return jQuery('head meta[name=' + name + ']');
		};

		test('processMeta() create meta tag', assert => {
			const done = assert.async();
			assert.expect(3);

			jQuery('meta').remove();
			assert.equal(metaByName("m1").length, 0, "There must be no meta tag before the contribution.");
			
			var attrs = {
				u: 'data/ajax/metaId.xml',
				sh: [
					function() {
						done();
						assert.equal(metaByName("m1").length, 1, "There must be one meta tag after the contribution.");
						assert.equal(metaByName("m1").attr("content"), "c1", "The meta tag must have the content as requested.");
					}
				]
			};
			execute(attrs, assert, done);
		});
		
		test('processMeta() change meta tag', assert => {
			const done = assert.async();
			assert.expect(3);

			jQuery('meta').remove();
			jQuery('head').append('<meta name="m1" content="c1_old" />');
			assert.equal(metaByName("m1").length, 1, "There must be one old meta tag before the contribution.");
			
			var attrs = {
				u: 'data/ajax/metaId.xml',
				sh: [
					function() {
						done();
						assert.equal(metaByName("m1").length, 1, "There must be one meta tag after the contribution.");
						assert.equal(metaByName("m1").attr("content"), "c1", "The meta tag must have the content as requested.");
					}
				]
			};
			execute(attrs, assert, done);
		});

		test('processMeta() add meta tag', assert => {
			const done = assert.async();
			assert.expect(5);

			jQuery('meta').remove();
			jQuery('head').append('<meta name="m2" content="c2" />');
			assert.equal(metaByName("m2").length, 1, "There must be one old meta tag before the contribution.");
			
			var attrs = {
				u: 'data/ajax/metaId.xml',
				sh: [
					function() {
						done();
						assert.equal(metaByName("m2").length, 1, "There must be one old meta tag after the contribution.");
						assert.equal(metaByName("m2").attr("content"), "c2", "The old meta tag must still have the old content.");
						assert.equal(metaByName("m1").length, 1, "There must be one new meta tag after the contribution.");
						assert.equal(metaByName("m1").attr("content"), "c1", "The meta tag must have the content as requested.");
					}
				]
			};
			execute(attrs, assert, done);
		});
		
		test('no ajax send on component placeholder', assert => {
			const done = assert.async();
			assert.expect(1);

			var attrs = {
				u: 'data/ajax/componentPlaceholderId.xml',
				c: 'componentPlaceholderId',
				bsh: [
					function() {
						assert.ok(false, 'should not be sent');
					}
				],
				dh: [
					function() {
						done();
						assert.ok('Done handler should be called');
					}
				]
			};
			execute(attrs, assert, done);
		});
	}
});
