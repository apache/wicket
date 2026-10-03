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

	let written;
	let failing;
	let original;

	module("Wicket.Clipboard", {
		beforeEach: function() {
			written = [];
			failing = false;
			original = Object.getOwnPropertyDescriptor(window.navigator, 'clipboard');
			Object.defineProperty(window.navigator, 'clipboard', {
				configurable: true,
				value: {
					writeText: function (text) {
						if (failing) {
							return Promise.reject(new Error('denied'));
						}
						written.push(text);
						return Promise.resolve();
					}
				}
			});
			Wicket.Clipboard.iconMillis = 20;
		},
		afterEach: function() {
			if (original) {
				Object.defineProperty(window.navigator, 'clipboard', original);
			} else {
				delete window.navigator.clipboard;
			}
			Wicket.Clipboard.iconMillis = 1500;
		}
	});

	function fire(element, type) {
		element.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true }));
	}

	test('copy writes the text and shows the icon for a while', function(assert) {
		const done = assert.async();
		const target = document.getElementById('plain');

		Wicket.Clipboard.copy('some text', target).then(function () {
			assert.deepEqual(written, ['some text']);
			assert.ok(target.querySelector('.wicket-clipboard-copied svg'), 'the icon is shown');
			assert.ok(target.classList.contains('wicket-clipboard-host'));
			assert.equal(document.getElementById('wicket-clipboard-status').textContent, 'Copied');
			window.setTimeout(function () {
				assert.notOk(target.querySelector('.wicket-clipboard-copied'), 'the icon is gone');
				assert.notOk(target.classList.contains('wicket-clipboard-host'));
				done();
			}, 60);
		});
	});

	test('an element copies its data-wicket-copy on a click', function(assert) {
		const done = assert.async();

		fire(document.getElementById('clickCopy'), 'click');
		fire(document.getElementById('clickCopy'), 'dblclick');

		window.setTimeout(function () {
			assert.deepEqual(written, ['click text'], 'a double click does not copy it again');
			done();
		}, 10);
	});

	test('an element copying on a double click ignores single clicks', function(assert) {
		const done = assert.async();
		const element = document.getElementById('doubleCopy');

		fire(element, 'click');
		fire(element, 'dblclick');

		window.setTimeout(function () {
			assert.deepEqual(written, ['double text']);
			done();
		}, 10);
	});

	test('a failed copy shows no icon', function(assert) {
		const done = assert.async();
		failing = true;
		const target = document.getElementById('plain');

		Wicket.Clipboard.copy('denied', target).catch(function () {
			assert.notOk(target.querySelector('.wicket-clipboard-copied'));
			done();
		});
	});
});
