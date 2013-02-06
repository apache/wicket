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
 QUnit: true, module: true, expect: true, stop: true */

jQuery(document).ready(function() {
	"use strict";

	module('Wicket.ChannelManager');

	/**
	 * Tests queueing channel.
	 * For 0 to 9 adds callback functions that just appends the current value of
	 * the counter to the 'result'.
	 * Verifies that the final result contains all values of the counter.
	 */
	test('queue', function () {
	
		var cm		= new Wicket.ChannelManager(),
			ch		= 'name|s',
			i		= 0,
			result	= '',
			toExecute = function (j) {
				result += j;
				cm.done(ch);
			};

		for (; i < 10; i++) {
			cm.schedule(ch, toExecute(i));
		}
		
		equal(result, '0123456789');
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
	test('drop', function () {

		expect(1);

		stop();

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
					start();

					equal(result, '0!drop!6789');
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
	test('active', function () {

		expect(1);

		stop();

		var cm      = new Wicket.ChannelManager(),	// the manager
			name    = 'name',						// the channel's name
			cha     = name + '|a',					// the active channel
			number  = 10,							// the number of requests to schedule while the active request is still running
			i       = 0,							// the current iteration
			queueCallback = function() {

				// run in a timeout to simulate long running request
				setTimeout(function() {
					start();
					ok(true, "The initial request is executed!");

					// mark the channel non-busy
					cm.done(cha);
				}, 100);
			},
			toExecute = function () {
				ok(false, "Requests in the active channel should not be executed.");
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
	test('clean up', function () {

		expect(11);

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
			ok(cm.channels[name], "A channel exists.");
		}

		window.setTimeout(function() {
			start();
			equal(undefined, cm.channels[name], "The channel should not be in the manager anymore");
		}, 500);
	});
});
