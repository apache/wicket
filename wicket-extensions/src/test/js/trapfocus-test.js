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

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	const TAB_KEYCODE = 9;

	function tab(target, shiftKey) {
		target.dispatchEvent(new KeyboardEvent('keydown', {
			key: 'Tab',
			code: 'Tab',
			keyCode: TAB_KEYCODE,
			which: TAB_KEYCODE,
			shiftKey: !!shiftKey,
			bubbles: true,
			cancelable: true
		}));
	}

	module("Wicket.trapFocus");

	test("Tab on the last focusable element wraps focus to the first, without throwing", assert => {
		window.Wicket.trapFocus('trapFocusContainer', 'wicket-trap-focus-test');

		var first = document.getElementById('trapFocusFirst');
		var last = document.getElementById('trapFocusLast');
		last.focus();

		var threw = false;
		try {
			tab(last, false);
		} catch (e) {
			threw = true;
		}

		assert.notOk(threw, "tabbing past the last focusable element threw an exception");
		assert.equal(document.activeElement, first,
			"focus did not wrap around to the first focusable element");
	});

	test("Shift+Tab on the first focusable element wraps focus to the last, without throwing", assert => {
		window.Wicket.trapFocus('trapFocusContainer', 'wicket-trap-focus-test');

		var first = document.getElementById('trapFocusFirst');
		var last = document.getElementById('trapFocusLast');
		first.focus();

		var threw = false;
		try {
			tab(first, true);
		} catch (e) {
			threw = true;
		}

		assert.notOk(threw, "shift-tabbing past the first focusable element threw an exception");
		assert.equal(document.activeElement, last,
			"focus did not wrap around to the last focusable element");
	});
});
