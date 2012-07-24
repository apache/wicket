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

jQuery(document).ready(function() {

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
			result	= '';

		for (; i < 10; i++) {
			cm.schedule(ch, function () {
				result += i;
				cm.done(ch);
			});
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
	 * The final result is "0!drop!10101010"
	 * - 0 for the first queueing callback
	 * - !drop! for the 5th
	 * - a '10' for 6, 7, 8 and 9 (because I didn't find a way to pass the current value of 'i')
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
			j 		= 0,							// the counter that decides when to release the test
			result	= '',							// the container for the actual result
			queueCallback = function(k) {
				result += k;
				cm.done(chq);

				if (++j === (number / 2)) {
					start();

					//equal(result, '0!drop!6789'); // desired check, but cannot find how to pass 
													// the current value to the channel's callback

					equal(result, '0!drop!10101010'); // one '10' for 6,7,8,9
				}
			};

		for (; i < number; i++) {

			cm.schedule(chq, function () {

				// TODO: how to pass the current value of 'i' ?!
				setTimeout(queueCallback, 1, i);

			});

			if (i === number / 2) {
				cm.schedule(chd, function() {
					result += '!drop!';
					cm.done(chd);
				});
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
			j       = 0,							// the counter that decides when to release the test
			result  = '',							// the container for the actual result
			queueCallback = function() {

				// run in a timeout to simulate long running request
				setTimeout(function() {
					start();
					ok(true, "The initial request is executed!");

					// mark the channel non-busy
					cm.done(cha);
				}, 100);
			}

		// schedule the long running callback (the active one)
		cm.schedule(cha, queueCallback);

		// try to schedule more requests
		// they will be disacarded because the channel is busy
		for (; i < number; i++) {

			cm.schedule(cha, function () {
				ok(false, "Requests in the active channel should not be executed.")
			});
		}

	});
});
