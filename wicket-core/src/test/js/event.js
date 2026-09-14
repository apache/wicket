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
 QUnit: true, module: true, expect: true, KeyboardEvent: true */

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	// creates a test element inside #qunit-fixture, without depending on jQuery
	var createTestElement = function (id) {
		var el = document.createElement('div');
		el.id = id || 'addTestId';
		el.textContent = 'element body';
		document.getElementById('qunit-fixture').appendChild(el);
		return el;
	};

	module('Wicket.Event.getId');

	test('getId - of an element with specified id', assert => {

		var element = document.querySelector('.getIdClass1');
		var id = Wicket.Event.getId(element);

		assert.equal(id, 'specifiedId', 'The specified element id is properly read');
	});

	test('getId - of an element without specified id', assert => {

		var element = document.querySelector('.getIdClass2');
		var id = Wicket.Event.getId(element);

		assert.equal(id, 'wicket-generated-id-0', 'The element without specified id will have an auto generated one');

		var element2 = document.querySelector('.getIdClass2');
		assert.equal(element2.id, 'wicket-generated-id-0', 'The generated id is assigned');
	});

	module('Wicket.Event.keyCode');

	test('keyCode', assert => {

		var evt = new KeyboardEvent("keydown", { keyCode: 123 });

		assert.equal(Wicket.Event.keyCode(evt), 123, 'event.keyCode should be used if available');
	});

	module('Wicket.Event.stop');

	test('stop', assert => {

		var fixture = document.getElementById('qunit-fixture');

		// stopPropagation(): must prevent the event from bubbling to the parent,
		// but must not stop other listeners registered on the same element
		var parent1 = document.createElement('div');
		var child1 = document.createElement('div');
		parent1.appendChild(child1);
		fixture.appendChild(parent1);

		var parent1Notified = false;
		parent1.addEventListener('click', function () { parent1Notified = true; });

		var secondListener1Notified = false;
		child1.addEventListener('click', function (evt) { Wicket.Event.stop(evt); });
		child1.addEventListener('click', function () { secondListener1Notified = true; });

		child1.dispatchEvent(new Event('click', { bubbles: true, cancelable: true }));

		assert.equal(parent1Notified, false, "stop() should prevent the event from bubbling to the parent");
		assert.equal(secondListener1Notified, true, "stop() without 'immediate' should not stop other listeners on the same element");

		// stopImmediatePropagation(): must also prevent other listeners on the same element
		var parent2 = document.createElement('div');
		var child2 = document.createElement('div');
		parent2.appendChild(child2);
		fixture.appendChild(parent2);

		var parent2Notified = false;
		parent2.addEventListener('click', function () { parent2Notified = true; });

		var secondListener2Notified = false;
		child2.addEventListener('click', function (evt) { Wicket.Event.stop(evt, true); });
		child2.addEventListener('click', function () { secondListener2Notified = true; });

		child2.dispatchEvent(new Event('click', { bubbles: true, cancelable: true }));

		assert.equal(parent2Notified, false, "stop(evt, true) should prevent the event from bubbling to the parent");
		assert.equal(secondListener2Notified, false, "stop(evt, true) should stop other listeners on the same element");

		parent1.remove();
		parent2.remove();
	});

	module('Wicket.Event.fix');

	test('fix', assert => {

		assert.expect(1);

		var handler = function (event) {
			Wicket.Event.remove(document, 'keydown', handler);
			var fixedEvt = Wicket.Event.fix(event);
			assert.strictEqual(fixedEvt, event, "Wicket.Event.fix() should return the same event it was given");
		};
		Wicket.Event.add(document, 'keydown', handler);

		document.dispatchEvent(new Event('keydown', { bubbles: true, cancelable: true }));
	});


	module('Wicket.Event.fire');

	test('fire', assert => {

		assert.expect(1);

		var el = createTestElement('fireTestId');
		el.addEventListener('click', function() {
			assert.ok(true, 'This event must be fired!');
		});

		Wicket.Event.fire(el, 'click');

		el.remove();
	});


	module('Wicket.Event.add');

	test('add - any event', assert => {

		assert.expect(1);

		var el = createTestElement();

		var handler = function() {
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add(el, 'click', handler);

		Wicket.Event.fire(el, 'click');

		el.remove();
	});

	test('remove - any event', assert => {

		assert.expect(1);

		var el = createTestElement();

		var handler = function() {
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add(el, 'click', handler);

		Wicket.Event.fire(el, 'click');

		Wicket.Event.remove(el, 'click', handler);

		Wicket.Event.fire(el, 'click');

		el.remove();
	});

	test('add - mousewheel', assert => {

		assert.expect(1);

		var el = createTestElement();

		var handler = function() {
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add(el, 'mousewheel', handler);

		Wicket.Event.fire(el, 'mousewheel');

		el.remove();
	});

	test('add - domready on non-window element', assert => {
		const done = assert.async();
		assert.expect(1);

		var el = createTestElement();

		var handler = function() {
			done();
			assert.ok(true, 'This event must be fired!');
		};

		Wicket.Event.add(el, 'domready', handler);

		Wicket.Event.fire(el, 'domready');

		el.remove();
	});

	test('add - with data', assert => {

		assert.expect(1);

		var el = createTestElement();

		var expectedData = {
			pass: true
		};

		var handler = function(jqEvent, data) {
			assert.deepEqual(data, expectedData, "Wicket.Event.add should be able to pass data to the event.");
		};

		Wicket.Event.add(el, 'dummy', handler, expectedData);

		Wicket.Event.fire(el, 'dummy');

		el.remove();
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
			assert.equal(arguments.length, 3, "1 event object + our two args");
			assert.equal(arguments[1], "arg1", "'arg1' must be at position 1");
			assert.equal(arguments[2], "arg2", "'arg2' must be at position 2");
		};

		Wicket.Event.subscribe('*', subscriber);

		Wicket.Event.publish('topicName1', "arg1", "arg2");
		Wicket.Event.publish('topicName2', "arg1", "arg2");

	});
});
