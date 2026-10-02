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

	const ROWS = [
		{ key: '1', data: { name: 'Ada' } },
		{ key: '2', data: { name: 'Linus' } }
	];

	let table;

	function builtin(template, data, meta) {
		return Wicket.DynamicDataTable.engines.builtin.compile(template)(data, meta);
	}

	function init(overrides, action, messages) {
		const cfg = Object.assign({
			id: 'dynamicTable',
			dataUrl: 'data:application/json,' + encodeURIComponent(JSON.stringify({ rows: [] })),
			engine: 'builtin',
			columns: [
				{ template: '{{name}}' },
				{ template: '<a href="#" data-dt-action="select">Select</a>', cssClass: 'actions' }
			],
			push: false
		}, overrides);
		table = new Wicket.DynamicDataTable(cfg, action || function () { }, undefined, messages);
		return table;
	}

	function bodyRows() {
		return document.getElementById('dynamicTable').tBodies[0].rows;
	}

	module("Wicket.DynamicDataTable", {
		afterEach: function() {
			if (table) {
				table.destroy();
				table = undefined;
			}
		}
	});

	test('the built-in engine escapes {{ }} and not {{{ }}}', function(assert) {
		assert.equal(builtin('<b>{{v}}</b>|{{{v}}}', { v: '<i>"x" & \'y\'</i>' }),
			'<b>&lt;i&gt;&quot;x&quot; &amp; &#39;y&#39;&lt;/i&gt;</b>|<i>"x" & \'y\'</i>');
	});

	test('the built-in engine follows dotted paths and writes nothing for missing values', function(assert) {
		assert.equal(builtin('{{ a.b }}-{{a.c}}-{{x.y.z}}', { a: { b: 0, c: null } }), '0--');
	});

	test('render renders one row per entry, keyed and with the column classes', function(assert) {
		init().render(ROWS);

		const rows = bodyRows();
		assert.equal(rows.length, 2);
		assert.equal(rows[1].getAttribute('data-key'), '2');
		assert.equal(rows[1].id, 'dynamicTable-row-2');
		assert.equal(rows[1].cells[0].innerHTML, 'Linus');
		assert.equal(rows[1].cells[1].className, 'actions');
	});

	test('the built-in engine reads row variables with @', function(assert) {
		assert.equal(builtin('{{@number}}. {{name}} ({{@key}})', { name: 'Ada', number: 7 },
			{ key: 'a<1>', number: 3 }), '3. Ada (a&lt;1&gt;)');
		assert.equal(builtin('{{@missing}}', { }, { }), '');
	});

	test('the Handlebars adapter hands the row variables to Handlebars as @data', function(assert) {
		const calls = [];
		window.Handlebars = {
			compile: function (template) {
				return function (context, options) {
					calls.push([template, context, options]);
					return 'cell';
				};
			}
		};
		try {
			const cell = Wicket.DynamicDataTable.engines.handlebars.compile('{{@number}} {{name}}');

			assert.equal(cell({ name: 'Ada' }, { key: '1', number: 3 }), 'cell');
			assert.deepEqual(calls, [['{{@number}} {{name}}', { name: 'Ada' },
				{ data: { key: '1', number: 3 } }]]);
		} finally {
			delete window.Handlebars;
		}
	});

	test('rows are marked even and odd, the first one even', function(assert) {
		init().render([
			{ key: '1', number: 11, data: { name: 'Ada' } },
			{ key: '2', number: 12, data: { name: 'Linus' } }
		]);

		assert.equal(bodyRows()[0].className, 'even');
		assert.equal(bodyRows()[1].className, 'odd');
		table.renderRow('2', { name: 'Grace' });
		assert.equal(bodyRows()[1].className, 'odd', 'a repainted row keeps its parity');
	});

	test('resizing is attached as configured and reports widths as an action', function(assert) {
		const calls = [];
		const actions = [];
		const original = Wicket.ResizableColumns;
		Wicket.ResizableColumns = {
			attach: function (id, options) {
				calls.push(['attach', id, options]);
			},
			detach: function (id) {
				calls.push(['detach', id]);
			}
		};
		try {
			init({ resize: { mode: 'fit', action: '_wicket-resize', widths: [10, 20] } },
				function (action, key) {
					actions.push([action, key]);
				});

			assert.equal(calls.length, 1);
			assert.equal(calls[0][1], 'dynamicTable');
			assert.equal(calls[0][2].mode, 'fit');
			assert.deepEqual(calls[0][2].widths, [10, 20]);

			calls[0][2].onResize([120.04, 79.96]);
			assert.deepEqual(actions, [['_wicket-resize', '120,80']]);

			table.destroy();
			table = undefined;
			assert.deepEqual(calls[1], ['detach', 'dynamicTable']);
		} finally {
			Wicket.ResizableColumns = original;
		}
	});

	test('a table not configured for resizing does not attach it', function(assert) {
		let attached = false;
		const original = Wicket.ResizableColumns;
		Wicket.ResizableColumns = {
			attach: function () {
				attached = true;
			},
			detach: function () { }
		};
		try {
			init();
			assert.notOk(attached);
		} finally {
			Wicket.ResizableColumns = original;
		}
	});

	test('render numbers the rows, from the server or by position', function(assert) {
		init().render([
			{ key: '1', number: 11, data: { name: 'Ada' } },
			{ key: '2', number: 12, data: { name: 'Linus' } }
		]);
		assert.equal(bodyRows()[1].getAttribute('data-number'), '12');

		table.render(ROWS);
		assert.equal(bodyRows()[0].getAttribute('data-number'), '1');
		assert.equal(bodyRows()[1].getAttribute('data-number'), '2');
	});

	test('a row number column shows the number of each row', function(assert) {
		init({ columns: [{ template: '{{@number}}' }, { template: '{{name}}' }] }).render([
			{ key: '1', number: 11, data: { name: 'Ada' } },
			{ key: '2', number: 12, data: { name: 'Linus' } }
		]);

		assert.equal(bodyRows()[0].cells[0].innerHTML, '11');
		assert.equal(bodyRows()[1].cells[0].innerHTML, '12');
	});

	test('renderRow keeps the number of the row', function(assert) {
		init({ columns: [{ template: '{{@number}}' }, { template: '{{name}}' }] }).render([
			{ key: '1', number: 11, data: { name: 'Ada' } },
			{ key: '2', number: 12, data: { name: 'Linus' } }
		]);

		table.renderRow('2', { name: 'Grace' });

		assert.equal(bodyRows()[1].cells[0].innerHTML, '12');
		assert.equal(bodyRows()[1].cells[1].innerHTML, 'Grace');
	});

	test('renderRow repaints only its row and ignores rows that are not shown', function(assert) {
		init().render(ROWS);
		const first = bodyRows()[0];

		table.renderRow('2', { name: 'Grace' });
		table.renderRow('42', { name: 'Nobody' });

		const rows = bodyRows();
		assert.equal(rows.length, 2);
		assert.strictEqual(rows[0], first);
		assert.equal(rows[1].id, 'dynamicTable-row-2');
		assert.equal(rows[1].cells[0].innerHTML, 'Grace');
	});

	test('a click on an action sends the action and the row key', function(assert) {
		const calls = [];
		init({}, function (action, key) {
			calls.push([action, key]);
		}).render(ROWS);

		bodyRows()[1].querySelector('[data-dt-action]').click();

		assert.deepEqual(calls, [['select', '2']]);
	});

	test('the rows are loaded from the data URL', function(assert) {
		const done = assert.async();
		init({
			dataUrl: 'data:application/json,' + encodeURIComponent(JSON.stringify({ rows: ROWS }))
		});

		const started = Date.now();
		(function waitForRows() {
			if (bodyRows().length === 2 || Date.now() - started > 2000) {
				assert.equal(bodyRows().length, 2);
				assert.equal(bodyRows()[0].cells[0].innerHTML, 'Ada');
				done();
			} else {
				setTimeout(waitForRows, 10);
			}
		})();
	});

	test('a pushed message is handed to the table it addresses', function(assert) {
		init({ push: true }, undefined, {
			updateRows: function (message) {
				Wicket.DynamicDataTable.instances[message.tableId].render(message.rows);
			}
		});

		Wicket.Event.publish('/websocket/message',
			JSON.stringify({ tableId: 'other', typeId: 'updateRows', rows: ROWS }));
		assert.equal(bodyRows().length, 0);

		Wicket.Event.publish('/websocket/message',
			JSON.stringify({ tableId: 'dynamicTable', typeId: 'updateRows', rows: ROWS }));
		assert.equal(bodyRows().length, 2);
	});

	test('a pushed message is handed to the handler the table has for its type', function(assert) {
		const calls = [];
		init({ push: true }, undefined, {
			custom: function (message) {
				calls.push(message.value);
			}
		});

		Wicket.Event.publish('/websocket/message',
			JSON.stringify({ tableId: 'dynamicTable', typeId: 'custom', value: 42 }));
		Wicket.Event.publish('/websocket/message',
			JSON.stringify({ tableId: 'dynamicTable', typeId: 'unknown', value: 43 }));

		assert.deepEqual(calls, [42]);
	});

	test('a table is registered in instances until it is destroyed', function(assert) {
		init();
		assert.strictEqual(Wicket.DynamicDataTable.instances.dynamicTable, table);

		table.destroy();
		assert.notOk('dynamicTable' in Wicket.DynamicDataTable.instances);
		table = undefined;
	});

	test('without resizing, the columns follow their proportions over the full width', function(assert) {
		init({ layout: { proportions: [1, 3], adjustToParent: true } });
		const element = document.getElementById('dynamicTable');

		const cols = element.querySelectorAll('colgroup.dynamic-data-table-colgroup col');
		assert.deepEqual(Array.prototype.map.call(cols, function (col) {
			return col.style.width;
		}), ['25%', '75%']);
		assert.equal(element.style.width, '100%');
		assert.equal(element.style.tableLayout, 'fixed');

		table.destroy();
		init({ layout: { proportions: [1, 1] } });
		assert.equal(element.querySelectorAll('colgroup.dynamic-data-table-colgroup').length, 1,
			'a new instance reuses the column group');
		element.querySelector('colgroup.dynamic-data-table-colgroup').remove();
		element.style.width = '';
		element.style.tableLayout = '';
	});

	function addOverlay() {
		const caption = document.createElement('caption');
		caption.className = 'dynamic-data-table-overlay';
		caption.innerHTML = '<div id="overlayWindow" class="dynamic-data-table-overlay-window">' +
			'<input id="overlayField"></div>';
		const element = document.getElementById('dynamicTable');
		element.insertBefore(caption, element.firstChild);
		return caption;
	}

	function escape(target) {
		const event = new window.KeyboardEvent('keydown', { bubbles: true, cancelable: true, key: 'Escape' });
		target.dispatchEvent(event);
		return event;
	}

	test('Escape in the overlay asks the server to close it', function(assert) {
		const calls = [];
		init({}, function (action, key) {
			calls.push([action, key]);
		});
		const caption = addOverlay();
		try {
			assert.ok(escape(document.getElementById('overlayField')).defaultPrevented);
			assert.deepEqual(calls, [['_wicket-close-overlay', null]]);

			escape(document.getElementById('dynamicTable').tHead);
			assert.equal(calls.length, 1, 'Escape outside the overlay is left alone');
		} finally {
			caption.remove();
		}
	});

	test('Escape leaves an overlay with changes open', function(assert) {
		const calls = [];
		init({}, function (action, key) {
			calls.push([action, key]);
		});
		const caption = addOverlay();
		try {
			const field = document.getElementById('overlayField');
			field.value = 'changed';
			assert.ok(escape(field).defaultPrevented);
			assert.deepEqual(calls, [], 'a changed field keeps the overlay');

			field.value = field.defaultValue;
			const marker = document.createElement('div');
			marker.setAttribute('data-dt-changed', 'true');
			document.getElementById('overlayWindow').appendChild(marker);
			escape(field);
			assert.deepEqual(calls, [], 'data-dt-changed keeps the overlay');

			marker.remove();
			escape(field);
			assert.deepEqual(calls, [['_wicket-close-overlay', null]]);
		} finally {
			caption.remove();
		}
	});

	test('changes are found in check boxes, selects and text', function(assert) {
		const box = document.createElement('div');
		box.innerHTML = '<input type="checkbox"><select><option>a</option><option>b</option></select>' +
			'<textarea>t</textarea>';
		const isChanged = Wicket.DynamicDataTable.isChanged;
		assert.notOk(isChanged(box));
		box.querySelector('input').checked = true;
		assert.ok(isChanged(box), 'check box');
		box.querySelector('input').checked = false;
		box.querySelector('select').selectedIndex = 1;
		assert.ok(isChanged(box), 'select');
		box.querySelector('select').selectedIndex = 0;
		box.querySelector('textarea').value = 'u';
		assert.ok(isChanged(box), 'text');
	});

	test('the overlay window is placed inside the table', function(assert) {
		const caption = addOverlay();
		try {
			Wicket.DynamicDataTable.placeOverlay('overlayWindow');
			assert.equal(document.getElementById('overlayWindow').style.top, '16px');
			Wicket.DynamicDataTable.placeOverlay('missing');
			assert.ok(true, 'a missing window is ignored');
		} finally {
			caption.remove();
		}
	});

	test('a table is destroyed when Wicket removes it or one of its ancestors', function(assert) {
		const element = document.getElementById('dynamicTable');

		init();
		Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING, element);
		assert.notOk('dynamicTable' in Wicket.DynamicDataTable.instances);

		init();
		Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING, element.parentNode);
		assert.notOk('dynamicTable' in Wicket.DynamicDataTable.instances);
		table = undefined;
	});

	test('a table stays registered when Wicket replaces an element inside it', function(assert) {
		init();
		Wicket.Event.publish(Wicket.Event.Topic.DOM_NODE_REMOVING,
			document.getElementById('dynamicTable').tBodies[0]);
		assert.strictEqual(Wicket.DynamicDataTable.instances.dynamicTable, table);
	});

	const CHECKBOX = '<input type="checkbox" class="dynamic-data-table-select" ' +
		'data-dt-action="_wicket-select" data-dt-action-unchecked="_wicket-deselect">';

	function initSelectable(action) {
		const head = document.getElementById('dynamicTable').tHead.rows[0];
		head.cells[0].innerHTML = '<input type="checkbox" class="dynamic-data-table-select-page" ' +
			'data-dt-action="_wicket-select-page" ' +
			'data-dt-action-unchecked="_wicket-deselect-page">';
		return init({ columns: [{ template: CHECKBOX }, { template: '{{name}}' }] }, action);
	}

	function pageCheckbox() {
		return document.querySelector('#dynamicTable .dynamic-data-table-select-page');
	}

	function rowCheckbox(index) {
		return bodyRows()[index].querySelector('.dynamic-data-table-select');
	}

	test('rows show their selection, readable in templates as @selected', function(assert) {
		init({ columns: [{ template: CHECKBOX }, { template: '{{@selected}}' }] }).render([
			{ key: '1', selected: true, data: { name: 'Ada' } },
			{ key: '2', data: { name: 'Linus' } }
		]);

		assert.ok(rowCheckbox(0).checked);
		assert.ok(bodyRows()[0].classList.contains('selected'));
		assert.equal(bodyRows()[0].cells[1].innerHTML, 'true');
		assert.notOk(rowCheckbox(1).checked);
		assert.notOk(bodyRows()[1].classList.contains('selected'));
		assert.equal(bodyRows()[1].cells[1].innerHTML, 'false');
	});

	test('a row checkbox sends select or deselect and keeps its state', function(assert) {
		const calls = [];
		initSelectable(function (action, key) {
			calls.push([action, key]);
		}).render(ROWS);

		rowCheckbox(1).click();
		assert.ok(rowCheckbox(1).checked, 'the click is not prevented');
		assert.ok(bodyRows()[1].classList.contains('selected'));

		rowCheckbox(1).click();
		assert.notOk(rowCheckbox(1).checked);
		assert.notOk(bodyRows()[1].classList.contains('selected'));

		assert.deepEqual(calls, [['_wicket-select', '2'], ['_wicket-deselect', '2']]);
	});

	test('the header checkbox shows whether all, some or none rows are selected', function(assert) {
		initSelectable().render([
			{ key: '1', selected: true, data: { name: 'Ada' } },
			{ key: '2', data: { name: 'Linus' } }
		]);
		assert.notOk(pageCheckbox().checked);
		assert.ok(pageCheckbox().indeterminate, 'some');

		rowCheckbox(1).click();
		assert.ok(pageCheckbox().checked);
		assert.notOk(pageCheckbox().indeterminate, 'all');

		rowCheckbox(0).click();
		rowCheckbox(1).click();
		assert.notOk(pageCheckbox().checked);
		assert.notOk(pageCheckbox().indeterminate, 'none');
	});

	test('the header checkbox of an empty page is unchecked', function(assert) {
		initSelectable().render([]);
		pageCheckbox().checked = true;

		table.updateSelection();

		assert.notOk(pageCheckbox().checked);
		assert.notOk(pageCheckbox().indeterminate);
	});

	test('the header checkbox selects and deselects the page', function(assert) {
		const calls = [];
		initSelectable(function (action, key) {
			calls.push([action, key]);
		}).render(ROWS);

		pageCheckbox().click();
		assert.ok(rowCheckbox(0).checked && rowCheckbox(1).checked);
		assert.ok(bodyRows()[0].classList.contains('selected'));

		pageCheckbox().click();
		assert.notOk(rowCheckbox(0).checked || rowCheckbox(1).checked);

		assert.deepEqual(calls, [['_wicket-select-page', null], ['_wicket-deselect-page', null]]);
	});

	test('a checkbox without an unchecked action sends nothing when unchecked', function(assert) {
		const calls = [];
		init({ columns: [{ template: '<input type="checkbox" data-dt-action="flag">' }] },
			function (action, key) {
				calls.push([action, key]);
			}).render(ROWS);

		bodyRows()[0].querySelector('input').click();
		bodyRows()[0].querySelector('input').click();

		assert.deepEqual(calls, [['flag', '1']]);
	});

	test('selectRows checks or unchecks every row', function(assert) {
		initSelectable().render(ROWS);

		table.selectRows(true);
		assert.ok(rowCheckbox(0).checked && rowCheckbox(1).checked);
		assert.ok(pageCheckbox().checked);

		table.selectRows(false);
		assert.notOk(rowCheckbox(0).checked || rowCheckbox(1).checked);
		assert.notOk(bodyRows()[1].classList.contains('selected'));
		assert.notOk(pageCheckbox().checked);
	});

	test('renderRow keeps the selection of the row', function(assert) {
		initSelectable().render([
			{ key: '1', data: { name: 'Ada' } },
			{ key: '2', selected: true, data: { name: 'Linus' } }
		]);

		table.renderRow('2', { name: 'Grace' });

		assert.ok(rowCheckbox(1).checked);
		assert.ok(bodyRows()[1].classList.contains('selected'));
		assert.equal(bodyRows()[1].cells[1].innerHTML, 'Grace');
	});

	test('update repaints the rows and records the paging, asking for no toolbars', function(assert) {
		let navigations = 0;
		const cfg = {
			id: 'dynamicTable',
			dataUrl: 'data:application/json,' + encodeURIComponent(JSON.stringify({ rows: [] })),
			engine: 'builtin',
			columns: [{ template: '{{name}}' }],
			push: false,
			itemCount: 2,
			currentPage: 0
		};
		table = new Wicket.DynamicDataTable(cfg, function () { }, function () {
			navigations++;
		});

		table.update({ rows: ROWS, itemCount: 7, currentPage: 3 });

		assert.equal(bodyRows().length, 2);
		assert.equal(bodyRows()[1].cells[0].innerHTML, 'Linus');
		assert.equal(table.itemCount, 7);
		assert.equal(table.currentPage, 3);
		assert.equal(navigations, 0);
	});

	test('resizable columns are fitted to the first rows, once', function(assert) {
		const fitted = [];
		const original = Wicket.ResizableColumns;
		Wicket.ResizableColumns = {
			attach: function () { },
			detach: function () { },
			fit: function (id) {
				fitted.push(id);
			}
		};
		try {
			init({ resize: { mode: 'stretch', action: '_wicket-resize' } });
			table.render([]);
			assert.deepEqual(fitted, [], 'not without rows');

			table.render(ROWS);
			table.render(ROWS);
			assert.deepEqual(fitted, ['dynamicTable']);
		} finally {
			Wicket.ResizableColumns = original;
		}
	});

	test('rows sent along with the table are rendered without fetching them', function(assert) {
		const cfg = {
			id: 'dynamicTable',
			dataUrl: 'data:application/json,' + encodeURIComponent(JSON.stringify({ rows: [] })),
			engine: 'builtin',
			columns: [{ template: '{{name}}' }],
			push: false
		};
		table = new Wicket.DynamicDataTable(cfg, function () { }, undefined, undefined,
			{ rows: ROWS, itemCount: 2, currentPage: 0 });

		assert.equal(bodyRows().length, 2, 'the rows are there at once');
		assert.equal(bodyRows()[1].cells[0].textContent, 'Linus');
	});

	test('columns with widths from the server are not fitted', function(assert) {
		const fitted = [];
		const original = Wicket.ResizableColumns;
		Wicket.ResizableColumns = {
			attach: function () { },
			detach: function () { },
			fit: function (id) {
				fitted.push(id);
			}
		};
		try {
			init({ resize: { mode: 'stretch', action: '_wicket-resize', widths: [10, 20] } });
			table.render(ROWS);
			assert.deepEqual(fitted, []);
		} finally {
			Wicket.ResizableColumns = original;
		}
	});

	test('a table not accepting pushes ignores them', function(assert) {
		const calls = [];
		init({ push: false }, undefined, {
			custom: function (message) {
				calls.push(message.value);
			}
		});

		Wicket.Event.publish('/websocket/message',
			JSON.stringify({ tableId: 'dynamicTable', typeId: 'custom', value: 42 }));

		assert.deepEqual(calls, []);
	});
	const MOVABLE_HEAD = '<tr class="dynamic-data-table-headers"><th>#</th>' +
		'<th data-dt-movable="true"><button class="dynamic-data-table-move">m</button>A</th>' +
		'<th data-dt-movable="true"><button class="dynamic-data-table-move">m</button>B</th></tr>';

	function withHead(html, run) {
		const head = document.getElementById('dynamicTable').tHead;
		const original = head.innerHTML;
		head.innerHTML = html;
		try {
			run();
		} finally {
			head.innerHTML = original;
		}
	}

	function initMovable(calls) {
		return init({
			move: '_wicket-move',
			columns: [{ template: '{{@number}}' }, { template: '{{name}}' }, { template: '{{key}}' }]
		}, function (action, key) {
			calls.push([action, key]);
		});
	}

	function headerCell(index) {
		return document.querySelector('#dynamicTable tr.dynamic-data-table-headers').cells[index];
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

	function dragColumn(from, startX, moves) {
		pointer(headerCell(from).querySelector('button'), 'pointerdown', startX);
		moves.forEach(function (x) {
			pointer(document, 'pointermove', x);
		});
	}

	function marker() {
		return document.querySelector('.dynamic-data-table-drop-marker');
	}

	function ghost() {
		return document.querySelector('.dynamic-data-table-move-ghost');
	}

	test('rows have one cell per column, also with movable columns', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			initMovable([]).render(ROWS);

			assert.equal(bodyRows()[0].cells.length, 3);
		});
	});

	test('a dragged column shows where it will land and is moved there on release', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			const calls = [];
			initMovable(calls).render(ROWS);
			layOut();

			dragColumn(2, 250, [240, 120]);

			assert.ok(ghost(), 'a ghost of the header follows the pointer');
			assert.equal(ghost().textContent, 'mB');
			assert.equal(marker().style.display, '', 'the marker is shown');
			assert.equal(parseFloat(marker().style.left), 100 + window.scrollX,
				'on the left edge of the column it goes in front of');
			assert.ok(headerCell(2).classList.contains('dynamic-data-table-moving'));
			assert.ok(bodyRows()[0].cells[2].classList.contains('dynamic-data-table-moving'));
			assert.deepEqual(calls, [], 'nothing moves while dragging');

			pointer(document, 'pointerup', 120);

			assert.deepEqual(calls, [['_wicket-move', '2,1']]);
			assert.equal(marker(), null, 'the marker is removed');
			assert.equal(ghost(), null, 'the ghost is removed');
			assert.notOk(headerCell(2).classList.contains('dynamic-data-table-moving'));
			assert.deepEqual(Wicket.DynamicDataTable.focusAfterMove, { id: 'dynamicTable', index: 1 });
			delete Wicket.DynamicDataTable.focusAfterMove;
		});
	});

	test('a column dropped on the right half of the last one goes to the end', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			const calls = [];
			initMovable(calls);
			layOut();

			dragColumn(1, 150, [290]);
			assert.equal(parseFloat(marker().style.left), 300 + window.scrollX,
				'the marker is on the right edge of the last column');
			pointer(document, 'pointerup', 290);

			assert.deepEqual(calls, [['_wicket-move', '1,3']]);
			delete Wicket.DynamicDataTable.focusAfterMove;
		});
	});

	test('no marker is shown where a column may not go, and it is not moved there', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			const calls = [];
			initMovable(calls);
			layOut();
			const table = document.getElementById('dynamicTable');

			dragColumn(1, 150, [20]);
			assert.equal(marker().style.display, 'none', 'not in front of a fixed column');
			assert.ok(table.classList.contains('dynamic-data-table-no-drop'));
			pointer(document, 'pointerup', 20);

			dragColumn(1, 150, [170]);
			assert.equal(marker().style.display, 'none', 'not in its own place');
			pointer(document, 'pointerup', 170);

			assert.deepEqual(calls, []);
			assert.notOk(table.classList.contains('dynamic-data-table-no-drop'));
		});
	});

	test('a short movement on the handle does not drag the column', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			const calls = [];
			initMovable(calls);
			layOut();

			dragColumn(2, 250, [248]);
			assert.equal(ghost(), null);
			pointer(document, 'pointerup', 248);

			assert.deepEqual(calls, []);
		});
	});

	test('Escape cancels dragging a column', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			const calls = [];
			initMovable(calls);
			layOut();

			dragColumn(2, 250, [120]);
			document.dispatchEvent(new window.KeyboardEvent('keydown',
				{ bubbles: true, cancelable: true, key: 'Escape' }));
			pointer(document, 'pointerup', 120);

			assert.deepEqual(calls, []);
			assert.equal(marker(), null);
		});
	});

	test('a move is valid when only movable columns change their places', function(assert) {
		withHead('<tr class="dynamic-data-table-headers"><th>#</th>' +
				'<th data-dt-movable="true">A</th><th data-dt-movable="true">B</th><th>C</th>' +
				'<th data-dt-movable="true">D</th></tr>', function () {
			const t = initMovable([]);

			assert.ok(t.isValidMove(1, 3), 'A behind B');
			assert.ok(t.isValidMove(2, 1), 'B in front of A');
			assert.notOk(t.isValidMove(1, 5), 'A to the end would shift C');
			assert.notOk(t.isValidMove(4, 1), 'D in front of A would shift C');
			assert.notOk(t.isValidMove(1, 0), 'A in front of #');
			assert.notOk(t.isValidMove(3, 1), 'C is not movable');
			assert.notOk(t.isValidMove(1, 2), 'A in its own place');
		});
	});

	test('the arrow keys on the handle move a column past a movable neighbour', function(assert) {
		withHead(MOVABLE_HEAD, function () {
			const calls = [];
			initMovable(calls);
			const handle = headerCell(1).querySelector('button');

			handle.dispatchEvent(new window.KeyboardEvent('keydown',
				{ bubbles: true, cancelable: true, key: 'ArrowLeft' }));
			handle.dispatchEvent(new window.KeyboardEvent('keydown',
				{ bubbles: true, cancelable: true, key: 'ArrowRight' }));

			assert.deepEqual(calls, [['_wicket-move', '1,3']]);
			assert.deepEqual(Wicket.DynamicDataTable.focusAfterMove, { id: 'dynamicTable', index: 2 });
			delete Wicket.DynamicDataTable.focusAfterMove;
		});
	});

	const CHOOSER_HEAD = '<tr><td><details class="dynamic-data-table-column-chooser" open>' +
		'<summary>Columns</summary><div><ul>' +
		'<li><button data-dt-column="0" aria-pressed="true">A</button></li>' +
		'<li><button data-dt-column="2" aria-pressed="false">B</button></li>' +
		'</ul><button data-dt-apply="true">Apply</button></div></details></td></tr>' +
		'<tr><th>Name</th><th>Actions</th></tr>';

	test('the column chooser toggles the ticks and applies the ticked columns', function(assert) {
		withHead(CHOOSER_HEAD, function () {
			const calls = [];
			init({ }, function (action, key) {
				calls.push([action, key]);
			});
			const chooser = document.querySelector('#dynamicTable details');
			const columns = chooser.querySelectorAll('[data-dt-column]');
			const apply = chooser.querySelector('[data-dt-apply]');

			columns[1].click();
			columns[0].click();
			assert.equal(columns[0].getAttribute('aria-pressed'), 'false');
			assert.equal(columns[1].getAttribute('aria-pressed'), 'true');
			assert.ok(chooser.open, 'toggling keeps the chooser open');
			apply.click();

			assert.deepEqual(calls, [['_wicket-show-columns', '2']]);
			assert.notOk(chooser.open);
		});
	});

	test('the column chooser cannot apply without a column and closes on a click elsewhere', function(assert) {
		withHead(CHOOSER_HEAD, function () {
			init();
			const chooser = document.querySelector('#dynamicTable details');

			chooser.querySelector('[data-dt-column="0"]').click();
			assert.ok(chooser.querySelector('[data-dt-apply]').disabled);

			document.body.click();
			assert.notOk(chooser.open);
		});
	});
});
