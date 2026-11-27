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
 QUnit: true, expect: true, stop: true */

;(function (undefined) {
	'use strict';

	Wicket.Event.add(window, 'domready', function() {
		const { test } = QUnit;

		QUnit.module('Wicket.ChannelManager', function() {

		/**
		 * Tests queueing channel.
		 * For 0 to 9 adds callback functions that just appends the current value of
		 * the counter to the 'result'.
		 * Verifies that the final result contains all values of the counter.
		 */
		test('queue', assert => {
			const done = assert.async();
			assert.expect(1);
			stop();

			var cm		= new Wicket.ChannelManager(),
				ch		= 'name|s',
				i		= 0,
				result	= '',
				iterations = 10,
				toExecute = function (j) {
					return function() {
						window.setTimeout(function() {
							result += j;
							cm.done(ch);

							if (j === iterations - 1) {
								assert.equal(result, '0123456789');
								done();
							}
						}, 1);
					};
				};

			for (; i < iterations; i++) {
				cm.schedule(ch, toExecute(i));
			}
		});

		/**
		 * Tests 'drop' channel.
		 * For 0 to 9 adds callback functions to the queueing channel. Only for the
		 * value of 5 adds a callback function to the drop channel.
		 * The execution starts with 0 but the DROP callback (for 5) drops the callbacks for
		 * 1, 2, 3 and 4, so they are missed. '5' registers a '!drop!' and then all following
		 * queueing callbacks are executed.
		 * The final result must be "0!drop!6789"
		 * - 0 for the first queueing callback
		 * - !drop! for the 5th
		 * - 6, 7, 8 and 9
		 */
		test('drop', assert => {
			const done = assert.async();
			assert.expect(1);

			// stop();

			var cm		= new Wicket.ChannelManager(),	// the manager
				name	= 'name',						// the channel's name
				chq		= name + '|s',					// the channel(s) to queue
				chd		= name + '|d',					// the channel to drop
				number	= 10,							// the number of channels to schedule
				i		= 0,							// the current iteration
				result	= '',							// the container for the actual result
				queueCallback = function(k) {
					result += k;
					cm.done(chq);

					if (k === (number - 1)) {
						assert.equal(result, '0!drop!6789');
						done();
					}
				},
				toExecuteQueued = function (y) {
					return function() {
						window.setTimeout(function() {queueCallback(y);}, 1);
					};
				},
				toExecuteDropped = function() {
					result += '!drop!';
					cm.done(chd);
				};

			for (; i < number; i++) {

				cm.schedule(chq, toExecuteQueued(i));

				if (i === (number / 2)) {
					cm.schedule(chd, toExecuteDropped);
				}
			}
		});

		/**
		 * Tests 'active' channel type.
		 * Schedules one long running request and 10 normal ones after it.
		 * All 10 normal ones should be discarded.
		 */
		test('active', assert => {
			const done = assert.async();
			assert.expect(1);

			stop();

			var cm      = new Wicket.ChannelManager(),	// the manager
				name    = 'name',						// the channel's name
				cha     = name + '|a',					// the active channel
				number  = 10,							// the number of requests to schedule while the active request is still running
				i       = 0,							// the current iteration
				queueCallback = function() {

					// run in a timeout to simulate long running request
					setTimeout(function() {
						assert.ok(true, "The initial request is executed!");

						// mark the channel non-busy
						cm.done(cha);
						done();
					}, 100);
				},
				toExecute = function () {
					assert.ok(false, "Requests in the active channel should not be executed.");
				};

			// schedule the long running callback (the active one)
			cm.schedule(cha, queueCallback);

			// try to schedule more requests
			// they will be disacarded because the channel is busy
			for (; i < number; i++) {
				cm.schedule(cha, toExecute);
			}

		});

		/**
		 * Asserts that the ChannelManager removes entries for done()-ed channels
		 */
		test('clean up', assert => {
			const done = assert.async();
			assert.expect(11);

			stop();

			var cm      = Wicket.channelManager,		// the manager
				name    = 'name',						// the channel name
				cha     = name + '|s',					// the channel
				number  = 10,							// the number of requests to schedule while the active request is still running
				i       = 0,							// the current iteration
				callback = function() {
					window.setTimeout(function() {
						cm.done(cha);
					}, 0);
				};

			for (; i < number; i++) {
				cm.schedule(cha, callback);
				assert.ok(cm.channels[name], "A channel exists.");
			}

			window.setTimeout(function() {
				assert.equal(undefined, cm.channels[name], "The channel should not be in the manager anymore");
				done();
			}, 500);
		});

		// schedules a callback that records what happens to it
		const schedule = function (cm, ch, name, events, callback) {
			cm.schedule(ch, callback || function () {
				events.push('run ' + name);
			}, function (channel) {
				events.push('queued ' + name + ' ' + channel);
			}, function (reason) {
				events.push('dropped ' + name + ' ' + reason);
			});
		};

		test('queue: tells the callbacks that wait', assert => {
			const done = assert.async();
			assert.expect(1);

			const cm = new Wicket.ChannelManager();
			const ch = 'name|s';
			const events = [];

			schedule(cm, ch, 'first', events);
			schedule(cm, ch, 'second', events, function () {
				events.push('run second');
				assert.deepEqual(events, [ 'run first', 'queued second name', 'run second' ]);
				done();
			});
			cm.done(ch);
		});

		test('drop: tells the callbacks a later one replaces', assert => {
			const done = assert.async();
			assert.expect(1);

			const cm = new Wicket.ChannelManager();
			const ch = 'name|d';
			const events = [];

			schedule(cm, ch, 'first', events);
			schedule(cm, ch, 'second', events);
			schedule(cm, ch, 'third', events, function () {
				events.push('run third');
				assert.deepEqual(events, [ 'run first', 'queued second name', 'queued third name',
					'dropped second drop', 'run third' ]);
				done();
			});
			cm.done(ch);
		});

		test('active: tells the callbacks it ignores', assert => {
			assert.expect(1);

			const cm = new Wicket.ChannelManager();
			const ch = 'name|a';
			const events = [];

			schedule(cm, ch, 'first', events);
			schedule(cm, ch, 'second', events);
			cm.done(ch);
			schedule(cm, ch, 'third', events);

			assert.deepEqual(events, [ 'run first', 'dropped second active', 'run third' ]);
		});

		test('a listener that throws does not keep the others from being told', assert => {
			const done = assert.async();
			assert.expect(1);

			const cm = new Wicket.ChannelManager();
			const events = [];

			schedule(cm, 'name|s', 'first', events);
			cm.schedule('name|s', function () {
				events.push('run second');
			}, null, function () {
				throw new Error('a failing listener');
			});
			schedule(cm, 'name|s', 'third', events);
			schedule(cm, 'name|d', 'fourth', events, function () {
				events.push('run fourth');
				assert.deepEqual(events, [ 'run first', 'queued third name', 'queued fourth name',
					'dropped third drop', 'run fourth' ]);
				done();
			});
			cm.done('name|s');
		});

		});

		QUnit.module('Wicket.Throttler', function() {

		test('tells the function a later one replaces', assert => {
			const done = assert.async();
			assert.expect(1);

			const throttler = new Wicket.Throttler(false);
			const id = 'throttlerTest';
			const events = [];
			const throttle = function (name, func) {
				throttler.throttle(id, 10, func || function () {
					events.push('run ' + name);
				}, function () {
					events.push('dropped ' + name);
				});
			};

			throttler.throttle(id, 10, function () {
				events.push('run without listener');
			});
			throttle('first');
			throttle('second');
			throttle('third', function () {
				events.push('run third');
				assert.deepEqual(events, [ 'dropped first', 'dropped second', 'run third' ]);
				done();
			});
		});

		});

		// Ajax calls are made only when the tests are served by a web server
		if (QUnit.isLocal) {
			return;
		}

		QUnit.module('Wicket.Ajax channels and throttling', function() {

		const topics = Wicket.Event.Topic;

		// records the topics an Ajax call publishes and the handlers it executes; asserts once for
		// each done or dropped, and once in stop(), that every call ends with the attrs object it
		// published on init or queued
		const recorder = function (assert) {
			const events = [];
			const subscriptions = [];
			const pending = new Set();
			const nameOf = function (attrs) {
				return attrs.event ? attrs.event.extraData.round : attrs.call;
			};
			const start = function (label, attrs) {
				pending.add(attrs);
			};
			const end = function (label, attrs) {
				assert.ok(pending.delete(attrs),
					label + ' ' + nameOf(attrs) + ' receives the attrs of its init or queued');
			};
			const record = function (topic, label, withArgument, track) {
				const subscriber = function (jqEvent, attrs, argument) {
					events.push(label + ' ' + nameOf(attrs) + (withArgument ? ' ' + argument : ''));
					if (track) {
						track(label, attrs);
					}
				};
				Wicket.Event.subscribe(topic, subscriber);
				subscriptions.push({ topic: topic, subscriber: subscriber });
			};

			record(topics.AJAX_CALL_INIT, 'init', false, start);
			record(topics.AJAX_CALL_QUEUED, 'queued', true, start);
			record(topics.AJAX_CALL_DROPPED, 'dropped', true, end);
			record(topics.AJAX_CALL_BEFORE, 'before');
			record(topics.AJAX_CALL_DONE, 'done', false, end);

			return {
				events: events,

				attributes: function (attrs) {
					return Object.assign({
						u: 'data/ajax/nonWicketResponse.json',
						dt: 'json',
						wr: false,
						qh: [ function (attributes, channel) {
							events.push('qh ' + nameOf(attributes) + ' ' + channel);
						} ],
						drh: [ function (attributes, reason) {
							events.push('drh ' + nameOf(attributes) + ' ' + reason);
						} ],
						dh: [ function (attributes) {
							events.push('dh ' + nameOf(attributes));
						} ]
					}, attrs);
				},

				stop: function () {
					subscriptions.forEach(function (subscription) {
						Wicket.Event.unsubscribe(subscription.topic, subscription.subscriber);
					});
					assert.strictEqual(pending.size, 0,
						'every call that published init or queued published done or dropped');
				}
			};
		};

		// keeps the channel busy, as a running call does, until Wicket.channelManager.done(ch)
		const occupy = function (ch) {
			Wicket.channelManager.schedule(ch, function () {});
		};

		test('a dropping channel publishes queued for the calls that wait and dropped for those it replaces', assert => {
			const done = assert.async();
			assert.expect(5);

			const ch = 'dropChannel|d';
			const calls = recorder(assert);

			Wicket.Event.subscribe(topics.AJAX_CALL_DONE, function finish() {
				Wicket.Event.unsubscribe(topics.AJAX_CALL_DONE, finish);
				calls.stop();
				assert.deepEqual(calls.events, [
					'init 0', 'qh 0 dropChannel', 'queued 0 dropChannel',
					'init 1', 'qh 1 dropChannel', 'queued 1 dropChannel', 'drh 0 drop', 'dropped 0 drop',
					'init 2', 'qh 2 dropChannel', 'queued 2 dropChannel', 'drh 1 drop', 'dropped 1 drop',
					'before 2', 'dh 2', 'done 2'
				]);
				done();
			});

			occupy(ch);
			Wicket.Ajax.ajax(calls.attributes({ e: 'event1', ch: ch }));
			Wicket.Event.fire(window, 'event1', { round: 0 });
			Wicket.Event.fire(window, 'event1', { round: 1 });
			Wicket.Event.fire(window, 'event1', { round: 2 });
			Wicket.Event.remove(window, 'event1');
			Wicket.channelManager.done(ch);
		});

		test('an active channel publishes dropped for the calls it ignores', assert => {
			const done = assert.async();
			assert.expect(4);

			const ch = 'activeChannel|a';
			const calls = recorder(assert);

			Wicket.Event.subscribe(topics.AJAX_CALL_DONE, function finish() {
				Wicket.Event.unsubscribe(topics.AJAX_CALL_DONE, finish);
				calls.stop();
				assert.deepEqual(calls.events, [
					'init 0', 'drh 0 active', 'dropped 0 active',
					'init 1', 'before 1', 'dh 1', 'done 1'
				]);
				done();
			});

			occupy(ch);
			Wicket.Ajax.ajax(calls.attributes({ e: 'event1', ch: ch }));
			Wicket.Event.fire(window, 'event1', { round: 0 });
			Wicket.channelManager.done(ch);
			Wicket.Event.fire(window, 'event1', { round: 1 });
			Wicket.Event.remove(window, 'event1');
		});

		[ false, true ].forEach(function (postponeTimerOnUpdate) {
			test('throttling publishes dropped for the calls a later one replaces, postponeTimerOnUpdate: ' +
					postponeTimerOnUpdate, assert => {
				const done = assert.async();
				assert.expect(5);

				const calls = recorder(assert);

				Wicket.Event.subscribe(topics.AJAX_CALL_DONE, function finish() {
					Wicket.Event.unsubscribe(topics.AJAX_CALL_DONE, finish);
					calls.stop();
					assert.deepEqual(calls.events, [
						'init 0',
						'init 1', 'drh 0 throttle', 'dropped 0 throttle',
						'init 2', 'drh 1 throttle', 'dropped 1 throttle',
						'before 2', 'dh 2', 'done 2'
					]);
					done();
				});

				Wicket.Ajax.ajax(calls.attributes({ e: 'event1', ch: 'throttleChannel|s',
					tr: { id: 'throttledCall', d: 20, p: postponeTimerOnUpdate } }));
				Wicket.Event.fire(window, 'event1', { round: 0 });
				Wicket.Event.fire(window, 'event1', { round: 1 });
				Wicket.Event.fire(window, 'event1', { round: 2 });
				Wicket.Event.remove(window, 'event1');
			});
		});

		test('a throttled call on a busy dropping channel is dropped by the throttler, then by the channel', assert => {
			const done = assert.async();
			assert.expect(5);

			const ch = 'throttledDropChannel|d';
			const calls = recorder(assert);

			Wicket.Event.subscribe(topics.AJAX_CALL_QUEUED, function next() {
				Wicket.Event.unsubscribe(topics.AJAX_CALL_QUEUED, next);
				Wicket.Event.fire(window, 'event1', { round: 2 });
				Wicket.Event.remove(window, 'event1');
			});
			Wicket.Event.subscribe(topics.AJAX_CALL_DROPPED, function release(jqEvent, attrs, reason) {
				if (reason === 'drop') {
					Wicket.Event.unsubscribe(topics.AJAX_CALL_DROPPED, release);
					Wicket.channelManager.done(ch);
				}
			});
			Wicket.Event.subscribe(topics.AJAX_CALL_DONE, function finish() {
				Wicket.Event.unsubscribe(topics.AJAX_CALL_DONE, finish);
				calls.stop();
				assert.deepEqual(calls.events, [
					'init 0',
					'init 1', 'drh 0 throttle', 'dropped 0 throttle',
					'qh 1 throttledDropChannel', 'queued 1 throttledDropChannel',
					'init 2',
					'qh 2 throttledDropChannel', 'queued 2 throttledDropChannel',
					'drh 1 drop', 'dropped 1 drop',
					'before 2', 'dh 2', 'done 2'
				]);
				done();
			});

			occupy(ch);
			Wicket.Ajax.ajax(calls.attributes({ e: 'event1', ch: ch,
				tr: { id: 'throttledDropCall', d: 20 } }));
			Wicket.Event.fire(window, 'event1', { round: 0 });
			Wicket.Event.fire(window, 'event1', { round: 1 });
		});

		test('a channel whose type changes between calls treats each call as its own type says', assert => {
			const done = assert.async();
			assert.expect(5);

			const calls = recorder(assert);

			Wicket.Event.subscribe(topics.AJAX_CALL_DONE, function finish() {
				Wicket.Event.unsubscribe(topics.AJAX_CALL_DONE, finish);
				calls.stop();
				assert.deepEqual(calls.events, [
					'init queue', 'qh queue mixedChannel', 'queued queue mixedChannel',
					'init drop', 'qh drop mixedChannel', 'queued drop mixedChannel',
					'drh queue drop', 'dropped queue drop',
					'init active', 'drh active active', 'dropped active active',
					'before drop', 'dh drop', 'done drop'
				]);
				done();
			});

			const types = { queue: 's', drop: 'd', active: 'a' };
			occupy('mixedChannel|s');
			Object.keys(types).forEach(function (round) {
				const event = round + 'Event';
				Wicket.Ajax.ajax(calls.attributes({ e: event, ch: 'mixedChannel|' + types[round] }));
				Wicket.Event.fire(window, event, { round: round });
				Wicket.Event.remove(window, event);
			});
			Wicket.channelManager.done('mixedChannel|s');
		});

		test('a queueing channel publishes queued, for Wicket.Ajax.get() and Wicket.Ajax.Call#ajax() too', assert => {
			const done = assert.async();
			assert.expect(4);

			const ch = 'queueChannel|s';
			const calls = recorder(assert);

			Wicket.Event.subscribe(topics.AJAX_CALL_QUEUED, function next(jqEvent, attrs) {
				if (attrs.call === 'get') {
					new Wicket.Ajax.Call().ajax(calls.attributes({ ch: ch, call: 'call' }));
				} else {
					Wicket.Event.unsubscribe(topics.AJAX_CALL_QUEUED, next);
					Wicket.channelManager.done(ch);
				}
			});
			Wicket.Event.subscribe(topics.AJAX_CALL_DONE, function finish(jqEvent, attrs) {
				if (attrs.call === 'call') {
					Wicket.Event.unsubscribe(topics.AJAX_CALL_DONE, finish);
					calls.stop();
					assert.deepEqual(calls.events, [
						'init get', 'qh get queueChannel', 'queued get queueChannel',
						'qh call queueChannel', 'queued call queueChannel',
						'before get', 'dh get', 'done get',
						'before call', 'dh call', 'done call'
					]);
					done();
				}
			});

			occupy(ch);
			Wicket.Ajax.get(calls.attributes({ ch: ch, call: 'get' }));
		});

		});
	});
})();
