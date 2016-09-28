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
 gym: true, start: true, notOk: true */

$q(document).ready(function() {
	"use strict";

	module('Ajax');

	asyncTest('ajax form validation', function () {
		expect(6);

		var $nameInput, $emailInput;

		gym.load('/ajax/form').then(function($) {
			var $form = $('form.validation');
			$nameInput = $('input[name="p::name"]', $form);
			$emailInput = $('input[name=email]', $form);

			// enter just the name field
			$nameInput.focus();
			var name = 'Aj';
			$nameInput.val(name);

			return gym.ajaxEvent('keydown', $emailInput);
		}).then(function($) {

			// an error feedback message that email is mandatory is expected
			var $feedback = $('li.feedbackPanelERROR > span');
			equal($feedback.length, 2, 'The error feedback message that name is too short and the email is missing');
			equal($feedback.eq(0).text(), 'Name must be at least 4 characters', 'The error feedback matches');
			equal($feedback.eq(1).text(), 'Email is required', 'The error feedback matches');

			var name = 'Ajax form name';
			$nameInput.val(name);

			return gym.ajaxEvent('keydown', $emailInput);
		}).then(function($) {

			// an error feedback message that email is mandatory is expected
			var $feedback = $('li.feedbackPanelERROR > span');
			equal($feedback.length, 1, 'The error feedback message that email is missing is here');
			equal($feedback.text(), 'Email is required', 'The error feedback matches');

			// enter the email field too
			var email = 'contact@example.com';
			$emailInput.val(email);

			return gym.ajaxEvent('keydown', $nameInput);
		}).then(function($) {

			// the feedback panel must be empty now
			var $feedback = $('li.feedbackPanelERROR > span');
			equal($feedback.length, 0, 'The error feedback message should be gone');

		}).always(start);
	});

	asyncTest('Prevent ajax form submit on ENTER key', function () {
		expect(2);

		var $nameInput, $emailInput;

		gym.load('/ajax/form').then(function($) {

			var $form = $('form.preventEnter');
			$nameInput = $('input[name="p::name"]', $form);
			$emailInput = $('input[name=email]', $form);

			// enter just the name field
			$nameInput.focus();
			var name = 'abcdef';
			$nameInput.val(name);

			var evt = $q.Event("keydown");
			evt.keyCode = evt.which = 13; // ENTER key
			var prevented = false;
			evt.preventDefault = function() {prevented = true;};
			equal(prevented, false, "The JS event default behavior is not yet prevented!");

			setTimeout(function() {
				equal(prevented, true, "The JS event default behavior must be prevented!");
				start();
			}, 10);

			return gym.ajaxEvent(evt, $emailInput);
		}).always(function() {
			notOk(true, "Always must not be called!");
		});
	});

});
