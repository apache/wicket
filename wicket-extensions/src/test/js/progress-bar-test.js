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

	function label() {
		return document.querySelector('#bar .wicket-progress-bar-label');
	}

	/**
	 * @return the vertical middle of the ink of the label's text, from its baseline and the
	 *         extent the font draws the text with above and below it
	 */
	function inkMiddle(element) {
		const marker = document.createElement('span');
		marker.style.display = 'inline-block';
		marker.style.width = '0';
		marker.style.height = '0';
		element.appendChild(marker);
		const baseline = marker.getBoundingClientRect().bottom;
		marker.remove();
		const style = window.getComputedStyle(element);
		const context = document.createElement('canvas').getContext('2d');
		context.font = style.fontWeight + ' ' + style.fontSize + ' ' + style.fontFamily;
		const metrics = context.measureText(element.textContent);
		return baseline + (metrics.actualBoundingBoxDescent - metrics.actualBoundingBoxAscent) / 2;
	}

	function boxMiddle(element) {
		const rect = element.getBoundingClientRect();
		return rect.top + rect.height / 2;
	}

	module("wicket-progress-bar.css");

	test('the label is as high as the bar', function(assert) {
		const bar = document.getElementById('bar').getBoundingClientRect();
		const rect = label().getBoundingClientRect();

		assert.ok(Math.abs(rect.height - (bar.height - 2)) < 0.5, 'the bar minus its border');
	});

	test('the digits of the label are centered vertically, whatever the size', function(assert) {
		const bar = document.getElementById('bar');
		if (!window.CSS || !CSS.supports('text-box', 'trim-both cap alphabetic')) {
			assert.ok(true, 'the browser cannot trim text boxes');
			return;
		}
		[12, 16, 24, 40, 64].forEach(function (size) {
			bar.style.fontSize = size + 'px';
			const offset = inkMiddle(label()) - boxMiddle(label());

			assert.ok(Math.abs(offset) <= Math.max(1, size * 0.02),
				size + 'px: the digits are ' + offset.toFixed(2) + 'px off the middle');
		});
	});
});
