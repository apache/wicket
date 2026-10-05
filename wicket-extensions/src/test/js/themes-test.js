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

	const DARK_PRIMARY = 'rgb(52, 85, 122)';
	const DARK_SURFACE = 'rgb(30, 36, 44)';

	function style(selector, property) {
		return window.getComputedStyle(document.querySelector(selector)).getPropertyValue(property);
	}

	module("themes of the components");

	test('a modal dialog in a theme takes its colors', function(assert) {
		assert.equal(style('#themed .modal-dialog', 'background-color'), DARK_SURFACE);
		assert.equal(style('#themed .modal-dialog-header', 'background-color'), DARK_PRIMARY);
		assert.equal(style('#themed .modal-dialog-overlay', 'background-color'),
			'rgba(10, 14, 20, 0.6)', 'the veil of the theme');
		assert.equal(style('#themed .modal-dialog-footer', 'border-top-style'), 'solid');
	});

	test('a modal dialog in a theme of the application takes its colors', function(assert) {
		assert.equal(style('#custom .modal-dialog-header', 'background-color'), 'rgb(142, 36, 170)');
	});

	test('a modal dialog outside a theme keeps its look', function(assert) {
		assert.equal(style('#plain .modal-dialog', 'background-color'), 'rgb(255, 255, 255)');
		assert.equal(style('#plain .modal-dialog-overlay', 'background-color'),
			'rgba(0, 0, 0, 0.2)');
		assert.equal(style('#plain .modal-dialog-header', 'background-color'), 'rgba(0, 0, 0, 0)',
			'the header is left to the application');
	});

	test('the suggestions of a field in a theme take its colors', function(assert) {
		const container = document.createElement('div');
		container.className = 'wicket-aa-container';
		container.innerHTML = '<div class="wicket-aa"><ul><li class="selected">One</li></ul></div>';
		document.body.appendChild(container);
		try {
			Wicket.AutoComplete.copyTheme(document.getElementById('themedField'), container);

			assert.ok(container.classList.contains('wicket-aa-themed'));
			assert.equal(container.style.getPropertyValue('--wicket-theme-primary'), '#34557a');
			assert.equal(window.getComputedStyle(container.querySelector('li.selected'))
				.backgroundColor, DARK_PRIMARY);
			assert.equal(window.getComputedStyle(container.querySelector('.wicket-aa'))
				.backgroundColor, DARK_SURFACE);

			Wicket.AutoComplete.copyTheme(document.getElementById('plainField'), container);

			assert.notOk(container.classList.contains('wicket-aa-themed'),
				'a field outside a theme leaves the suggestions as they are');
			assert.equal(container.style.getPropertyValue('--wicket-theme-primary'), '');
		} finally {
			container.remove();
		}
	});

	test('tabs take the colors of their theme, and have their own outside one', function(assert) {
		assert.equal(style('#themed .wicket-tabs .selected a', 'background-color'), DARK_PRIMARY);
		assert.equal(style('#plain .wicket-tabs .selected a', 'background-color'),
			'rgb(31, 58, 104)');
		assert.equal(style('#themed .wicket-tabs ul', 'display'), 'flex',
			'also with the wicket:panel tag a page rendered in development mode keeps');
	});

	test('the upload progress bar shows the percentage on the bar and its label', function(assert) {
		const bar = new Wicket.WUPB(null, 'uploadStatus', 'uploadBar', 'about:blank', null,
			'Starting', function () { });

		bar.setPercent(40);

		assert.equal(document.querySelector('#uploadBar progress').value, 40);
		assert.equal(document.querySelector('#uploadBar .wicket-progress-bar-label').textContent,
			'40%');
	});

	test('the upload progress bar ignores a status without a percentage', function(assert) {
		const bar = new Wicket.WUPB(null, 'uploadStatus', 'uploadBar', 'about:blank', null,
			'Starting', function () { });
		bar.setPercent(40);

		bar.setPercent(undefined);
		bar.setPercent('<html>');
		assert.equal(document.querySelector('#uploadBar progress').value, 40);

		bar.setPercent('250');
		assert.equal(document.querySelector('#uploadBar .wicket-progress-bar-label').textContent,
			'100%', 'clamped');
	});
});
