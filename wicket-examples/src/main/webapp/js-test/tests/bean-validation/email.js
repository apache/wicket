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

$q(document).ready(function() {
	"use strict";

	var selectors = {

		emailInput: "input[name=email]",
		emailNullErrorFeedback: "li.feedbackPanelERROR > span:contains(\"'Email' is required\")",
		emailInvalidErrorFeedback: "li.feedbackPanelERROR > span:contains(\"'Email' is not a well-formed email address\")"

	};

	var submit = function($) {
		return gym.click($('input[value=Submit]'));
	};

	module('Bean validation');

	asyncTest('email', function () {
		expect(5);

		gym.load('/bean-validation')
			.then(function($) {

				// enter email without characters (@NotNull)
				var $input = $(selectors.emailInput);
				$input.val('');

				equal($(selectors.emailNullErrorFeedback).length, 0, 'The feedback message for null email is NOT there');

				return submit($);
			}).then(function($) {

				equal($(selectors.emailNullErrorFeedback).length, 1, 'The feedback message for null name is there');

				// enter invalid email
				var $input = $(selectors.emailInput);
				$input.val('abc');

				return submit($);
			}).then(function($) {

				equal($(selectors.emailInvalidErrorFeedback).length, 1, 'The feedback message for invalid email is there');

				// enter valid email
				var $input = $(selectors.emailInput);
				$input.val('abc@example.com');

				return submit($);
			}).then(function($) {

				equal($(selectors.emailNullErrorFeedback).length, 0, 'The feedback message for null email is NOT there');
				equal($(selectors.emailInvalidErrorFeedback).length, 0, 'The feedback message for invalid email is NOT there');
			}).always(start);
	});

});
