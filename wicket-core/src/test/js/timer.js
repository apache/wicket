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

jQuery(document).ready(function() {
	"use strict";

	const { module, test } = QUnit;

	module('Wicket.Timer');

	test('set', assert => {
		const done = assert.async();
		assert.expect(2);

		var timerId = 'timerId',
			run = function() {
				assert.ok(typeof(Wicket.TimerHandles[timerId]) === 'undefined', "There is no handle to the timeout!");
				assert.ok("The timer is ran!");
				done();
			};

		Wicket.Timer.set(timerId, run, 1);
	});

	test('clear', assert => {
		const done = assert.async();
		assert.expect(2);

		var timerId = 'timerId',
			run = function() {
				assert.ok(false, "timeout is not called");
			};

		Wicket.Timer.set('timerId', run, 1);
		assert.ok(Wicket.TimerHandles[timerId], "There is a handle to the timeout!");
		
		Wicket.Timer.clear(timerId);
		assert.ok(typeof(Wicket.TimerHandles[timerId]) === 'undefined', "There is no handle to the timeout!");

		setTimeout(function() {
			done();
		}, 2);
	});
});
