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

jQuery(document).ready(function() {

	execute = function (attributes) {
		
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
	if ( !isLocal ) {

		module('Wicket.Ajax.stateChangeCallback');

		asyncTest('Wicket.Ajax - processEvaluation with mock data.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/evaluationId.xml',
				c: 'evaluationId'
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - processEvaluation with mock data (priority-evaluate).', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/priorityEvaluationId.xml',
				c: 'priorityEvaluationId'
			}
			execute(attrs);
		});

		/**
		 * Executes the second part of 'something|functionBody' by passing 'notify' function as parameter
		 */
		asyncTest('Wicket.Ajax - processEvaluation with identifier|code.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/evaluationIdentifierAndCodeId.xml',
				c: 'evaluationIdentifierAndCodeId'
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - processComponent, normal case.', function () {

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
			}
			execute(attrs);
		});


		asyncTest('Wicket.Ajax - processComponent() but the old component doesn\'t exist.', function () {

			expect(2);

			var oldWicketLogError = Wicket.Log.error;

			Wicket.Log.error = function(msg) {
				start();
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
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - processComponent() replace a component with a table with scripts inside.', function () {

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
			}
			execute(attrs);

		});


		asyncTest('Wicket.Ajax - processComponent() replace title\'s text.', function () {

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
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - non-wicket response.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(data, textStatus, jqXHR) {
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
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - listen on several events.', function () {

			expect(4);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: [ 'event1', 'event2' ],
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(data, textStatus, jqXHR) {
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
			}

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.triggerHandler("event2");
			target.off("event1 event2");
		});


		asyncTest('Wicket.Ajax - throttle execution.', function () {

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
					function(data, textStatus, jqXHR) {
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
			}

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);

			// this one will be throttled
			target.triggerHandler("event1");

			// this one will override the previous and will be throttled too
			target.triggerHandler("event1");

			target.off("event1");
		});

		asyncTest('Wicket.Ajax - verify arguments to IAjaxCallListener handlers. Success scenario.', function () {

			expect(11);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(data, textStatus, jqXHR, attributes) {
						start();
						var expected = {
							one: 1,
							two: '2',
							three: true
						};
						deepEqual(data, expected);
						equal('success', textStatus);
						deepEqual(attrs, attributes);
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
					}
				],
				fh: [
					function(attributes) {
						ok(false, 'Should not be called');
					}
				],
				bh: [
					function(attributes, jqXHR, settings) {
						deepEqual(attrs, attributes);
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						ok(jQuery.isFunction(settings.beforeSend), 'Assert that settings is the object passed to jQuery.ajax()');
					}
				],
				ah: [
					function(attributes) {
						deepEqual(attrs, attributes);
					}
				],
				coh: [
					function(jqXHR, textStatus, attributes) {
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						equal('success', textStatus);
						deepEqual(attrs, attributes);
					}
				]
			}

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		asyncTest('Wicket.Ajax - verify arguments to IAjaxCallListener handlers. Failure scenario.', function () {

			expect(8);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false, // not Wicket's <ajax-response>
				sh: [
					function(data, textStatus, jqXHR, attributes) {
						ok(false, 'Should not be called');
					}
				],
				fh: [
					function(attributes) {
						start();
						deepEqual(attrs, attributes);
					}
				],
				bh: [
					function(attributes, jqXHR, settings) {
						deepEqual(attrs, attributes);
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						ok(jQuery.isFunction(settings.beforeSend), 'Assert that settings is the object passed to jQuery.ajax()');
					}
				],
				ah: [
					function(attributes) {
						deepEqual(attrs, attributes);
					}
				],
				coh: [
					function(jqXHR, textStatus, attributes) {
						ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
						equal('error', textStatus);
						deepEqual(attrs, attributes);
					}
				]
			}

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");
		});

		/**
		 * Only attributes with non-default values are transfered to the client side.
		 * All defaults are initialized at the client side.
		 */
		asyncTest('Wicket.Ajax - verify default attributes.', function () {

			expect(23);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				coh: [
					function(jqXHR, textStatus, attributes) {
						start();
						equal(textStatus, "parsererror", "textStatus")
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
						equal(attributes.async, false, 'asynchronous');
						equal(attributes.rt, 0, 'request timeout');
						equal(attributes.ad, false, 'allow default');

					}
				]
			}

			Wicket.Ajax.ajax(attrs);
		});

		asyncTest('Wicket.Ajax - verify arguments to global listeners. Success scenario.', function () {

			expect(11);

			var attrs = {
				u: 'data/ajax/nonWicketResponse.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false // not Wicket's <ajax-response>
			};

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, data, textStatus, jqXHR, attributes) {
				start();
				var expected = {
					one: 1,
					two: '2',
					three: true
				};
				deepEqual(data, expected, 'data');
				equal('success', textStatus, 'textStatus');
				deepEqual(attrs, attributes, 'attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes) {
				ok(false, 'Failure handler should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attributes, jqXHR, settings) {
				deepEqual(attrs, attributes, 'attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
				ok(jQuery.isFunction(settings.beforeSend), 'Assert that settings is the object passed to jQuery.ajax()');
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes) {
				deepEqual(attrs, attributes, 'attrs');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, jqXHR, textStatus, attributes) {
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
				equal('success', textStatus, 'textStatus');
				deepEqual(attrs, attributes, 'attrs');
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");

			// unregister all subscribers
			jQuery(document).off();
		});

		asyncTest('Wicket.Ajax - verify arguments to global listeners. Failure scenario.', function () {

			expect(11);

			var attrs = {
				u: 'data/ajax/nonExisting.json',
				e: 'event1',
				dt: 'json', // datatype
				wr: false // not Wicket's <ajax-response>
			};

			Wicket.Event.subscribe('/ajax/call/success', function(jqEvent, data, textStatus, jqXHR, attributes) {
				ok(false, 'Success handles should not be called');
			});

			Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, errorThrown, attributes, jqXHR, textStatus) {
				start();
				ok('Not Found', errorThrown);
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
				equal('error', textStatus, 'textStatus');
				deepEqual(attrs, attributes, 'attrs');
			});

			Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attributes, jqXHR, settings) {
				deepEqual(attrs, attributes, 'attrs');
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
				ok(jQuery.isFunction(settings.beforeSend), 'Assert that settings is the object passed to jQuery.ajax()');
			});

			Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes) {
				deepEqual(attrs, attributes, 'attrs');
			});

			Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, jqXHR, textStatus, attributes) {
				ok(jQuery.isFunction(jqXHR.getResponseHeader), 'Assert that jqXHR is a XMLHttpRequest');
				equal('error', textStatus, 'textStatus');
				deepEqual(attrs, attributes, 'attrs');
			});

			Wicket.Ajax.ajax(attrs);

			var target = jQuery(window);
			target.triggerHandler("event1");
			target.off("event1");

			// unregister all subscribers
			jQuery(document).off();
		});
	}
});
