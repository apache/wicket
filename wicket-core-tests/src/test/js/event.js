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

/*global ok: true, start: true, stop: true, test: true, equal: true, deepEqual: true,
 QUnit: true, module: true, expect: true */

jQuery(document).ready(function() {
	"use strict";

	const { module, test } = QUnit;

	module('Wicket.Event.getId');

	test('getId - of an element with specified id', assert => {

		var element = jQuery('.getIdClass1')[0];
		var id = Wicket.Event.getId(element);

		assert.equal(id, 'specifiedId', 'The specified element id is properly read');
	});

	test('getId - of an element without specified id', assert => {

		var element = jQuery('.getIdClass2')[0];
		var id = Wicket.Event.getId(element);

		assert.equal(id, 'wicket-generated-id-0', 'The element without specified id will have an auto generated one');

		var element2 = jQuery('.getIdClass2');
		assert.equal(element2.prop('id'), 'wicket-generated-id-0', 'The generated id is assigned');
	});

	module('Wicket.Event.keyCode');

	test('keyCode', assert => {

		var evt = jQuery.Event("keydown", { keyCode: 123 });

		assert.equal(Wicket.Event.keyCode(evt), 123, 'event.keyCode should be used if available');
	});

	test('which', assert => {

		var evt = jQuery.Event("which", { keyCode: 123 });

		assert.equal(Wicket.Event.keyCode(evt), 123, 'event.which should be used if event.keyCode is not available');
	});

	module('Wicket.Event.stop');

	test('stop', assert => {

		var evt = jQuery.Event("keydown", { keyCode: 123 });

		assert.equal(evt.isPropagationStopped(), false);
		assert.equal(evt.isImmediatePropagationStopped(), false);

		Wicket.Event.stop(evt);

		assert.equal(evt.isPropagationStopped(), true);
		assert.equal(evt.isImmediatePropagationStopped(), false);

		Wicket.Event.stop(evt, true);

		assert.equal(evt.isPropagationStopped(), true);
		assert.equal(evt.isImmediatePropagationStopped(), true);
	});

	module('Wicket.Event.fix');

	test('fix', assert => {

		assert.expect(1);

		var evt = jQuery.Event("keydown", { keyCode: 123 });
		jQuery(document)
			.on('keydown', function(event) {
				var fixedEvt = Wicket.Event.fix(event);
				assert.deepEqual(fixedEvt, evt);
			})
			.trigger(evt);
	});
	
	
	module('Wicket.Event.fire');

	test('fire', assert => {

		assert.expect(1);

		var $el = jQuery('<div id="fireTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));
		$el.on('click', function() {
			assert.ok(true, 'This event must be fired!');
		});

		Wicket.Event.fire($el[0], 'click');

		$el.remove();
	});

	
	module('Wicket.Event.add');

	test('add - any event', assert => {

		assert.expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add($el[0], 'click', handler);

		Wicket.Event.fire($el[0], 'click');

		$el.remove();
	});

	test('remove - any event', assert => {

		assert.expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			assert.ok(true, 'This event must be fired!');
		};

		var el = $el[0];
		Wicket.Event.add(el, 'click', handler);

		Wicket.Event.fire(el, 'click');

		Wicket.Event.remove(el, 'click', handler);

		Wicket.Event.fire(el, 'click');
	});
	
	test('add - mousewheel', assert => {

		assert.expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add($el[0], 'mousewheel', handler);

		Wicket.Event.fire($el[0], 'mousewheel');

		$el.remove();
	});
	
	test('add - domready on non-window element', assert => {
		const done = assert.async();
		assert.expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var handler = function() {
			done();
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add($el[0], 'domready', handler);

		Wicket.Event.fire($el[0], 'domready');

		$el.remove();
	});

	test('add - with data', assert => {

		assert.expect(1);

		var $el = jQuery('<div id="addTestId">element body</div>');
		$el.appendTo(jQuery('#qunit-fixture'));

		var expectedData = {
			pass: true
		};

		var handler = function(jqEvent) {
			assert.deepEqual(jqEvent.data, expectedData, "Wicket.Event.add should be able to pass data to the event.");
		};

		Wicket.Event.add($el[0], 'dummy', handler, expectedData);

		Wicket.Event.fire($el[0], 'dummy');

		$el.remove();
	});

	test('add - domready on window', assert => {
		const done = assert.async();
		assert.expect(1);
		
		var handler = function() {
			done();
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add(window, 'domready', handler);
	});


	module('Wicket.Event.pubsub');

	test('specified topic', assert => {
		assert.expect(1);

		var subscriber = function() {
			assert.ok(true);
		};

		Wicket.Event.subscribe('topicName', subscriber);

		Wicket.Event.publish('topicName');
	});

	test('unsubscribe a signle subscriber', assert => {
		assert.expect(2);

		var topic = "someTopicName";

		var subscriber = function() {
			assert.ok(true, "The subscriber is notified");
		};

		Wicket.Event.subscribe(topic, subscriber);

		Wicket.Event.publish(topic);

		Wicket.Event.unsubscribe(topic, subscriber);
		assert.ok(true, "The subscriber is un-subscribed");

		Wicket.Event.publish(topic);
	});

	test('unsubscribe all subscribers per topic', assert => {
		assert.expect(3);

		var topic = "someTopicName";

		var subscriber1 = function() {
			assert.ok(true, "Subscriber 1 is notified");
		};

		var subscriber2 = function() {
			assert.ok(true, "Subscriber 2 is notified");
		};

		Wicket.Event.subscribe(topic, subscriber1);
		Wicket.Event.subscribe(topic, subscriber2);

		Wicket.Event.publish(topic);

		Wicket.Event.unsubscribe(topic);
		assert.ok(true, "The subscribers are un-subscribed");

		Wicket.Event.publish(topic);
	});

	test('unsubscribe all subscribers (for all topics)', assert => {
		assert.expect(3);

		var topic = "someTopicName";

		var subscriber1 = function() {
			assert.ok(true, "Subscriber 1 is notified");
		};

		var subscriber2 = function() {
			assert.ok(true, "Subscriber 2 is notified");
		};

		Wicket.Event.subscribe(topic, subscriber1);
		Wicket.Event.subscribe(topic, subscriber2);

		Wicket.Event.publish(topic);

		Wicket.Event.unsubscribe();
		assert.ok(true, "The subscribers are un-subscribed");

		Wicket.Event.publish(topic);
	});

	test('all topics', assert => {
		assert.expect(8);

		var subscriber = function () {
			assert.ok(true, 'Should be notified for any topic name');
			assert.equal(arguments.length, 3, "1 jQuery.Event + our two args");
			assert.equal(arguments[1], "arg1", "'arg1' must be at position 1");
			assert.equal(arguments[2], "arg2", "'arg2' must be at position 2");
		};

		Wicket.Event.subscribe('*', subscriber);

		Wicket.Event.publish('topicName1', "arg1", "arg2");
		Wicket.Event.publish('topicName2', "arg1", "arg2");

	});
});
