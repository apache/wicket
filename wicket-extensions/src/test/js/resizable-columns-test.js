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
	const RC = Wicket.ResizableColumns;
	const measure = RC.measure;

	function table() {
		return document.getElementById('resizable');
	}

	function headers() {
		return table().tHead.rows[1].cells;
	}

	function handles() {
		return table().querySelectorAll('.wicket-col-resizer');
	}

	function colWidths() {
		return Array.prototype.map.call(table().querySelectorAll('colgroup col'), function (col) {
			return col.style.width;
		});
	}

	function guide() {
		return document.querySelector('.wicket-col-resize-guide');
	}

	function pointer(target, type, clientX, button) {
		target.dispatchEvent(new MouseEvent(type, {
			bubbles: true, cancelable: true, clientX: clientX, button: button || 0
		}));
	}

	function key(target, name) {
		const event = new window.KeyboardEvent('keydown', { bubbles: true, cancelable: true, key: name });
		target.dispatchEvent(event);
		return event;
	}

	module("Wicket.ResizableColumns", {
		beforeEach: function() {
			RC.measure = function (cell) {
				return Number(cell.getAttribute('data-width'));
			};
		},
		afterEach: function() {
			RC.detach('resizable');
			RC.measure = measure;
			const leftover = guide();
			if (leftover) {
				leftover.remove();
			}
		}
	});

	test('fit: a column grows by what its neighbour can give', function(assert) {
		assert.equal(RC.clamp('fit', [100, 200, 100], 0, 1, 50, 30), 50);
		assert.equal(RC.clamp('fit', [100, 200, 100], 0, 1, 500, 30), 170);
		assert.equal(RC.clamp('fit', [100, 200, 100], 2, -1, 50, 30), 0, 'the last column has no neighbour');
	});

	test('a column does not shrink below the minimum', function(assert) {
		assert.equal(RC.clamp('fit', [100, 200, 100], 0, 1, -500, 30), -70);
		assert.equal(RC.clamp('stretch', [100, 200, 100], 1, -1, -500, 30), -170);
	});

	test('stretch: a column grows without limit', function(assert) {
		assert.equal(RC.clamp('stretch', [100, 200, 100], 2, -1, 5000, 30), 5000);
	});

	test('a column already narrower than the minimum is not made narrower', function(assert) {
		assert.equal(RC.clamp('fit', [20, 200], 0, 1, -10, 30), 0);
		assert.equal(RC.clamp('fit', [100, 20], 0, 1, 10, 30), 0);
	});

	test('fit: resizing keeps the total width', function(assert) {
		assert.deepEqual(RC.resize('fit', [100, 200, 100], 1, 2, -40, 30), [100, 160, 140]);
		assert.deepEqual(RC.resize('fit', [100, 200, 100], 1, 2, 0, 30), [100, 200, 100]);
	});

	test('stretch: resizing changes only the column', function(assert) {
		assert.deepEqual(RC.resize('stretch', [100, 200, 100], 1, -1, 40, 30), [100, 240, 100]);
	});

	test('resize does not change the given widths', function(assert) {
		const widths = [100, 200, 100];
		RC.resize('fit', widths, 0, 1, 10, 30);
		assert.deepEqual(widths, [100, 200, 100]);
	});

	test('attaching to a missing table does nothing', function(assert) {
		assert.strictEqual(RC.attach('missing', { }), undefined);
		assert.notOk('missing' in RC.instances);
	});

	test('fit: columns get shares of the full width, all but the last a handle', function(assert) {
		RC.attach('resizable', { mode: 'fit' });

		assert.deepEqual(colWidths(), ['25%', '50%', '25%']);
		assert.equal(table().style.width, '100%');
		assert.equal(table().style.tableLayout, 'fixed');
		assert.ok(table().classList.contains('wicket-resizable-columns-fit'));
		assert.equal(handles().length, 2);
		assert.equal(headers()[2].querySelector('.wicket-col-resizer'), null);
	});

	test('stretch: columns get pixel widths, every one a handle', function(assert) {
		RC.attach('resizable', { mode: 'stretch' });

		assert.deepEqual(colWidths(), ['100px', '200px', '100px']);
		assert.equal(table().style.width, '400px');
		assert.equal(handles().length, 3);
	});

	function parentOfWidth(width) {
		const parent = table().parentNode;
		parent.getBoundingClientRect = function () {
			return { width: width };
		};
		return parent;
	}

	test('fit: proportions give the shares of the columns', function(assert) {
		RC.attach('resizable', { mode: 'fit', proportions: [1, 1, 2] });

		assert.deepEqual(colWidths(), ['25%', '25%', '50%']);
		assert.equal(table().style.width, '100%');
	});

	test('proportions that do not match the columns are ignored', function(assert) {
		RC.attach('resizable', { mode: 'fit', proportions: [1, 0] });

		assert.deepEqual(colWidths(), ['25%', '50%', '25%']);
	});

	test('stretch: the table adjusts to the width of its parent', function(assert) {
		const parent = parentOfWidth(800);
		try {
			RC.attach('resizable', { mode: 'stretch', adjustToParent: true });
			assert.deepEqual(colWidths(), ['200px', '400px', '200px'], 'content widths, scaled');
			assert.equal(table().style.width, '800px');

			RC.attach('resizable', { mode: 'stretch', adjustToParent: true, proportions: [1, 1, 2] });
			assert.deepEqual(colWidths(), ['200px', '200px', '400px'], 'proportions, scaled');
		} finally {
			delete parent.getBoundingClientRect;
		}
	});

	test('stretch: saved widths win over adjusting to the parent', function(assert) {
		const parent = parentOfWidth(800);
		try {
			RC.attach('resizable', { mode: 'stretch', adjustToParent: true, widths: [50, 60, 70] });
			assert.deepEqual(colWidths(), ['50px', '60px', '70px']);
		} finally {
			delete parent.getBoundingClientRect;
		}
	});

	test('stretch: the table follows the window until the user resizes a column', function(assert) {
		const parent = parentOfWidth(800);
		try {
			RC.attach('resizable', { mode: 'stretch', adjustToParent: true, proportions: [1, 1, 2] });
			parentOfWidth(400);
			window.dispatchEvent(new Event('resize'));
			assert.deepEqual(colWidths(), ['100px', '100px', '200px']);

			key(headers()[0].querySelector('.wicket-col-resizer'), 'ArrowRight');
			const resized = colWidths();
			parentOfWidth(1200);
			window.dispatchEvent(new Event('resize'));
			assert.deepEqual(colWidths(), resized, 'the user\'s widths stay');
		} finally {
			delete parent.getBoundingClientRect;
		}
	});

	test('an unknown mode is fit', function(assert) {
		assert.equal(RC.attach('resizable', { mode: 'sideways' }).mode, 'fit');
	});

	test('handles are focusable vertical separators on the header cells', function(assert) {
		RC.attach('resizable', { mode: 'fit' });

		const handle = headers()[0].querySelector('.wicket-col-resizer');
		assert.equal(handle.getAttribute('role'), 'separator');
		assert.equal(handle.getAttribute('aria-orientation'), 'vertical');
		assert.equal(handle.tabIndex, 0);
		assert.equal(handle.getAttribute('data-index'), '0');
		assert.equal(table().tHead.rows[0].querySelector('.wicket-col-resizer'), null,
			'a row without header cells, such as the navigation, is not the header row');
	});

	test('while dragging, a band shows the extent of the column and a label its width', function(assert) {
		RC.attach('resizable', { mode: 'stretch', minWidth: 30 });
		const handle = headers()[1].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		const band = document.querySelector('.wicket-col-resize-band');
		const label = guide().querySelector('.wicket-col-resize-label');
		assert.ok(band, 'the band is shown');
		assert.equal(parseFloat(band.style.width), 200);
		assert.equal(label.textContent, '200 px');

		pointer(document, 'pointermove', 540);
		assert.equal(parseFloat(band.style.width), 240);
		assert.equal(label.textContent, '240 px');

		pointer(document, 'pointerup', 540);
		assert.equal(document.querySelector('.wicket-col-resize-band'), null, 'the band is removed');
	});

	test('dragging shows a guide and applies the width on release', function(assert) {
		RC.attach('resizable', { mode: 'stretch', minWidth: 30 });
		const handle = headers()[1].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		assert.ok(guide(), 'the guide is shown');
		assert.ok(table().classList.contains('wicket-resizing'));
		const start = parseFloat(guide().style.left);

		pointer(document, 'pointermove', 540);
		assert.equal(parseFloat(guide().style.left), start + 40);
		assert.deepEqual(colWidths(), ['100px', '200px', '100px'], 'nothing changes while dragging');

		pointer(document, 'pointerup', 560);
		assert.equal(guide(), null, 'the guide is removed');
		assert.notOk(table().classList.contains('wicket-resizing'));
		assert.deepEqual(colWidths(), ['100px', '260px', '100px']);
		assert.equal(table().style.width, '460px');
	});

	test('the guide stops at the limits', function(assert) {
		RC.attach('resizable', { mode: 'fit', minWidth: 30 });
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		const start = parseFloat(guide().style.left);
		pointer(document, 'pointermove', 1500);
		assert.equal(parseFloat(guide().style.left), start + 170);
		pointer(document, 'pointermove', 0);
		assert.equal(parseFloat(guide().style.left), start - 70);
		pointer(document, 'pointerup', 0);

		assert.deepEqual(colWidths(), ['7.5%', '67.5%', '25%']);
	});

	test('a cancelled drag changes nothing', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		pointer(document, 'pointermove', 550);
		pointer(document, 'pointercancel', 550);

		assert.equal(guide(), null);
		assert.deepEqual(colWidths(), ['25%', '50%', '25%']);
		pointer(document, 'pointerup', 600);
		assert.deepEqual(colWidths(), ['25%', '50%', '25%'], 'the drag listeners are gone');
	});

	test('the click ending a drag reaches no button', function(assert) {
		const done = assert.async();
		RC.attach('resizable', { mode: 'stretch' });
		const handle = headers()[0].querySelector('.wicket-col-resizer');
		let clicks = 0;
		const count = function () {
			clicks++;
		};
		table().addEventListener('click', count);

		pointer(handle, 'pointerdown', 500);
		pointer(document, 'pointermove', 540);
		pointer(document, 'pointerup', 540);
		pointer(headers()[1], 'click', 540);
		assert.equal(clicks, 0, 'the click right after the drag is swallowed');

		window.setTimeout(function () {
			pointer(headers()[1], 'click', 540);
			assert.equal(clicks, 1, 'later clicks pass');
			table().removeEventListener('click', count);
			done();
		}, 10);
	});

	test('Escape and leaving the window cancel a drag', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		pointer(document, 'pointermove', 550);
		key(document.body, 'Escape');
		assert.equal(guide(), null, 'Escape removes the guide');

		pointer(handle, 'pointerdown', 500);
		window.dispatchEvent(new Event('blur'));
		assert.equal(guide(), null, 'leaving the window removes the guide');

		pointer(document, 'pointerup', 600);
		assert.deepEqual(colWidths(), ['25%', '50%', '25%']);
	});

	test('a second drag leaves a single guide', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		pointer(handle, 'pointerdown', 500);
		assert.equal(document.querySelectorAll('.wicket-col-resize-guide').length, 1);

		pointer(document, 'pointerup', 500);
		assert.equal(guide(), null);
	});

	test('a failing release still removes the guide', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const handle = headers()[0].querySelector('.wicket-col-resizer');
		const clamp = RC.clamp;

		pointer(handle, 'pointerdown', 500);
		RC.clamp = function () {
			throw new Error('failing');
		};
		try {
			window.onerror = function () {
				return true;
			};
			pointer(document, 'pointerup', 600);
		} finally {
			RC.clamp = clamp;
			window.onerror = null;
		}

		assert.equal(guide(), null);
		assert.notOk(table().classList.contains('wicket-resizing'));
	});

	test('only the primary button on a handle drags', function(assert) {
		RC.attach('resizable', { mode: 'fit' });

		pointer(headers()[0].querySelector('.wicket-col-resizer'), 'pointerdown', 500, 2);
		assert.equal(guide(), null);

		pointer(headers()[0], 'pointerdown', 500);
		assert.equal(guide(), null);
	});

	test('arrow keys on a handle resize by the step', function(assert) {
		RC.attach('resizable', { mode: 'stretch', step: 15 });
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		assert.ok(key(handle, 'ArrowRight').defaultPrevented);
		assert.deepEqual(colWidths(), ['115px', '200px', '100px']);

		headers()[0].setAttribute('data-width', '115');
		key(handle, 'ArrowLeft');
		key(handle, 'ArrowLeft');
		assert.deepEqual(colWidths(), ['100px', '200px', '100px']);

		assert.notOk(key(handle, 'Enter').defaultPrevented, 'other keys are left alone');
		key(headers()[0], 'ArrowRight');
		assert.deepEqual(colWidths(), ['100px', '200px', '100px'], 'only on a handle');
	});

	test('widths that cannot be measured keep the last known ones', function(assert) {
		const instance = RC.attach('resizable', { mode: 'stretch' });
		RC.measure = function () {
			return 0;
		};

		assert.deepEqual(instance.measureAll(), [100, 200, 100]);
	});

	test('attaching again replaces the earlier instance', function(assert) {
		const first = RC.attach('resizable', { mode: 'fit' });
		const second = RC.attach('resizable', { mode: 'fit' });

		assert.notStrictEqual(first, second);
		assert.strictEqual(RC.instances.resizable, second);
		assert.equal(handles().length, 2, 'handles are not duplicated');
		assert.equal(table().querySelectorAll('colgroup').length, 1);
	});

	test('detaching removes the handles and stops listening', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		RC.detach('resizable');

		assert.equal(handles().length, 0);
		assert.notOk('resizable' in RC.instances);
		pointer(handle, 'pointerdown', 500);
		headers()[0].appendChild(handle);
		pointer(handle, 'pointerdown', 500);
		assert.equal(guide(), null);
	});

	test('a re-rendered header row gets its handles back', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const row = table().tHead.rows[1];
		const replacement = row.cloneNode(true);
		Array.prototype.forEach.call(replacement.querySelectorAll('.wicket-col-resizer'),
			function (handle) {
				handle.remove();
			});
		row.replaceWith(replacement);
		assert.equal(handles().length, 0);

		Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_ADDED, replacement);

		assert.equal(handles().length, 2);
		assert.deepEqual(colWidths(), ['25%', '50%', '25%'], 'the widths are kept');
	});

	test('a table removed from the page is detached', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		const removed = table();
		removed.remove();

		Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_ADDED, document.body);

		assert.notOk('resizable' in RC.instances);
		document.getElementById('qunit-fixture').appendChild(removed);
	});

	test('fit: a column takes its width from the next resizable column', function(assert) {
		assert.deepEqual(RC.resize('fit', [100, 200, 100], 0, 2, 50, 30), [150, 200, 50]);
	});

	test('a column marked not resizable gets no handle and gives no width', function(assert) {
		headers()[1].setAttribute('data-resizable', 'false');
		RC.attach('resizable', { mode: 'fit' });

		assert.equal(headers()[1].querySelector('.wicket-col-resizer'), null);
		const first = headers()[0].querySelector('.wicket-col-resizer');
		assert.equal(first.getAttribute('data-neighbour'), '2', 'the next resizable column');

		pointer(first, 'pointerdown', 500);
		pointer(document, 'pointerup', 520);
		assert.deepEqual(colWidths(), ['30%', '50%', '20%']);
	});

	test('fit: a column with no resizable column after it gets no handle', function(assert) {
		headers()[2].setAttribute('data-resizable', 'false');
		RC.attach('resizable', { mode: 'fit' });

		assert.equal(handles().length, 1);
		assert.equal(headers()[1].querySelector('.wicket-col-resizer'), null);
	});

	test('stretch: every resizable column has a handle', function(assert) {
		headers()[0].setAttribute('data-resizable', 'false');
		RC.attach('resizable', { mode: 'stretch' });

		assert.equal(handles().length, 2);
		assert.equal(headers()[0].querySelector('.wicket-col-resizer'), null);
	});

	test('saved widths are applied instead of measured ones', function(assert) {
		RC.attach('resizable', { mode: 'stretch', widths: [50, 60, 70] });

		assert.deepEqual(colWidths(), ['50px', '60px', '70px']);
	});

	test('fit measures the content again and keeps those widths', function(assert) {
		RC.attach('resizable', { mode: 'stretch' });
		const layouts = [];
		RC.measure = function (cell) {
			layouts.push(table().style.tableLayout);
			return Number(cell.getAttribute('data-width')) + 10;
		};

		RC.fit('resizable');

		assert.deepEqual(colWidths(), ['110px', '210px', '110px']);
		assert.equal(table().style.width, '430px');
		assert.equal(layouts[0], 'auto', 'measured with the automatic layout');
		assert.equal(table().style.tableLayout, 'fixed');
	});

	test('fit keeps saved widths and widths the user chose', function(assert) {
		RC.attach('resizable', { mode: 'stretch', widths: [50, 60, 70] });
		RC.fit('resizable');
		assert.deepEqual(colWidths(), ['50px', '60px', '70px']);

		RC.attach('resizable', { mode: 'stretch' });
		key(handles()[0], 'ArrowRight');
		RC.fit('resizable');
		assert.deepEqual(colWidths(), ['110px', '200px', '100px']);
	});

	test('fit on a table without columns resizing does nothing', function(assert) {
		RC.fit('unknown');
		assert.ok(true);
	});

	test('saved widths that do not fit the columns are ignored', function(assert) {
		RC.attach('resizable', { mode: 'stretch', widths: [50, 60] });
		assert.deepEqual(colWidths(), ['100px', '200px', '100px']);

		RC.attach('resizable', { mode: 'stretch', widths: [50, 0, 70] });
		assert.deepEqual(colWidths(), ['100px', '200px', '100px']);
	});

	test('every resize is reported to onResize', function(assert) {
		const reported = [];
		RC.attach('resizable', {
			mode: 'stretch', step: 10,
			onResize: function (widths) {
				reported.push(widths);
			}
		});
		const handle = headers()[0].querySelector('.wicket-col-resizer');

		pointer(handle, 'pointerdown', 500);
		pointer(document, 'pointerup', 525);
		key(handle, 'ArrowRight');
		pointer(handle, 'pointerdown', 500);
		pointer(document, 'pointerup', 500);

		assert.deepEqual(reported, [[125, 200, 100], [110, 200, 100]],
			'a drag that changes nothing is not reported');
	});

	test('the guide spans the header row and the body, not the navigation', function(assert) {
		RC.attach('resizable', { mode: 'fit' });
		table().tHead.rows[1].getBoundingClientRect = function () {
			return { top: 40, bottom: 60, left: 0, right: 400, width: 400, height: 20 };
		};
		table().tBodies[0].getBoundingClientRect = function () {
			return { top: 60, bottom: 260, left: 0, right: 400, width: 400, height: 200 };
		};

		pointer(headers()[0].querySelector('.wicket-col-resizer'), 'pointerdown', 500);

		assert.equal(parseFloat(guide().style.top), 40 + window.scrollY);
		assert.equal(parseFloat(guide().style.height), 220);
		pointer(document, 'pointercancel', 500);
	});
});
