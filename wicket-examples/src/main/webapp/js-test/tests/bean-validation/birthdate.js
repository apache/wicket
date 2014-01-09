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

		birthdateInput: "input[name=birthdate]",
		birthdateInvalidDateErrorFeedback: "li.feedbackPanelERROR > span:contains(\"The value of 'Birthdate' is not a valid Date.\")",
		birthdateNotInPastErrorFeedback: "li.feedbackPanelERROR > span:contains(\"'Birthdate' must be in the past\")"

	};

	var submit = function($) {
		return gym.click($('input[value=Submit]'));
	};

	module('Bean validation');

	asyncTest('birthdate', function () {
		expect(6);

		gym.load('/bean-validation')
			.then(function($) {

				// enter non-date birthdate
				var $input = $(selectors.birthdateInput);
				$input.val('abc');

				equal($(selectors.birthdateInvalidDateErrorFeedback).length, 0, 'The feedback message for invalid birthdate is NOT there');

				return submit($);
			}).then(function($) {

				equal($(selectors.birthdateInvalidDateErrorFeedback).length, 1, 'The feedback message for invalid birthdate is there');

				// enter birthdate in the future
				var $input = $(selectors.birthdateInput);
				$input.val('3/22/2345');

				equal($(selectors.birthdateNotInPastErrorFeedback).length, 0, 'The feedback message for birthdate in the future is NOT there');

				return submit($);
			}).then(function($) {

				equal($(selectors.birthdateNotInPastErrorFeedback).length, 1, 'The feedback message for birthdate in the future is there');

				// enter birthdate in the past
				var $input = $(selectors.birthdateInput);
				$input.val('3/22/2012');

				return submit($);
			}).then(function($) {

				equal($(selectors.birthdateInvalidDateErrorFeedback).length, 0, 'The feedback message for invalid birthdate is NOT there');
				equal($(selectors.birthdateNotInPastErrorFeedback).length, 0, 'The feedback message for birthdate in the future is NOT there');

			}).always(start);
	});
});