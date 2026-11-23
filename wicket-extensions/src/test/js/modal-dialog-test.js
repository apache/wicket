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

/*global QUnit: true, KeyboardEvent: true */

;(function (undefined) {
	'use strict';

	Wicket.Event.add(window, 'domready', function () {

		const { module, test } = QUnit;

		const PAGE = '<button id="trigger">open</button>' +
			'<div id="outerOverlay"><div id="outerDialog">' +
			'<a id="outerFirst" href="#">first</a>' +
			'<input id="outerText" type="text">' +
			'<div id="innerOverlay"><div id="innerDialog">' +
			'<a id="innerFirst" href="#">inner first</a>' +
			'<a id="innerLast" href="#">inner last</a>' +
			'</div></div>' +
			'<a id="outerLast" href="#">last</a>' +
			'</div></div>' +
			'<button id="popup">popup outside of the dialogs</button>';

		const OUTER = { overlay: 'outerOverlay', dialog: 'outerDialog', trapFocus: true,
			escape: { u: 'closeOuter' } };

		const INNER = { overlay: 'innerOverlay', dialog: 'innerDialog', trapFocus: true,
			escape: { u: 'closeInner' } };

		let requests;
		let realAjax;

		function $(id) {
			return document.getElementById(id);
		}

		function fixture() {
			return $('qunit-fixture');
		}

		function open(settings) {
			Wicket.ModalDialog.open(Object.assign({}, settings));
		}

		function key(target, name, keyCode, shiftKey) {
			const event = new KeyboardEvent('keydown', {
				key: name,
				keyCode: keyCode,
				which: keyCode,
				shiftKey: !!shiftKey,
				bubbles: true,
				cancelable: true
			});
			target.dispatchEvent(event);
			return event;
		}

		function tab(target, shiftKey) {
			return key(target, 'Tab', 9, shiftKey);
		}

		function escape(target) {
			return key(target, 'Escape', 27, false);
		}

		function remove(element) {
			Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING, element);
			element.parentNode.removeChild(element);
		}

		function settled() {
			return new Promise(function (resolve) {
				window.setTimeout(resolve, 0);
			});
		}

		function urls() {
			return requests.map(function (attrs) {
				return attrs.u;
			});
		}

		module('Wicket.ModalDialog', {
			beforeEach: function () {
				fixture().innerHTML = PAGE;
				requests = [];
				realAjax = Wicket.Ajax.ajax;
				Wicket.Ajax.ajax = function (attrs) {
					requests.push(attrs);
				};
				$('trigger').focus();
			},
			afterEach: function () {
				Wicket.Ajax.ajax = realAjax;
				Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING, fixture());
				fixture().innerHTML = '';
				return settled();
			}
		});

		test('focus moves to the first element of the dialog that can be focused', assert => {
			open(OUTER);

			assert.equal(document.activeElement, $('outerFirst'));
		});

		test('an element with autofocus takes the focus', assert => {
			$('outerText').setAttribute('autofocus', 'autofocus');

			open(OUTER);

			assert.equal(document.activeElement, $('outerText'));
		});

		test('a dialog without elements that can be focused takes the focus itself', assert => {
			$('innerDialog').innerHTML = 'Saved.';

			open(OUTER);
			open(INNER);

			assert.equal(document.activeElement, $('innerDialog'));
			assert.equal($('innerDialog').getAttribute('tabindex'), '-1');
		});

		test('focus that a script moved into the dialog stays there', assert => {
			$('outerLast').focus();

			open(OUTER);

			assert.equal(document.activeElement, $('outerLast'));
		});

		test('Tab and Shift+Tab cycle through the dialog', assert => {
			open(OUTER);
			open(INNER);

			let event = tab($('innerLast'), false);
			assert.ok(event.defaultPrevented, 'Tab on the last element is taken over');
			assert.equal(document.activeElement, $('innerFirst'));

			event = tab($('innerFirst'), true);
			assert.ok(event.defaultPrevented, 'Shift+Tab on the first element is taken over');
			assert.equal(document.activeElement, $('innerLast'));

			event = tab($('innerFirst'), false);
			assert.notOk(event.defaultPrevented, 'Tab between elements is left to the browser');
		});

		test('Tab from the dialog itself or from no element goes into the dialog', assert => {
			open(OUTER);

			$('outerDialog').setAttribute('tabindex', '-1');
			tab($('outerDialog'), false);
			assert.equal(document.activeElement, $('outerFirst'));

			document.activeElement.blur();
			tab(document.body, true);
			assert.equal(document.activeElement, $('outerLast'));
		});

		test('only the checked radio of a group is in the tab order', assert => {
			$('innerDialog').innerHTML = '<input id="radioA" type="radio" name="r" checked>' +
				'<input id="radioB" type="radio" name="r">';
			open(OUTER);
			open(INNER);

			const event = tab($('radioA'), false);

			assert.ok(event.defaultPrevented, 'the checked radio is the last element');
			assert.equal(document.activeElement, $('radioA'));
		});

		test('Tab is left to the browser without trapFocus', assert => {
			open(Object.assign({}, OUTER, { trapFocus: false }));

			const event = tab($('outerLast'), false);

			assert.notOk(event.defaultPrevented);
		});

		test('Escape closes the dialog with its Ajax request and the event', assert => {
			open(OUTER);

			const event = escape($('outerFirst'));

			assert.deepEqual(urls(), ['closeOuter']);
			assert.equal(requests[0].event, event);
			assert.ok(event.defaultPrevented);
		});

		test('Escape closes only the innermost dialog', assert => {
			open(OUTER);
			open(INNER);

			escape($('innerFirst'));
			assert.deepEqual(urls(), ['closeInner']);

			escape($('outerText'));
			assert.deepEqual(urls(), ['closeInner', 'closeOuter']);
		});

		test('Escape with no focused element closes the dialog opened last', assert => {
			open(OUTER);
			open(INNER);
			document.activeElement.blur();

			escape(document.body);

			assert.deepEqual(urls(), ['closeInner']);
		});

		test('Escape keeps a dialog open without an escape request', assert => {
			open(OUTER);
			open(Object.assign({}, INNER, { escape: null }));

			const event = escape($('innerFirst'));

			assert.deepEqual(urls(), [], 'the outer dialog is not closed either');
			assert.notOk(event.defaultPrevented);
		});

		test('Escape that a widget in the dialog handled keeps the dialog open', assert => {
			open(OUTER);
			$('outerText').addEventListener('keydown', function (event) {
				event.preventDefault();
			});

			escape($('outerText'));

			assert.deepEqual(urls(), []);
		});

		test('Escape outside of the dialogs is ignored', assert => {
			open(OUTER);

			escape($('popup'));

			assert.deepEqual(urls(), []);
		});

		test('focus goes back to the trigger when the dialog is closed', assert => {
			open(OUTER);
			remove($('outerOverlay'));

			return settled().then(function () {
				assert.equal(document.activeElement, $('trigger'));
			});
		});

		test('focus goes back to the trigger of a nested dialog', assert => {
			open(OUTER);
			$('outerText').focus();
			open(INNER);
			assert.equal(document.activeElement, $('innerFirst'));

			remove($('innerOverlay'));

			return settled().then(function () {
				assert.equal(document.activeElement, $('outerText'));

				escape($('outerText'));
				assert.deepEqual(urls(), ['closeOuter'], 'the closed dialog gets no more keys');
			});
		});

		test('focus goes to an element that replaced the trigger', assert => {
			open(OUTER);
			const trigger = $('trigger');
			const replacement = trigger.cloneNode(true);
			trigger.parentNode.replaceChild(replacement, trigger);

			remove($('outerOverlay'));

			return settled().then(function () {
				assert.equal(document.activeElement, replacement);
			});
		});

		test('focus goes back to the trigger that was focused before another script', assert => {
			$('outerFirst').focus();
			$('trigger').focus();
			$('outerLast').focus();
			open(OUTER);

			remove($('outerOverlay'));

			return settled().then(function () {
				assert.equal(document.activeElement, $('trigger'));
			});
		});

		test('a dialog rendered again keeps the focus and its trigger', assert => {
			open(OUTER);
			$('outerText').focus();

			const overlay = $('outerOverlay');
			const rendered = overlay.cloneNode(true);
			Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING, overlay);
			overlay.parentNode.replaceChild(rendered, overlay);
			$('outerText').focus();
			open(OUTER);

			return settled().then(function () {
				assert.equal(document.activeElement, $('outerText'),
					'the focus is not given back while the dialog is open');

				remove($('outerOverlay'));
				return settled();
			}).then(function () {
				assert.equal(document.activeElement, $('trigger'));
			});
		});

		test('a dialog rendered again keeps its trigger until its script runs again', assert => {
			open(OUTER);

			const overlay = $('outerOverlay');
			Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING, overlay);
			overlay.parentNode.replaceChild(overlay.cloneNode(true), overlay);
			$('outerText').focus();

			return settled().then(function () {
				assert.equal(document.activeElement, $('outerText'),
					'the focus is not given back while the dialog is shown');

				open(OUTER);
				remove($('outerOverlay'));
				return settled();
			}).then(function () {
				assert.equal(document.activeElement, $('trigger'));
			});
		});

		test('focus is not given back to an element behind a dialog that is still open', assert => {
			fixture().insertAdjacentHTML('beforeend', '<div id="stackedOverlay">' +
				'<div id="stackedDialog"><a id="stackedFirst" href="#">stacked</a></div></div>');
			open(OUTER);
			$('outerText').focus();
			open({ overlay: 'stackedOverlay', dialog: 'stackedDialog', trapFocus: true,
				escape: null });

			remove($('outerOverlay'));

			return settled().then(function () {
				assert.equal(document.activeElement, $('stackedFirst'));
			});
		});
	});
})();
