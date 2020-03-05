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
 
/**
 * Javascript modal window
 * Licensed under the Apache License, Version 2.0
 * @author Matej Knopp
 */

;(function (undefined) {
	'use strict';

	/**
	 * In case wicket-ajax.js is not yet loaded, create
	 * Wicket namespace and Wicket.Class.create.
	 */
	if (typeof(Wicket) === "undefined") {
		window.Wicket = {};
	}

	if (!Wicket.Class) {
		Wicket.Class = {
			create: function() {
				return function() {
					this.initialize.apply(this, arguments);
				};
			}
		};
	}

	if (!Wicket.Object) {
		Wicket.Object = { };
	}

	if (!Wicket.Object.extend) {
		Wicket.Object.extend = function(destination, source) {
			for (var property in source) {
				destination[property] = source[property];
			}
			return destination;
		};
	}

	/**
	 * Draggable (and optionally resizable) window that can either hold a div
	 * or an iframe.
	 */
	Wicket.Window = Wicket.Class.create();

	/**
	 * Creates a wicket window instance. The advantage of using this is
	 * that in case an iframe modal window is opened in an already displayed
	 * iframe modal window, the new window is created as a top-level window.
	 *
	 */
	Wicket.Window.create = function(settings) {
		var Win;

		// if it is an iframe window...
		if (typeof(settings.src) !== "undefined") {
			// attempt to get class from parent
			try {
				Win = window.parent.Wicket.Window;
			} catch (ignore) {}
		}

		// no parent...
		if (typeof(Win) === "undefined") {
			Win = Wicket.Window;
		}

		// create and return instance
		return new Win(settings);
	};

	/**
	 * Returns the current top level window (null if none).
	 */
	Wicket.Window.get = function() {
		var win = null;

		if (typeof(Wicket.Window.current) !== "undefined") {
			win = Wicket.Window.current;
		} else {
			try {
				win = window.parent.Wicket.Window.current;
			} catch (ignore) {}
		}
		return win;
	};


	/**
	 * Closes the current open window. This method is supposed to
	 * be called from inside the window (therefore it checks window.parent).
	 */
	Wicket.Window.close = function() {

		var win;
		try {
			win = window.parent.Wicket.Window;
		} catch (ignore) {}

		if (win && win.current) {
			// we can't call close directly, because it will delete our window,
			// so we will schedule it as timeout for parent's window
			window.parent.setTimeout(function() {
				win.current.close();
			}, 0);
		}
	};

	Wicket.Window.prototype = {

		/**
		 * Creates a new window instance.
		 * Note:
		 *   Width refers to the width of entire window (including frame).
		 *   Height refers to the height of user content.
		 *
		 * @param {Object} settings - map that contains window settings. the default
		 *                            values are below - together with description
		 */
		initialize: function(settings) {

			// override default settings with user settings
			this.settings = Wicket.Object.extend({

				minWidth: 200,  /* valid only if resizable */
				minHeight: 150, /* valid only if resizable */

				className: "w_blue", /* w_silver */

				width: 600,  /* initial width */
				height: 300, /* may be null for non-iframe, non-resizable window (automatic height) */

				modalSpacing: 10, /* spacing between the modal and viewport border when modal is wider than viewport */
				headerHeight: 40,

				overflow: "auto",

				resizable: true,

				widthUnit: "px", /* valid only if not resizable */
				heightUnit: "px", /* valid only if not resizable */

				src: null,     /* iframe src - this takes precedence over the "element" property */
				element: null, /* content element (for non-iframe window) */

				iframeName: null, /* name of the iframe */

				cookieId: null, /* id of position (and size if resizable) cookie */

				title: null, /* window title. if null and window content is iframe, title of iframe document will be used. */

				onCloseButton: Wicket.bind(function() {
					/* On firefox on Linux, at least, we need to blur() textfields, etc.
					 * to get it to update its DOM model. Otherwise you'll lose any changes
					 * made to the current form component you're editing.
					 */
					this.caption.getElementsByTagName("a")[0].focus();
					this.caption.getElementsByTagName("a")[0].blur();
					this.close();
					return false;
				}, this), /* called when close button is clicked */

				afterInit: function() { },

				onClose: function() { }, /* called when window is closed */

				mask: "semi-transparent", /* or "transparent" */

				unloadConfirmation : true /* Display confirmation dialog if the user is about to leave a page (IE and FF) */

			}, settings || { });

		},

		/**
		 * Returns true if the window is iframe-based.
		 */
		isIframe: function() {
			return this.settings.src != null;
		},

		/**
		 * Creates the DOM elements of the window.
		 */
		createDOM: function() {
			var idWindow = this.newId();
			var idClassElement = this.newId();
			var idCaption = this.newId();
			var idFrame = this.newId();
			var idTop = this.newId();
			var idTopLeft = this.newId();
			var idTopRight = this.newId();
			var idLeft = this.newId();
			var idRight = this.newId();
			var idBottomLeft = this.newId();
			var idBottomRight = this.newId();
			var idBottom = this.newId();
			var idCaptionText = this.newId();

			var markup = Wicket.Window.getMarkup(idWindow, idClassElement, idCaption, idFrame,
					idTop, idTopLeft, idTopRight, idLeft, idRight, idBottomLeft, idBottomRight,
					idBottom, idCaptionText, this.isIframe());

			var element = document.createElement("div");
			element.id = idWindow;
			document.body.appendChild(element);
			Wicket.DOM.replace(element, markup);

			var _ = function(name) { return document.getElementById(name); };

			this.window = _(idWindow);
			this.classElement = _(idClassElement);
			this.caption = _(idCaption);
			this.content = _(idFrame);
			this.top = _(idTop);
			this.topLeft = _(idTopLeft);
			this.topRight = _(idTopRight);
			this.left = _(idLeft);
			this.right = _(idRight);
			this.bottomLeft = _(idBottomLeft);
			this.bottomRight = _(idBottomRight);
			this.bottom = _(idBottom);
			this.captionText = _(idCaptionText);

			// fix the cursors
			if (this.settings.resizable === false) {
				this.top.style.cursor =  this.topLeft.style.cursor = this.topRight.style.cursor =
				this.bottom.style.cursor = this.bottomLeft.style.cursor = this.bottomRight.style.cursor =
				this.left.style.cursor = this.right.style.cursor = "default";
			}
		},

		/**
		 * Creates the new unique id for window element.
		 */
		newId: function() {
			return "_wicket_window_" + Wicket.Window.idCounter++;
		},

		/**
		 * Binds the handler to the drag event on given element.
		 */
		bind: function(element, handler) {
			Wicket.Drag.init(element, Wicket.bind(this.onBegin, this), Wicket.bind(this.onEnd, this), Wicket.bind(handler, this));
		},

		/**
		 * Unbinds the handler from a drag event on given element.
		 */
		unbind: function(element) {
			Wicket.Drag.clean(element);
		},

		/**
		 * Binds the event handlers to the elements.
		 */
		bindInit: function() {
			this.bind(this.caption, this.onMove);

			if (this.settings.resizable) {
				this.bind(this.bottomRight, this.onResizeBottomRight);
				this.bind(this.bottomLeft, this.onResizeBottomLeft);
				this.bind(this.bottom, this.onResizeBottom);
				this.bind(this.left, this.onResizeLeft);
				this.bind(this.right, this.onResizeRight);
				this.bind(this.topLeft, this.onResizeTopLeft);
				this.bind(this.topRight, this.onResizeTopRight);
				this.bind(this.top, this.onResizeTop);
			} else {
				this.bind(this.bottomRight, this.onMove);
				this.bind(this.bottomLeft, this.onMove);
				this.bind(this.bottom, this.onMove);
				this.bind(this.left, this.onMove);
				this.bind(this.right, this.onMove);
				this.bind(this.topLeft, this.onMove);
				this.bind(this.topRight, this.onMove);
				this.bind(this.top, this.onMove);
			}

			this.caption.getElementsByTagName("a")[0].onclick = Wicket.bind(this.settings.onCloseButton, this);
		},

		/**
		 * Unbinds the event handlers.
		 */
		bindClean: function() {
			this.unbind(this.caption);
			this.unbind(this.bottomRight);
			this.unbind(this.bottomLeft);
			this.unbind(this.bottom);
			this.unbind(this.left);
			this.unbind(this.right);
			this.unbind(this.topLeft);
			this.unbind(this.topRight);
			this.unbind(this.top);

			this.caption.getElementsByTagName("a")[0].onclick = null;
		},

		/**
		 * Returns the content document
		 */
		getContentDocument: function() {
			if (this.isIframe() === true) {
				return this.content.contentWindow.document;
			} else {
				return document;
			}
		},

		/**
		 * Places the window to the center of the viewport.
		 */
		center: function() {
			var scTop = 0;
			var scLeft = 0;

			var width = Wicket.Window.getViewportWidth();
			var height = Wicket.Window.getViewportHeight();

			var modalWidth = this.window.offsetWidth;
			var modalHeight = this.window.offsetHeight;

			if (modalWidth > width - this.settings.modalSpacing) {
				this.window.style.width = (width - this.settings.modalSpacing) + "px";
				modalWidth = this.window.offsetWidth;
			}
			if (modalHeight > height - this.settings.headerHeight) {
				this.content.style.height = (height - this.settings.headerHeight) + "px";
				modalHeight = this.window.offsetHeight;
			}

			var left = (width / 2) - (modalWidth / 2) + scLeft;
			var top = (height / 2) - (modalHeight / 2) + scTop;
			if (left < 0) {
				left = 0;
			}
			if (top < 0) {
				top = 0;
			}

			this.window.style.left = left + "px";
			this.window.style.top = top + "px";
		},

		cookieKey: "wicket-modal-window-positions",
		cookieExp: 31,

		findPositionString: function(remove) {
			var cookie = Wicket.Cookie.get(this.cookieKey);

			var entries = cookie != null ? cookie.split("|") : [];

			for (var i = 0; i < entries.length; ++i) {
				if (entries[i].indexOf(this.settings.cookieId + "::") === 0) {
					var string = entries[i];
					if (remove) {
						entries.splice(i, 1);
						Wicket.Cookie.set(this.cookieKey, entries.join("|"), this.cookieExp);
					}
					return string;
				}
			}
			return null;
		},

		/**
		 * Saves the position (and size if resizable) as a cookie.
		 */
		savePosition: function() {
			this.savePositionAs(this.window.style.left, this.window.style.top, this.window.style.width, this.content.style.height);
		},

		savePositionAs: function(x, y, width, height) {
			if (this.settings.cookieId) {

				this.findPositionString(true);

//				if (!cookie || cookie.length === 0) {
//					cookie = "";
//				} else {
//					cookie = cookie + "|";
//				}
				var cookie = this.settings.cookieId;
				cookie += "::";

				cookie += x + ",";
				cookie += y + ",";
				cookie += width + ",";
				cookie += height;

				var rest = Wicket.Cookie.get(this.cookieKey);
				if (rest != null) {
					cookie += "|" + rest;
				}
				Wicket.Cookie.set(this.cookieKey, cookie, this.cookieExp);
			}
		},

		/**
		 * Restores the position (and size if resizable) from the cookie.
		 */
		loadPosition: function() {
			if (this.settings.cookieId) {

				var string = this.findPositionString(false);

				if (string != null) {
					var array = string.split("::");
					var positions = array[1].split(",");
					if (positions.length === 4) {
						this.window.style.left = positions[0];
						this.window.style.top = positions[1];
						this.window.style.width = positions[2];
						this.content.style.height = positions[3];
					}
				}
			}
		},

		/**
		 * Creates the mask accordingly to the settings.
		 */
		createMask: function() {
			if (this.settings.mask === "transparent") {
				this.mask = new Wicket.Window.Mask(true);
			} else if (this.settings.mask === "semi-transparent") {
				this.mask = new Wicket.Window.Mask(false);
			}
			if (typeof(this.mask) !== "undefined") {
				this.mask.show();
			}
		},

		/**
		 * Destroys the mask.
		 */
		destroyMask: function() {
			this.mask.hide();
			this.mask = null;
		},

		/**
		 * Loads the content
		 */
		load: function() {
			if (!this.settings.title) {
				this.update = window.setInterval(Wicket.bind(this.updateTitle, this), 100);
			}

			this.content.contentWindow.name = this.settings.iframeName;

			try
			{
				this.content.contentWindow.location.replace(this.settings.src);
			}
			catch(ignore)
			{
				this.content.src = this.settings.src;
			}
		},

		/**
		 * Shows the window.
		 */
		show: function() {

			// create the DOM elements
			this.createDOM();

			// set the class of window (blue or silver by default)
			this.classElement.className = this.settings.className;

			// is it an iframe window?
			if (this.isIframe()) {
				// load the file
				this.load();
			} else {
				// it's an element content

				// is the element specified?
				if (this.settings.element == null) {
					throw "Either src or element must be set.";
				}

				// reparent the element
				this.oldParent = this.settings.element.parentNode;
				this.settings.element.parentNode.removeChild(this.settings.element);
				this.content.appendChild(this.settings.element);

				// set the overflow style so that scrollbars are shown when the element is bigger than window
				this.content.style.overflow = this.settings.overflow;
			}

			// bind the events
			this.bindInit();

			// if the title is specified set it
			if (this.settings.title != null) {
				this.captionText.innerHTML = this.settings.title;
			}

			// initial width and height
			this.window.style.width = this.settings.width + (this.settings.resizable ? "px" : this.settings.widthUnit);

			if (this.settings.height) {
				this.content.style.height = this.settings.height + (this.settings.resizable ? "px" : this.settings.heightUnit);
			}

			//if 'auto' flag was set to true call autoresize function
			if (this.settings.autoSize) {
				this.autoSizeWindow();
			}

			// center the window
			this.center();

			// load position from cookie
			this.loadPosition();

			var doShow = Wicket.bind(function() {
				this.adjustOpenWindowZIndexesOnShow();
				this.window.style.visibility="visible";

			}, this);

			this.adjustOpenWindowsStatusOnShow();

			doShow();

			// if the content supports focus and blur it, which means
			// that the already focused element will lose it's focus
			if (this.content.focus) {
				this.content.focus();
				this.content.blur();
			}
			// preserve old unload hanler
			this.old_onunload = window.onunload;

			// new unload handler - close the window to prevent memory leaks in ie
			window.onunload = Wicket.bind(function() {
				this.close(true);
				if (this.old_onunload) {
					return this.old_onunload();
				}
			}, this);

			if (this.settings.unloadConfirmation) {
				Wicket.Event.add(window, 'beforeunload',this.onbeforeunload);
			}

			// create the mask that covers the background
			this.createMask();

			this.settings.afterInit(this);
		},

		onbeforeunload: function() {
			return "Reloading this page will cause the modal window to disappear.";
		},

		adjustOpenWindowZIndexesOnShow: function() {
			// if there is a previous window
			if (this.oldWindow) {
				// lower it's z-index so that it's moved under the mask
				this.oldWindow.window.style.zIndex = Wicket.Window.Mask.zIndex - 1;
			}
		},

		adjustOpenWindowsStatusOnShow: function() {
			// is there a window displayed already?
			if (Wicket.Window.current) {
				// save the reference to it
				this.oldWindow = Wicket.Window.current;
			}
			// keep reference to this window
			Wicket.Window.current = this;
		},

		/**
		 * Returns true if the window can be closed.
		 */
		canClose: function() {
			return true;
		},

		/**
		 * Prevent user from closing the window if there's another (nested) modal window in the iframe.
		 */
		canCloseInternal: function() {
			try {
				if (this.isIframe() === true) {
					var current = this.content.contentWindow.Wicket.Window.current;
					if (current) {
						window.alert('You can\'t close this modal window. Close the top-level modal window first.');
						return false;
					}
				}
			} catch (ignore) {}
			return true;
		},

		/**
		 * Closes the window.
		 * @param {Boolean} force - internal argument
		 */
		close: function(force) {

			// can user close the window?
			if (force !== true && (!this.canClose() || !this.canCloseInternal())) {
				return;
			}

			// if the update handler was set clean it
			if (typeof(this.update) !== "undefined") {
				window.clearInterval(this.update);
			}

			// clean event bindings
			this.bindClean();

			// hide elements
			this.window.style.display = "none";

			// if the window has a div content, the div is reparented to it's old parent
			if (typeof(this.oldParent) !== "undefined") {
				try {
					this.content.removeChild(this.settings.element);
					this.oldParent.appendChild(this.settings.element);
					this.oldParent = null;
				} catch (ignore) {}
			}

			// remove the elements from document
			this.window.parentNode.removeChild(this.window);

			// clean references to elements
			this.window = this.classElement = this.caption = this.bottomLeft = this.bottomRight = this.bottom =
			this.left = this.right = this.topLeft = this.topRight = this.top = this.captionText = null;

			// restore old unload handler
			window.onunload = this.old_onunload;
			this.old_onunload = null;

			Wicket.Event.remove(window, 'beforeunload',this.onbeforeunload);

			// hids and cleanup the mask
			this.destroyMask();

			if (force !== true) {
				// call onclose handler
				this.settings.onClose();
			}

			this.adjustOpenWindowsStatusAndZIndexesOnClose();
		},

		adjustOpenWindowsStatusAndZIndexesOnClose: function() {
			// if there was a window shown before this one
			if (this.oldWindow != null) {
				// set the old as current
				Wicket.Window.current = this.oldWindow;
				// increase it's z-index so that it's moved above the mask
				Wicket.Window.current.window.style.zIndex = Wicket.Window.Mask.zIndex + 1;
				this.oldWindow = null;
			} else {
				// remove reference to the window
				Wicket.Window.current = null;
			}
		},

		/**
		 * Cleans the internal state of the window
		 */
		destroy: function() {
			this.settings = null;
		},

		/**
		 * If the window is Iframe, updates the title with iframe's document title.
		 */
		updateTitle: function() {
			try {
				if (this.content.contentWindow.document.title) {
					if (this.captionText.innerHTML !== this.content.contentWindow.document.title) {
						this.captionText.innerHTML = this.content.contentWindow.document.title;
					}
				}
			} catch (ignore) {
					Wicket.Log.info(ignore);
			}
		},

		/**
		 * Called when dragging has started.
		 */
		onBegin: function(element, event) {
			// all resize elements must be clicked directly
			if (jQuery(element).is('.w_caption') === false && element !== event.target) {
				return false;
			}

			jQuery(this.window).find('iframe').css('pointer-events', 'none');
			
			return true;
		},

		/**
		 * Called when dragging has ended.
		 */
		onEnd: function(object) {
			jQuery(this.window).find('iframe').css('pointer-events', 'auto');

			if (this.content.style.visibility==='hidden') {
				this.content.style.visibility='hidden';
				window.setTimeout(Wicket.bind(function() { this.content.style.visibility='visible'; }, this),  0 );
			}

			this.savePosition();
		},

		/**
		 * Called when window is moving (draggin the caption).
		 */
		onMove: function(object, deltaX, deltaY) {
			var w = this.window;
			this.left_ = parseInt(w.style.left, 10) + deltaX;
			this.top_ = parseInt(w.style.top, 10) + deltaY;

			if (this.left_ < 0) {
				this.left_ = 0;
			}

			if (this.top_ < 0) {
				this.top_ = 0;
			}

			w.style.left = this.left_ + "px";
			w.style.top = this.top_ + "px";

			this.moving();
		},

		/**
		 * Called when window is being moved
		 */
		moving: function() {
		},

		/**
		 * Called when window is resizing.
		 */
		resizing: function() {
		},

		/**
		 * Ensures that the size of window is not smaller than minimal size.
		 */
		clipSize : function(swapX, swapY) {
			this.res = [0, 0];

			if (this.width < this.settings.minWidth) {
				this.left_ -= this.settings.minWidth - this.width;
				this.res[0] = this.settings.minWidth - this.width;
				this.width = this.settings.minWidth;
			}

			if (this.height < this.settings.minHeight) {
				this.top_ -= this.settings.minHeight - this.height;
				this.res[1] = this.settings.minHeight - this.height;
				this.height = this.settings.minHeight;
			}

			if (swapX === true) {
				this.res[0] = -this.res[0];
			}
			if (swapY === true) {
				this.res[1] = -this.res[1];
			}
		},

		//
		// These methods are handlers for parts of window frame
		//

		onResizeBottomRight: function(object, deltaX, deltaY) {
			var w = this.window;
			var f = this.content;

			this.width = parseInt(w.style.width, 10) + deltaX;
			this.height = parseInt(f.style.height, 10) + deltaY;

			this.clipSize();

			w.style.width = this.width + "px";
			f.style.height = this.height + "px";

			this.moving();
			this.resizing();

			return this.res;
		},

		onResizeBottomLeft: function(object, deltaX, deltaY) {
			var w = this.window;
			var f = this.content;

			this.width = parseInt(w.style.width, 10) - deltaX;
			this.height = parseInt(f.style.height, 10) + deltaY;
			this.left_ = parseInt(w.style.left, 10) + deltaX;

			this.clipSize(true);

			w.style.width = this.width + "px";
			w.style.left = this.left_ + "px";
			f.style.height = this.height  + "px";

			this.moving();
			this.resizing();

			return this.res;
		},

		onResizeBottom: function(object, deltaX, deltaY) {
			var f = this.content;
			this.height = parseInt(f.style.height, 10) + deltaY;

			this.clipSize();

			f.style.height = this.height + "px";

			this.resizing();

			return this.res;
		},

		onResizeLeft: function(object, deltaX, deltaY) {
			var w = this.window;

			this.width = parseInt(w.style.width, 10) - deltaX;
			this.left_ = parseInt(w.style.left, 10) + deltaX;

			this.clipSize(true);

			w.style.width = this.width + "px";
			w.style.left = this.left_ + "px";

			this.moving();
			this.resizing();

			return this.res;
		},

		onResizeRight: function(object, deltaX, deltaY) {
			var w = this.window;

			this.width = parseInt(w.style.width, 10) + deltaX;

			this.clipSize();

			w.style.width = this.width + "px";

			this.resizing();

			return this.res;
		},

		onResizeTopLeft: function(object, deltaX, deltaY) {
			var w = this.window;
			var f = this.content;

			this.width = parseInt(w.style.width, 10) - deltaX;
			this.height = parseInt(f.style.height, 10) - deltaY;
			this.left_ = parseInt(w.style.left, 10) + deltaX;
			this.top_ =  parseInt(w.style.top, 10) + deltaY;

			this.clipSize(true, true);

			w.style.width = this.width + "px";
			w.style.left = this.left_ + "px";
			f.style.height = this.height  + "px";
			w.style.top = this.top_ + "px";

			this.moving();
			this.resizing();

			return this.res;
		},

		onResizeTopRight: function(object, deltaX, deltaY) {
			var w = this.window;
			var f = this.content;

			this.width = parseInt(w.style.width, 10) + deltaX;
			this.height = parseInt(f.style.height, 10) - deltaY;
			this.top_ = parseInt(w.style.top, 10) + deltaY;

			this.clipSize(false, true);

			w.style.width = this.width + "px";
			f.style.height = this.height  + "px";
			w.style.top = this.top_ + "px";

			this.moving();
			this.resizing();

			return this.res;
		},

		onResizeTop: function(object, deltaX, deltaY) {
			var f = this.content;
			var w = this.window;

			this.height = parseInt(f.style.height, 10) - deltaY;
			this.top_ = parseInt(w.style.top, 10) + deltaY;

			this.clipSize(false, true);

			f.style.height = this.height  + "px";
			w.style.top = this.top_ + "px";

			this.moving();
			this.resizing();

			return this.res;
		},

		/**
	    * Resize windows in order to fit content's width and heigth
	    */
		autoSizeWindow: function(){
			var targetWindow = this.window;
			var targetContent = this.content;

			targetContent.style.height = this.settings.minHeight +'px';
			targetWindow.style.width = this.settings.minWidth +'px';

			targetContent.style.overflow = 'hidden';

			var newHeight = targetContent.scrollHeight +'px';
			var newWidth = (targetContent.scrollWidth + targetWindow.clientWidth - targetContent.clientWidth) + 'px';

			targetContent.style.height = newHeight;

			targetWindow.style.width = newWidth;

			targetContent.style.overflow = this.settings.overflow;
		}
	};

	/**
	 * Counter for generating unique component ids.
	 */
	Wicket.Window.idCounter = 0;

	/**
	 * Returns the modal window markup with specified element identifiers.
	 */
	Wicket.Window.getMarkup = function(idWindow, idClassElement, idCaption, idContent, idTop, idTopLeft, idTopRight, idLeft, idRight, idBottomLeft, idBottomRight, idBottom, idCaptionText, isFrame) {
		var s =
				"<div class=\"wicket-modal\" id=\""+idWindow+"\" role=\"dialog\" aria-labelledby=\""+idCaptionText+"\" style=\"top: 10px; left: 10px; width: 100px;\"><form style='background-color:transparent;padding:0px;margin:0px;border-width:0px;position:static'>"+
				"<div id=\""+idClassElement+"\">"+

					"<div class=\"w_top_1\">"+

					"<div class=\"w_topLeft\" id=\""+idTopLeft+"\">"+
					"</div>"+

					"<div class=\"w_topRight\" id=\""+idTopRight+"\">"+
					"</div>"+

					"<div class=\"w_top\" id='"+idTop+"'>"+
					"</div>"+

					"</div>"+

					"<div class=\"w_left\" id='"+idLeft+"'>"+
						"<div class=\"w_right_1\">"+
							"<div class=\"w_right\" id='"+idRight+"'>"+
								"<div class=\"w_content_1\">"+
									"<div class=\"w_caption\"  id=\""+idCaption+"\">"+
										"<a class=\"w_close\" style=\"z-index:1\" href=\"#\"></a>"+
										"<h3 id=\""+idCaptionText+"\" class=\"w_captionText\"></h3>"+
									"</div>"+

									"<div class=\"w_content_2\">"+
									"<div class=\"w_content_3\">"+
			                            "<div class=\"w_content\">";
					if (isFrame) {
						s+= "<iframe frameborder=\"0\" id=\""+idContent+"\" allowtransparency=\"false\" style=\"height: 200px\" class=\"wicket_modal\"></iframe>";
					} else {
						s+= "<div id='"+idContent+"' class='w_content_container'></div>";
					}
						s+=
										"</div>"+
									"</div>"+
									"</div>"+
								"</div>"+
							"</div>"+
						"</div>"+
					"</div>"+


					"<div class=\"w_bottom_1\" id=\""+idBottom+"_1\">"+

						"<div class=\"w_bottomRight\"  id=\""+idBottomRight+"\">"+
						"</div>"+

						"<div class=\"w_bottomLeft\" id=\""+idBottomLeft+"\">"+
						"</div>"+

						"<div class=\"w_bottom\" id=\""+idBottom+"\">"+
						"</div>"+


					"</div>"+

				"</div>"+
			"</form></div>";

			return s;
	};

	/**
	 * Transparent or semi-transparent masks that prevents user from interacting
	 * with the portion of page behind a window.
	 */
	Wicket.Window.Mask = Wicket.Class.create();

	Wicket.Window.Mask.zIndex = 20000;

	Wicket.Window.Mask.prototype = {

		/**
		 * Creates the mask.
		 * Created mask is not visible immediately. You have to call <code>show()</code> to
		 * make it visible.
		 * @param {boolean} transparent - whether the mask should be transparent (true) or
		 *                                semi-transparent (false).
		 */
		initialize: function(transparent) {
			this.transparent = transparent;
		},

		/**
		 * Shows the mask.
		 */
		show: function() {

			// if the mask is not already shown...
			if (!Wicket.Window.Mask.element) {

				// create the mask element and add it to the document
				var e = document.createElement("div");
				document.body.appendChild(e);

				// set the proper css class name
				if (this.transparent) {
					e.className = "wicket-mask-transparent";
				} else {
					e.className = "wicket-mask-dark";
				}

				e.style.zIndex = Wicket.Window.Mask.zIndex;

				// if the mask is not transparent we have to make the background-image invisible (setting it to null)
				if (this.transparent === false) {
					e.style.backgroundImage = "none";
				}

				// set the element
				this.element = e;

				// preserver old handlers
				this.old_onscroll = window.onscroll;
				this.old_onresize = window.onresize;

				// set new handlers
				window.onscroll = Wicket.bind(this.onScrollResize, this);
				window.onresize = Wicket.bind(this.onScrollResize, this);

				// fix the mask position
				this.onScrollResize(true);

				// set a static reference to mask
				Wicket.Window.Mask.element = e;
			} else {
				// mask is already shown - don't hide it
				this.dontHide = true;
			}

			this.shown=true;
			this.focusDisabled=false;

			this.disableCoveredContent();
		},

		/**
		 * Hides the mask.
		 */
		hide: function() {

			// cancel any pending tasks
			this.cancelPendingTasks();

			// if the mask is visible and we can hide it
			if (typeof(Wicket.Window.Mask.element) !== "undefined" && typeof(this.dontHide) === "undefined") {

				// remove element from document
				document.body.removeChild(this.element);
				this.element = null;

				// restore old handlers
				window.onscroll = this.old_onscroll;
				window.onresize = this.old_onresize;

				Wicket.Window.Mask.element = null;
			}

			this.shown=false;

			this.reenableCoveredContent();
		},

		// disable user interaction for content that is covered by the mask
		disableCoveredContent: function() {
			var doc = document;
			var old = Wicket.Window.current.oldWindow;
			if (old) {
				doc = old.getContentDocument();
			}

			this.doDisable(doc, Wicket.Window.current);
		},

		tasks: [],
		startTask: function (fn, delay) {
			var taskId=setTimeout(Wicket.bind(function() { fn(); this.clearTask(taskId); }, this), delay);
			this.tasks.push(taskId);
		},
		clearTask: function (taskId) {
			var index=-1;
			for (var i=0;i<this.tasks.length;i++) {
				if (this.tasks[i] === taskId) {
					index=i;break;
				}
			}
			if (index>=0) {
				this.tasks.splice(index,1);
			}
		},
		cancelPendingTasks: function() {
			while (this.tasks.length>0) {
				var taskId=this.tasks.shift();
				clearTimeout(taskId);
			}
		},

		// disable user interaction for content that is covered by the mask inside the given document, taking into consideration that this modal window is or not in an iframe
		// and has the given content
		doDisable: function(doc, win) {
			this.startTask(Wicket.bind(function() {this.hideSelectBoxes(doc, win);}, this), 300);
			this.startTask(Wicket.bind(function() {this.disableTabs(doc, win);}, this), 400);
			this.startTask(Wicket.bind(function() {this.disableFocus(doc, win);}, this), 1000);
		},

		// reenable user interaction for content that was covered by the mask
		reenableCoveredContent: function() {
			// show old select boxes (ie only)
			this.showSelectBoxes();

			// restore tab order
			this.restoreTabs();

			// revert onfocus handlers
			this.enableFocus();
		},

		/**
		 * Used to update the position (ie) and size (ie, opera) of the mask.
		 */
		onScrollResize: function(dontChangePosition) {
			// if the iframe is not position:fixed fix it's position
			if (this.element.style.position === "absolute") {

				var w = Wicket.Window.getViewportWidth();
				var h = Wicket.Window.getViewportHeight();

				var scTop = 0;
				var scLeft = 0;

	            scLeft = Wicket.Window.getScrollX();
				scTop = Wicket.Window.getScrollY();

				this.element.style.top = scTop + "px";
				this.element.style.left = scLeft + "px";

				if (document.all) { // opera or explorer
					this.element.style.width = w;
				}
				this.element.style.height = h;
			}
		},

		/**
		 * Returns true if 'element' is a child (anywhere in hierarchy) of 'parent'
		 */
		isParent: function(element, parent) {
			if (element.parentNode === parent) {
				return true;
			}
			if (typeof(element.parentNode) === "undefined" || element.parentNode === document.body) {
				return false;
			}
			return this.isParent(element.parentNode, parent);
		},

		/**
		 * For internet explorer hides the select boxes (because they
		 * have always bigger z-order than any other elements).
		 */
		hideSelectBoxes : function(doc, win) {
			if (!this.shown) {
				return;
			}
		},

		/**
		 * Shows the select boxes if they were hidden.
		 */
		showSelectBoxes: function() {
			if (typeof (this.boxes) !== "undefined") {
				for (var i = 0; i < this.boxes.length; ++i) {
					var element = this.boxes[i];
					element.style.visibility="visible";
				}
				this.boxes = null;
			}
		},

		/**
		 * Disable focus on element and all it's children.
		 */
		disableFocusElement: function(element, revertList, win) {

			if (win && win.window !== element) {

				revertList.push([element, element.onfocus]);
				element.onfocus = function() { element.blur(); };

				for (var i = 0; i < element.childNodes.length; ++i) {
					this.disableFocusElement(element.childNodes[i], revertList, win);
				}
			}
		},

		/**
		 * Disable focus on all elements in document
		 */
		disableFocus: function(doc, win) {
			if (!this.shown) {
				return;
			}
			
			this.focusRevertList = [];
			var body = doc.getElementsByTagName("body")[0];
			for (var i = 0; i < body.childNodes.length; ++i) {
				this.disableFocusElement(body.childNodes[i], this.focusRevertList, win);
			}
			
			this.focusDisabled=true;
		},

		/**
		 * Enables focus on all elements where the focus has been disabled.
		 */
		enableFocus: function() {
			if (this.focusDisabled === false) {
				return;
			}

			if (typeof(this.focusRevertList) !== "undefined") {
				for (var i = 0; i < this.focusRevertList.length; ++i) {
					var item = this.focusRevertList[i];
					item[0].onfocus = item[1];
				}
			}
			this.focusRevertList = null;
		},

		/**
		 * Disable tab indexes (ie).
		 */
		disableTabs: function (doc, win) {
			if (!this.shown) {
				return;
			}

			if (typeof (this.tabbableTags) === "undefined") {
				this.tabbableTags = ["A", "BUTTON", "TEXTAREA", "INPUT", "IFRAME", "SELECT"];
			}
		},

		/**
		 * Restore tab indexes if they were disabled.
		 */
		restoreTabs: function() {
			if (typeof (this.disabledTabsRevertList) !== "undefined" && this.disabledTabsRevertList !== null) {
				for (var i = 0; i < this.disabledTabsRevertList.length; ++i) {
					var element = this.disabledTabsRevertList[i];
					if (typeof(element.hiddenTabIndex) !== 'undefined') {
						element.tabIndex = element.hiddenTabIndex;
						try {
							delete element.hiddenTabIndex;
						} catch (e) {
							element.hiddenTabIndex = undefined;
						}
					}
				}
				this.disabledTabsRevertList = null;
			}
		}
	};

	/**
	 * Returns the height of visible area.
	 */
	Wicket.Window.getViewportHeight = function() {
		if (typeof(window.innerHeight) !== "undefined") {
			return window.innerHeight;
		}

		if (document.compatMode === 'CSS1Compat') {
			return document.documentElement.clientHeight;
		}

		if (document.body) {
			return document.body.clientHeight;
		}

		return undefined;
	};

	/**
	 * Returns the width of visible area.
	 */
	Wicket.Window.getViewportWidth =  function() {
		if (typeof(window.innerWidth) !== "undefined") {
			return window.innerWidth;
		}

		if (document.compatMode === 'CSS1Compat') {
			return document.documentElement.clientWidth;
		}

		if (document.body) {
			return document.body.clientWidth;
		}

		return undefined;
	};

	/**
	 * Returns the horizontal scroll offset
	 */
	Wicket.Window.getScrollX = function() {
		var iebody = (document.compatMode && document.compatMode !== "BackCompat") ? document.documentElement : document.body;
		return document.all? iebody.scrollLeft : window.pageXOffset;
	};

	/**
	 * Returns the vertical scroll offset
	 */
	Wicket.Window.getScrollY = function() {
		var iebody = (document.compatMode && document.compatMode !== "BackCompat") ? document.documentElement : document.body;
		return document.all? iebody.scrollTop : window.pageYOffset;
	};

	/**
	 * Convenience methods for getting and setting cookie values.
	 */
	Wicket.Cookie = {

		/**
		 * Returns the value for cookie of given name.
		 * @param {String} name - name of cookie
		 */
		get: function(name) {
			if (document.cookie.length > 0) {
				var start = document.cookie.indexOf (name + "=");
	            if (start !== -1) {
					start = start + name.length + 1;
					var end = document.cookie.indexOf(";", start);
					if (end === -1) {
						end = document.cookie.length;
					}
					return window.unescape(document.cookie.substring(start,end));
				}
	        } else {
				return null;
			}
		},

		/**
		 * Sets the value for cookie of given name.
		 * @param {Object} name - name of cookie
		 * @param {Object} value - new value
		 * @param {Object} expiredays - how long will the cookie be persisted
		 */
		set: function(name, value, expiredays) {
			var exdate = new Date();
			exdate.setDate(exdate.getDate() + expiredays);
			var secure = /^https/.test(location.protocol) ? ';secure' : '';
			document.cookie = name + "=" + window.escape(value) +
				((expiredays === null) ? "" : ";expires="+exdate) +
				secure;
		}
	};

	/**
	 * Flexible dragging support.
	 */
	Wicket.Drag = {

		/**
		 * Initializes dragging on the specified element.
		 * 
		 * @param element {Element}
		 *            element clicking on which
		 *            the drag should begin
		 * @param onDragBegin {Function}
		 *            called at the begin of dragging - passed element and event as parameters,
		 *            may return false to prevent the start
		 * @param onDragEnd {Function}
		 *            handler called at the end of dragging - passed element as parameter
		 * @param onDrag {Function}
		 *            handler called during dragging - passed element and mouse deltas as parameters
		 */
		init: function(element, onDragBegin, onDragEnd, onDrag) {

			if (typeof(onDragBegin) === "undefined") {
				onDragBegin = jQuery.noop;
			}

			if (typeof(onDragEnd) === "undefined") {
				onDragEnd = jQuery.noop;
			}

			if (typeof(onDrag) === "undefined") {
				onDrag = jQuery.noop;
			}

			element.wicketOnDragBegin = onDragBegin;
			element.wicketOnDrag = onDrag;
			element.wicketOnDragEnd = onDragEnd;


			// set the mousedown handler
			Wicket.Event.add(element, "mousedown", Wicket.Drag.mouseDownHandler);
		},

		mouseDownHandler: function (e) {
			e = Wicket.Event.fix(e);

			var element = this;

			if (element.wicketOnDragBegin(element, e) === false) {
				return;
			}

			if (e.preventDefault) {
				e.preventDefault();
			}

			element.lastMouseX = e.clientX;
			element.lastMouseY = e.clientY;

			element.old_onmousemove = document.onmousemove;
			element.old_onmouseup = document.onmouseup;
			element.old_onselectstart = document.onselectstart;
			element.old_onmouseout = document.onmouseout;

			document.onselectstart = function () {
				return false;
			};
			document.onmousemove = Wicket.Drag.mouseMove;
			document.onmouseup = Wicket.Drag.mouseUp;
			document.onmouseout = Wicket.Drag.mouseOut;

			Wicket.Drag.current = element;
		},

		/**
		 * Deinitializes the dragging support on given element.
		 */
		clean: function (element) {
			element.onmousedown = null;
		},

		/**
		 * Called when mouse is moved. This method fires the onDrag event
		 * with element instance, deltaX and deltaY (the distance
		 * between this call and the previous one).

		 * The onDrag handler can optionally return an array of two integers
		 * - the delta correction. This is used, for example, if there is
		 * element being resized and the size limit has been reached (but the
		 * mouse can still move).
		 *
		 * @param {Event} e
		 */
		mouseMove: function (e) {
			e = Wicket.Event.fix(e);
			var o = Wicket.Drag.current;

			// this happens sometimes in Safari
			if (e.clientX < 0 || e.clientY < 0) {
				return;
			}

			if (o !== null) {
				var deltaX = e.clientX - o.lastMouseX;
				var deltaY = e.clientY - o.lastMouseY;

				var res = o.wicketOnDrag(o, deltaX, deltaY, e);

				if (res !== "undefined") {
					res = [0, 0];
				}

				o.lastMouseX = e.clientX + res[0];
				o.lastMouseY = e.clientY + res[1];
			}

			return false;
		},

		/**
		 * Called when the mouse button is released.
		 * Cleans all temporary variables and callback methods.
		 */
		mouseUp: function () {
			var o = Wicket.Drag.current;

			if (o) {
				o.wicketOnDragEnd(o);

				o.lastMouseX = null;
				o.lastMouseY = null;

				document.onmousemove = o.old_onmousemove;
				document.onmouseup = o.old_onmouseup;
				document.onselectstart = o.old_onselectstart;

				document.onmouseout = o.old_onmouseout;

				o.old_mousemove = null;
				o.old_mouseup = null;
				o.old_onselectstart = null;
				o.old_onmouseout = null;

				Wicket.Drag.current = null;
			}
		},

		/**
		 * Called when mouse leaves an element. We need this for firefox, as otherwise
		 * the dragging would continue after mouse leaves the document.
		 * Unfortunately this break dragging in firefox immediately after the mouse leaves
		 * page.
		 */
		mouseOut: function (e) {
		}
	};
	
})();
