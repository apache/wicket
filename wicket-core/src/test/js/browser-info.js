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

/*global QUnit: true */

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	module("Wicket.BrowserInfo");

	// Mock timezone offsets for January and July without relying on the test machine's TZ.
	function collectWithOffsets(januaryMinutes, julyMinutes) {
		var original = Date.prototype.getTimezoneOffset;
		Date.prototype.getTimezoneOffset = function() {
			if (this.getMonth() === 0) {
				return januaryMinutes;
			}
			if (this.getMonth() === 6) {
				return julyMinutes;
			}
			return original.call(this);
		};
		try {
			return Wicket.BrowserInfo.collect();
		} finally {
			Date.prototype.getTimezoneOffset = original;
		}
	}

	test("Northern hemisphere: standard and daylight offsets", assert => {
		// New York: January UTC-5, July UTC-4.
		var info = collectWithOffsets(300, 240);
		assert.strictEqual(info.utcOffset, -5);
		assert.strictEqual(info.utcDSTOffset, -4);
	});

	test("Southern hemisphere: January is daylight saving time", assert => {
		// Sydney: January UTC+11, July UTC+10.
		var info = collectWithOffsets(-660, -600);
		assert.strictEqual(info.utcOffset, 10);
		assert.strictEqual(info.utcDSTOffset, 11);
	});

	test("No daylight saving time", assert => {
		// Colombo: UTC+5:30 throughout the year.
		var info = collectWithOffsets(-330, -330);
		assert.strictEqual(info.utcOffset, 5.5);
		assert.strictEqual(info.utcDSTOffset, 5.5);
	});

	test("Southern hemisphere: half-hour daylight saving time", assert => {
		// Lord Howe Island: January UTC+11, July UTC+10:30.
		var info = collectWithOffsets(-660, -630);
		assert.strictEqual(info.utcOffset, 10.5);
		assert.strictEqual(info.utcDSTOffset, 11);
	});
});
