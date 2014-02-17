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

	module('Mail Template');

	var text = "Gym.js";

	var generate = function($, linkIndex) {

		var $nameInput = $('input[name=name]');

		$nameInput.val(text);

		return gym.ajaxClick($('a:contains("generate")').eq(linkIndex));
	};


	asyncTest('Page', function () {
		expect(3);

		gym.load('/mailtemplate').then(function($) {
			return generate($, 0);
		}).then(function($) {

			var $resultText = $('#result').text();
			ok($resultText, 'The page is rendered');
			ok($resultText.indexOf("<!DOCTYPE html>") > -1, 'The HTML5 doctype is here');
			ok($resultText.indexOf("Hello, <span wicket:id=\"name\">"+text+"</span>") > -1,
				'The entered text is here');

		}).always(start);
	});


	asyncTest('Panel', function () {
		expect(3);

		gym.load('/mailtemplate').then(function($) {
			return generate($, 1);
		}).then(function($) {

			var $resultText = $('#result').text();
			ok($resultText, 'The panel is rendered');
			ok($resultText.indexOf("<wicket:panel>") > -1, 'The panel markup is here');
			ok($resultText.indexOf("Hello, <span wicket:id=\"name\">"+text+"</span>") > -1,
				'The entered text is here');

		}).always(start);
	});

	asyncTest('Text Template', function () {
		expect(2);

		gym.load('/mailtemplate').then(function($) {
			return generate($, 2);
		}).then(function($) {

			var $resultText = $('#result').text();
			ok($resultText, 'The text template is rendered');
			ok($resultText.indexOf("Hello "+text) > -1, 'The entered text is here');

		}).always(start);
	});

});
