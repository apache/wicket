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

/*
 * Client side of DynamicDataTable: renders the table body from JSON rows with per-column
 * templates, and sends clicks on [data-dt-action] elements to the table's Ajax behavior.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === "undefined") {
		window.Wicket = { };
	}

	if (typeof(Wicket.DynamicDataTable) !== "undefined") {
		return;
	}

	const HTML_ESCAPES = {
		'&': '&amp;',
		'<': '&lt;',
		'>': '&gt;',
		'"': '&quot;',
		"'": '&#39;'
	};

	const escapeHtml = function (value) {
		return String(value).replace(/[&<>"']/g, function (c) {
			return HTML_ESCAPES[c];
		});
	};

	const ROW_CHECKBOX = 'input.dynamic-data-table-select';

	const PAGE_CHECKBOX = 'input.dynamic-data-table-select-page';

	const OVERLAY_CLASS = 'dynamic-data-table-overlay';

	const CLOSE_OVERLAY_ACTION = '_wicket-close-overlay';

	const SHOW_COLUMNS_ACTION = '_wicket-show-columns';

	const HEADERS_CLASS = 'dynamic-data-table-headers';

	const FILLER_CLASS = 'dynamic-data-table-filler';

	const FILLER_WIDTH = '3em';

	const BODY_ROW_CLASS = 'dynamic-data-table-body-row';

	const BODY_CELL_CLASS = 'dynamic-data-table-body-cell';

	const BODY_CLASS = 'dynamic-data-table-body';

	const ROWS_CLASS = 'dynamic-data-table-rows';

	const COLGROUP_CLASS = 'dynamic-data-table-colgroup';

	const CHOOSER = '.dynamic-data-table-column-chooser';

	const lookup = function (data, path) {
		return path.split('.').reduce(function (value, name) {
			return (value === null || value === undefined) ? undefined : value[name];
		}, data);
	};

	const builtinEngine = {
		compile: function (template) {
			const placeholder = /\{\{\{\s*(@?[\w$.]+)\s*\}\}\}|\{\{\s*(@?[\w$.]+)\s*\}\}/g;
			const parts = [];
			let last = 0;
			let match;
			while ((match = placeholder.exec(template)) !== null) {
				parts.push(template.substring(last, match.index));
				parts.push({ path: match[1] || match[2], raw: match[1] !== undefined });
				last = placeholder.lastIndex;
			}
			parts.push(template.substring(last));

			return function (data, meta) {
				return parts.map(function (part) {
					if (typeof part === 'string') {
						return part;
					}
					const value = part.path.charAt(0) === '@' ?
						lookup(meta, part.path.substring(1)) : lookup(data, part.path);
					if (value === null || value === undefined) {
						return '';
					}
					return part.raw ? String(value) : escapeHtml(value);
				}).join('');
			};
		}
	};

	/**
	 * A table whose body is rendered from JSON rows.
	 *
	 * @param cfg {id, dataUrl, engine, columns: [{template, cssClass}], push, pollMillis,
	 *            itemCount, currentPage, filler, bodyHeight, resize, layout}, filler being
	 *            whether the rows end with an empty cell, bodyHeight {value, unit} the height of
	 *            the body, if it has one, resize {widths} present if a ResizableColumnsBehavior
	 *            resizes the columns, and layout {proportions, adjustToParent}
	 * @param action function(action, key) calling the table's Ajax behavior
	 * @param navigation function() re-rendering the table's navigation via Ajax
	 * @param messages the handlers of the messages pushed to the table, by type id, each called
	 *            as function(message)
	 * @param data {rows, itemCount, currentPage}, the rows of the current page when the server
	 *            sends them along, for example when it re-renders the table; fetched otherwise
	 */
	Wicket.DynamicDataTable = Wicket.Class.create();
	Wicket.DynamicDataTable.prototype = {

		initialize : function (cfg, action, navigation, messages, data) {
			const previous = Wicket.DynamicDataTable.instances[cfg.id];
			if (previous) {
				previous.destroy();
			}

			this.id = cfg.id;
			this.cfg = cfg;
			this.action = action;
			this.navigation = navigation;
			this.messages = messages || { };
			this.itemCount = cfg.itemCount;
			this.currentPage = cfg.currentPage;
			this.table = Wicket.$(cfg.id);
			if (!this.table) {
				return;
			}

			const engine = Wicket.DynamicDataTable.engines[cfg.engine];
			if (!engine) {
				Wicket.Log.error('Wicket.DynamicDataTable: no template engine registered as "' +
					cfg.engine + '"');
				return;
			}
			this.cells = cfg.columns.map(function (column) {
				return engine.compile(column.template);
			});

			this.clickHandler = Wicket.bind(this.onClick, this);
			this.table.addEventListener('click', this.clickHandler);
			this.keyHandler = Wicket.bind(this.onKeyDown, this);
			this.table.addEventListener('keydown', this.keyHandler);
			this.documentClickHandler = Wicket.bind(this.closeChoosers, this);
			document.addEventListener('click', this.documentClickHandler);
			Wicket.DynamicDataTable.instances[this.id] = this;
			this.applyBodyHeight();
			this.shareLayout();
			this.applyLayout();

			if (data) {
				this.update(data);
			} else {
				this.load();
			}

			if (cfg.pollMillis) {
				this.timer = window.setInterval(Wicket.bind(this.poll, this), cfg.pollMillis);
			}
		},

		/**
		 * Fetches the rows from the table's data URL and renders them.
		 */
		load : function () {
			const self = this;
			window.fetch(this.cfg.dataUrl, {
				credentials: 'same-origin',
				headers: { 'Accept': 'application/json' }
			}).then(function (response) {
				if (!response.ok) {
					throw new Error('HTTP ' + response.status);
				}
				return response.json();
			}).then(function (json) {
				if (Wicket.DynamicDataTable.instances[self.id] === self) {
					self.render(json.rows);
					self.updatePaging(json.itemCount, json.currentPage);
				}
			}).catch(function (error) {
				Wicket.Log.error('Wicket.DynamicDataTable: loading the rows of #' + self.id +
					' failed', error);
			});
		},

		/**
		 * Replaces the rows of the table with the rows of a page the server sent along with an
		 * Ajax response, whose toolbars match its paging state already.
		 *
		 * @param data {rows, itemCount, currentPage}
		 */
		update : function (data) {
			this.setPaging(data.itemCount, data.currentPage);
			this.render(data.rows);
		},

		/**
		 * Replaces the rows of the table.
		 *
		 * @param rows [{key, number, selected, data}], number being the 1-based position of the
		 *            row across all pages; without it the position in the given rows is used
		 */
		render : function (rows) {
			const self = this;
			const fragment = document.createDocumentFragment();
			(rows || []).forEach(function (row, index) {
				const number = row.number !== undefined ? row.number : index + 1;
				fragment.appendChild(self.buildRow(row.key, number, row.data, row.selected));
			});
			this.rowsBody().replaceChildren(fragment);
			this.updateSelection();
			this.layoutBody();
			if (!this.fitted && rows && rows.length > 0) {
				this.fitted = true;
				this.fitColumns();
			}
		},

		/**
		 * @return the body holding the rows: the table's own, or that of the table in the
		 *         scrolling body when the table has a body height
		 */
		rowsBody : function () {
			return this.rowsTable ? this.rowsTable.tBodies[0] : this.table.tBodies[0];
		},

		/**
		 * Gives the body the height cfg.bodyHeight. The rows go into a table of their own, in an
		 * element of that height in the table's body, which scrolls them vertically; its columns
		 * take the widths of the header cells. A table wider than its space is as wide as its
		 * columns, as any table, and the page scrolls sideways.
		 */
		applyBodyHeight : function () {
			const height = this.cfg.bodyHeight;
			if (!height) {
				return;
			}
			const table = this.table;
			const rows = document.createElement('table');
			rows.className = table.className + ' ' + ROWS_CLASS;
			rows.setAttribute('role', 'presentation');
			rows.appendChild(document.createElement('colgroup'));
			rows.appendChild(document.createElement('tbody'));
			const body = document.createElement('div');
			body.className = BODY_CLASS;
			body.appendChild(rows);
			const cell = document.createElement('td');
			cell.className = BODY_CELL_CLASS;
			cell.appendChild(body);
			const host = document.createElement('tr');
			host.className = BODY_ROW_CLASS;
			host.appendChild(cell);
			table.tBodies[0].replaceChildren(host);
			this.rowsTable = rows;
			this.body = body;

			const self = this;
			if (window.ResizeObserver) {
				this.bodyObserver = new window.ResizeObserver(function () {
					self.layoutBody();
				});
				this.bodyObserver.observe(table);
				this.headerCells().forEach(function (header) {
					self.bodyObserver.observe(header);
				});
			}
			this.layoutBody();
		},

		/**
		 * @return all cells of the header row, the empty one at the end too
		 */
		headerCells : function () {
			const row = this.table.tHead && this.table.tHead.querySelector('tr.' + HEADERS_CLASS);
			return row ? Array.prototype.slice.call(row.cells) : [];
		},

		/**
		 * Lays the scrolling body out: as high as cfg.bodyHeight and its columns as wide as the
		 * header cells above them. The empty column at the end leaves the room the vertical scroll
		 * bar takes.
		 */
		layoutBody : function () {
			const body = this.body;
			if (!body || !this.table.isConnected) {
				return;
			}
			this.copyClasses();
			const height = this.cfg.bodyHeight;
			body.style.height = height.unit === '%' ? this.percentOfParent(height.value) :
				height.value + height.unit;

			const headers = this.headerCells();
			body.parentElement.colSpan = Math.max(1, headers.length);
			const widths = headers.map(function (header) {
				return header.getBoundingClientRect().width;
			});
			const scrollBar = body.offsetWidth - body.clientWidth;
			if (this.cfg.filler && widths.length > 0) {
				widths[widths.length - 1] = Math.max(0, widths[widths.length - 1] - scrollBar);
			}
			const colgroup = this.rowsTable.querySelector('colgroup');
			while (colgroup.children.length < widths.length) {
				colgroup.appendChild(document.createElement('col'));
			}
			while (colgroup.children.length > widths.length) {
				colgroup.lastChild.remove();
			}
			widths.forEach(function (width, index) {
				colgroup.children[index].style.width = width + 'px';
			});
			const style = window.getComputedStyle(this.table);
			const spacing = style.borderCollapse === 'collapse' ? 0 :
				parseFloat(style.borderSpacing) || 0;
			this.rowsTable.style.borderSpacing = style.borderSpacing;
			this.rowsTable.style.marginLeft = -spacing + 'px';
			this.rowsTable.style.width = widths.reduce(function (sum, width) {
				return sum + width;
			}, 0) + spacing * (widths.length + 1) + 'px';
		},

		/**
		 * Gives the table of the rows the CSS classes of the table, which scripts such as that of
		 * a ResizableColumnsBehavior add after the rows were put into it, so the same rules apply.
		 */
		copyClasses : function () {
			const classes = this.table.className + ' ' + ROWS_CLASS;
			if (this.rowsTable.className !== classes) {
				this.rowsTable.className = classes;
			}
		},

		/**
		 * @return the given percent of the height of the element the table is in, if that
		 *         element has a height of its own, which does not depend on the table; else
		 *         nothing, the body then taking the height of its rows
		 */
		percentOfParent : function (percent) {
			const parent = this.table.parentElement;
			if (!parent) {
				return '';
			}
			const height = parent.clientHeight;
			this.table.style.display = 'none';
			const without = parent.clientHeight;
			this.table.style.display = '';
			return without === height && height > 0 ? (height * percent / 100) + 'px' : '';
		},

		/**
		 * Measures the widths the columns take by their content when the rows are in a table of
		 * their own: that table is laid out by the browser with a copy of the header cells above
		 * the rows, as wide as its content, or at most as wide as the parent of the table when the
		 * table takes the width of its parent.
		 *
		 * @param cells the header cells to measure
		 * @return the width of each, in pixels
		 */
		measureColumns : function (cells) {
			const headers = this.headerCells();
			const rows = this.rowsTable;
			this.copyClasses();
			const cols = Array.prototype.slice.call(rows.querySelectorAll('col'));
			const saved = cols.map(function (col) {
				return col.style.width;
			});
			const width = rows.style.width;
			cols.forEach(function (col) {
				col.style.width = '';
			});
			const parent = this.table.parentElement;
			this.body.style.width = (parent ? parent.clientWidth : 0) + 'px';
			rows.style.width = (this.cfg.layout || { }).adjustToParent ? '' : 'max-content';
			rows.style.tableLayout = 'auto';
			const head = rows.createTHead();
			const copies = head.insertRow();
			headers.forEach(function (header) {
				const copy = header.cloneNode(true);
				copy.removeAttribute('id');
				copy.querySelectorAll('[id]').forEach(function (element) {
					element.removeAttribute('id');
				});
				copies.appendChild(copy);
			});
			const result = cells.map(function (cell) {
				const copy = copies.cells[headers.indexOf(cell)];
				return copy ? copy.getBoundingClientRect().width : cell.getBoundingClientRect().width;
			});
			head.remove();
			rows.style.tableLayout = '';
			rows.style.width = width;
			this.body.style.width = '';
			cols.forEach(function (col, index) {
				col.style.width = saved[index];
			});
			return result;
		},

		/**
		 * Lets resizable columns take the widths their content needs, unless the server sent
		 * widths the user chose before.
		 */
		fitColumns : function () {
			const resize = this.cfg.resize;
			const resizable = resize && Wicket.ResizableColumns &&
				Wicket.ResizableColumns.instances[this.id];
			if (resizable && resizable.table === this.table && !resize.widths) {
				Wicket.ResizableColumns.fit(this.id);
			} else if (!resize) {
				this.fitRows();
			}
			this.layoutBody();
		},

		/**
		 * Sizes the columns of a table whose rows are in a table of their own by the content of
		 * the header cells and the rows, which the header row alone does not tell, unless the
		 * columns have proportions.
		 */
		fitRows : function () {
			const layout = this.cfg.layout || { };
			if (!this.rowsTable || layout.proportions) {
				return;
			}
			let colgroup = this.table.querySelector('colgroup.' + COLGROUP_CLASS);
			if (colgroup) {
				colgroup.remove();
			}
			this.table.style.tableLayout = 'auto';
			this.table.style.width = '';
			const headers = this.headerCells();
			const widths = this.measureColumns(headers);
			const total = widths.reduce(function (sum, width) {
				return sum + width;
			}, 0);
			colgroup = document.createElement('colgroup');
			colgroup.className = COLGROUP_CLASS;
			widths.forEach(function (width) {
				const col = document.createElement('col');
				col.style.width = layout.adjustToParent ? (width / total * 100) + '%' : width + 'px';
				colgroup.appendChild(col);
			});
			this.table.insertBefore(colgroup, this.table.firstChild);
			this.table.style.tableLayout = 'fixed';
			this.table.style.width = layout.adjustToParent ? '100%' : total + 'px';
		},

		/**
		 * Repaints one row in place, keeping its number and selection. A row the table does not
		 * show is ignored.
		 *
		 * @param key the key of the row
		 * @param data the row's data
		 */
		renderRow : function (key, data) {
			const tr = document.getElementById(this.rowId(key));
			if (tr && this.rowsBody().contains(tr)) {
				const number = Number(tr.getAttribute('data-number'));
				tr.replaceWith(this.buildRow(key, number, data, tr.classList.contains('selected')));
			}
		},

		rowId : function (key) {
			return this.id + '-row-' + key;
		},

		/**
		 * Builds the element of a row. The column templates are evaluated as cell(data, meta),
		 * meta holding the row's key, number and selection, for example for {{@number}}.
		 */
		buildRow : function (key, number, data, selected) {
			const self = this;
			const meta = { key: key, number: number, selected: !!selected };
			const tr = document.createElement('tr');
			tr.id = this.rowId(key);
			tr.setAttribute('data-key', key);
			tr.setAttribute('data-number', number);
			tr.className = number % 2 === 1 ? 'even' : 'odd';
			this.cells.forEach(function (cell, index) {
				const td = document.createElement('td');
				const cssClass = self.cfg.columns[index].cssClass;
				if (cssClass) {
					td.className = cssClass;
				}
				td.innerHTML = cell(data, meta);
				tr.appendChild(td);
			});
			if (this.cfg.filler) {
				const filler = document.createElement('td');
				filler.className = FILLER_CLASS;
				filler.setAttribute('aria-hidden', 'true');
				tr.appendChild(filler);
			}
			tr.querySelectorAll(ROW_CHECKBOX).forEach(function (checkbox) {
				checkbox.checked = meta.selected;
			});
			tr.classList.toggle('selected', meta.selected);
			return tr;
		},

		/**
		 * Checks or unchecks the selection checkboxes of all rows.
		 *
		 * @param selected whether the rows are selected
		 */
		selectRows : function (selected) {
			this.rowsBody().querySelectorAll(ROW_CHECKBOX).forEach(function (checkbox) {
				checkbox.checked = selected;
			});
			this.updateSelection();
		},

		/**
		 * Marks the rows whose selection checkbox is checked as selected, and shows in the header
		 * checkbox whether all, some or none of them are.
		 */
		updateSelection : function () {
			let count = 0;
			let checked = 0;
			Array.prototype.forEach.call(this.rowsBody().rows, function (tr) {
				const checkbox = tr.querySelector(ROW_CHECKBOX);
				if (checkbox) {
					count++;
					checked += checkbox.checked ? 1 : 0;
				}
				tr.classList.toggle('selected', !!checkbox && checkbox.checked);
			});
			const header = this.table.tHead && this.table.tHead.querySelector(PAGE_CHECKBOX);
			if (header) {
				header.checked = count > 0 && checked === count;
				header.indeterminate = checked > 0 && checked < count;
			}
		},

		/**
		 * Re-renders the navigation if the number of rows or the current page changed.
		 */
		updatePaging : function (itemCount, currentPage) {
			if (itemCount === undefined) {
				return;
			}
			const changed = itemCount !== this.itemCount || currentPage !== this.currentPage;
			this.setPaging(itemCount, currentPage);
			if (changed && this.navigation) {
				this.navigation();
			}
		},

		/**
		 * Records the paging state the navigation was rendered with.
		 */
		setPaging : function (itemCount, currentPage) {
			this.itemCount = itemCount;
			this.currentPage = currentPage;
		},

		poll : function () {
			if (Wicket.$(this.id) !== this.table) {
				this.destroy();
			} else {
				this.load();
			}
		},

		/**
		 * Sends the action of a clicked [data-dt-action] element. A checkbox keeps its new state
		 * and sends its data-dt-action-unchecked action, if any, when it was unchecked.
		 */
		onClick : function (event) {
			if (this.onChooserClick(event)) {
				return;
			}
			const actionElement = event.target.closest('[data-dt-action]');
			if (!actionElement || !this.table.contains(actionElement)) {
				return;
			}
			let action = actionElement.getAttribute('data-dt-action');
			if (actionElement.type === 'checkbox') {
				if (!actionElement.checked) {
					action = actionElement.getAttribute('data-dt-action-unchecked');
				}
				if (actionElement.matches(PAGE_CHECKBOX)) {
					this.selectRows(actionElement.checked);
				} else {
					this.updateSelection();
				}
				if (!action) {
					return;
				}
			} else {
				event.preventDefault();
			}
			const row = actionElement.closest('tr[data-key]');
			this.action(action, row ? row.getAttribute('data-key') : null);
		},

		/**
		 * Asks the server to close the overlay when Escape is pressed in it, unless the user
		 * changed something in it.
		 */
		onKeyDown : function (event) {
			const chooser = event.key === 'Escape' && event.target.closest &&
				event.target.closest(CHOOSER);
			if (chooser && chooser.open) {
				event.preventDefault();
				chooser.open = false;
				chooser.querySelector('summary').focus();
				return;
			}
			const overlay = event.key === 'Escape' && event.target.closest &&
				event.target.closest('.' + OVERLAY_CLASS);
			if (overlay) {
				event.preventDefault();
				if (!Wicket.DynamicDataTable.isChanged(overlay)) {
					this.action(CLOSE_OVERLAY_ACTION, null);
				}
			}
		},

		/**
		 * Handles a click in a column chooser: a column's button toggles whether it is shown, the
		 * apply button sends the shown columns, as SHOW_COLUMNS_ACTION with the values of their
		 * data-dt-column attributes, comma separated, as key.
		 *
		 * @return whether the click was in a column chooser's menu
		 */
		onChooserClick : function (event) {
			const chooser = event.target.closest(CHOOSER);
			if (!chooser || !this.table.contains(chooser)) {
				return false;
			}
			const column = event.target.closest('[data-dt-column]');
			if (column) {
				column.setAttribute('aria-pressed',
					String(column.getAttribute('aria-pressed') !== 'true'));
				this.updateChooser(chooser);
				return true;
			}
			if (event.target.closest('[data-dt-apply]')) {
				const shown = Array.prototype.filter.call(
					chooser.querySelectorAll('[data-dt-column]'), function (button) {
						return button.getAttribute('aria-pressed') === 'true';
					}).map(function (button) {
						return button.getAttribute('data-dt-column');
					});
				chooser.open = false;
				this.action(SHOW_COLUMNS_ACTION, shown.join(','));
				return true;
			}
			return false;
		},

		/**
		 * Disables the apply button of a column chooser while no column is chosen.
		 */
		updateChooser : function (chooser) {
			const apply = chooser.querySelector('[data-dt-apply]');
			if (apply) {
				apply.disabled = !chooser.querySelector('[data-dt-column][aria-pressed="true"]');
			}
		},

		/**
		 * Closes the open column choosers of the table a click went past.
		 */
		closeChoosers : function (event) {
			this.table.querySelectorAll(CHOOSER + '[open]').forEach(function (chooser) {
				if (!chooser.contains(event.target)) {
					chooser.open = false;
				}
			});
		},

		/**
		 * Hands the layout of the columns to a ResizableColumnsBehavior of the table, which
		 * attaches after the table: the proportions, whether the table takes the width of its
		 * parent, and how to measure the columns when the rows are in a table of their own.
		 */
		shareLayout : function () {
			if (!Wicket.ResizableColumns) {
				return;
			}
			const layout = this.cfg.layout || { };
			Wicket.ResizableColumns.tableOptions[this.id] = {
				proportions: layout.proportions,
				adjustToParent: layout.adjustToParent,
				measure: this.rowsTable ? Wicket.bind(this.measureColumns, this) : undefined
			};
		},

		/**
		 * Lays out the columns of a table whose columns cannot be resized: by their proportions,
		 * if any, and over the full width of the parent if the table adjusts to it.
		 */
		applyLayout : function () {
			const layout = this.cfg.layout;
			if (!layout || this.cfg.resize) {
				return;
			}
			if (layout.adjustToParent) {
				this.table.style.width = '100%';
			}
			const proportions = layout.proportions;
			if (Array.isArray(proportions) && proportions.length > 0) {
				const total = proportions.reduce(function (sum, proportion) {
					return sum + proportion;
				}, 0);
				let colgroup = this.table.querySelector('colgroup.' + COLGROUP_CLASS);
				if (!colgroup) {
					colgroup = document.createElement('colgroup');
					colgroup.className = COLGROUP_CLASS;
					this.table.insertBefore(colgroup, this.table.firstChild);
				}
				colgroup.replaceChildren();
				const filler = this.cfg.filler;
				proportions.forEach(function (proportion) {
					const col = document.createElement('col');
					const share = proportion / total;
					col.style.width = filler ? 'calc((100% - ' + FILLER_WIDTH + ') * ' + share + ')' :
						(share * 100) + '%';
					colgroup.appendChild(col);
				});
				if (filler) {
					const col = document.createElement('col');
					col.style.width = FILLER_WIDTH;
					colgroup.appendChild(col);
				}
				this.table.style.tableLayout = 'fixed';
			}
		},

		destroy : function () {
			if (this.bodyObserver) {
				this.bodyObserver.disconnect();
			}
			if (this.cfg.resize && Wicket.ResizableColumns) {
				Wicket.ResizableColumns.detach(this.id);
			}
			if (this.timer) {
				window.clearInterval(this.timer);
			}
			this.table.removeEventListener('click', this.clickHandler);
			this.table.removeEventListener('keydown', this.keyHandler);
			document.removeEventListener('click', this.documentClickHandler);
			const instances = Wicket.DynamicDataTable.instances;
			if (instances[this.id] === this) {
				delete instances[this.id];
			}
		}
	};

	Wicket.DynamicDataTable.escapeHtml = escapeHtml;

	/**
	 * Tells whether the user changed a form field inside an element, or an element in it carries
	 * the attribute data-dt-changed, for example a form the server rendered again with errors.
	 *
	 * @param element the element to look into
	 * @return whether something in it was changed
	 */
	Wicket.DynamicDataTable.isChanged = function (element) {
		if (element.querySelector('[data-dt-changed]')) {
			return true;
		}
		return Array.prototype.some.call(element.querySelectorAll('input, select, textarea'),
			function (field) {
				if (field.type === 'checkbox' || field.type === 'radio') {
					return field.checked !== field.defaultChecked;
				}
				if (field.tagName === 'SELECT') {
					const options = Array.prototype.slice.call(field.options);
					const preset = options.some(function (option) {
						return option.defaultSelected;
					});
					return options.some(function (option, index) {
						const initial = option.defaultSelected ||
							(!preset && !field.multiple && index === 0);
						return option.selected !== initial;
					});
				}
				return field.value !== field.defaultValue;
			});
	};

	/**
	 * Moves the window of a table's overlay into the part of the table the user sees, so it is
	 * not shown above or below the screen of a long table. An overlay over the whole page centers
	 * its window by CSS.
	 *
	 * @param id the id of the overlay's window
	 */
	Wicket.DynamicDataTable.placeOverlay = function (id) {
		const box = Wicket.$(id);
		const table = box && box.closest('table');
		if (!table || box.closest('.' + OVERLAY_CLASS + '-body')) {
			return;
		}
		const margin = 16;
		const tableTop = table.getBoundingClientRect().top;
		const highest = Math.max(margin, table.offsetHeight - box.offsetHeight - margin);
		box.style.top = Math.min(Math.max(margin, margin - tableTop), highest) + 'px';
	};

	/**
	 * The live tables, by the markup id of their table element.
	 */
	Wicket.DynamicDataTable.instances = { };

	/**
	 * The template engines, by the name a table refers to them by. An engine is an object whose
	 * compile(template) returns function(data) -> markup.
	 */
	Wicket.DynamicDataTable.engines = {
		builtin : builtinEngine
	};

	Wicket.Event.subscribe(Wicket.Event.Topic.DOM_NODE_REMOVING, function (jqEvent, element) {
		const instances = Wicket.DynamicDataTable.instances;
		Object.keys(instances).forEach(function (id) {
			const table = instances[id].table;
			if (element === table || (element.contains && element.contains(table))) {
				instances[id].destroy();
			}
		});
	});

	Wicket.Event.subscribe('/websocket/message', function (jqEvent, message) {
		if (typeof message !== 'string' || message.charAt(0) !== '{') {
			return;
		}
		let parsed;
		try {
			parsed = JSON.parse(message);
		} catch (e) {
			return;
		}
		const instance = Wicket.DynamicDataTable.instances[parsed.tableId];
		if (!instance || !instance.cfg.push) {
			return;
		}
		const handler = instance.messages[parsed.typeId];
		if (handler) {
			handler(parsed);
		} else {
			Wicket.Log.error('Wicket.DynamicDataTable: no handler for messages of type "' +
				parsed.typeId + '"');
		}
	});
})();
