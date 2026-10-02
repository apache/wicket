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
 * Client side of MovableColumnsBehavior: lets the user move the columns of a table whose header
 * cells carry data-movable, by dragging the handle in their header with the mouse, a pen or a
 * finger, or with the arrow keys on that handle. While dragging, a marker shows where the column
 * will land; positions that would move a column that is not movable show none. The move itself is
 * left to the server, which renders the table again.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === "undefined") {
		window.Wicket = { };
	}

	if (typeof(Wicket.MovableColumns) !== "undefined") {
		return;
	}

	const HANDLE_CLASS = 'wicket-col-mover';
	const DRAGGING_CLASS = 'wicket-col-dragging';
	const NO_DROP_CLASS = 'wicket-col-no-drop';
	const MOVING_CLASS = 'wicket-col-moving';
	const GHOST_CLASS = 'wicket-col-move-ghost';
	const MARKER_CLASS = 'wicket-col-drop-marker';

	/**
	 * How far, in pixels, the pointer has to travel on a column's handle before it drags the column.
	 */
	const DRAG_THRESHOLD = 4;

	const GRIP = '<svg aria-hidden="true" focusable="false" viewBox="0 0 10 16" width="8" ' +
		'height="13"><path fill="currentColor" d="M2 1h2v2H2zm4 0h2v2H6zM2 7h2v2H2zm4 0h2v2H6zm-4 ' +
		'6h2v2H2zm4 0h2v2H6z"/></svg>';

	const MC = Wicket.MovableColumns = {

		/**
		 * The tables whose columns can be moved, by id.
		 */
		instances: { },

		/**
		 * Where to put the focus after the table was rendered again for a move: {id, index}.
		 */
		focusAfterMove: undefined,

		/**
		 * @return whether moving the column at from in front of the one at to, or to the end for
		 *         the number of columns, moves a movable column and leaves every other column in
		 *         its place
		 */
		isValidMove : function (movable, from, to) {
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

		detach : function (id) {
			const instance = MC.instances[id];
			if (instance) {
				instance.destroy();
				delete MC.instances[id];
			}
		}
	};

	function Instance(table, options) {
		this.table = table;
		this.id = table.id;
		this.onMove = options.onMove;
		this.label = options.label || 'Move {0}';
		if (Array.isArray(options.movable)) {
			const cells = this.headerCells();
			options.movable.forEach(function (index) {
				if (cells[index]) {
					cells[index].setAttribute('data-movable', 'true');
				}
			});
		}
		this.ensureHandles();

		this.onPointerDown = this.pointerDown.bind(this);
		this.onKeyDown = this.keyDown.bind(this);
		this.table.addEventListener('pointerdown', this.onPointerDown);
		this.table.addEventListener('keydown', this.onKeyDown);

		const focus = MC.focusAfterMove;
		if (focus && focus.id === this.id) {
			MC.focusAfterMove = undefined;
			const header = this.headerCells()[focus.index];
			const handle = header && header.querySelector('.' + HANDLE_CLASS);
			if (handle) {
				handle.focus();
			}
		}
	}

	Instance.prototype = {

		/**
		 * @return the th cells of the header row, the last row of the head holding th cells,
		 *         except those carrying data-resizable-skip, which are no columns of their own
		 */
		headerCells : function () {
			const rows = this.table.tHead ? this.table.tHead.rows : [];
			for (let i = rows.length - 1; i >= 0; i--) {
				const cells = Array.prototype.filter.call(rows[i].cells, function (cell) {
					return cell.tagName === 'TH';
				});
				if (cells.length > 0) {
					return cells.filter(function (cell) {
						return !cell.hasAttribute('data-resizable-skip');
					});
				}
			}
			return [];
		},

		movable : function () {
			return this.headerCells().map(function (cell) {
				return cell.getAttribute('data-movable') === 'true';
			});
		},

		/**
		 * Puts a handle into every movable header cell lacking one, at its start.
		 */
		ensureHandles : function () {
			const self = this;
			this.headerCells().forEach(function (cell) {
				if (cell.getAttribute('data-movable') !== 'true' ||
						cell.querySelector('.' + HANDLE_CLASS)) {
					return;
				}
				const handle = document.createElement('button');
				handle.type = 'button';
				handle.className = HANDLE_CLASS;
				const label = self.label.replace('{0}', cell.textContent.trim());
				handle.title = label;
				handle.setAttribute('aria-label', label);
				handle.innerHTML = GRIP;
				cell.insertBefore(handle, cell.firstChild);
			});
		},

		pointerDown : function (event) {
			const handle = event.target.closest && event.target.closest('.' + HANDLE_CLASS);
			const header = handle && handle.closest('th');
			const from = header ? this.headerCells().indexOf(header) : -1;
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
		 * @return the cells of the column at index: its header cell and the cells of the rows,
		 *         also of rows in a table nested in the body
		 */
		columnCells : function (index) {
			const cells = [this.headerCells()[index]];
			this.table.querySelectorAll('tbody > tr').forEach(function (tr) {
				if (tr.cells.length > 1 && tr.cells[index]) {
					cells.push(tr.cells[index]);
				}
			});
			return cells.filter(Boolean);
		},

		/**
		 * @return the index of the column a column dragged from the index from goes in front of when
		 *         dropped with the pointer at clientX, the number of columns for the end, or -1
		 *         where it may not go
		 */
		dropPosition : function (from, clientX) {
			const headers = this.headerCells();
			let to = headers.length;
			for (let i = 0; i < headers.length; i++) {
				const rect = headers[i].getBoundingClientRect();
				if (clientX < rect.left + rect.width / 2) {
					to = i;
					break;
				}
			}
			return MC.isValidMove(this.movable(), from, to) ? to : -1;
		},

		/**
		 * Shows the drop marker on the left edge of the column at index, on the right edge of the
		 * last column for the end, or hides it for -1. It reaches down to the end of the bodies.
		 */
		placeMarker : function (marker, index) {
			const headers = this.headerCells();
			if (index < 0 || headers.length === 0) {
				marker.style.display = 'none';
				return;
			}
			const end = index >= headers.length;
			const rect = headers[end ? headers.length - 1 : index].getBoundingClientRect();
			const bodies = this.table.tBodies;
			const bottom = bodies.length > 0 ?
				bodies[bodies.length - 1].getBoundingClientRect().bottom : rect.bottom;
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
				this.move(move.from, move.to);
			}
		},

		/**
		 * Moves the column whose handle has the focus one place to the left or right with the
		 * arrow keys, past a movable neighbour.
		 */
		keyDown : function (event) {
			if ((event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') ||
					!event.target.matches || !event.target.matches('.' + HANDLE_CLASS)) {
				return;
			}
			event.preventDefault();
			const headers = this.headerCells();
			const from = headers.indexOf(event.target.closest('th'));
			const to = event.key === 'ArrowLeft' ? from - 1 : from + 2;
			if (from >= 0 && MC.isValidMove(this.movable(), from, to)) {
				this.move(from, to);
			}
		},

		/**
		 * Reports a move to onMove(from, to), keeping the focus on the moved column's handle once
		 * the table is rendered again.
		 */
		move : function (from, to) {
			MC.focusAfterMove = { id: this.id, index: to > from ? to - 1 : to };
			if (this.onMove) {
				this.onMove(from, to);
			}
		},

		destroy : function () {
			this.endMove(false);
			this.table.removeEventListener('pointerdown', this.onPointerDown);
			this.table.removeEventListener('keydown', this.onKeyDown);
		}
	};

	MC.Instance = Instance;

	/**
	 * Lets the user move the columns of a table, replacing an earlier attachment to it.
	 *
	 * @param id the id of the table
	 * @param options {movable: the indexes of the movable columns, marking their header cells
	 *            with data-movable, which may be marked so already; label: the label of a handle,
	 *            {0} standing for the column's header; onMove: function(from, to) called with the
	 *            index of a moved column and the index of the column it goes in front of, or the
	 *            number of columns for the end}
	 * @return the instance, or undefined if there is no such table
	 */
	MC.attach = function (id, options) {
		MC.detach(id);
		const table = document.getElementById(id);
		if (!table) {
			return undefined;
		}
		const instance = new Instance(table, options || { });
		MC.instances[id] = instance;
		return instance;
	};

	Wicket.Event.subscribe(Wicket.Event.Topic.DOM_NODE_ADDED, function (jqEvent, element) {
		Object.keys(MC.instances).forEach(function (id) {
			const instance = MC.instances[id];
			if (!document.body.contains(instance.table)) {
				MC.detach(id);
			} else if (element && instance.table.contains(element)) {
				instance.ensureHandles();
			}
		});
	});
})();
