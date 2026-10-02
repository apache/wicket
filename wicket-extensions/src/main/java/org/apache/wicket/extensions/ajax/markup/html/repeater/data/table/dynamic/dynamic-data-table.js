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

	const MOVE_HANDLE = '.dynamic-data-table-move';

	const DRAGGING_CLASS = 'dynamic-data-table-dragging';

	const NO_DROP_CLASS = 'dynamic-data-table-no-drop';

	const MOVING_CLASS = 'dynamic-data-table-moving';

	const GHOST_CLASS = 'dynamic-data-table-move-ghost';

	const MARKER_CLASS = 'dynamic-data-table-drop-marker';

	/**
	 * How far, in pixels, the pointer has to travel on a column's handle before it drags the column.
	 */
	const DRAG_THRESHOLD = 4;

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
	 *            itemCount, currentPage, move}, move being the action reporting a moved column,
	 *            if columns can be moved
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
			this.attachMove();
			this.attachResize();
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
			this.table.tBodies[0].replaceChildren(fragment);
			this.updateSelection();
			if (!this.fitted && rows && rows.length > 0) {
				this.fitted = true;
				this.fitColumns();
			}
		},

		/**
		 * Lets resizable columns take the widths their content needs, unless the server sent
		 * widths the user chose before.
		 */
		fitColumns : function () {
			const resize = this.cfg.resize;
			if (resize && !resize.widths && Wicket.ResizableColumns &&
					Wicket.ResizableColumns.fit) {
				Wicket.ResizableColumns.fit(this.id);
			}
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
			if (tr && this.table.tBodies[0].contains(tr)) {
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
			this.table.tBodies[0].querySelectorAll(ROW_CHECKBOX).forEach(function (checkbox) {
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
			Array.prototype.forEach.call(this.table.tBodies[0].rows, function (tr) {
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
			if (this.onMoveKey(event)) {
				return;
			}
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
		 * @return the header cells of the columns
		 */
		columnHeaders : function () {
			const row = this.table.tHead && this.table.tHead.querySelector('tr.' + HEADERS_CLASS);
			if (!row) {
				return [];
			}
			return Array.prototype.filter.call(row.cells, function (cell) {
				return cell.tagName === 'TH';
			});
		},

		/**
		 * Lets the columns marked data-dt-movable be dragged by their handle, with the mouse, a
		 * pen or a finger, and dropped between two columns. While dragging, a marker shows where
		 * the column will land; positions that would move a column that is not movable show none.
		 * A drop is reported to the server as the action cfg.move with the index of the column and
		 * the index of the column it goes in front of, or the number of columns for the end,
		 * comma separated, as key.
		 */
		attachMove : function () {
			if (!this.cfg.move) {
				return;
			}
			this.movePointerDownHandler = Wicket.bind(this.onMovePointerDown, this);
			this.table.addEventListener('pointerdown', this.movePointerDownHandler);

			const focus = Wicket.DynamicDataTable.focusAfterMove;
			if (focus && focus.id === this.id) {
				delete Wicket.DynamicDataTable.focusAfterMove;
				const header = this.columnHeaders()[focus.index];
				const handle = header && header.querySelector(MOVE_HANDLE);
				if (handle) {
					handle.focus();
				}
			}
		},

		onMovePointerDown : function (event) {
			const handle = event.target.closest && event.target.closest(MOVE_HANDLE);
			const header = handle && handle.closest('th');
			const from = header ? this.columnHeaders().indexOf(header) : -1;
			if (from < 0 || (event.button !== undefined && event.button !== 0)) {
				return;
			}
			event.preventDefault();
			handle.focus();
			this.endMove(false);
			const self = this;
			const move = {
				from: from,
				header: header,
				startX: event.clientX,
				startY: event.clientY,
				started: false,
				to: -1
			};
			move.pointerMove = function (e) {
				self.dragMove(e.clientX, e.clientY);
			};
			move.pointerUp = function (e) {
				self.dragMove(e.clientX, e.clientY);
				self.endMove(true);
			};
			move.cancel = function () {
				self.endMove(false);
			};
			move.key = function (e) {
				if (e.key === 'Escape') {
					e.preventDefault();
					self.endMove(false);
				}
			};
			document.addEventListener('pointermove', move.pointerMove);
			document.addEventListener('pointerup', move.pointerUp);
			document.addEventListener('pointercancel', move.cancel);
			document.addEventListener('keydown', move.key);
			window.addEventListener('blur', move.cancel);
			this.moving = move;
		},

		/**
		 * Follows the pointer while a column is dragged: starts the drag once the pointer left the
		 * handle's threshold, moves the ghost and places the drop marker.
		 */
		dragMove : function (clientX, clientY) {
			const move = this.moving;
			if (!move) {
				return;
			}
			if (!move.started) {
				if (Math.abs(clientX - move.startX) < DRAG_THRESHOLD &&
						Math.abs(clientY - move.startY) < DRAG_THRESHOLD) {
					return;
				}
				this.startMove(move);
			}
			move.ghost.style.left = (clientX + window.scrollX + 12) + 'px';
			move.ghost.style.top = (clientY + window.scrollY + 12) + 'px';
			const to = this.dropPosition(move.from, clientX);
			move.to = to;
			this.table.classList.toggle(NO_DROP_CLASS, to < 0);
			this.placeMarker(move.marker, to);
		},

		startMove : function (move) {
			move.started = true;
			this.table.classList.add(DRAGGING_CLASS);
			this.columnCells(move.from).forEach(function (cell) {
				cell.classList.add(MOVING_CLASS);
			});
			move.ghost = document.createElement('div');
			move.ghost.className = GHOST_CLASS;
			move.ghost.textContent = move.header.textContent.trim();
			move.marker = document.createElement('div');
			move.marker.className = MARKER_CLASS;
			move.marker.style.display = 'none';
			document.body.appendChild(move.ghost);
			document.body.appendChild(move.marker);
			this.copyTheme(move.ghost);
			this.copyTheme(move.marker);
		},

		/**
		 * Copies the theme's colors to an element appended to the body, outside of the table.
		 */
		copyTheme : function (element) {
			const style = window.getComputedStyle(this.table);
			['--wicket-theme-primary', '--wicket-theme-on-primary', '--wicket-theme-accent']
				.forEach(function (name) {
					const value = style.getPropertyValue(name);
					if (value) {
						element.style.setProperty(name, value.trim());
					}
				});
		},

		/**
		 * @return the cells of the column at index, the header and those of the body
		 */
		columnCells : function (index) {
			const cells = [this.columnHeaders()[index]];
			Array.prototype.forEach.call(this.table.tBodies[0].rows, function (tr) {
				if (tr.cells[index]) {
					cells.push(tr.cells[index]);
				}
			});
			return cells.filter(Boolean);
		},

		/**
		 * @return the index of the column a column dragged from the index from goes in front of when
		 *         dropped with the pointer at clientX, the number of columns for the end, or -1
		 *         where it may not go: in its own place, or anywhere that moves a column that is
		 *         not movable
		 */
		dropPosition : function (from, clientX) {
			const headers = this.columnHeaders();
			let to = headers.length;
			for (let i = 0; i < headers.length; i++) {
				const rect = headers[i].getBoundingClientRect();
				if (clientX < rect.left + rect.width / 2) {
					to = i;
					break;
				}
			}
			return this.isValidMove(from, to) ? to : -1;
		},

		/**
		 * @return whether moving the column at from in front of the one at to, or to the end,
		 *         moves a movable column and leaves every other column in its place
		 */
		isValidMove : function (from, to) {
			const movable = this.columnHeaders().map(function (header) {
				return header.hasAttribute('data-dt-movable');
			});
			if (from < 0 || from >= movable.length || to < 0 || to > movable.length ||
					to === from || to === from + 1 || !movable[from]) {
				return false;
			}
			const order = movable.map(function (value, index) {
				return index;
			});
			order.splice(to > from ? to - 1 : to, 0, order.splice(from, 1)[0]);
			return order.every(function (column, index) {
				return movable[column] || column === index;
			});
		},

		/**
		 * Shows the drop marker on the left edge of the column at index, on the right edge of the
		 * last column for the end, or hides it for -1.
		 */
		placeMarker : function (marker, index) {
			const headers = this.columnHeaders();
			if (index < 0 || headers.length === 0) {
				marker.style.display = 'none';
				return;
			}
			const end = index >= headers.length;
			const rect = headers[end ? headers.length - 1 : index].getBoundingClientRect();
			const bodies = this.table.tBodies;
			const bottom = bodies.length > 0 && bodies[0].rows.length > 0 ?
				bodies[0].getBoundingClientRect().bottom : rect.bottom;
			marker.style.display = '';
			marker.style.left = ((end ? rect.right : rect.left) + window.scrollX) + 'px';
			marker.style.top = (rect.top + window.scrollY) + 'px';
			marker.style.height = Math.max(0, bottom - rect.top) + 'px';
		},

		/**
		 * Ends dragging a column, moving it to where the marker was if drop is true.
		 */
		endMove : function (drop) {
			const move = this.moving;
			if (!move) {
				return;
			}
			delete this.moving;
			document.removeEventListener('pointermove', move.pointerMove);
			document.removeEventListener('pointerup', move.pointerUp);
			document.removeEventListener('pointercancel', move.cancel);
			document.removeEventListener('keydown', move.key);
			window.removeEventListener('blur', move.cancel);
			if (!move.started) {
				return;
			}
			move.ghost.remove();
			move.marker.remove();
			this.table.classList.remove(DRAGGING_CLASS, NO_DROP_CLASS);
			this.table.querySelectorAll('.' + MOVING_CLASS).forEach(function (cell) {
				cell.classList.remove(MOVING_CLASS);
			});
			if (drop && move.to >= 0) {
				Wicket.DynamicDataTable.focusAfterMove = {
					id: this.id,
					index: move.to > move.from ? move.to - 1 : move.to
				};
				this.move(move.from, move.to);
			}
		},

		/**
		 * Moves the column whose handle has the focus one place to the left or right with the
		 * arrow keys.
		 *
		 * @return whether the key moved a column
		 */
		onMoveKey : function (event) {
			if (!this.cfg.move || (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') ||
					!event.target.matches || !event.target.matches(MOVE_HANDLE)) {
				return false;
			}
			event.preventDefault();
			const headers = this.columnHeaders();
			const from = headers.indexOf(event.target.closest('th'));
			const to = event.key === 'ArrowLeft' ? from - 1 : from + 2;
			const neighbour = headers[event.key === 'ArrowLeft' ? from - 1 : from + 1];
			if (from >= 0 && neighbour && neighbour.hasAttribute('data-dt-movable')) {
				Wicket.DynamicDataTable.focusAfterMove = {
					id: this.id,
					index: to > from ? to - 1 : to
				};
				this.move(from, to);
			}
			return true;
		},

		move : function (from, to) {
			if (from !== to && from + 1 !== to) {
				this.action(this.cfg.move, from + ',' + to);
			}
		},

		/**
		 * Makes the columns resizable if the table is configured so, reporting every resize to
		 * the server through the action callback, as the action cfg.resize.action with the
		 * widths, comma separated, as key.
		 */
		attachResize : function () {
			const resize = this.cfg.resize;
			if (!resize || !Wicket.ResizableColumns) {
				return;
			}
			const self = this;
			const layout = this.cfg.layout || { };
			Wicket.ResizableColumns.attach(this.id, {
				mode: resize.mode,
				widths: resize.widths,
				proportions: layout.proportions,
				adjustToParent: layout.adjustToParent,
				onResize: function (widths) {
					self.action(resize.action, widths.map(function (width) {
						return Math.round(width * 10) / 10;
					}).join(','));
				}
			});
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
				let colgroup = this.table.querySelector('colgroup.dynamic-data-table-colgroup');
				if (!colgroup) {
					colgroup = document.createElement('colgroup');
					colgroup.className = 'dynamic-data-table-colgroup';
					this.table.insertBefore(colgroup, this.table.firstChild);
				}
				colgroup.replaceChildren();
				proportions.forEach(function (proportion) {
					const col = document.createElement('col');
					col.style.width = (proportion / total * 100) + '%';
					colgroup.appendChild(col);
				});
				this.table.style.tableLayout = 'fixed';
			}
		},

		destroy : function () {
			if (this.cfg.resize && Wicket.ResizableColumns) {
				Wicket.ResizableColumns.detach(this.id);
			}
			if (this.timer) {
				window.clearInterval(this.timer);
			}
			this.table.removeEventListener('click', this.clickHandler);
			this.table.removeEventListener('keydown', this.keyHandler);
			document.removeEventListener('click', this.documentClickHandler);
			if (this.cfg.move) {
				this.endMove(false);
				this.table.removeEventListener('pointerdown', this.movePointerDownHandler);
			}
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
	 * not shown above or below the screen of a long table.
	 *
	 * @param id the id of the overlay's window
	 */
	Wicket.DynamicDataTable.placeOverlay = function (id) {
		const box = Wicket.$(id);
		const table = box && box.closest('table');
		if (!table) {
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
