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

		passwordInput: "input[name=password]",
		passwordMissingDigitsErrorFeedback: "li.feedbackPanelERROR > span:contains(\"You need to have at least 2 digits in your password.\")",
		passwordInvalidContentErrorFeedback: "li.feedbackPanelERROR > span:contains(\"Password value can contain only characters and digits.\")"

	};

	var submit = function($) {
		return gym.click($('input[value=Submit]'));
	};

	module('Bean validation');

	asyncTest('password', function () {
		expect(7);

		gym.load('/bean-validation')
			.then(function($) {

				// enter invalid chars for password
				var $input = $(selectors.passwordInput);
				$input.val('abc!?D');

				equal($(selectors.passwordInvalidContentErrorFeedback).length, 0, 'The feedback message for invalid chars is NOT there');

				return submit($);
			}).then(function($) {

				equal($(selectors.passwordInvalidContentErrorFeedback).length, 1, 'The feedback message for invalid chars is there');
				equal($(selectors.passwordMissingDigitsErrorFeedback).length, 0, 'The feedback message for missing digits is NOT there');

				// enter a value without 2 digits
				var $input = $(selectors.passwordInput);
				$input.val('abcdefAA');
				
				return submit($);
			}).then(function($) {

				equal($(selectors.passwordInvalidContentErrorFeedback).length, 0, 'The feedback message for invalid chars is NOT there');
				equal($(selectors.passwordMissingDigitsErrorFeedback).length, 1, 'The feedback message for missing digits is there');

				// enter a valid password
				var $input = $(selectors.passwordInput);
				$input.val('abc4def5AA');
				
				return submit($);
			}).then(function($) {
				equal($(selectors.passwordInvalidContentErrorFeedback).length, 0, 'The feedback message for invalid chars is NOT there');
				equal($(selectors.passwordMissingDigitsErrorFeedback).length, 0, 'The feedback message for missing digits is NOT there');
			}).always(start);
	});
});
