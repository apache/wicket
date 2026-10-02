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

	const MC = Wicket.MovableColumns;

	let moves;

	function table() {
		return document.getElementById('movable');
	}

	function headerCell(index) {
		return table().tHead.rows[1].cells[index];
	}

	function attach(options) {
		moves = [];
		return MC.attach('movable', Object.assign({
			movable: [2],
			onMove: function (from, to) {
				moves.push([from, to]);
			}
		}, options));
	}

	/**
	 * Lays the header cells out side by side, each 100 pixels wide.
	 */
	function layOut() {
		[0, 1, 2].forEach(function (index) {
			headerCell(index).getBoundingClientRect = function () {
				return { left: index * 100, right: index * 100 + 100, width: 100, top: 10,
					bottom: 30, height: 20 };
			};
		});
	}

	function pointer(target, type, clientX) {
		const event = new MouseEvent(type, {
			bubbles: true, cancelable: true, clientX: clientX, clientY: 20, button: 0
		});
		target.dispatchEvent(event);
		return event;
	}

	function dragColumn(from, startX, positions) {
		pointer(headerCell(from).querySelector('.wicket-col-mover'), 'pointerdown', startX);
		positions.forEach(function (x) {
			pointer(document, 'pointermove', x);
		});
	}

	function key(target, name) {
		target.dispatchEvent(new window.KeyboardEvent('keydown',
			{ bubbles: true, cancelable: true, key: name }));
	}

	function marker() {
		return document.querySelector('.wicket-col-drop-marker');
	}

	function ghost() {
		return document.querySelector('.wicket-col-move-ghost');
	}

	module('Wicket.MovableColumns', {
		afterEach: function () {
			MC.detach('movable');
			MC.focusAfterMove = undefined;
		}
	});

	test('a move is valid when only movable columns change their places', function (assert) {
		const movable = [false, true, true, false, true];

		assert.ok(MC.isValidMove(movable, 1, 3), 'A behind B');
		assert.ok(MC.isValidMove(movable, 2, 1), 'B in front of A');
		assert.notOk(MC.isValidMove(movable, 1, 5), 'A to the end would shift C');
		assert.notOk(MC.isValidMove(movable, 4, 1), 'D in front of A would shift C');
		assert.notOk(MC.isValidMove(movable, 1, 0), 'A in front of #');
		assert.notOk(MC.isValidMove(movable, 3, 1), 'C is not movable');
		assert.notOk(MC.isValidMove(movable, 1, 2), 'A in its own place');
	});

	test('the movable columns get a handle, the others none', function (assert) {
		attach({ label: 'Move {0}' });

		const handles = table().querySelectorAll('.wicket-col-mover');
		assert.equal(handles.length, 2, 'the existing handle is kept, one is added');
		assert.equal(headerCell(2).getAttribute('data-movable'), 'true');
		const added = headerCell(2).querySelector('.wicket-col-mover');
		assert.equal(added.getAttribute('type'), 'button');
		assert.equal(added.getAttribute('aria-label'), 'Move B');
		assert.equal(headerCell(0).querySelector('.wicket-col-mover'), null);
		assert.equal(headerCell(3).querySelector('.wicket-col-mover'), null,
			'a skipped cell is no column');
	});

	test('a dragged column shows where it will land and is moved there on release',
		function (assert) {
			attach();
			layOut();

			dragColumn(2, 250, [240, 120]);

			assert.ok(ghost(), 'a ghost of the header follows the pointer');
			assert.equal(ghost().textContent, 'B');
			assert.equal(marker().style.display, '', 'the marker is shown');
			assert.equal(parseFloat(marker().style.left), 100 + window.scrollX,
				'on the left edge of the column it goes in front of');
			assert.ok(headerCell(2).classList.contains('wicket-col-moving'));
			assert.ok(table().tBodies[0].rows[0].cells[2].classList.contains('wicket-col-moving'));
			assert.deepEqual(moves, [], 'nothing moves while dragging');

			pointer(document, 'pointerup', 120);

			assert.deepEqual(moves, [[2, 1]]);
			assert.equal(marker(), null, 'the marker is removed');
			assert.equal(ghost(), null, 'the ghost is removed');
			assert.notOk(headerCell(2).classList.contains('wicket-col-moving'));
			assert.deepEqual(MC.focusAfterMove, { id: 'movable', index: 1 });
		});

	test('a column dropped on the right half of the last one goes to the end', function (assert) {
		attach();
		layOut();

		dragColumn(1, 150, [290]);
		assert.equal(parseFloat(marker().style.left), 300 + window.scrollX,
			'the marker is on the right edge of the last column');
		pointer(document, 'pointerup', 290);

		assert.deepEqual(moves, [[1, 3]]);
	});

	test('no marker is shown where a column may not go, and it is not moved there',
		function (assert) {
			attach();
			layOut();

			dragColumn(1, 150, [20]);
			assert.equal(marker().style.display, 'none', 'not in front of a fixed column');
			assert.ok(table().classList.contains('wicket-col-no-drop'));
			pointer(document, 'pointerup', 20);

			dragColumn(1, 150, [170]);
			assert.equal(marker().style.display, 'none', 'not in its own place');
			pointer(document, 'pointerup', 170);

			assert.deepEqual(moves, []);
			assert.notOk(table().classList.contains('wicket-col-no-drop'));
		});

	test('a short movement on the handle does not drag the column', function (assert) {
		attach();
		layOut();

		dragColumn(2, 250, [248]);
		assert.equal(ghost(), null);
		pointer(document, 'pointerup', 248);

		assert.deepEqual(moves, []);
	});

	test('Escape cancels dragging a column', function (assert) {
		attach();
		layOut();

		dragColumn(2, 250, [120]);
		key(document, 'Escape');
		pointer(document, 'pointerup', 120);

		assert.deepEqual(moves, []);
		assert.equal(marker(), null);
	});

	test('the arrow keys on the handle move a column past a movable neighbour', function (assert) {
		attach();
		const handle = headerCell(1).querySelector('.wicket-col-mover');

		key(handle, 'ArrowLeft');
		key(handle, 'ArrowRight');

		assert.deepEqual(moves, [[1, 3]]);
		assert.deepEqual(MC.focusAfterMove, { id: 'movable', index: 2 });
	});

	test('the handle of a moved column gets the focus when the table is attached again',
		function (assert) {
			MC.focusAfterMove = { id: 'movable', index: 2 };

			attach();

			assert.equal(document.activeElement, headerCell(2).querySelector('.wicket-col-mover'));
			assert.equal(MC.focusAfterMove, undefined);
		});

	test('a detached table does not move its columns any more', function (assert) {
		attach();
		layOut();
		MC.detach('movable');

		dragColumn(2, 250, [120]);
		pointer(document, 'pointerup', 120);

		assert.deepEqual(moves, []);
		assert.equal(ghost(), null);
	});
});
