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

	const choices =
		'<ul>' +
		'<li textvalue="Amsterdam" data-country="NL">Amsterdam</li>' +
		'<li textvalue="Antwerp" data-country="BE">Antwerp</li>' +
		'</ul>';

	let originalAjax;

	// Opens the popup with the given choices and calls back once its items can be clicked.
	function showChoices(callback, markup) {
		Wicket.Ajax.ajax = function(attrs) {
			attrs.sh.forEach(function(handler) {
				handler(attrs, null, markup || choices, 'success');
			});
		};
		new Wicket.AutoComplete({ c: 'acInput', u: 'autocomplete' }, { throttleDelay: 1 });

		const input = document.getElementById('acInput');
		input.focus();
		input.value = 'A';
		input.dispatchEvent(new Event('input', { bubbles: true }));

		const poll = function() {
			const menu = document.getElementById('acInput-autocomplete');
			if (menu && menu.getElementsByTagName('li').length === 2) {
				callback(input, menu.getElementsByTagName('li'));
			} else {
				window.setTimeout(poll, 10);
			}
		};
		poll();
	}

	module("Wicket.AutoComplete selection", {
		beforeEach: function() {
			originalAjax = Wicket.Ajax.ajax;
		},
		afterEach: function() {
			Wicket.Ajax.ajax = originalAjax;
			const container = document.getElementById('acInput-autocomplete-container');
			if (container) {
				container.parentNode.removeChild(container);
			}
		}
	});

	test("selecting a choice copies its text value into the input", assert => {
		const done = assert.async();
		showChoices(function(input, items) {
			items[0].click();

			assert.equal(input.value, 'Amsterdam');
			done();
		});
	});

	test("a select listener receives the chosen element and may replace the value", assert => {
		const done = assert.async();
		showChoices(function(input, items) {
			input.addEventListener('wicket-autocomplete-select', function(event) {
				assert.strictEqual(event.detail.item, items[0]);
				event.detail.value = event.detail.value + ' (' + event.detail.item.dataset.country + ')';
			});

			items[0].click();

			assert.equal(input.value, 'Amsterdam (NL)');
			done();
		});
	});

	test("a select listener that cancels the event leaves the input unchanged", assert => {
		const done = assert.async();
		showChoices(function(input, items) {
			input.addEventListener('wicket-autocomplete-select', function(event) {
				event.preventDefault();
			});

			items[0].click();

			assert.equal(input.value, 'A');
			done();
		});
	});

	test("a deprecated onselect expression still gets the text value as arguments[0]", assert => {
		const done = assert.async();
		showChoices(function(input, items) {
			let selected;
			input.addEventListener('wicket-autocomplete-select', function(event) {
				selected = event.detail.value;
			});

			items[0].click();

			assert.equal(selected, 'Amsterdam!');
			assert.equal(input.value, 'Amsterdam!');
			done();
		}, '<ul>' +
			'<li textvalue="Amsterdam" onselect="arguments[0] + &quot;!&quot;">Amsterdam</li>' +
			'<li textvalue="Antwerp">Antwerp</li>' +
			'</ul>');
	});
});
