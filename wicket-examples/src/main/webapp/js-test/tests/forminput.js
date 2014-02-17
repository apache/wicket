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

	module('Form Input');

	asyncTest('Change StringProperty', function () {
		expect(2);

		var text = 'qunit test value';

		gym.load('/forminput').then(function($) {

			var $stringPropertyInput = $('#stringProperty');

			$stringPropertyInput.val(text);

			return gym.click($('input[value=save]'));
		}).then(function($) {

			var $feedback = $('li.feedbackPanelINFO > span');
			equal($feedback.length, 1, 'The feedback is here');
			equal($feedback.text().indexOf("stringProperty = '"+text+"'"), 29, 'The entered text is here');

		}).always(start);
	});

	asyncTest('Change the locale', function () {
		expect(2);

		gym.load('/forminput').then(function($) {

			var $select = $('select[name=localeSelect]');
			var locale = '2'; // German

			$select.val(locale);

			return gym.click($('input[value=save]'));
		}).then(function($) {

			var $integerInRangeProperty = $('label[for=integerInRangeProperty]');
			equal($integerInRangeProperty.length, 1, 'The label for integerInRangeProperty is here');
			equal($integerInRangeProperty.text(), 'Nur Werte zwischen 0 und 100 sind erlaubt', 'The german version is correct');

		}).always(start);
	});

});
