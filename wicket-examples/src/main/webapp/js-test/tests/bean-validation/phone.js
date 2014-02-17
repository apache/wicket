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

	var selectors = {

		phoneInput: "input[name=phone]",
		phoneInvalidErrorFeedback: "li.feedbackPanelERROR > span:contains(\"'Phone' must match \"[0-9]{3}-[0-9]{4}\"\")"
	};

	var submit = function($) {
		return gym.click($('input[value=Submit]'));
	};

	module('Bean validation');

	asyncTest('phone', function () {
		expect(3);

		gym.load('/bean-validation')
			.then(function($) {

				// enter phone with invalid pattern
				var $input = $(selectors.phoneInput);
				$input.val('abc');

				equal($(selectors.phoneInvalidErrorFeedback).length, 0, 'The feedback message for invalid phone is NOT there');

				return submit($);
			}).then(function($) {

				equal($(selectors.phoneInvalidErrorFeedback).length, 1, 'The feedback message for invalid phone is there');

				// enter valid phone
				var $input = $(selectors.phoneInput);
				$input.val('123-1234');

				return submit($);
			}).then(function($) {

				equal($(selectors.phoneInvalidErrorFeedback).length, 0, 'The feedback message for invalid phone is NOT there');

			}).always(start);
	});

});
