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

/*global ok: true, start: true, test: true, equal: true, deepEqual: true,
 QUnit: true, module: true, expect: true */

jQuery(document).ready(function() {
	"use strict";

	module('Wicket.Event.getId');

	test('getId - of an element with specified id', function() {

		var element = jQuery('.getIdClass1')[0];
		var id = Wicket.Event.getId(element);

		equal(id, 'specifiedId', 'The specified element id is properly read');
	});

	test('getId - of an element without specified id', function() {

		var element = jQuery('.getIdClass2')[0];
		var id = Wicket.Event.getId(element);

		equal(id, 'wicket-generated-id-0', 'The element without specified id will have an auto generated one');

		var element2 = jQuery('.getIdClass2');
		equal(element2.prop('id'), 'wicket-generated-id-0', 'The generated id is assigned');
	});

	module('Wicket.Event.keyCode');

	test('keyCode', function () {

		var evt = jQuery.Event("keydown", { keyCode: 123 });

		equal(Wicket.Event.keyCode(evt), 123, 'event.keyCode should be used if available');
	});

	test('which', function () {

		var evt = jQuery.Event("which", { keyCode: 123 });

		equal(Wicket.Event.keyCode(evt), 123, 'event.which should be used if event.keyCode is not available');
	});

	module('Wicket.Event.stop');

	test('stop', function () {

		var evt = jQuery.Event("keydown", { keyCode: 123 });

		equal(evt.isPropagationStopped(), false);
		equal(evt.isImmediatePropagationStopped(), false);

		Wicket.Event.stop(evt);

		equal(evt.isPropagationStopped(), true);
		equal(evt.isImmediatePropagationStopped(), false);

		Wicket.Event.stop(evt, true);

		equal(evt.isPropagationStopped(), true);
		equal(evt.isImmediatePropagationStopped(), true);
	});


	module('Wicket.Event.fix');

	test('fix', function () {

		expect(1);

		var evt = jQuery.Event("keydown", { keyCode: 123 });
		jQuery(document)
			.bind('keydown', function(event) {
				var fixedEvt = Wicket.Event.fix(event);
				deepEqual(fixedEvt, evt);
			})
			.trigger(evt);
	});
	
	
	module('Wicket.Event.fire');

	test('fire', function () {

		expect(1);

		var $el = jQuery('<div id="fireTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));
		$el.on('click', function() {
			ok(true, 'This event must be fired!');
		});

		Wicket.Event.fire($el[0], 'click');

		$el.remove();
	});

	
	module('Wicket.Event.add');

	test('add - any event', function () {

		expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			ok(true, 'This event must be fired!');
		};

		Wicket.Event.add($el[0], 'click', handler);

		Wicket.Event.fire($el[0], 'click');

		$el.remove();
	});

	
	test('add - mousewheel', function () {

		expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			ok(true, 'This event must be fired!');
		};

		Wicket.Event.add($el[0], 'mousewheel', handler);

		Wicket.Event.fire($el[0], 'mousewheel');

		$el.remove();
	});
	
	test('add - domready on non-window element', function () {

		expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			ok(true, 'This event must be fired!');
		};

		Wicket.Event.add($el[0], 'domready', handler);

		Wicket.Event.fire($el[0], 'domready');

		$el.remove();
	});

	test('add - with data', function () {

		expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var expectedData = {
			pass: true
		};

		var handler = function(jqEvent) {
			deepEqual(jqEvent.data, expectedData, "Wicket.Event.add should be able to pass data to the event.");
		};

		Wicket.Event.add($el[0], 'dummy', handler, expectedData);

		Wicket.Event.fire($el[0], 'dummy');

		$el.remove();
	});

	test('add - domready on window', function () {

		expect(1);
		
		var handler = function() {
			ok(true, 'This event must be fired!');
		};

		Wicket.Event.add(window, 'domready', handler);
	});


	module('Wicket.Event.pubsub');

	test('specified topic', function() {
		expect(1);

		var subscriber = function() {
			ok(true);
		};

		Wicket.Event.subscribe('topicName', subscriber);

		Wicket.Event.publish('topicName');
	});

	test('all topics', function() {
		expect(8);

		var subscriber = function() {
			ok(true, 'Should be notified for any topic name');
			equal(arguments.length, 3, "1 jQuery.Event + our two args");
			equal(arguments[1], "arg1", "'arg1' must be at position 1");
			equal(arguments[2], "arg2", "'arg2' must be at position 2");
		};

		Wicket.Event.subscribe('*', subscriber);

		Wicket.Event.publish('topicName1', "arg1", "arg2");
		Wicket.Event.publish('topicName2', "arg1", "arg2");
	});

	module("Custom events");

	test('inputchange', function() {
		stop();
		if (Wicket.Browser.isIE()) {
			expect(3);
		} else {
			expect(1);
		}

		var $input = jQuery("#inputChangeInput");
		$input.on("inputchange", function() {
			ok(true, "inputchange event is triggered!");
		});

		if (Wicket.Browser.isIE()) {
			$input.trigger("paste");
			$input.trigger("keyup");
			$input.trigger("cut");
		} else {
			$input.trigger("input");
		}
		start();

	});
});
