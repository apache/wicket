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

	module('Ajax');

	asyncTest('successful ajax form submit', function () {
		expect(3);

		gym.load('/ajax/form').then(function($) {

			// enter just the name field
			var $nameInput = $('input[name=name]');
			var name = 'Ajax form name';
			$nameInput.val(name);

			return gym.ajaxClick($('input[name=ajax-button]'));
		}).then(function($) {

			// an error feedback message that email is mandatory is expected
			var $feedback = $('li.feedbackPanelERROR > span');
			equal($feedback.length, 1, 'The error feedback message that email is missing is here');
			equal($feedback.text(), 'Email is required', 'The error feedback matches');

			// enter the email field too
			var $emailInput = $('input[name=email]');
			var email = 'contact@example.com';
			$emailInput.val(email);

			return gym.ajaxClick($('input[name=ajax-button]'));
		}).then(function($) {

			// the feedback panel must be empty now
			var $feedback = $('li.feedbackPanelERROR > span');
			equal($feedback.length, 0, 'The error feedback message should be gone');

		}).always(start);
	});

});
