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

/*global module: true, ok: true, asyncTest: true, equal: true, expect: true, $q: true,
 gym: true, start: true */

$q(document).ready(function() {
	"use strict";

	var countSelector = 'p > span';

	var increment = function($) {
		return gym.click($('a:contains("increment")'));
	};

	var nextPage = function($) {
		return gym.click($('a:contains("Continue")'));
	};

	module('CDI');

	asyncTest('conversation', function () {
		expect(4);
		var initialValue;

		gym.load('/cdi/conversation').then(function($) {

			initialValue = $(countSelector).text();
			initialValue = parseInt(initialValue, 10);

			return increment($);
		}).then(function($) {

			var counterLabelValue = $(countSelector).text();
			var expectedValue = initialValue + 1;
			equal(counterLabelValue, "" + expectedValue, 'The new value of the counter is +1');

			return nextPage($);
		}).then(function($) {

			var counterLabelValue = $(countSelector).text();
			var expectedValue = initialValue + 1;
			equal(counterLabelValue, "" + expectedValue, 'The value of the counter is the same as in the previous page');

			return increment($);
		}).then(function($) {

			var counterLabelValue = $(countSelector).text();
			var expectedValue = initialValue + 2;
			equal(counterLabelValue, "" + expectedValue, 'The new value of the counter is +2');

			return nextPage($);
		}).then(function($) {

			var counterLabelValue = $(countSelector).text();
			equal(counterLabelValue, "0", 'The value of the counter is 0 (no conversation)');

		}).always(start);
	});

});
