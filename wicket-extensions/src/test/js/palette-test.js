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

jQuery(document).ready(function() {
	"use strict";

	const { module, test } = QUnit;

	function selectionOptionValues() {
		var selection = document.getElementById('paletteSelection');
		return Array.prototype.map.call(selection.options, function(option) {
			return option.value;
		});
	}

	function selectOnly(selection, value) {
		Array.prototype.forEach.call(selection.options, function(option) {
			option.selected = (option.value === value);
		});
	}

	module("Wicket.Palette", {
		beforeEach: function() {
			var selection = document.getElementById('paletteSelection');
			selection.innerHTML =
				'<option value="1">one</option>' +
				'<option value="2">two</option>' +
				'<option value="3">three</option>';
			document.getElementById('paletteRecorder').value = '1,2,3';
		}
	});

	test("moveUp reorders the selected option without throwing", assert => {
		var selection = document.getElementById('paletteSelection');
		selectOnly(selection, '3');

		Wicket.Palette.moveUp('paletteChoices', 'paletteSelection', 'paletteRecorder');

		assert.deepEqual(selectionOptionValues(), ['1', '3', '2'],
			"moveUp did not move the selected option in front of its predecessor");
	});

	test("moveUp updates the hidden recorder input", assert => {
		var selection = document.getElementById('paletteSelection');
		selectOnly(selection, '3');

		Wicket.Palette.moveUp('paletteChoices', 'paletteSelection', 'paletteRecorder');

		assert.equal(document.getElementById('paletteRecorder').value, '1,3,2',
			"the recorder was not updated, so the server never sees the new order");
	});

	test("moveUp on the first option is a no-op and does not throw", assert => {
		var selection = document.getElementById('paletteSelection');
		selectOnly(selection, '1');

		Wicket.Palette.moveUp('paletteChoices', 'paletteSelection', 'paletteRecorder');

		assert.deepEqual(selectionOptionValues(), ['1', '2', '3'],
			"the order should not have changed");
	});

	test("moveDown reorders the selected option without throwing", assert => {
		var selection = document.getElementById('paletteSelection');
		selectOnly(selection, '1');

		Wicket.Palette.moveDown('paletteChoices', 'paletteSelection', 'paletteRecorder');

		assert.deepEqual(selectionOptionValues(), ['2', '1', '3'],
			"moveDown did not move the selected option behind its successor");
	});
});
