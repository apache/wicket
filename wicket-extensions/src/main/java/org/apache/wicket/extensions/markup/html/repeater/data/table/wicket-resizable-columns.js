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
 * Client side of ResizableColumnsBehavior: lets the columns of a table be resized by dragging the
 * right edge of their header cells, or with the arrow keys on that edge.
 *
 * In 'fit' mode the table keeps its width and a column takes its width from the next resizable one;
 * in 'stretch' mode a column grows or shrinks on its own and the table follows. While dragging, a
 * vertical guide with arrows shows where the edge will land, a band the extent of the column and a
 * label its new width; the width is applied on release.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === "undefined") {
		window.Wicket = { };
	}

	if (typeof(Wicket.ResizableColumns) !== "undefined") {
		return;
	}

	const HANDLE_CLASS = 'wicket-col-resizer';
	const GUIDE_CLASS = 'wicket-col-resize-guide';
	const BAND_CLASS = 'wicket-col-resize-band';
	const LABEL_CLASS = 'wicket-col-resize-label';
	const RESIZING_CLASS = 'wicket-resizing';
	const COLGROUP_CLASS = 'wicket-resizable-colgroup';

	const sum = function (widths) {
		return widths.reduce(function (total, width) {
			return total + width;
		}, 0);
	};

	const positiveWidths = function (widths, count) {
		return Array.isArray(widths) && widths.length === count && widths.every(function (width) {
			return width > 0;
		});
	};

	const scale = function (widths, total) {
		const current = sum(widths);
		return current > 0 ? widths.map(function (width) {
			return width * total / current;
		}) : widths;
	};

	const RC = Wicket.ResizableColumns = {

		/**
		 * The resizable tables, by id.
		 */
		instances: { },

		/**
		 * Options a table gives for its own columns, by id, which attach takes where it is not
		 * given them: proportions, adjustToParent and measure, for example.
		 */
		tableOptions: { },

		/**
		 * Limits how much the column at index may change: no column gets narrower than min, and in
		 * 'fit' mode the neighbour, the next resizable column, gives what the column takes. A
		 * column already narrower than min is not made narrower still, nor its neighbour.
		 *
		 * @param neighbour the index of the neighbour, or -1 if there is none
		 * @return the allowed change, in pixels
		 */
		clamp : function (mode, widths, index, neighbour, delta, min) {
			const lowest = Math.min(0, min - widths[index]);
			let highest = Infinity;
			if (mode === 'fit') {
				highest = neighbour >= 0 ? Math.max(0, widths[neighbour] - min) : 0;
			}
			return Math.max(lowest, Math.min(highest, delta));
		},

		/**
		 * @return the widths after changing the column at index by delta, within the limits of clamp
		 */
		resize : function (mode, widths, index, neighbour, delta, min) {
			const allowed = RC.clamp(mode, widths, index, neighbour, delta, min);
			const result = widths.slice();
			result[index] += allowed;
			if (mode === 'fit' && allowed !== 0) {
				result[neighbour] -= allowed;
			}
			return result;
		},

		/**
		 * @return the rendered width of a cell, in pixels
		 */
		measure : function (cell) {
			return cell.getBoundingClientRect().width;
		},

		/**
		 * Lays the columns of a table out by their content again and keeps those widths, unless
		 * the table was given widths or the user resized a column.
		 */
		fit : function (id) {
			const instance = RC.instances[id];
			if (instance) {
				instance.fitContent();
			}
		},

		detach : function (id) {
			const instance = RC.instances[id];
			if (instance) {
				instance.destroy();
				delete RC.instances[id];
			}
		}
	};

	/**
	 * Stops the click a browser fires when a drag ends, so it reaches no button under the pointer.
	 */
	const swallowClick = function () {
		const swallow = function (event) {
			event.preventDefault();
			event.stopPropagation();
		};
		document.addEventListener('click', swallow, true);
		window.setTimeout(function () {
			document.removeEventListener('click', swallow, true);
		}, 0);
	};

	function Instance(table, options) {
		this.table = table;
		this.mode = options.mode === 'stretch' ? 'stretch' : 'fit';
		this.minWidth = options.minWidth !== undefined ? options.minWidth : 30;
		this.step = options.step !== undefined ? options.step : 10;
		this.onResize = options.onResize;
		this.adjustToParent = options.adjustToParent === true;
		this.measureContent = options.measure;

		this.onPointerDown = this.pointerDown.bind(this);
		this.onKeyDown = this.keyDown.bind(this);
		this.table.addEventListener('pointerdown', this.onPointerDown);
		this.table.addEventListener('keydown', this.onKeyDown);

		this.table.classList.add('wicket-resizable-columns', 'wicket-resizable-columns-' + this.mode);
		this.skippedWidth = 0;
		this.ensureHandles();
		this.table.style.tableLayout = 'fixed';
		const count = this.headerCells().length;
		this.sized = positiveWidths(options.widths, count);
		this.proportions = positiveWidths(options.proportions, count) ?
			options.proportions.slice() : undefined;
		if (this.sized) {
			this.apply(options.widths);
		} else {
			this.base = this.proportions || this.measureFit();
			this.apply(this.layout(this.base));
		}

		if (this.adjustToParent && !this.sized) {
			this.onWindowResize = this.refit.bind(this);
			window.addEventListener('resize', this.onWindowResize);
		}
	}

	Instance.prototype = {

		/**
		 * @return the cells of the header row, the last row of the head holding th cells, except
		 *         those carrying data-resizable-skip
		 */
		headerCells : function () {
			return this.headerRowCells().filter(function (cell) {
				return !cell.hasAttribute('data-resizable-skip');
			});
		},

		/**
		 * @return the th cells of the header row carrying data-resizable-skip, which are not
		 *         columns of their own but keep their width, such as an empty column at the end
		 */
		skippedCells : function () {
			return this.headerRowCells().filter(function (cell) {
				return cell.hasAttribute('data-resizable-skip');
			});
		},

		/**
		 * @return the width of the cells carrying data-resizable-skip; the last known one while
		 *         it cannot be measured, for example while the table is hidden
		 */
		skipped : function () {
			const width = sum(this.skippedCells().map(RC.measure));
			if (width > 0) {
				this.skippedWidth = width;
			}
			return this.skippedWidth;
		},

		headerRowCells : function () {
			const rows = this.table.tHead ? this.table.tHead.rows : [];
			for (let i = rows.length - 1; i >= 0; i--) {
				const cells = Array.prototype.filter.call(rows[i].cells, function (cell) {
					return cell.tagName === 'TH';
				});
				if (cells.length > 0) {
					return cells;
				}
			}
			return [];
		},

		/**
		 * @return the current widths of the columns; the last known ones if they cannot be
		 *         measured, for example while the table is hidden
		 */
		measureAll : function () {
			const widths = this.headerCells().map(function (cell) {
				return RC.measure(cell);
			});
			const measured = widths.length > 0 && widths.every(function (width) {
				return width > 0;
			});
			return measured || !this.widths ? widths : this.widths.slice();
		},

		/**
		 * @return the widths the columns take by their content, measured by options.measure if
		 *         given, else the widths of the header cells
		 */
		measureFit : function () {
			if (!this.measureContent) {
				return this.measureAll();
			}
			const widths = this.measureContent(this.headerCells());
			return widths.length > 0 && widths.every(function (width) {
				return width > 0;
			}) ? widths : this.measureAll();
		},

		fitContent : function () {
			if (this.sized || this.proportions) {
				return;
			}
			this.cols().forEach(function (col) {
				col.style.width = '';
			});
			this.table.style.width = '';
			this.table.style.tableLayout = 'auto';
			this.base = this.measureFit();
			this.table.style.tableLayout = 'fixed';
			this.apply(this.layout(this.base));
		},

		/**
		 * @return the widths for base widths or proportions: in 'stretch' mode scaled to the width
		 *         of the parent if the table adjusts to it, and proportions scaled to the width the
		 *         table has
		 */
		layout : function (base) {
			if (this.mode !== 'stretch') {
				return base.slice();
			}
			let total = 0;
			if (this.adjustToParent) {
				total = this.parentWidth();
			} else if (this.proportions) {
				total = this.table.getBoundingClientRect().width;
			}
			total -= this.skipped();
			return total > 0 ? scale(base, total) : base.slice();
		},

		/**
		 * @return the width of the content box of the table's parent, in pixels, without a
		 *         vertical scroll bar of the parent
		 */
		parentWidth : function () {
			const parent = this.table.parentNode;
			if (!parent || !parent.getBoundingClientRect) {
				return 0;
			}
			const style = window.getComputedStyle(parent);
			const borders = (parseFloat(style.borderLeftWidth) || 0) +
				(parseFloat(style.borderRightWidth) || 0);
			const scrollBar = Math.max(0, parent.offsetWidth - parent.clientWidth - borders);
			return parent.getBoundingClientRect().width - borders - scrollBar -
				(parseFloat(style.paddingLeft) || 0) - (parseFloat(style.paddingRight) || 0);
		},

		/**
		 * Lays the columns out for the parent's width again, unless the user resized one.
		 */
		refit : function () {
			if (!this.sized && this.base) {
				this.apply(this.layout(this.base));
			}
		},

		cols : function () {
			let colgroup = this.table.querySelector('colgroup.' + COLGROUP_CLASS);
			const count = this.headerCells().length;
			if (!colgroup || colgroup.children.length !== count) {
				if (colgroup) {
					colgroup.remove();
				}
				colgroup = document.createElement('colgroup');
				colgroup.className = COLGROUP_CLASS;
				for (let i = 0; i < count; i++) {
					colgroup.appendChild(document.createElement('col'));
				}
				this.table.insertBefore(colgroup, this.table.firstChild);
			}
			return Array.prototype.slice.call(colgroup.children);
		},

		/**
		 * Sets the widths of the columns: as shares of the table in 'fit' mode, in pixels in
		 * 'stretch' mode, where the table takes their sum.
		 */
		apply : function (widths) {
			this.widths = widths.slice();
			const cols = this.cols();
			const total = sum(widths);
			const fit = this.mode === 'fit';
			const skipped = this.skipped();
			cols.forEach(function (col, index) {
				const width = widths[index] || 0;
				if (!fit) {
					col.style.width = width + 'px';
				} else if (total <= 0) {
					col.style.width = '';
				} else if (skipped > 0) {
					col.style.width = 'calc((100% - ' + skipped + 'px) * ' + (width / total) + ')';
				} else {
					col.style.width = (width / total * 100) + '%';
				}
			});
			this.table.style.width = fit ? '100%' : (total + skipped) + 'px';
		},

		/**
		 * @return the index of the column giving or taking width when the column at index is
		 *         resized in 'fit' mode, the next one that is resizable, or -1
		 */
		neighbourOf : function (index, cells) {
			for (let i = index + 1; i < cells.length; i++) {
				if (cells[i].getAttribute('data-resizable') !== 'false') {
					return i;
				}
			}
			return -1;
		},

		isResizable : function (index, cells) {
			if (cells[index].getAttribute('data-resizable') === 'false') {
				return false;
			}
			return this.mode === 'stretch' || this.neighbourOf(index, cells) >= 0;
		},

		/**
		 * Applies new widths, and reports them to onResize.
		 */
		commit : function (widths) {
			this.sized = true;
			this.apply(widths);
			if (this.onResize) {
				this.onResize(widths.slice());
			}
		},

		/**
		 * Puts a handle on the right edge of every resizable header cell that lacks one, for
		 * example after the header row was re-rendered.
		 */
		ensureHandles : function () {
			const self = this;
			const cells = this.headerCells();
			cells.forEach(function (cell, index) {
				let handle = Array.prototype.find.call(cell.children, function (child) {
					return child.classList.contains(HANDLE_CLASS);
				});
				if (!self.isResizable(index, cells)) {
					if (handle) {
						handle.remove();
					}
					return;
				}
				if (!handle) {
					handle = document.createElement('span');
					handle.className = HANDLE_CLASS;
					handle.setAttribute('role', 'separator');
					handle.setAttribute('aria-orientation', 'vertical');
					handle.setAttribute('aria-label', 'Resize column');
					handle.tabIndex = 0;
					cell.appendChild(handle);
				}
				handle.setAttribute('data-index', index);
				handle.setAttribute('data-neighbour', self.mode === 'fit' ? self.neighbourOf(index, cells) : -1);
			});
			if (this.widths &&
					(this.cols().length !== cells.length || this.widths.length !== cells.length)) {
				this.apply(this.measureAll());
			}
		},

		handleOf : function (event) {
			const handle = event.target.closest ? event.target.closest('.' + HANDLE_CLASS) : null;
			return handle && this.table.contains(handle) ? handle : null;
		},

		pointerDown : function (event) {
			const handle = this.handleOf(event);
			if (!handle || (event.button !== undefined && event.button !== 0)) {
				return;
			}
			event.preventDefault();
			this.startDrag(Number(handle.getAttribute('data-index')),
				Number(handle.getAttribute('data-neighbour')), event.clientX);
		},

		/**
		 * @return the vertical extent of the header row and the body, leaving out navigation
		 *         rows and the footer
		 */
		extent : function (cell) {
			const top = cell.parentNode.getBoundingClientRect().top;
			const bodies = this.table.tBodies;
			const bottom = bodies.length > 0 ?
				bodies[bodies.length - 1].getBoundingClientRect().bottom :
				this.table.getBoundingClientRect().bottom;
			return { top: top, height: Math.max(0, bottom - top) };
		},

		startDrag : function (index, neighbour, clientX) {
			const self = this;
			this.endDrag(false);
			Array.prototype.forEach.call(
				document.querySelectorAll('.' + GUIDE_CLASS + ', .' + BAND_CLASS),
				function (leftover) {
					leftover.remove();
				});
			const cell = this.headerCells()[index];
			const extent = this.extent(cell);
			const cellRect = cell.getBoundingClientRect();
			const drag = {
				index: index,
				neighbour: neighbour,
				startX: clientX,
				widths: this.measureAll(),
				edge: cellRect.right + window.scrollX,
				delta: 0,
				guide: document.createElement('div'),
				band: document.createElement('div'),
				label: document.createElement('span')
			};
			drag.guide.className = GUIDE_CLASS;
			drag.guide.style.top = (extent.top + window.scrollY) + 'px';
			drag.guide.style.height = extent.height + 'px';
			drag.guide.style.left = drag.edge + 'px';
			drag.label.className = LABEL_CLASS;
			drag.guide.appendChild(drag.label);
			drag.band.className = BAND_CLASS;
			drag.band.style.top = drag.guide.style.top;
			drag.band.style.height = drag.guide.style.height;
			drag.band.style.left = (cellRect.left + window.scrollX) + 'px';
			this.copyTheme(drag.guide);
			this.copyTheme(drag.band);
			document.body.appendChild(drag.band);
			document.body.appendChild(drag.guide);
			this.showDrag(drag);
			this.table.classList.add(RESIZING_CLASS);

			drag.move = function (event) {
				self.moveDrag(event.clientX);
			};
			drag.up = function (event) {
				try {
					self.moveDrag(event.clientX);
				} finally {
					self.endDrag(true);
				}
			};
			drag.cancel = function () {
				self.endDrag(false);
			};
			drag.key = function (event) {
				if (event.key === 'Escape') {
					self.endDrag(false);
				}
			};
			document.addEventListener('pointermove', drag.move);
			document.addEventListener('pointerup', drag.up);
			document.addEventListener('pointercancel', drag.cancel);
			document.addEventListener('keydown', drag.key);
			window.addEventListener('blur', drag.cancel);
			this.drag = drag;
		},

		moveDrag : function (clientX) {
			const drag = this.drag;
			if (!drag) {
				return;
			}
			drag.delta = RC.clamp(this.mode, drag.widths, drag.index, drag.neighbour,
				clientX - drag.startX, this.minWidth);
			this.showDrag(drag);
		},

		/**
		 * Places the guide on the edge the column would get, and shows its extent and width.
		 */
		showDrag : function (drag) {
			const width = drag.widths[drag.index] + drag.delta;
			drag.guide.style.left = (drag.edge + drag.delta) + 'px';
			drag.band.style.width = Math.max(0, width) + 'px';
			drag.label.textContent = Math.round(width) + ' px';
		},

		/**
		 * Copies the theme's colors to an element appended to the body, outside of the table.
		 */
		copyTheme : function (element) {
			const style = window.getComputedStyle(this.table);
			['--wicket-theme-accent', '--wicket-theme-on-primary'].forEach(function (name) {
				const value = style.getPropertyValue(name);
				if (value) {
					element.style.setProperty(name, value.trim());
				}
			});
		},

		/**
		 * Ends dragging, applying the new width if commit is true.
		 */
		endDrag : function (commit) {
			const drag = this.drag;
			if (!drag) {
				return;
			}
			this.drag = undefined;
			document.removeEventListener('pointermove', drag.move);
			document.removeEventListener('pointerup', drag.up);
			document.removeEventListener('pointercancel', drag.cancel);
			document.removeEventListener('keydown', drag.key);
			window.removeEventListener('blur', drag.cancel);
			drag.guide.remove();
			drag.band.remove();
			this.table.classList.remove(RESIZING_CLASS);
			swallowClick();
			if (commit && drag.delta !== 0) {
				this.commit(RC.resize(this.mode, drag.widths, drag.index, drag.neighbour, drag.delta,
					this.minWidth));
			}
		},

		keyDown : function (event) {
			const handle = this.handleOf(event);
			if (!handle) {
				return;
			}
			let delta;
			if (event.key === 'ArrowLeft') {
				delta = -this.step;
			} else if (event.key === 'ArrowRight') {
				delta = this.step;
			} else {
				return;
			}
			event.preventDefault();
			this.commit(RC.resize(this.mode, this.measureAll(), Number(handle.getAttribute('data-index')),
				Number(handle.getAttribute('data-neighbour')), delta, this.minWidth));
		},

		destroy : function () {
			this.endDrag(false);
			if (this.onWindowResize) {
				window.removeEventListener('resize', this.onWindowResize);
			}
			this.table.removeEventListener('pointerdown', this.onPointerDown);
			this.table.removeEventListener('keydown', this.onKeyDown);
			Array.prototype.forEach.call(this.table.querySelectorAll('.' + HANDLE_CLASS),
				function (handle) {
					handle.remove();
				});
		}
	};

	RC.Instance = Instance;

	/**
	 * Makes the columns of a table resizable, replacing an earlier attachment to it.
	 *
	 * @param id the id of the table
	 * @param options {mode: 'fit' or 'stretch', minWidth: pixels, step: pixels per key press,
	 *            widths: saved widths in pixels, proportions: relative widths of the columns,
	 *            adjustToParent: whether the table takes the width of its parent,
	 *            onResize: function(widths) called after a resize,
	 *            measure: function(cells) returning the widths the columns of the given header
	 *            cells take by their content, where the header cells alone do not tell}
	 * @return the instance, or undefined if there is no such table
	 */
	RC.attach = function (id, options) {
		RC.detach(id);
		const table = document.getElementById(id);
		if (!table) {
			return undefined;
		}
		const instance = new Instance(table, Object.assign({ }, RC.tableOptions[id], options));
		RC.instances[id] = instance;
		return instance;
	};

	Wicket.Event.subscribe(Wicket.Event.Topic.DOM_NODE_ADDED, function (jqEvent, element) {
		Object.keys(RC.instances).forEach(function (id) {
			const instance = RC.instances[id];
			if (!document.body.contains(instance.table)) {
				RC.detach(id);
			} else if (element && instance.table.contains(element)) {
				instance.ensureHandles();
			}
		});
	});
})();
