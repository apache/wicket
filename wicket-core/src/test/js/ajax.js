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

	then run it by opening "http://localhost/ajax-tests/test/js/all.html" in the browser

 */

/*global ok: true, start: true, asyncTest: true, test: true, equal: true, deepEqual: true,
 QUnit: true, module: true, expect: true */

jQuery(document).ready(function() {
	"use strict";

	var execute = function (attributes) {

		var defaults = {
				fh: [
					function () {
						start();
						ok(false, 'Failure handler should not be executed!');
					}
				],
				ch: '0|s',
				sh: [
					function () {
						ok(true, 'Success handler is executed');
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
			setup: function() {
				// unsubscribe all global listeners
				jQuery(document).off();
			}
		});

		asyncTest('processEvaluation with mock data.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/evaluationId.xml',
				c: 'evaluationId'
			};
			execute(attrs);
		});

		asyncTest('processEvaluation with mock data (priority-evaluate).', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/priorityEvaluationId.xml',
				c: 'priorityEvaluationId'
			};
			execute(attrs);
		});

		/**
		 * Executes the second part of 'something|functionBody' by passing 'notify' function as parameter
		 */
		asyncTest('processEvaluation with identifier|code.', function () {

			expect(3);

			var attrs = {
				u: 'data/ajax/evaluationIdentifierAndCodeId.xml',
				c: 'evaluationIdentifierAndCodeId'
			};
			execute(attrs);
		});

		/**
		 * Executes the second part of 'something|functionBody' by passing 'notify' function as parameter.
		 * There are two functions with passed 'notify' function which leads to splitting the text in
		 * <(priority-)evaluate> elements to eval one function at a time to be able to call notify manually.
		 */
		asyncTest('processEvaluation*s* with identifier|code.', function () {

			expect(7);

			var attrs = {
				u: 'data/ajax/twoEvaluationsWithIdentifier.xml',
				c: 'twoEvaluationsWithIdentifier',
				counter: 0,
				coh: [
					function(attrs) {
						start();
						equal(attrs.counter, 3, "The counter is incremented in both evaluations with and without manual notifying");
					}
				]
			};
			execute(attrs);
		});

		asyncTest('processComponent, normal case.', function () {

			expect(2);

			equal(jQuery('#componentToReplace').text(), 'old body', 'The component is existing and has the old innerHTML');

			var attrs = {
				u: 'data/ajax/componentId.xml',
				c: 'componentId',
				sh: [
					function() {
						start();
						equal(jQuery('#componentToReplace').text(), 'new body', 'The component must be replaced');
					}
				]
			};
			execute(attrs);
		});


		asyncTest('processComponent() but the old component doesn\'t exist.', function () {

			expect(2);

			var oldWicketLogError = Wicket.Log.error;

			Wicket.Log.error = function(msg) {
				equal(msg, 'Wicket.Ajax.Call.processComponent: Component with id [[componentToReplaceDoesNotExist]] was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.');

				// restore the original method
				Wicket.Log.error = oldWicketLogError;
			};

			var attrs = {
				u: 'data/ajax/componentDoesNotExistsId.xml',
				c: 'componentDoesNotExistsId',
				sh: [
					function() {
						start();
						equal(jQuery('#componentToReplaceDoesNotExist').length, 0, 'A component with id \'componentToReplaceDoesNotExist\' must not exist!');
					}
				]
			};
			execute(attrs);
		});

		asyncTest('processComponent() replace a component with a table with scripts inside.', function () {

			expect(4);

			var attrs = {
				u: 'data/ajax/complexComponentId.xml',
				c: 'complexComponentId',
				sh: [
					function() {
						start();
						equal(jQuery('#componentToReplace')[0].tagName.toLowerCase(), 'table', 'A component with id \'componentToReplace\' must be a table now!');
					}
				]
			};
			execute(attrs);

		});


		asyncTest('processComponent() replace title\'s text.', function () {

			expect(1);

			var oldTitle = jQuery('title').text();

			var attrs = {
				u: 'data/ajax/componentToReplaceTitle.xml',
				c: 'componentToReplaceTitle',
				sh: [
					function() {
						start();
						var $title = jQuery('title');
						equal($title.text(), 'new title', 'The title text should be updated!');
						$title.text(oldTitle);
					}
				]
			};
			execute(attrs);
		});

		asyncTest('non-wicket response.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						start();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						deepEqual(data, expected);
						equal('success', textStatus);
					}
				]
			};
			execute(attrs);
		});

		asyncTest('listen on several events.', function () {

			expect(4);

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
						deepEqual(data, expected);
						equal('success', textStatus);

						if (++calls === 2) {
							start();
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


		asyncTest('throttle execution.', function () {

			expect(2);

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
						start();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						deepEqual(data, expected);
						equal('success', textStatus);
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

		asyncTest('verify arguments to IAjaxCallListener handlers. Success scenario.', function () {

			expect(11);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						start();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						deepEqual(data, expected, 'Success: data deep equal');
						equal('success', textStatus, 'Success: textStatus');
						equal(attrs.u, attributes.u, 'Success: attributes equal');
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Success: Assert that jqXHR is a XMLHttpRequest');
					}
				],
				fh: [
					function(attributes, errorMessage) {
						ok(false, 'Should not be called');
					}
				],
				bsh: [
					function(attributes, jqXHR, settings) {
						equal(attrs.u, attributes.u, 'Before: attributes equal');
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
						ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
					}
				],
				ah: [
					function(attributes) {
						equal(attrs.u, attributes.u, 'After: attributes equal');
					}
				],
				coh: [
					function(attributes, jqXHR, textStatus) {
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Complete: Assert that jqXHR is a XMLHttpRequest');
						equal('success', textStatus, 'Complete: textStatus');
						equal(attrs.u, attributes.u, 'Complete: attributes equal');
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		asyncTest('verify arguments to IAjaxCallListener handlers. Failure scenario.', function () {

			expect(8);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(attributes, jqXHR, data, textStatus) {
						ok(false, 'Should not be called');
					}
				],
				fh: [
					function(attributes) {
						start();
						equal(attrs.u, attributes.u);
					}
				],
				bsh: [
					function(attributes, jqXHR, settings) {
						equal(attrs.u, attributes.u);
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						ok(jQuery.isFunction(settings.beforeSend), 'Assert that settings is the object passed to jQuery.ajax()');
					}
				],
				ah: [
					function(attributes) {
						equal(attrs.u, attributes.u);
					}
				],
				coh: [
					function(attributes, jqXHR, textStatus) {
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						equal('error', textStatus);
						equal(attrs.u, attributes.u);
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * Only attributes with non-default values are transfered to the client side.
		 * All defaults are initialized at the client side.
		 */
		asyncTest('verify default attributes.', function () {

			expect(23);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				coh: [
					function(attributes, jqXHR, textStatus) {
						start();
						equal(textStatus, "parsererror", "textStatus");
						equal(attributes.u, attrs.u, "url");
						deepEqual(attributes.e, [ "domready" ], "events");
						equal(attributes.ch, '0|s', 'channel');
						equal(attributes.dt, 'xml', 'data type');
						equal(attributes.wr, true, 'wicket ajax response');
						equal(attributes.m, 'GET', 'method');
						ok(jQuery.isWindow(attributes.c), 'component');
						ok(attributes.f === undefined, 'form');
						ok(attributes.mp === undefined, 'multipart');
						ok(attributes.sc === undefined, 'submitting component');
						ok(attributes.i === undefined, 'indicator');
						ok(attributes.pre === undefined, 'preconditions');
						ok(attributes.bh === undefined, 'before handlers');
						ok(attributes.ah === undefined, 'after handler');
						ok(attributes.sh === undefined, 'success handlers');
						ok(attributes.fh === undefined, 'failure handlers');
						deepEqual(attrs.coh, attributes.coh, 'complete handlers');
						ok(attributes.ep === undefined, 'extra parameters');
						ok(attributes.dep === undefined, 'dynamic extra parameters');
						equal(attributes.async, true, 'asynchronous');
						equal(attributes.rt, 0, 'request timeout');
						equal(attributes.ad, false, 'allow default');

					}
				]
			};

			Wicket.Ajax.ajax(attrs);
		});

		asyncTest('verify arguments to global listeners. Success scenario.', function () {

			expect(11);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false // not Wicket's <ajax-response>
			};

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, attributes, jqXHR, data, textStatus) {
				start();
				var expected = {
					one: 1,
					two: '2',
					three: true
				};
				deepEqual(data, expected, 'Success: data');
				equal('success', textStatus, 'Success: textStatus');
				equal(attrs.u, attributes.u, 'Success: attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Success: Assert that jqXHR is a XMLHttpRequest');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes) {
				ok(false, 'Failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				equal(attrs.u, attributes.u, 'Before: attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes) {
				equal(attrs.u, attributes.u, 'After: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, textStatus) {
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Complete: Assert that jqXHR is a XMLHttpRequest');
				equal('success', textStatus, 'Complete: textStatus');
				equal(attrs.u, attributes.u, 'Complete: attrs');

				// unregister all subscribers
				jQuery(document).off();
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		asyncTest('verify arguments to global listeners. Failure scenario.', function () {

			expect(11);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false // not Wicket's <ajax-response>
			};

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, attributes, jqXHR, data, textStatus) {
				ok(false, 'Success handles should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
				start();
				equal('Not Found', errorThrown, 'Failure: errorThrown');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Failure: Assert that jqXHR is a XMLHttpRequest');
				equal('error', textStatus, 'Failure: textStatus');
				equal(attrs.u, attributes.u, 'Failure: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				equal(attrs.u, attributes.u, 'Before: attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes) {
				equal(attrs.u, attributes.u, 'After: attrs');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, textStatus) {
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Complete: Assert that jqXHR is a XMLHttpRequest');
				equal('error', textStatus, 'Complete: textStatus');
				equal(attrs.u, attributes.u, 'Complete: attrs');

				// unregister all subscribers
				jQuery(document).off();
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");

		});

		asyncTest('show/hide incrementally (WICKET-4364)', function() {

			expect(4);

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
				ok(count === 1 || count === 2, "'showIncrementallyCount' must be 1 or 2. Value is: " + count);
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
						start();
						ok(true, 'Request 3: Precondition called.');

						var count = getCurrentCount();
						equal(0, count, "'showIncrementallyCount' must be 0 after the executions but is: " + count);
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
		asyncTest('verify dynamic parameters are appended to the Ajax GET params.', function () {

			expect(5);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				m: 'get',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				dep: [ function() {return { "one": 1, "two": 2 }; } ]
			};

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attributes, jqXHR, settings) {
				equal(attrs.u, attributes.u, 'Before: attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
				ok(settings.url.indexOf('one=1') > 0, 'Parameter "one" with value "1" is found');
				ok(settings.url.indexOf('two=2') > 0, 'Parameter "two" with value "2" is found');
				start();

				jQuery(document).off();
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
		asyncTest('verify dynamic parameters are appended to the Ajax POST params.', function () {

			expect(7);

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
				equal(attrs.u, attributes.u, 'Before: attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Before: Assert that jqXHR is a XMLHttpRequest');
				ok(jQuery.isFunction(settings.beforeSend), 'Before: Assert that settings is the object passed to jQuery.ajax()');
				ok(settings.data.indexOf('one=static1') > -1, 'Parameter "one" with value "static1" is found');
				ok(settings.data.indexOf('one=static2') > -1, 'Parameter "one" with value "static2" is found');
				ok(settings.data.indexOf('one=dynamic1') > -1, 'Parameter "one" with value "dynamic1" is found');
				ok(settings.data.indexOf('one=dynamic2') > -1, 'Parameter "one" with value "dynamic2" is found');
				start();

				jQuery(document).off();
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
		asyncTest('before handler.', function () {

			expect(3);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				bh: [function(attributes) {
					equal(attrs.u, attributes.u, 'Before: attrs');
				}],
				pre: [function() {
					ok(true, "Precondition is called!");
					// do not allow calling of beforeSend handlers
					return false;
				}],
				bsh: [function() {
					ok(false, 'beforeSend handles should not be called');
				}]
			};

			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attributes) {
				equal(attrs.u, attributes.u, 'Global before: attrs');
				start();
			});

			Wicket.Ajax.ajax(attrs);
			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * Verifies the order of execution of the callbacks.
		 * The order must be: before, precondition, beforeSend, after, success, complete.
		 * Three consecutive executions are made on the same Ajax channel validating
		 * that they do not overlap.
		 */
		asyncTest('callbacks order - success scenario.', function () {

			expect(36);

			var order = 0,

			// the number of assertions per iteration
			numberOfTests = 12,

			// calculates the offset for the order depending on the execution number
			offset = function(extraData) {
				return numberOfTests * extraData.round;
			};

			var attrs = {
				u: 'data/ajax/emptyAjaxResponse.xml',
				e: 'event1',
				bh: [
					function(attrs) {
						equal((1 + offset(attrs.event.extraData)), ++order, "Before handler");
					}
				],
				pre: [
					function(attrs) {
						equal((3 + offset(attrs.event.extraData)), ++order, "Precondition");
						return true;
					}
				],
				bsh: [
					function(attrs) {
						equal((5 + offset(attrs.event.extraData)), ++order, "BeforeSend handler");
					}
				],
				ah: [
					function(attrs) {
						equal((7 + offset(attrs.event.extraData)), ++order, "After handler");
					}
				],
				sh: [
					function(attrs) {
						equal((9 + offset(attrs.event.extraData)), ++order, "Success handler");
					}
				],
				fh: [
					function() {
						ok(false, 'Should not be called');
					}
				],
				coh: [
					function(attrs) {
						equal((11 + offset(attrs.event.extraData)), ++order, "Complete handler");
					}
				]
			};


			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attrs) {
				equal((2 + offset(attrs.event.extraData)), ++order, "Global before handler");
			});

			Wicket.Event.subscribe('/ajax/call/precondition', function(jqEvent, attrs) {
				equal((4 + offset(attrs.event.extraData)), ++order, "Global precondition");
				return true;
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attrs) {
				equal((6 + offset(attrs.event.extraData)), ++order, "Global beforeSend handler");
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attrs) {
				equal((8 + offset(attrs.event.extraData)), ++order, "Global after handler");
			});

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, attrs) {
				equal((10 + offset(attrs.event.extraData)), ++order, "Global success handler");
			});

			Wicket.Event.subscribe('/ajax/call/failure', function() {
				ok(false, 'Global failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attrs) {
				equal((12 + offset(attrs.event.extraData)), ++order, "Global complete handler");

				if (attrs.event.extraData.round === 2) {
					// unregister all global subscribers
					jQuery(document).off();
					jQuery(window).off("event1");

					start();
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
		asyncTest('callbacks order - failure scenario.', function () {

			expect(36);

			var order = 0,

			// the number of assertions per iteration
			numberOfTests = 12,

			// calculates the offset for the order depending on the execution number
			offset = function(extraData) {
				return numberOfTests * extraData.round;
			};

			var attrs = {
				u: 'data/ajax/nonExistingResponse.xml',
				e: 'event1',
				bh: [
					function(attrs) {
						equal((1 + offset(attrs.event.extraData)), ++order, "Before handler");
					}
				],
				pre: [
					function(attrs) {
						equal((3 + offset(attrs.event.extraData)), ++order, "Precondition");
						return true;
					}
				],
				bsh: [
					function(attrs) {
						equal((5 + offset(attrs.event.extraData)), ++order, "BeforeSend handler");
					}
				],
				ah: [
					function(attrs) {
						equal((7 + offset(attrs.event.extraData)), ++order, "After handler");
					}
				],
				sh: [
					function() {
						ok(false, 'Should not be called');
					}
				],
				fh: [
					function(attrs) {
						equal((9 + offset(attrs.event.extraData)), ++order, "Failure handler");
					}
				],
				coh: [
					function(attrs) {
						equal((11 + offset(attrs.event.extraData)), ++order, "Complete handler");
					}
				]
			};


			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attrs) {
				equal((2 + offset(attrs.event.extraData)), ++order, "Global before handler");
			});

			Wicket.Event.subscribe('/ajax/call/precondition', function(jqEvent, attrs) {
				equal((4 + offset(attrs.event.extraData)), ++order, "Global precondition");
				return true;
			});

			Wicket.Event.subscribe('/ajax/call/beforeSend', function(jqEvent, attrs) {
				equal((6 + offset(attrs.event.extraData)), ++order, "Global beforeSend handler");
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attrs) {
				equal((8 + offset(attrs.event.extraData)), ++order, "Global after handler");
			});

			Wicket.Event.subscribe('/ajax/call/success', function() {
				ok(false, 'Global failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attrs) {
				equal((10 + offset(attrs.event.extraData)), ++order, "Global failure handler");
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attrs) {
				equal((12 + offset(attrs.event.extraData)), ++order, "Global complete handler");

				if (attrs.event.extraData.round === 2) {
					// unregister all global subscribers
					jQuery(document).off();

					jQuery(window).off("event1");

					start();
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
		 * The submit uses <iframe>.
		 *
		 * https://issues.apache.org/jira/browse/WICKET-4673
		 */
		asyncTest('Submit nested form.', function () {

			expect(1);

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
		asyncTest('Submit nested form - success scenario.', function () {

			expect(9);

			var attrs = {
				f:  "innerForm", // the id of the form to submit
				mp: true,  // multipart
				u:  "data/ajax/submitNestedForm.xml", // the mock response
				e:  "nestedFormSubmit", // the event
				c:  "innerSubmitButton", // the component that submits the form
				m:  "POST", // submit method,
				ad: true, // do not allow default behavior
				bh: [ function(attrs) { ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) { ok(true, "BeforeSend handler executed"); } ],
				ah: [ function(attrs) { ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { ok(true, "Success handler executed"); } ],
				fh: [ function(attrs) { ok(false, "Failure handler should not be executed"); } ],
				coh: [
					function(attrs) {
						ok(true, "Complete handler executed");
						equal(attrs.event.isDefaultPrevented(), false, "default behavior is allowed");
					}
				]
				,
				dep: [
					function(attrs) {
						ok(true, "Dynamic parameters are collected in success scenario!");
						return { 'one': 1 };
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#'+ attrs.c).triggerHandler("nestedFormSubmit");
		});

		/**
		 * Tests that Ajax call handlers are called when a (nested) multipart form
		 * is submitted with Ajax.
		 * Since the url points to not existing resource the final result is a failure.
		 */
		asyncTest('Submit nested form - failure scenario.', function () {

			expect(8);

			var attrs = {
				f:  "innerForm", // the id of the form to submit
				mp: true,  // multipart
				u:  "data/ajax/submitNestedForm-NotExist.xml", // the mock response
				e:  "nestedFormSubmit", // the event
				c:  "innerSubmitButton", // the component that submits the form
				m:  "POST", // submit method,
				ad: false,
				bh: [ function(attrs) { ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) { ok(true, "BeforeSend handler executed"); } ],
				ah: [ function(attrs) { ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { ok(false, "Success handler should not be executed"); } ],
				fh: [ function(attrs) { ok(true, "Failure handler executed"); start(); } ],
				coh: [
					function(attrs) {
						ok(true, "Complete handler executed");
						equal(attrs.event.isDefaultPrevented(), true, "default behavior is prevented");
					}
				],
				dep: [
					function(attrs) {
						ok(true, "Dynamic parameters are collected in failure scenario!");
						return { 'one': 1 };
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#'+ attrs.c).triggerHandler("nestedFormSubmit");
		});

		/**
		 * Tests that a huge response with more than 1000 evaluations is properly executed.
		 * FunctionsExecuter can execute at most 1000 functions in one go, the rest are executed
		 * in setTimeout() to prevent stack size exceeds.
		 * WICKET-4675
		 */
		asyncTest('Process response with 2k+ evaluations.', function () {

			expect(2133);

			var attrs = {
				u:  "data/ajax/manyEvaluationsResponse.xml", // the mock response
				e:  "manyEvaluations", // the event
				bh: [ function(attrs) { ok(true, "Before handler executed"); } ],
				pre: [ function(attrs) {ok(true, "Precondition executed"); return true; } ],
				bsh: [ function(attrs) { ok(true, "BeforeSend handler executed"); } ],
				ah: [ function(attrs) { ok(true, "After handler executed"); } ],
				sh: [ function(attrs) { ok(true, "Success handler executed"); } ],
				fh: [ function(attrs) { ok(false, "Failure handler should not be executed"); } ],
				coh: [ function(attrs) { ok(true, "Complete handler executed"); } ]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery(window).triggerHandler("manyEvaluations");
		});

		/**
		 * The DOM elememt of the HTML element is used as a context (this)
		 * in the callbacks.
		 * https://issues.apache.org/jira/browse/WICKET-5025
		 */
		asyncTest('The HTML DOM element should be the context in the callbacks - success case.', function () {

			expect(6);

			var attrs = {
				u: 'data/ajax/emptyAjaxResponse.xml',
				c: 'usedAsContextWicket5025',
				e: 'asContextSuccess',
				bh: [ function() { equal(this.id, 'usedAsContextWicket5025', "Before handler executed"); } ],
				pre: [ function() { equal(this.id, 'usedAsContextWicket5025', "Precondition executed"); return true; } ],
				bsh: [ function() { equal(this.id, 'usedAsContextWicket5025', "BeforeSend handler executed"); } ],
				ah: [ function() { equal(this.id, 'usedAsContextWicket5025', "After handler executed"); } ],
				sh: [ function() { equal(this.id, 'usedAsContextWicket5025', "Success handler executed"); } ],
				fh: [ function() { ok(false, "Failure handler should not be executed"); } ],
				coh: [
					function() {
						equal(this.id, 'usedAsContextWicket5025', "Complete handler executed");
						jQuery('#usedAsContextWicket5025').off();
						start();
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
		asyncTest('The HTML DOM element should be the context in the callbacks - failure case.', function () {

			expect(6);

			var attrs = {
				u: 'data/ajax/nonExisting.xml',
				c: 'usedAsContextWicket5025',
				e: 'asContextFailure',
				bh: [ function() { equal(this.id, 'usedAsContextWicket5025', "Before handler executed"); } ],
				pre: [ function() { equal(this.id, 'usedAsContextWicket5025', "Precondition executed"); return true; } ],
				bsh: [ function() { equal(this.id, 'usedAsContextWicket5025', "BeforeSend handler executed"); } ],
				ah: [ function() { equal(this.id, 'usedAsContextWicket5025', "After handler executed"); } ],
				sh: [ function() { ok(false, "Success handler should not be executed"); } ],
				fh: [ function() { equal(this.id, 'usedAsContextWicket5025', "Failure handler should not be executed"); } ],
				coh: [
					function() {
						equal(this.id, 'usedAsContextWicket5025', "Complete handler executed");
						jQuery('#usedAsContextWicket5025').off();
						start();
					}
				]
			};

			Wicket.Ajax.ajax(attrs);

			jQuery('#usedAsContextWicket5025').triggerHandler("asContextFailure");
		});

		/**
		 * https://issues.apache.org/jira/browse/WICKET-5047
		 */
		asyncTest('try/catch only the content of \'script type="text/javascript"\'.', function () {

			// manually call HTMLScriptElement.onload() to let
			// FunctionsExecutor finish its work
			var oldAddElement = Wicket.Head.addElement;
			Wicket.Head.addElement = function(element) {
				oldAddElement(element);
				if (element.onload) {
					element.onload();
				}
			};

			expect(2);

			var attrs = {
				u: 'data/ajax/javaScriptTemplate.xml',
				coh: [
					function() {
						start();

						var jsTemplateText = jQuery('#jsTemplate').text();
						equal(jsTemplateText, 'var data = 123;', 'JavaScript template is *not* try/catched');

						var jsNonTemplateText = jQuery('#jsNonTemplate').text();
						equal(jsNonTemplateText, 'try{var data = 456;}catch(e){Wicket.Log.error(e);}', 'JavaScript non template *is* try/catched');

					}
				]
			};

			Wicket.Ajax.ajax(attrs);
		});
	}
});
