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

/*global DOMParser: true, console: true */

/*
 * Wicket Ajax Support - plain JavaScript (no jQuery) implementation
 *
 * This file provides the exact same public API as wicket-ajax-jquery.js
 * (Wicket.Ajax, Wicket.Event, Wicket.DOM, Wicket.Form, Wicket.Head, Wicket.Focus, ...)
 * but is implemented using only native browser APIs. It can be used as a drop-in
 * replacement by configuring
 * {@code Application.get().getJavaScriptLibrarySettings().setWicketAjaxReference(WicketAjaxResourceReference.get())}
 * and does not require jQuery to be loaded at all.
 *
 * @author Igor Vaynberg
 * @author Matej Knopp
 */

;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'undefined') {
		window.Wicket = {};
	}

	if (typeof(Wicket.Head) === 'object') {
		return;
	}

	const isUndef = function (target) {
		return (typeof(target) === 'undefined' || target === null);
	};

	/**
	 * A safe getter for Wicket's Ajax base URL.
	 * If the value is not defined or is empty string then
	 * return '.' (current folder) as base URL.
	 * Used for request header and parameter
	 */
	const getAjaxBaseUrl = function () {
		const baseUrl = Wicket.Ajax.baseUrl || '.';
		return baseUrl;
	};

	/**
	 * Converts a NodeList to an Array
	 *
	 * @param nodeList The NodeList to convert
	 * @returns {Array} The array with document nodes
	 */
	const nodeListToArray = function (nodeList) {
		const arr = [];
		let nodeId;
		if (nodeList && nodeList.length) {
			for (nodeId = 0; nodeId < nodeList.length; nodeId++) {
				arr.push(nodeList.item(nodeId));
			}
		}
		return arr;
	};

	/**
	 * A minimal replacement for jQuery.isPlainObject - true for objects created
	 * via {} or `new Object()`, false for arrays, DOM nodes, null, etc.
	 */
	const isPlainObject = function (obj) {
		return typeof(obj) === 'object' && obj !== null && !Array.isArray(obj) &&
			Object.prototype.toString.call(obj) === '[object Object]';
	};

	/**
	 * Caches the browser's default `display` value per tag name (e.g. 'div' -> 'block',
	 * 'span' -> 'inline'), used by Wicket.DOM.show() to restore visibility of an element
	 * whose 'display' is not known upfront.
	 */
	const elementDisplayCache = {};
	const defaultDisplay = function (nodeName) {
		nodeName = nodeName.toLowerCase();
		let display = elementDisplayCache[nodeName];
		if (!display) {
			const element = document.createElement(nodeName);
			document.body.appendChild(element);
			display = window.getComputedStyle(element).display;
			element.parentNode.removeChild(element);
			if (display === 'none') {
				display = 'block';
			}
			elementDisplayCache[nodeName] = display;
		}
		return display;
	};

	/**
	 * <script> elements created by HTML fragment parsing (e.g. via innerHTML, which is how
	 * a <template> is filled) are flagged by the browser as already started and never execute,
	 * even once moved into the live document - this is standard, intentional HTML parser
	 * behavior. Wicket.DOM.replace()/add() replicate jQuery's DOM-manipulation methods, which
	 * DO run such inline scripts, by replacing each one with a freshly created <script> that
	 * carries the same attributes and text - a plain new node has no such flag and executes
	 * normally once inserted.
	 *
	 * @param root {DocumentFragment|Element} - the (still detached) parsed markup to fix up
	 */
	const reviveScripts = function (root) {
		const scripts = root.querySelectorAll ? root.querySelectorAll('script') : [];
		for (let i = 0; i < scripts.length; i++) {
			const oldScript = scripts[i];
			const newScript = document.createElement('script');
			for (let a = 0; a < oldScript.attributes.length; a++) {
				const attr = oldScript.attributes[a];
				newScript.setAttribute(attr.name, attr.value);
			}
			newScript.text = oldScript.textContent;
			oldScript.parentNode.replaceChild(newScript, oldScript);
		}
	};

	/**
	 * Functions executer takes array of functions and executes them.
	 * The functions are executed one by one as far as the return value is FunctionsExecuter.DONE.
	 * If the return value is FunctionsExecuter.ASYNC or undefined then the execution of
	 * the functions will be resumed once the `notify` callback function is called.
	 * This is needed because header contributions need to do asynchronous download of JS and/or CSS
	 * and they have to let next function to run only after the download.
	 * After the FunctionsExecuter is initialized, the start methods triggers the first function.
	 *
	 * @param functions {Array} - an array of functions to execute
	 */
	const FunctionsExecuter = function (functions) {

		this.functions = functions;

		/**
		 * The index of the currently executed function
		 * @type {number}
		 */
		this.current = 0;

		/**
		 * Tracks the depth of the call stack when `notify` is used for
		 * asynchronous notification that a function execution has finished.
		 * Should be reset to 0 when at some point to avoid problems like
		 * "too much recursion". The reset may break the atomicity by allowing
		 * another instance of FunctionsExecuter to run its functions
		 * @type {number}
		 */
		this.depth = 0; // we need to limit call stack depth

		this.processNext = function () {
			if (this.current < this.functions.length) {
				let run;

				const f = this.functions[this.current];
				run = function () {
					try {
						const n = this.notify.bind(this);
						return f(n);
					}
					catch (e) {
						Wicket.Log.error("FunctionsExecuter.processNext:", e);
						return FunctionsExecuter.FAIL;
					}
				};
				run = run.bind(this);
				this.current++;

				if (this.depth > FunctionsExecuter.DEPTH_LIMIT) {
					// to prevent stack overflow (see WICKET-4675)
					this.depth = 0;
					window.setTimeout(run, 1);
				} else {
					const retValue = run();
					if (isUndef(retValue) || retValue === FunctionsExecuter.ASYNC) {
						this.depth++;
					}
					return retValue;
				}
			}
		};

		this.start = function () {
			let retValue = FunctionsExecuter.DONE;
			while (retValue === FunctionsExecuter.DONE) {
				retValue = this.processNext();
			}
		};

		this.notify = function () {
			this.start();
		};
	};

	/**
	 * Response that should be used by a function when it finishes successfully
	 * in synchronous manner
	 * @type {number}
	 */
	FunctionsExecuter.DONE = 1;

	/**
	 * Response that should be used by a function when it finishes abnormally
	 * in synchronous manner
	 * @type {number}
	 */
	FunctionsExecuter.FAIL = 2;

	/**
	 * Response that may be used by a function when it executes asynchronous
	 * code and must wait `notify()` to be executed.
	 * @type {number}
	 */
	FunctionsExecuter.ASYNC = 3;

	/**
	 * An artificial number used as a limit of the call stack depth to avoid
	 * problems like "too much recursion" in the browser.
	 * The depth is not easy to be calculated because the memory used by the
	 * stack depends on many factors
	 * @type {number}
	 */
	FunctionsExecuter.DEPTH_LIMIT = 1000;

	// API start

	Wicket.Class = {
		create: function () {
			return function () {
				this.initialize.apply(this, arguments);
			};
		}
	};

    Wicket.isFunction = function (f) {
        return typeof(f) === 'function';
    };

    Wicket.isWindow = function (obj) {
        return typeof(obj) !== 'undefined' && obj !== null && obj === obj.window;
    };

	/**
	 * Logging functionality.
	 */
	Wicket.Log = {

		enabled: false,

		log: function () {
			if (Wicket.Log.enabled && typeof(console) !== "undefined" && typeof(console.log) === 'function') {
				console.log.apply(console, arguments);
			}
		},

		debug: function () {
			if (Wicket.Log.enabled && typeof(console) !== "undefined" && typeof(console.debug) === 'function') {
				console.debug.apply(console, arguments);
			}
		},

		info: function () {
			if (Wicket.Log.enabled && typeof(console) !== "undefined" && typeof(console.info) === 'function') {
				console.info.apply(console, arguments);
			}
		},

		warn: function () {
			if (Wicket.Log.enabled && typeof(console) !== "undefined" && typeof(console.warn) === 'function') {
				console.warn.apply(console, arguments);
			}
		},

		error: function () {
			if (Wicket.Log.enabled && typeof(console) !== "undefined" && typeof(console.error) === 'function') {
				console.error.apply(console, arguments);
			}
		}
	};

	/**
	 * Channel management
	 *
	 * Wicket Ajax requests are organized in channels. A channel maintain the order of
	 * requests and determines, what should happen when a request is fired while another
	 * one is being processed. The default behavior (stack) puts the all subsequent requests
	 * in a queue, while the drop behavior limits queue size to one, so only the most
	 * recent of subsequent requests is executed.
	 * The name of channel determines the policy. E.g. channel with name foochannel|s is
	 * a stack channel, while barchannel|d is a drop channel.
	 *
	 * The Channel class is supposed to be used through the ChannelManager.
	 */
	Wicket.Channel = Wicket.Class.create();

	Wicket.Channel.prototype = {
		initialize: function (name) {
			name = name || '0|s';
			const res = name.match(/^([^|]+)\|(d|s|a)$/);
			if (isUndef(res)) {
				this.name = '0'; // '0' is the default channel name
				this.type = 's'; // default to stack/queue
			}
			else {
				this.name = res[1];
				this.type = res[2];
			}
			this.callbacks = [];
			this.busy = false;
		},

		schedule: function (callback) {
			if (this.busy === false) {
				this.busy = true;
				try {
					return callback();
				} catch (exception) {
					this.busy = false;
					Wicket.Log.error("An error occurred while executing Ajax request:", exception);
				}
			} else {
				const busyChannel = "Channel '"+ this.name+"' is busy";
				if (this.type === 's') { // stack/queue
					Wicket.Log.info("%s - scheduling the callback to be executed when the previous request finish.", busyChannel);
					this.callbacks.push(callback);
				}
				else if (this.type === 'd') { // drop
					Wicket.Log.info("%s - dropping all previous scheduled callbacks and scheduling a new one to be executed when the current request finish.", busyChannel);
					this.callbacks = [];
					this.callbacks.push(callback);
				} else if (this.type === 'a') { // active
					Wicket.Log.info("%s - ignoring the Ajax call because there is a running request.", busyChannel);
				}
				return null;
			}
		},

		done: function () {
			let callback = null;

			if (this.callbacks.length > 0) {
				callback = this.callbacks.shift();
			}

			if (callback !== null && typeof(callback) !== "undefined") {
				Wicket.Log.info("Calling postponed function...");
				// we can't call the callback from this call-stack
				// therefore we set it on timer event
				window.setTimeout(callback, 1);
			} else {
				this.busy = false;
			}
		}
	};

	/**
	 * Channel manager maintains a map of channels.
	 */
	Wicket.ChannelManager = Wicket.Class.create();

	Wicket.ChannelManager.prototype = {
		initialize: function () {
			this.channels = {};
		},

		// Schedules the callback to channel with given name.
		schedule: function (channel, callback) {
			const parsed = new Wicket.Channel(channel);
			let c = this.channels[parsed.name];
			if (isUndef(c)) {
				c = parsed;
				this.channels[c.name] = c;
			} else {
				c.type = parsed.type;
			}
			return c.schedule(callback);
		},

		// Tells the ChannelManager that the current callback in channel with given name
		// has finished processing and another scheduled callback can be executed (if any).
		done: function (channel) {
			const parsed = new Wicket.Channel(channel);
			const c = this.channels[parsed.name];
			if (!isUndef(c)) {
				c.done();
				if (!c.busy) {
					delete this.channels[parsed.name];
				}
			}
		}
	};

	Wicket.ChannelManager.FunctionsExecuter = FunctionsExecuter;

	/**
	 * The Ajax.Request class encapsulates a XmlHttpRequest.
	 */
	Wicket.Ajax = {};

	/**
	 * Ajax call fires a Wicket Ajax request and processes the response.
	 * The response can contain
	 *   - javascript that should be invoked
	 *   - body of components being replaced
	 *   - header contributions of components
	 *   - a redirect location
	 */
	Wicket.Ajax.Call = Wicket.Class.create();

	Wicket.Ajax._currentSuspension = undefined;

	/**
	 * Suspend the currently evaluated Ajax call, fails if no Ajax call is currently
	 * evaluated.
	 */
	Wicket.Ajax.suspendCall = function () {
		let suspension = Wicket.Ajax._currentSuspension;

		if (suspension === undefined) {
			Wicket.Log.error("Can't suspend: no Ajax call in process");
			return;
		}

		// suspend
		suspension.suspend();

		return function () {
			// release only once
			if (suspension !== null) {
				suspension.release();
				suspension = null;
			}
		};
	};

	/**
	 * Serializes an array of {name, value} pairs to an
	 * 'application/x-www-form-urlencoded' query string.
	 */
	const paramsToQueryString = function (params) {
		if (!params || !params.length) {
			return '';
		}
		const parts = [];
		for (let i = 0; i < params.length; i++) {
			let value = params[i].value;
			if (isUndef(value)) {
				value = '';
			}
			parts.push(encodeURIComponent(params[i].name) + '=' + encodeURIComponent(value));
		}
		return parts.join('&');
	};

	/**
	 * Performs an XMLHttpRequest based request. Mimics the subset of jQuery.ajax()'s
	 * behavior that Wicket.Ajax.Call relies on: GET/POST, custom headers, disabling the
	 * cache, sending either an url-encoded body or a FormData body, a timeout, and
	 * success/error/complete/beforeSend callbacks that receive the XMLHttpRequest object
	 * itself (which already exposes readyState, status, getResponseHeader(), responseText
	 * and responseXML, so a separate 'jqXHR' wrapper is not needed).
	 *
	 * @param options {Object} - url, type ('GET'/'POST'), data (an array of {name, value}
	 *      pairs, or a FormData instance), dataType ('xml' to parse the response body as
	 *      XML), headers, async, timeout, cache, beforeSend(xhr, options),
	 *      success(data, textStatus, xhr), error(xhr, textStatus, errorMessage),
	 *      complete(xhr, textStatus)
	 */
	const ajaxRequest = function (options) {
		const xhr = new XMLHttpRequest();
		const method = (options.type || 'GET').toUpperCase();
		let url = options.url;
		const data = options.data;
		const isFormData = (typeof(FormData) !== 'undefined') && (data instanceof FormData);
		let body = null;

		if (isFormData) {
			body = data;
		} else {
			const queryString = paramsToQueryString(data);
			if (method === 'GET' || method === 'HEAD') {
				if (queryString) {
					url += (url.indexOf('?') > -1 ? '&' : '?') + queryString;
				}
			} else {
				body = queryString;
			}
		}

		if (options.cache === false && (method === 'GET' || method === 'HEAD')) {
			url += (url.indexOf('?') > -1 ? '&' : '?') + '_=' + new Date().getTime();
		}

		// reflect the fully resolved url/body back onto the options ("settings") object,
		// the same way jQuery.ajax() does, so that beforeSend (and anything else inspecting
		// it) sees the request as it is actually going to be sent
		options.url = url;
		if (!isFormData) {
			options.data = body;
		}

		xhr.open(method, url, options.async !== false);

		if (!isFormData && method !== 'GET' && method !== 'HEAD') {
			xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
		}

		const headers = options.headers || {};
		for (const headerName in headers) {
			if (headers.hasOwnProperty(headerName)) {
				xhr.setRequestHeader(headerName, headers[headerName]);
			}
		}

		if (options.async !== false && options.timeout) {
			xhr.timeout = options.timeout;
		}

		let textStatus;
		let completed = false;
		const finish = function () {
			if (completed) {
				return;
			}
			completed = true;
			if (options.complete) {
				options.complete(xhr, textStatus);
			}
		};

		xhr.onload = function () {
			const status = xhr.status;
			if ((status >= 200 && status < 300) || status === 304) {
				textStatus = 'success';
				let responseData = xhr.responseText;
				if (options.dataType === 'xml') {
					responseData = Wicket.Xml.parse(xhr.responseText);
				} else if (options.dataType === 'json') {
					try {
						responseData = responseData ? JSON.parse(responseData) : null;
					} catch (parseError) {
						textStatus = 'parsererror';
						if (options.error) {
							options.error(xhr, textStatus, parseError.message);
						}
						finish();
						return;
					}
				}
				if (options.success) {
					options.success(responseData, textStatus, xhr);
				}
			} else {
				textStatus = 'error';
				if (options.error) {
					options.error(xhr, textStatus, xhr.statusText);
				}
			}
			finish();
		};

		xhr.onerror = function () {
			textStatus = 'error';
			if (options.error) {
				options.error(xhr, textStatus, xhr.statusText);
			}
			finish();
		};

		xhr.ontimeout = function () {
			textStatus = 'timeout';
			if (options.error) {
				options.error(xhr, textStatus, 'timeout');
			}
			finish();
		};

		xhr.onabort = function () {
			textStatus = 'abort';
			finish();
		};

		if (options.beforeSend) {
			options.beforeSend(xhr, options);
		}

		xhr.send(body);

		return xhr;
	};

	Wicket.Ajax.Call.prototype = {

		initialize: function () {},

		/**
		 * Initializes the default values for Ajax request attributes.
		 * The defaults are not set at the server side to save some bytes
		 * for the network transfer
		 *
		 * @param attrs {Object} - the ajax request attributes to enrich
		 * @private
		 */
		_initializeDefaults: function (attrs) {

			// (ajax channel)
			if (typeof(attrs.ch) !== 'string') {
				attrs.ch = '0|s';
			}

			// (wicketAjaxResponse) be default the Ajax result should be processed for <ajax-response>
			if (typeof(attrs.wr) !== 'boolean') {
				attrs.wr = true;
			}

			// (dataType) by default we expect XML responses from the Ajax behaviors
			if (typeof(attrs.dt) !== 'string') {
				attrs.dt = 'xml';
			}

			if (typeof(attrs.m) !== 'string') {
				attrs.m = 'GET';
			}

			if (attrs.async !== false) {
				attrs.async = true;
			}

			if (!Number.isFinite(attrs.rt)) {
				attrs.rt = 0;
			}

			if (attrs.pd !== true) {
				attrs.pd = false;
			}

			if (!attrs.sp) {
				attrs.sp = "bubble";
			}

			if (!attrs.sr) {
				attrs.sr = false;
			}
		},

		/**
		 * Extracts the HTML element that "caused" this Ajax call.
		 * An Ajax call is usually caused by JavaScript event but maybe be also
		 * caused by manual usage of the JS API..
		 *
		 * @param attrs {Object} - the ajax request attributes
		 * @return {HTMLElement} - the DOM element
		 * @private
		 */
		_getTarget: function (attrs) {
			let target;
			if (attrs.event) {
				target = attrs.event.target;
			} else if (!Wicket.isWindow(attrs.c)) {
				target = Wicket.$(attrs.c);
			} else {
				target = window;
			}
			return target;
		},

		/**
		 * A helper function that executes an array of handlers (before, success, failure)
		 *
		 * @param handlers {Array[Function]} - the handlers to execute
		 * @private
		 */
		_executeHandlers: function (handlers) {
			if (Array.isArray(handlers)) {

				// cut the handlers argument
				const args = Array.prototype.slice.call(arguments).slice(1);

				// assumes that the Ajax attributes is always the first argument
				const attrs = args[0];
				const that = this._getTarget(attrs);

				for (let i = 0; i < handlers.length; i++) {
					const handler = handlers[i];
					if (Wicket.isFunction(handler)) {
						handler.apply(that, args);
					} else {
						new Function(handler).apply(that, args);
					}
				}
			}
		},

		/**
		 * Converts an object (hash) to an array suitable for consumption
		 * as request parameters.
		 *
		 * @param {Object} parameters - the object to convert to an array of
		 *      name -> value pairs.
		 * @private
		 */
		_asParamArray: function(parameters) {
			let result = [],
				value,
				name;
			if (Array.isArray(parameters)) {
				result = parameters;
			}
			else if (isPlainObject(parameters)) {
				for (name in parameters) {
					if (name && parameters.hasOwnProperty(name)) {
						value = parameters[name];
						result.push({name: name, value: value});
					}
				}
			}

			for (let i = 0; i < result.length; i++) {
				if (result[i] === null) {
					result.splice(i, 1);
					i--;
				}
			}

			return result;
		},

		/**
		 * Executes all functions to calculate any dynamic extra parameters
		 *
		 * @param attrs The Ajax request attributes
		 * @returns {String} A query string snippet with any calculated request
		 *  parameters. An empty string if there are no dynamic parameters in attrs
		 * @private
		 */
		_calculateDynamicParameters: function(attrs) {
			const deps = attrs.dep;
			let params = [];

			for (let i = 0; i < deps.length; i++) {
				const dep = deps[i];
				let extraParam;
				if (Wicket.isFunction(dep)) {
					extraParam = dep(attrs);
				} else {
					extraParam = new Function('attrs', dep)(attrs);
				}
				extraParam = this._asParamArray(extraParam);
				params = params.concat(extraParam);
			}
			return params;
		},

		/**
		 * Executes or schedules for execution #doAjax()
		 *
		 * @param {Object} attrs - the Ajax request attributes configured at the server side
		 */
		ajax: function (attrs) {
			this._initializeDefaults(attrs);

			const res = Wicket.channelManager.schedule(attrs.ch, Wicket.bind(function () {
				this.doAjax(attrs);
			}, this));
			return res !== null ? res: true;
		},

		/**
		 * Is an element still present for Ajax requests.
		 */
		_isPresent: function(id) {
			if (isUndef(id)) {
				// no id so no check whether present
				return true;
			}

			const element = Wicket.$(id);
			if (isUndef(element)) {
				// not present
				return false;
			}

			// present if no attributes at all or not a placeholder
			return (!element.hasAttribute || !element.hasAttribute('data-wicket-placeholder'));
		},

		/**
		 * Handles execution of Ajax calls.
		 *
		 * @param {Object} attrs - the Ajax request attributes configured at the server side
		 */
		doAjax: function (attrs) {

			// the headers to use for each Ajax request
			const headers = {
				'Wicket-Ajax': 'true',
				'Wicket-Ajax-BaseURL': getAjaxBaseUrl()
			};

			let url = attrs.u;

			// the request (extra) parameters
			let data = this._asParamArray(attrs.ep);

			const self = this;

			// the precondition to use if there are no explicit ones
			const defaultPrecondition = [ function (attributes) {
				return self._isPresent(attributes.c) && self._isPresent(attributes.f);
			}];

			// a context that brings the common data for the success/fialure/complete handlers
			const context = {
				attrs: attrs,

				// initialize the array for steps (closures that execute each action)
				steps: []
			};
			const we = Wicket.Event;
			const topic = we.Topic;

			if (Wicket.Focus.lastFocusId) {
				// WICKET-6568 might contain non-ASCII
				headers["Wicket-FocusedElementId"] = Wicket.Form.encode(Wicket.Focus.lastFocusId);
			}

			self._executeHandlers(attrs.bh, attrs);
			we.publish(topic.AJAX_CALL_BEFORE, attrs);

			let preconditions = attrs.pre || [];
			preconditions = defaultPrecondition.concat(preconditions);
			if (Array.isArray(preconditions)) {

				const that = this._getTarget(attrs);

				for (let p = 0; p < preconditions.length; p++) {

					const precondition = preconditions[p];
					let result;
					if (Wicket.isFunction(precondition)) {
						result = precondition.call(that, attrs);
					} else {
						result = new Function(precondition).call(that, attrs);
					}
					if (result === false) {
						Wicket.Log.info("Ajax request stopped because of precondition check, url: %s", attrs.u);
						self.done(attrs);
						return false;
					}
				}
			}

			we.publish(topic.AJAX_CALL_PRECONDITION, attrs);

			if (attrs.f) {
				// serialize the form with id == attrs.f
				const form = Wicket.$(attrs.f);
				data = data.concat(Wicket.Form.serializeForm(form));

				// set the submitting component input name
				if (attrs.sc) {
					const scName = attrs.sc;
					data = data.concat({name: scName, value: 1});
				}
			} else if (attrs.c && !Wicket.isWindow(attrs.c)) {
				// serialize just the form component with id == attrs.c
				const el = Wicket.$(attrs.c);
				data = data.concat(Wicket.Form.serializeElement(el, attrs.sr));
			}

			// collect the dynamic extra parameters
			if (Array.isArray(attrs.dep)) {
				const dynamicData = this._calculateDynamicParameters(attrs);
				if (attrs.m.toLowerCase() === 'post') {
					data = data.concat(dynamicData);
				} else {
					const separator = url.indexOf('?') > -1 ? '&' : '?';
					url = url + separator + paramsToQueryString(dynamicData);
				}
			}

			let isMultipart = false;
			if (attrs.mp) {
				try {
					const formData = new FormData();
					for (let i = 0; i < data.length; i++) {
						formData.append(data[i].name, data[i].value || "");
					}

					data = formData;
					isMultipart = true;
				} catch (exception) {
					Wicket.Log.error("Ajax multipart not supported:", exception);
				}
			}

			Wicket.Log.info("Executing Ajax request");
			Wicket.Log.debug(attrs);

			// execute the request
			const xhr = ajaxRequest({
				url: url,
				type: attrs.m,

				beforeSend: function (xhr, settings) {
					self._executeHandlers(attrs.bsh, attrs, xhr, settings);
					we.publish(topic.AJAX_CALL_BEFORE_SEND, attrs, xhr, settings);

					if (attrs.i) {
						// show the indicator
						Wicket.DOM.showIncrementally(attrs.i);
					}
				},
				data: data,
				dataType: attrs.dt,
				async: attrs.async,
				timeout: attrs.rt,
				cache: false,
				headers: headers,
				success: function(data, textStatus, xhr) {
					if (attrs.wr) {
						self.processAjaxResponse(data, textStatus, xhr, context);
					} else {
						self._executeHandlers(attrs.sh, attrs, xhr, data, textStatus);
						we.publish(topic.AJAX_CALL_SUCCESS, attrs, xhr, data, textStatus);
					}
				},
				error: function(xhr, textStatus, errorMessage) {
					if (xhr.status === 301 && xhr.getResponseHeader('Ajax-Location')) {
						self.processAjaxResponse(data, textStatus, xhr, context);
					} else {
						self.failure(context, xhr, errorMessage, textStatus);
					}
				},
				complete: function (xhr, textStatus) {

					context.steps.push((function (notify) {
						if (attrs.i && context.isRedirecting !== true) {
							Wicket.DOM.hideIncrementally(attrs.i);
						}

						self._executeHandlers(attrs.coh, attrs, xhr, textStatus);
						we.publish(topic.AJAX_CALL_COMPLETE, attrs, xhr, textStatus);

						self.done(attrs);
						return FunctionsExecuter.DONE;
					}).bind(self));

					const executer = new FunctionsExecuter(context.steps);
					executer.start();
				}
			});

			// execute after handlers right after the Ajax request is fired
			self._executeHandlers(attrs.ah, attrs);
			we.publish(topic.AJAX_CALL_AFTER, attrs);

			return xhr;
		},

		/**
		 * Method that processes a manually supplied <ajax-response>.
		 *
		 * @param data {XmlDocument} - the <ajax-response> XML document
		 */
		process: function(data) {
			const context =  {
					attrs: {},
					steps: []
				};
			const xmlDocument = Wicket.Xml.parse(data);
			this.loadedCallback(xmlDocument, context);
			const executer = new FunctionsExecuter(context.steps);
			executer.start();
		},

		/**
		 * Method that processes the <ajax-response> in the context of an XMLHttpRequest.
		 *
		 * @param data {XmlDocument} - the <ajax-response> XML document
		 * @param textStatus {String} - the response status as text (e.g. 'success', 'parsererror', etc.)
		 * @param xhr {XMLHttpRequest} - the XMLHttpRequest of the Ajax call
		 * @param context {Object} - the request context with the Ajax request attributes and the FunctionExecuter's steps
		 */
		processAjaxResponse: function (data, textStatus, xhr, context) {

			if (xhr.readyState === 4) {

				// first try to get the redirect header
				let redirectUrl;
				try {
					redirectUrl = xhr.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}

				// the redirect header was set, go to new url
				if (typeof(redirectUrl) !== "undefined" && redirectUrl !== null && redirectUrl !== "") {

					// In case the page isn't really redirected. For example say the redirect is to an octet-stream.
					// A file download popup will appear but the page in the browser won't change.
					this.success(context);

					const withScheme  = /^[a-z][a-z0-9+.-]*:\/\//;  // checks whether the string starts with a scheme

					// support/check for non-relative redirectUrl like as provided and needed in a portlet context
					if (redirectUrl.charAt(0) === '/' || withScheme.test(redirectUrl)) {
						context.isRedirecting = true;
						Wicket.Ajax.redirect(redirectUrl);
					}
					else {
						let urlDepth = 0;
						while (redirectUrl.substring(0, 3) === "../") {
							urlDepth++;
							redirectUrl = redirectUrl.substring(3);
						}
						// Make this a string.
						let calculatedRedirect = window.location.pathname;
						while (urlDepth > -1) {
							urlDepth--;
							const i = calculatedRedirect.lastIndexOf("/");
							if (i > -1) {
								calculatedRedirect = calculatedRedirect.substring(0, i);
							}
						}
						calculatedRedirect += "/" + redirectUrl;

						context.isRedirecting = true;
						Wicket.Ajax.redirect(calculatedRedirect);
					}
				}
				else {
					// no redirect, just regular response
					Wicket.Log.info("Received ajax response (%s characters)", xhr.responseText.length);
					Wicket.Log.debug(xhr.responseXML);

					// invoke the loaded callback with an xml document
					return this.loadedCallback(data, context);
				}
			}
		},

		// Processes the response
		loadedCallback: function (envelope, context) {
			// To process the response, we go through the xml document and add a function for every action (step).
			// After this is done, a FunctionExecuter object asynchronously executes these functions.
			// The asynchronous execution is necessary, because some steps might involve loading external javascript,
			// which must be asynchronous, so that it doesn't block the browser, but we also have to maintain
			// the order in which scripts are loaded and we have to delay the next steps until the script is
			// loaded.
			try {
				const root = envelope.getElementsByTagName("ajax-response")[0];

				// the root element must be <ajax-response
				if (isUndef(root) || root.tagName !== "ajax-response") {
					this.failure(context, null, "Could not find root <ajax-response> element", null);
					return;
				}

				const steps = context.steps;

				// go through the ajax response and process priority evaluations and
				// header contributions first
				for (let i = 0; i < root.childNodes.length; ++i) {
					const childNode = root.childNodes[i];
					if (childNode.tagName === "header-contribution") {
						this.processHeaderContribution(context, childNode);
					} else if (childNode.tagName === "priority-evaluate") {
						this.processHeaderContribution(context, childNode);
					}
				}

				// ... then add components, process remaining evaluations and a
				// possible redirect
				let stepIndexOfLastReplacedComponent = -1;
				for (let c = 0; c < root.childNodes.length; ++c) {
					const node = root.childNodes[c];

					if (node.tagName === "component") {
						if (stepIndexOfLastReplacedComponent === -1) {
							this.processFocusedComponentMark(context);
						}
						stepIndexOfLastReplacedComponent = steps.length;
						this.processComponent(context, node);
					} else if (node.tagName === "evaluate") {
						this.processHeaderContribution(context, node);
					} else if (node.tagName === "redirect") {
						this.processRedirect(context, node);
					}
				}
				if (stepIndexOfLastReplacedComponent !== -1) {
					this.processFocusedComponentReplaceCheck(steps, stepIndexOfLastReplacedComponent);
				}

				// add the last step, which should trigger the success call the done method on request
				this.success(context);

			} catch (exception) {
				this.failure(context, null, exception, null);
			}
		},

		// Adds a closure to steps that should be invoked after all other steps have been successfully executed
		success: function (context) {
			context.steps.push((function (notify) {
				Wicket.Log.info("Response processed successfully.");

				const attrs = context.attrs;
				this._executeHandlers(attrs.sh, attrs, null, null, 'success');
				Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_SUCCESS, attrs, null, null, 'success');

				Wicket.Focus.requestFocus();

				// continue to next step (which should make the processing stop, as success should be the final step)
				return FunctionsExecuter.DONE;
			}).bind(this));
		},

		// On ajax request failure
		failure: function (context, xhr, errorMessage, textStatus) {
			context.steps.push((function (notify) {
				if (errorMessage) {
					Wicket.Log.error("Wicket.Ajax.Call.failure: Error while parsing response: %s", errorMessage);
				}
				const attrs = context.attrs;
				this._executeHandlers(attrs.fh, attrs, xhr, errorMessage, textStatus);
				Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_FAILURE, attrs, xhr, errorMessage, textStatus);

				return FunctionsExecuter.DONE;
			}).bind(this));
		},

		done: function (attrs) {
			this._executeHandlers(attrs.dh, attrs);
			Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_DONE, attrs);

			Wicket.channelManager.done(attrs.ch);
		},

		// Adds a closure that replaces a component
		processComponent: function (context, node) {
			context.steps.push(function (notify) {
				// get the component id
				const compId = node.getAttribute("id");

				// get existing component
				const element = Wicket.$(compId);

				if (isUndef(element)) {
					Wicket.Log.error("Wicket.Ajax.Call.processComponent: Component with id '%s' was not found while trying to perform markup update. " +
						"Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.", compId);
				} else {
					const text = Wicket.DOM.text(node);

					// replace the component
					Wicket.DOM.replace(element, text);
				}
				// continue to next step
				return FunctionsExecuter.DONE;
			});
		},

		// Adds a closure that processes a header contribution
		processHeaderContribution: function (context, node) {
			const c = Wicket.Head.Contributor;
			c.processContribution(context, node);
		},

		// Adds a closure that processes a redirect
		processRedirect: function (context, node) {
			const text = Wicket.DOM.text(node);
			Wicket.Log.info("Redirecting to: %s", text);
			context.isRedirecting = true;
			Wicket.Ajax.redirect(text);
		},

		// mark the focused component so that we know if it has been replaced by response
		processFocusedComponentMark: function (context) {
			context.steps.push(function (notify) {
				Wicket.Focus.markFocusedComponent();

				// continue to next step
				return FunctionsExecuter.DONE;
			});
		},

		// detect if the focused component was replaced
		processFocusedComponentReplaceCheck: function (steps, lastReplaceComponentStep) {
			// add this step imediately after all components have been replaced
			steps.splice(lastReplaceComponentStep + 1, 0, function (notify) {
				Wicket.Focus.checkFocusedComponentReplaced();

				// continue to next step
				return FunctionsExecuter.DONE;
			});
		}
	};


	/**
	 * Throttler's purpose is to make sure that ajax requests wont be fired too often.
	 */
	Wicket.ThrottlerEntry = Wicket.Class.create();

	Wicket.ThrottlerEntry.prototype = {
		initialize: function (func) {
			this.func = func;
			this.timestamp = new Date().getTime();
			this.timeoutVar = undefined;
		},

		getTimestamp: function () {
			return this.timestamp;
		},

		getFunc: function () {
			return this.func;
		},

		setFunc: function (func) {
			this.func = func;
		},

		getTimeoutVar: function () {
			return this.timeoutVar;
		},

		setTimeoutVar: function (timeoutVar) {
			this.timeoutVar = timeoutVar;
		}
	};

	Wicket.Throttler = Wicket.Class.create();

	// declare it as static so that it can be shared between Throttler instances
	Wicket.Throttler.entries = [];

	Wicket.Throttler.prototype = {

		/* "postponeTimerOnUpdate" is an optional parameter. If it is set to true, then the timer is
		   reset each time the throttle function gets called. Use this behaviour if you want something
		   to happen at X milliseconds after the *last* call to throttle.
		   If the parameter is not set, or set to false, then the timer is not reset. */
		initialize: function (postponeTimerOnUpdate) {
			this.postponeTimerOnUpdate = postponeTimerOnUpdate;
		},

		throttle: function (id, millis, func) {
			const entries = Wicket.Throttler.entries;
			let entry = entries[id];
			const me = this;
			if (typeof(entry) === 'undefined') {
				entry = new Wicket.ThrottlerEntry(func);
				entry.setTimeoutVar(window.setTimeout(function() { me.execute(id); }, millis));
				entries[id] = entry;
			} else {
				entry.setFunc(func);
				if (this.postponeTimerOnUpdate)
				{
					window.clearTimeout(entry.getTimeoutVar());
					entry.setTimeoutVar(window.setTimeout(function() { me.execute(id); }, millis));
				}
			}
		},

		execute: function (id) {
			const entries = Wicket.Throttler.entries;
			const entry = entries[id];
			if (typeof(entry) !== 'undefined') {
				const func = entry.getFunc();
				entries[id] = undefined;
				return func();
			}
		}
	};


	Wicket.channelManager = new Wicket.ChannelManager();

	Wicket.throttler = new Wicket.Throttler();

	Wicket.$ = function (arg) {
		return Wicket.DOM.get(arg);
	};

	/**
	 * returns if the element belongs to current document
	 * if the argument is not element, function returns true
	 */
	Wicket.$$ = function (element) {
		return Wicket.DOM.inDoc(element);
	};

	/**
	 * Merges two objects. Values of the second will overwrite values of the first.
	 *
	 * @param {Object} object1 - the first object to merge
	 * @param {Object} object2 - the second object to merge
	 * @return {Object} a new object with the values of object1 and object2
	 */
	Wicket.merge = function(object1, object2) {
		return Object.assign({}, object1, object2);
	};

	/**
	 * Takes a function and returns a new one that will always have a particular context, i.e. 'this' will be the passed context.
	 *
	 * @param {Function} fn - the function which context will be set
	 * @param {Object} context - the new context for the function
	 * @return {Function} the original function with the changed context
	 */
	Wicket.bind = function(fn, context) {
		return fn.bind(context);
	};

	Wicket.Xml = {
		parse: function (text) {
			const parser = new DOMParser();

			const xmlDocument = parser.parseFromString(text, "text/xml");

			return xmlDocument;
		}
	};

	/**
	 * Form serialization
	 *
	 * To post a form using Ajax Wicket first needs to serialize it, which means composing a string
	 * from form elments names and values. The string will then be set as body of POST request.
	 */

	Wicket.Form = {
		encode: function (text) {
			return window.encodeURIComponent(text);
		},

		/**
		 * Serializes HTMLFormSelectElement to URL encoded key=value string.
		 *
		 * @param select {HTMLFormSelectElement} - the form element to serialize
		 * @return an object of key -> value pair where 'value' can be an array of Strings if the select is .multiple,
		 *		or empty object if the form element is disabled.
		 */
		serializeSelect: function (select){
			const result = [];
			if (select && select.disabled === false) {
				const name = select.name;
				if (select.multiple) {
					for (let i = 0; i < select.options.length; i++) {
						const option = select.options[i];
						if (option.selected && !option.disabled) {
							result.push({ name: name, value: option.value });
						}
					}
				} else {
					const selectedIndex = select.selectedIndex;
					const value = selectedIndex >= 0 ? select.options[selectedIndex].value : null;
					result.push({ name: name, value: value });
				}
			}
			return result;
		},

		/**
		 * Serializes a form element to an array with a single element - an object
		 * with two keys - <em>name</em> and <em>value</em>.
		 *
		 * Example: [{"name": "searchTerm", "value": "abc"}].
		 *
		 * Note: this function intentionally ignores image and submit inputs.
		 *
		 * @param input {HtmlFormElement} - the form element to serialize
		 * @return the URL encoded key=value pair or empty string if the form element is disabled.
		 */
		serializeInput: function (input) {
			const result = [];
			if (input && input.type) {
				if (input.type === 'file') {
					for (let f = 0; f < input.files.length; f++) {
						result.push({"name" : input.name, "value" : input.files[f]});
					}
				} else if (input.type === 'image' || input.type === 'submit') {
					// intentionally ignored
				} else if (!input.disabled && input.name) {
					const checkable = (input.type === 'checkbox' || input.type === 'radio');
					if (!checkable || input.checked) {
						result.push({ name: input.name, value: input.value });
					}
				}
			}
			return result;
		},

		/**
		 * A hash of HTML form element to exclude from serialization
		 * As key the element's id is being used.
		 * As value - the string "true".
		 */
		excludeFromAjaxSerialization: {
		},

		/**
		 * Serializes a form element by checking its type and delegating the work to
		 * a more specific function.
		 *
		 * The form element will be ignored if it is registered as excluded in
		 * <em>Wicket.Form.excludeFromAjaxSerialization</em>
		 *
		 * @param element {HTMLFormElement} - the form element to serialize. E.g. HTMLInputElement
		 * @param serializeRecursively {Boolean} - a flag indicating whether to collect (submit) the
		 * 			name/value pairs for all HTML form elements children of the HTML element with
		 * 			the JavaScript listener
		 * @return An array with a single element - an object with two keys - <em>name</em> and <em>value</em>.
		 */
		serializeElement: function(element, serializeRecursively) {

			if (!element) {
				return [];
			}
			else if (typeof(element) === 'string') {
				element = Wicket.$(element);
			}

			if (Wicket.Form.excludeFromAjaxSerialization && element.id && Wicket.Form.excludeFromAjaxSerialization[element.id] === "true") {
				return [];
			}

			const tag = element.tagName.toLowerCase();
			if (tag === "select") {
				return Wicket.Form.serializeSelect(element);
			} else if (tag === "input" || tag === "textarea") {
				return Wicket.Form.serializeInput(element);
			} else {
				let result = [];
				if (serializeRecursively) {
					let elements = nodeListToArray(element.getElementsByTagName("input"));
					elements = elements.concat(nodeListToArray(element.getElementsByTagName("select")));
					elements = elements.concat(nodeListToArray(element.getElementsByTagName("textarea")));

					for (let i = 0; i < elements.length; ++i) {
						const el = elements[i];
						if (el.name && el.name !== "") {
							result = result.concat(Wicket.Form.serializeElement(el, serializeRecursively));
						}
					}
				}
				return result;
			}
		},

		serializeForm: function (form) {
			let result = [],
				elements;

			if (form) {
				if (form.tagName.toLowerCase() === 'form') {
					elements = form.elements;
				} else {
					do {
						form = form.parentNode;
					} while (form.tagName.toLowerCase() !== "form" && form.tagName.toLowerCase() !== "body");

					elements = nodeListToArray(form.getElementsByTagName("input"));
					elements = elements.concat(nodeListToArray(form.getElementsByTagName("select")));
					elements = elements.concat(nodeListToArray(form.getElementsByTagName("textarea")));
				}
			}

			for (let i = 0; i < elements.length; ++i) {
				const el = elements[i];
				if (el.name && el.name !== "") {
					result = result.concat(Wicket.Form.serializeElement(el, false));
				}
			}
			return result;
		},

		serialize: function (element, dontTryToFindRootForm) {
			if (typeof(element) === 'string') {
				element = Wicket.$(element);
			}

			if (element.tagName.toLowerCase() === "form") {
				return Wicket.Form.serializeForm(element);
			} else {
				// try to find a form in DOM parents
				const elementBck = element;

				if (dontTryToFindRootForm !== true) {
					do {
						element = element.parentNode;
					} while(element.tagName.toLowerCase() !== "form" && element.tagName.toLowerCase() !== "body");
				}

				if (element.tagName.toLowerCase() === "form"){
					return Wicket.Form.serializeForm(element);
				} else {
					// there is not form in dom hierarchy
					// simulate it
					const form = document.createElement("form");
					const parent = elementBck.parentNode;

					parent.replaceChild(form, elementBck);
					form.appendChild(elementBck);
					const result = Wicket.Form.serializeForm(form);
					parent.replaceChild(elementBck, form);

					return result;
				}
			}
		}
	};

	/**
	 * DOM nodes serialization functionality
	 *
	 * The purpose of these methods is to return a string representation
	 * of the DOM tree.
	 */
	Wicket.DOM = {

		/**
		 * Shows an element
		 * @param {HTMLElement | String} e   The HTML element (or its id) to show
		 * @param {String} display  The value of CSS display property to use,
		 *      e.g. 'block', 'inline'. Optional
		 */
		show: function (e, display) {
			e = Wicket.$(e);
			if (e !== null) {
				if (isUndef(display)) {
					// no explicit 'display' value is requested so let the stylesheet decide,
					// falling back to the browser's default display value for this tag if the
					// element still isn't actually rendering with a size afterwards (covers
					// both an ancestor/stylesheet hiding it and it being empty of content)
					e.style.display = '';
					const rects = e.getClientRects();
					if (!rects.length || !rects[0].width) {
						e.style.display = defaultDisplay(e.tagName);
					}
				} else {
					e.style.display = display;
				}
				e.removeAttribute("hidden");
			}
		},

		/** hides an element */
		hide: function (e) {
			e = Wicket.$(e);
			if (e !== null) {
				e.style.display = 'none';
				e.setAttribute("hidden", "");
			}
		},

		/**
		 * Add or remove one or more classes from each element in the
		 * set of matched elements, depending on either the class's presence
		 * or the value of the switch argument.
		 *
		 * @param {String} elementId The markup id of the element that will be manipulated.
		 * @param {String} cssClass One or more class names (separated by spaces)
		 *        to be toggled for each element in the matched set.
		 * @param {Boolean} Switch A Boolean (not just truthy/falsy) value to
		 *        determine whether the class should be added or removed.
		 */
		toggleClass: function(elementId, cssClass, Switch) {
			const e = document.getElementById(elementId);
			if (e) {
				const classes = cssClass.split(/\s+/);
				for (let i = 0; i < classes.length; i++) {
					if (classes[i]) {
						e.classList.toggle(classes[i], Switch);
					}
				}
			}
		},

		/** call-counting implementation of Wicket.DOM.show() */
		showIncrementally: function (e) {
			e = Wicket.$(e);
			if (e === null) {
				return;
			}
			let count = e.getAttribute("showIncrementallyCount");
			count = parseInt(isUndef(count) ? 0 : count, 10);
			if (count >= 0) {
				Wicket.DOM.show(e);
			}
			e.setAttribute("showIncrementallyCount", count + 1);
		},

		/** call-counting implementation of Wicket.DOM.hide() */
		hideIncrementally: function(e) {
			e = Wicket.$(e);
			if (e === null) {
				return;
			}
			let count = e.getAttribute("showIncrementallyCount");
			count = parseInt(isUndef(count) ? 0 : count - 1, 10);
			if (count <= 0) {
				Wicket.DOM.hide(e);
			}
			e.setAttribute("showIncrementallyCount", count);
		},

		get: function (arg) {
			if (isUndef(arg)) {
				return null;
			}
			if (arguments.length > 1) {
				const e = [];
				for (let i = 0; i < arguments.length; i++) {
					e.push(Wicket.DOM.get(arguments[i]));
				}
				return e;
			} else if (typeof arg === 'string') {
				return document.getElementById(arg);
			} else {
				return arg;
			}
		},

		/**
		 * returns if the element belongs to current document
		 * if the argument is not element, function returns true
		 */
		inDoc: function (element) {
			if (element === window) {
				return true;
			}
			if (typeof(element) === "string") {
				element = Wicket.$(element);
			}
			if (isUndef(element) || isUndef(element.tagName)) {
				return false;
			}

			const id = element.getAttribute('id');
			if (isUndef(id) || id === "") {
				return element.ownerDocument === document;
			}
			else {
				return document.getElementById(id) === element;
			}
		},

		/**
		 * A cross-browser method that replaces the markup of an element. The behavior
		 * is similar to calling element.outerHtml=text in internet explorer. However
		 * this method also takes care of executing javascripts within the markup on
		 * browsers that don't do that automatically.
		 * Also this method takes care of replacing table elements (tbody, tr, td, thead)
		 * by parsing the markup with a &lt;template&gt; element, which (unlike setting
		 * innerHTML on a plain container) correctly parses such elements without needing
		 * them to be wrapped in a &lt;table&gt;.
		 *
		 * This method sends notifications to all subscribers for channels with names
		 * '/dom/node/removing' with the element that is going to be replaced and
		 * '/dom/node/added' with the newly created element (the replacement).
		 *
		 * Note: the 'to be replaced' element must have an 'id' attribute
		 */
		replace: function (element, text) {

			const we = Wicket.Event;
			const topic = we.Topic;

			we.publish(topic.DOM_NODE_REMOVING, element);

			if (element.tagName.toLowerCase() === "title") {
				// match the text between the tags
				const titleText = />(.*?)</.exec(text)[1];
				document.title = titleText;
				return;
			} else {
				const cleanedText = text.trim();

				const template = document.createElement('template');
				template.innerHTML = cleanedText;
				reviveScripts(template.content);

				element.replaceWith(template.content);
			}

			const newElement = Wicket.$(element.id);
			if (newElement) {
				we.publish(topic.DOM_NODE_ADDED, newElement);
			}
		},

		add: function (element, text) {
			const we = Wicket.Event;
			const topic = we.Topic;

			const cleanedText = text.trim();

			const template = document.createElement('template');
			template.innerHTML = cleanedText;
			reviveScripts(template.content);

			element.append(template.content);

			const newElement = Wicket.$(element.id);
			if (newElement) {
				we.publish(topic.DOM_NODE_ADDED, newElement);
			}
		},

		remove: function (element) {
			const we = Wicket.Event;
			const topic = we.Topic;

			we.publish(topic.DOM_NODE_REMOVING, element);

			element.remove();
		},

		// Method for serializing DOM nodes to string
		// original taken from Tacos (http://tacoscomponents.jot.com)
		serializeNodeChildren: function (node) {
			if (isUndef(node)) {
				return "";
			}
			const result = [];

			if (node.childNodes.length > 0) {
				for (let i = 0; i < node.childNodes.length; i++) {
					const thisNode = node.childNodes[i];
					switch (thisNode.nodeType) {
						case 1: // ELEMENT_NODE
						case 5: // ENTITY_REFERENCE_NODE
							result.push(this.serializeNode(thisNode));
							break;
						case 8: // COMMENT
							result.push("<!--");
							result.push(thisNode.nodeValue);
							result.push("-->");
							break;
						case 4: // CDATA_SECTION_NODE
							result.push("<![CDATA[");
							result.push(thisNode.nodeValue);
							result.push("]]>");
							break;
						case 3: // TEXT_NODE
						case 2: // ATTRIBUTE_NODE
							result.push(thisNode.nodeValue);
							break;
						default:
							break;
					}
				}
			} else {
				result.push(node.textContent || node.text);
			}
			return result.join("");
		},

		serializeNode: function (node){
			if (isUndef(node)) {
				return "";
			}
			const result = [];
			result.push("<");
			result.push(node.nodeName);

			if (node.attributes && node.attributes.length > 0) {

				for (let i = 0; i < node.attributes.length; i++) {
					// serialize the attribute only if it has a meaningful value
					if (node.attributes[i].nodeValue) {
						result.push(" ");
						result.push(node.attributes[i].name);
						result.push("=\"");
						result.push(node.attributes[i].value);
						result.push("\"");
					}
				}
			}

			result.push(">");
			result.push(Wicket.DOM.serializeNodeChildren(node));
			result.push("</");
			result.push(node.nodeName);
			result.push(">");
			return result.join("");
		},

		// Utility function that determines whether given element is part of the current document
		containsElement: function (element) {
			const id = element.getAttribute("id");
			if (id) {
				return Wicket.$(id) !== null;
			}
			else {
				return false;
			}
		},

		/**
		 * Reads the text from the node's children nodes.
		 * @param node {DOMElement} the root node
		 */
		text: function (node) {
			if (isUndef(node)) {
				return "";
			}

			const result = [];

			if (node.childNodes.length > 0) {
				for (let i = 0; i < node.childNodes.length; i++) {
					const thisNode = node.childNodes[i];
					switch (thisNode.nodeType) {
						case 1: // ELEMENT_NODE
						case 5: // ENTITY_REFERENCE_NODE
							result.push(this.text(thisNode));
							break;
						case 3: // TEXT_NODE
						case 4: // CDATA_SECTION_NODE
							result.push(thisNode.nodeValue);
							break;
						default:
							break;
					}
				}
			} else {
				result.push(node.textContent || node.text);
			}

			return result.join("");
		}
	};

	/**
	 * The Ajax class handles low level details of creating XmlHttpRequest objects,
	 * as well as registering and execution of pre-call, post-call and failure handlers.
	 */
	 Object.assign(Wicket.Ajax, {

		Call: Wicket.Ajax.Call,

		/**
		 * Aborts the default event if attributes request it
		 *
		 * @param {Object} attrs - the Ajax request attributes configured at the server side
		 */
		_handleEventCancelation: function(attrs) {
			const evt = attrs.event;
			if (evt) {
				if (attrs.pd) {
					try {
						evt.preventDefault();
					} catch (ignore) {
						// WICKET-4986
					}
				}

				if (attrs.sp === "stop") {
					Wicket.Event.stop(evt);
				} else if (attrs.sp === "stopImmediate") {
					Wicket.Event.stop(evt, true);
				}
			}
		},

		get: function (attrs) {

			attrs.m = 'GET';

			return Wicket.Ajax.ajax(attrs);
		},

		post: function (attrs) {

			attrs.m = 'POST';

			return Wicket.Ajax.ajax(attrs);
		},

		ajax: function(attrs) {

			attrs.c = attrs.c || window;
			attrs.e = attrs.e || [ 'domready' ];

			if (!Array.isArray(attrs.e)) {
				attrs.e = [ attrs.e ];
			}

			attrs.e.forEach(function (evt) {
				Wicket.Event.add(attrs.c, evt, function (event, data) {
					const call = new Wicket.Ajax.Call();
					const attributes = Object.assign({}, attrs);

					if (evt !== "domready") {
						attributes.event = Wicket.Event.fix(event);
						if (data) {
							attributes.event.extraData = data;
						}
					}

					call._executeHandlers(attributes.ih, attributes);
					Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_INIT, attributes);

					const throttlingSettings = attributes.tr;
					if (throttlingSettings) {
						const postponeTimerOnUpdate = throttlingSettings.p || false;
						const throttler = new Wicket.Throttler(postponeTimerOnUpdate);
						throttler.throttle(throttlingSettings.id, throttlingSettings.d,
							Wicket.bind(function () {
								call.ajax(attributes);
							}, this));
					}
					else {
						call.ajax(attributes);
					}
					if (evt !== "domready") {
						Wicket.Ajax._handleEventCancelation(attributes);
					}
				}, null, attrs.sel);
			});
		},

		process: function(data) {
			const call = new Wicket.Ajax.Call();
			call.process(data);
		},

		/**
		 * An abstraction over native window.location.replace() to be able to suppress it for unit tests
		 *
		 * @param url The url to redirect to
		 */
		redirect: function(url) {
			window.location = url;
		}
	});

	/**
	 * Header contribution allows component to include custom javascript and stylesheet.
	 *
	 * Header contributor takes the code component would render to page head and
	 * interprets it just as browser would when loading a page.
	 * That means loading external javascripts and stylesheets, executing inline
	 * javascript and aplying inline styles.
	 *
	 * Header contributor also filters duplicate entries, so that it doesn't load/process
	 * resources that have been loaded.
	 * For inline styles and javascript, element id is used to filter out duplicate entries.
	 * For stylesheet and javascript references, url is used for filtering.
	 */
	Wicket.Head = {
		Contributor: {

			// Parses the header contribution element (returns a DOM tree with the contribution)
			parse: function (headerNode) {
				// the header contribution is stored as CDATA section in the header-contribution element,
				// we need to parse it since each header contribution needs to be treated separately

				// get the header contribution text and unescape it if necessary
				const text = Wicket.DOM.text(headerNode);

				// build a DOM tree of the contribution
				const xmldoc = Wicket.Xml.parse(text);
				return xmldoc;
			},

			// checks whether the passed node is the special "parsererror"
			// created by DOMParser if there is a error in XML parsing
			// TODO: move out of the API section
			_checkParserError: function (node) {
				let result = false;

				if (!isUndef(node.tagName) && node.tagName.toLowerCase() === "parsererror") {
					Wicket.Log.error("Error in parsing: %s", node.textContent);
					result = true;
				}
				return result;
			},

			// Processes the parsed header contribution
			processContribution: function (context, headerNode) {
				const xmldoc = this.parse(headerNode);
				const rootNode = xmldoc.documentElement;

				// Firefox and Opera reports the error in the documentElement
				if (this._checkParserError(rootNode)) {
					return;
				}

				// go through the individual elements and process them according to their type
				for (let i = 0; i < rootNode.childNodes.length; i++) {
					let node = rootNode.childNodes[i];

					// Chromium reports the error as a child node
					if (this._checkParserError(node)) {
						return;
					}

					if (!isUndef(node.tagName)) {
						let name = node.tagName.toLowerCase();

						// it is possible that a reference is surrounded by a <wicket:link
						// in that case, we need to find the inner element
						if (name === "wicket:link") {
							for (let j = 0; j < node.childNodes.length; ++j) {
								const childNode = node.childNodes[j];
								// try to find a regular node inside wicket:link
								if (childNode.nodeType === 1) {
									node = childNode;
									name = node.tagName.toLowerCase();
									break;
								}
							}
						}

						// process the element
						if (name === "link") {
							this.processLink(context, node);
						} else if (name === "script") {
							this.processScript(context, node);
						} else if (name === "style") {
							this.processStyle(context, node);
						} else if (name === "meta") {
							this.processMeta(context, node);
						}
					} else if (node.nodeType === 8) { // comment type
						this.processComment(context, node);
					}
				}
			},

			// Process an external stylesheet element
			processLink: function (context, node) {
				context.steps.push(function (notify) {
					const res = Wicket.Head.containsElement(node, "href");
					const oldNode = res.oldNode;
					if (res.contains) {
						// an element with same href attribute is in document, skip it
						return FunctionsExecuter.DONE;
					} else if (oldNode) {
						// remove another external element with the same id but different href
						oldNode.parentNode.removeChild(oldNode);
					}

					// create link element
					const css = Wicket.Head.createElement("link");

					// copy supplied attributes only.
					const attributes = node.attributes;
					for (let a = 0; a < attributes.length; a++) {
						css.setAttribute(attributes[a].name, attributes[a].value);
					}

					let notifyCalled = false;
					function doNotify() {
						if (!notifyCalled) {
							notifyCalled = true;
							notify();
						}
					}
					css.onerror = doNotify;
					css.onload = doNotify;
					// add element to head
					Wicket.Head.addElement(css);

					return FunctionsExecuter.ASYNC;
				});
			},

			// Process an inline style element
			processStyle: function (context, node) {
				context.steps.push(function (notify) {
					// if element with same id is already in document, skip it
					if (Wicket.DOM.containsElement(node)) {
						return FunctionsExecuter.DONE;
					}
					// serialize the style to string
					const content = Wicket.DOM.serializeNodeChildren(node);

					// create style element
					const style = Wicket.Head.createElement("style");

					// copy id attribute
					style.id = node.getAttribute("id");
					// copy nonce attribute
					style.nonce = node.getAttribute("nonce");

					const textNode = document.createTextNode(content);
					style.appendChild(textNode);

					Wicket.Head.addElement(style);

					// continue to next step
					return FunctionsExecuter.DONE;
				});
			},

			// Process a script element (both inline and external)
			processScript: function (context, node) {
				context.steps.push(function (notify) {

					if (!node.getAttribute("src") && Wicket.DOM.containsElement(node)) {
						// if an inline element with same id is already in document, skip it
						return FunctionsExecuter.DONE;
					} else {
						const res = Wicket.Head.containsElement(node, "src");
						const oldNode = res.oldNode;
						if (res.contains) {
							// an element with same src attribute is in document, skip it
							return FunctionsExecuter.DONE;
						} else if (oldNode) {
							// remove another external element with the same id but different src
							oldNode.parentNode.removeChild(oldNode);
						}
					}

					// convert the XML node to DOM node
					const scriptDomNode = document.createElement("script");
					const attrs = node.attributes;
					for (let a = 0; a < attrs.length; a++) {
						const attr = attrs[a];
						scriptDomNode[attr.name] = attr.value;
					}

					// determine whether it is external javascript (has src attribute set)
					const src = node.getAttribute("src");
					if (src !== null && src !== "") {
						const onScriptReady = function () {
							notify();
						};

						// first check for feature support
						if (typeof(scriptDomNode.onload) !== 'undefined') {
							scriptDomNode.onload = onScriptReady;
						} else if (typeof(scriptDomNode.onreadystatechange) !== 'undefined') {
							scriptDomNode.onreadystatechange = function () {
								if (scriptDomNode.readyState === 'loaded' || scriptDomNode.readyState === 'complete') {
									onScriptReady();
								}
							};
						} else {
							// as a final resort notify after the current function execution
							window.setTimeout(onScriptReady, 10);
						}

						Wicket.Head.addElement(scriptDomNode);

						return FunctionsExecuter.ASYNC;
					} else {
						const suspension = {
							suspended: 0,

							suspend: function() {
								suspension.suspended++;
							},

							release: function() {
								suspension.suspended--;
								if (suspension.suspended === 0) {
									notify();
								}
							}
						};

						// serialize the element content to string
						let text = Wicket.DOM.serializeNodeChildren(node);
						// get rid of prefix and suffix, they are not eval-d correctly
						text = text.replace(/^\n\/\*<!\[CDATA\[\*\/\n/, "");
						text = text.replace(/\n\/\*\]\]>\*\/\n$/, "");

						try {
							Wicket.Ajax._currentSuspension = suspension;

							scriptDomNode.innerHTML = text;

							const id = node.getAttribute("id");
							Wicket.Head.addElement(scriptDomNode, typeof(id) !== "string" || id.length === 0);
						} catch (exception) {
							Wicket.Log.error("Ajax.Call.processEvaluation: Exception evaluating javascript: %s", text, exception);
						} finally {
							Wicket.Ajax.currentSuspension = undefined;
						}

						// continue to next step
						if (suspension.suspended === 0) {
							// execution finished without suspension, just continue to next step
							return FunctionsExecuter.DONE;
						} else {
							// suspended, signal asynchronous execution of next step
							return FunctionsExecuter.ASYNC;
						}
					}
				});
			},

			processMeta: function (context, node) {
				context.steps.push(function (notify) {
					const meta = Wicket.Head.createElement("meta"),
						attrs = node.attributes,
						name = node.getAttribute("name"),
						httpEquiv = node.getAttribute("http-equiv");

					if (name) {
						const existingByName = document.querySelectorAll('meta[name="' + name + '"]');
						for (let n = 0; n < existingByName.length; n++) {
							existingByName[n].remove();
						}
					} else if (httpEquiv) {
						const existingByHttpEquiv = document.querySelectorAll('meta[http-equiv="' + httpEquiv + '"]');
						for (let h = 0; h < existingByHttpEquiv.length; h++) {
							existingByHttpEquiv[h].remove();
						}
					}

					for (let a = 0; a < attrs.length; a++) {
						meta.setAttribute(attrs[a].name, attrs[a].value);
					}

					Wicket.Head.addElement(meta);

					return FunctionsExecuter.DONE;
				});
			},

			// process (conditional) comments
			processComment: function (context, node) {
				context.steps.push(function (notify) {
					const comment = document.createComment(node.nodeValue);
					Wicket.Head.addElement(comment);
					return FunctionsExecuter.DONE;
				});
			}
		},

		// Creates an element in document
		createElement: function (name) {
			if (isUndef(name) || name === '') {
				Wicket.Log.error('Cannot create an element without a name');
				return;
			}
			return document.createElement(name);
		},

		// Adds the element to page head
		addElement: function (element, remove) {
			const headItems = document.querySelector('head meta[name="wicket.header.items"]');
			if (headItems) {
				headItems.parentNode.insertBefore(element, headItems);
			} else {
				const head = document.querySelector("head");
				if (head) {
					head.appendChild(element);
				}
			}

			if (remove) {
				element.parentNode.removeChild(element);
			}
		},

		// Returns true, if the page head contains element that has attribute with
		// name mandatoryAttribute same as the given element and their names match.
		//
		// e.g. Wicket.Head.containsElement(myElement, "src") return true, if there
		// is an element in head that is of same type as myElement, and whose src
		// attribute is same as myElement.src.
		containsElement: function (element, mandatoryAttribute) {
			const attr = element.getAttribute(mandatoryAttribute);
			if (isUndef(attr) || attr === "") {
				return {
					contains: false
				};
			}

			const elementTagName = element.tagName.toLowerCase();
			const elementId = element.getAttribute("id");
			let head = document.getElementsByTagName("head")[0];

			if (elementTagName === "script") {
				head = document;
			}

			const nodes = head.getElementsByTagName(elementTagName);

			for (let i = 0; i < nodes.length; ++i) {
				const node = nodes[i];

				// check node names and mandatory attribute values
				// we also have to check for attribute name that is suffixed by "_".
				// this is necessary for filtering script references
				if (node.tagName.toLowerCase() === elementTagName) {

					const loadedUrl = node.getAttribute(mandatoryAttribute);
					const loadedUrl_ = node.getAttribute(mandatoryAttribute+"_");
					if (loadedUrl === attr || loadedUrl_ === attr) {
						return {
							contains: true
						};
					} else if (elementId && elementId === node.getAttribute("id")) {
						return {
							contains: false,
							oldNode: node
						};
					}
				}
			}
			return {
				contains: false
			};
		}
	};

	// FOCUS FUNCTIONS

	Wicket.Focus = {
		lastFocusId : "",
		refocusLastFocusedComponentAfterResponse : false,
		focusSetFromServer : false,

		focusin: function (event) {
			event = Wicket.Event.fix(event);

			const target = event.target;
			if (target) {
				const WF = Wicket.Focus;
				WF.refocusLastFocusedComponentAfterResponse = false;
				const id = target.id;
				WF.lastFocusId = id;
				Wicket.Log.info("focus set on '%s'", id);
			}
		},

		focusout: function (event) {
			event = Wicket.Event.fix(event);

			const target = event.target;
			const WF = Wicket.Focus;
			if (target && WF.lastFocusId === target.id) {
				const id = target.id;
				if (WF.refocusLastFocusedComponentAfterResponse) {
					// replaced components seem to blur when replaced only on Safari - so do not modify lastFocusId so it gets refocused
					Wicket.Log.info("focus removed from '%s' but ignored because of component replacement", id);
				} else {
					WF.lastFocusId = null;
					Wicket.Log.info("focus removed from '%s'", id);
				}
			}
		},

		getFocusedElement: function () {
			const lastFocusId = Wicket.Focus.lastFocusId;
			if (lastFocusId) {
				const focusedElement = Wicket.$(lastFocusId);
				Wicket.Log.info("returned focused element:", focusedElement);
				return  focusedElement;
			}
		},

		setFocusOnId: function (id) {
			const WF = Wicket.Focus;
			if (id) {
				WF.refocusLastFocusedComponentAfterResponse = true;
				WF.focusSetFromServer = true;
				WF.lastFocusId = id;
				Wicket.Log.info("focus set on '%s' from server side", id);
			} else {
				WF.refocusLastFocusedComponentAfterResponse = false;
				Wicket.Log.info("refocus focused component after request stopped from server side");
			}
		},

		// mark the focused component so that we know if it has been replaced or not by response
		markFocusedComponent: function () {
			const WF = Wicket.Focus;
			const focusedElement = WF.getFocusedElement();
			if (focusedElement) {
				// create a property of the focused element that would not remain there if component is replaced
				focusedElement.wasFocusedBeforeComponentReplacements = true;
				WF.refocusLastFocusedComponentAfterResponse = true;
				WF.focusSetFromServer = false;
			} else {
				WF.refocusLastFocusedComponentAfterResponse = false;
			}
		},

		// detect if the focused component was replaced
		checkFocusedComponentReplaced: function () {
			const WF = Wicket.Focus;
			if (WF.refocusLastFocusedComponentAfterResponse) {
				const focusedElement = WF.getFocusedElement();
				if (focusedElement) {
					if (typeof(focusedElement.wasFocusedBeforeComponentReplacements) !== "undefined") {
						// focus component was not replaced - no need to refocus it
						WF.refocusLastFocusedComponentAfterResponse = false;
					}
				} else {
					// focused component dissapeared completely - no use to try to refocus it
					WF.refocusLastFocusedComponentAfterResponse = false;
					WF.lastFocusId = "";
				}
			}
		},

		requestFocus: function() {
			// if the focused component is replaced by the ajax response, a re-focus might be needed
			// (if focus was not changed from server) but if not, and the focus component should
			// remain the same, do not re-focus - fixes problem on IE6 for combos that have
			// the popup open (refocusing closes popup)
			const WF = Wicket.Focus;
			if (WF.refocusLastFocusedComponentAfterResponse && WF.lastFocusId) {
				const toFocus = Wicket.$(WF.lastFocusId);

				if (toFocus) {
					Wicket.Log.info("Calling focus on '%s'", WF.lastFocusId);

					const safeFocus = function() {
						try {
							toFocus.focus();
						} catch (ignore) {
							// WICKET-6209 IE fails if toFocus is disabled
						}
					};

					if (WF.focusSetFromServer) {
						// WICKET-5858
						window.setTimeout(safeFocus, 0);
					} else {
						// avoid loops like - onfocus triggering an event the modifies the tag => refocus => the event is triggered again
						const temp = toFocus.onfocus;
						toFocus.onfocus = null;

						// IE needs setTimeout (it seems not to call onfocus sync. when focus() is called
						window.setTimeout(function () { safeFocus(); toFocus.onfocus = temp; }, 0);
					}
				} else {
					WF.lastFocusId = "";
					Wicket.Log.info("Couldn't set focus on element with id '%s' because it is not in the page anymore", WF.lastFocusId);
				}
			} else if (WF.refocusLastFocusedComponentAfterResponse) {
				Wicket.Log.info("last focus id was not set");
			} else {
				Wicket.Log.info("refocus last focused component not needed/allowed");
			}
			Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
		}
	};

	/**
	 * Manages the functionality needed by AbstractAjaxTimerBehavior and its subclasses
	 */
	Wicket.Timer = {
		/**
		 * Schedules a timer
		 * @param {string} timerId - the identifier for the timer
		 * @param {function} f - the JavaScript function to execute after the timeout
		 * @param {number} delay - the timeout
		 */
		'set': function(timerId, f, delay) {
			if (typeof(Wicket.TimerHandles) === 'undefined') {
				Wicket.TimerHandles = {};
			}

			Wicket.Timer.clear(timerId);
			Wicket.TimerHandles[timerId] = setTimeout(function() {
				Wicket.Timer.clear(timerId);
				f();
			}, delay);
		},

		/**
		 * Clears a timer by its id
		 * @param {string} timerId - the identifier of the timer
		 */
		clear: function(timerId) {
			if (Wicket.TimerHandles && Wicket.TimerHandles[timerId]) {
				clearTimeout(Wicket.TimerHandles[timerId]);
				delete Wicket.TimerHandles[timerId];
			}
		},

		/**
		 * Clear all remaining timers.
		 */
		clearAll: function() {
			const WTH = Wicket.TimerHandles;
			if (WTH) {
				for (const th in WTH) {
					if (WTH.hasOwnProperty(th)) {
						Wicket.Timer.clear(th);
					}
				}
			}
		}
	};

	/**
	 * Events related code
	 */
	Wicket.Event = {
		idCounter: 0,

		// registry of the actual (wrapping) listener functions registered via Wicket.Event.add,
		// keyed by element, so that Wicket.Event.remove can find and detach them again given
		// only the original handler function, mirroring the (element, type, fn) triple used to add it
		_listenerRegistry: (typeof(WeakMap) !== 'undefined') ? new WeakMap() : null,

		// registry of subscribers per pub/sub topic, see #subscribe/#unsubscribe/#publish
		_topics: {},

		getId: function (element) {
			let id = element.id;

			if (typeof(id) === "string" && id.length > 0) {
				return id;
			} else {
				id = "wicket-generated-id-" + Wicket.Event.idCounter++;
				element.id = id;
				return id;
			}
		},

		keyCode: function (evt) {
			return Wicket.Event.fix(evt).keyCode;
		},

		/**
		 * Prevent event from bubbling up in the element hierarchy.
		 * @param evt {Event} - the event to stop
		 * @param immediate {Boolean} - true if the event should not be handled by other listeners registered
		 *      on the same HTML element. Optional
		 */
		stop: function (evt, immediate) {
			evt = Wicket.Event.fix(evt);
			if (immediate) {
				evt.stopImmediatePropagation();
			} else {
				evt.stopPropagation();
			}
			return evt;
		},

		/**
		 * Native DOM events don't need any normalization on the modern browsers Wicket
		 * targets, so this is effectively a passthrough. Kept for API compatibility with
		 * code that calls Wicket.Event.fix().
		 */
		fix: function (evt) {
			return evt;
		},

		/**
		 * Fires a (possibly custom) DOM event on an element.
		 *
		 * @param element {HTMLElement|String} - the element (or its id) to fire the event on
		 * @param event {String|Event} - the event type to fire, or an already created Event
		 * @param data {*} - optional extra data, made available to listeners registered
		 *      through Wicket.Event.add() as the second callback argument
		 */
		fire: function (element, event, data) {
			element = Wicket.$(element);
			if (element) {
				let evt = event;
				if (typeof(event) === 'string') {
					evt = new Event(event, { bubbles: true, cancelable: true });
				}
				if (typeof(data) !== 'undefined') {
					// namespaced so it can never collide with a native event property
					// (e.g. MouseEvent#detail, which is unrelated and always present)
					evt.wicketExtraData = data;
				}
				element.dispatchEvent(evt);
			}
		},

		/**
		 * Binds an event listener for an element
		 *
		 * Also supports the special 'domready' event on window.
		 * 'domready' is event fired when the DOM is complete, but
		 * before loading external resources (images, scripts, ...)
		 *
		 * @param element {HTMLElement} The host HTML element
		 * @param type {String} The type of the DOM event. Several space separated
		 *      event types can be given at once, e.g. 'input change'
		 * @param fn {Function} The event handler to bind
		 * @param data {Object} Extra data for the event
		 * @param selector {String} A selector string to filter the descendants of the selected
		 *      elements that trigger the event. If the selector is null or omitted,
		 *      the event is always triggered when it reaches the selected element.
		 */
		add: function (element, type, fn, data, selector) {
			if (type === 'domready') {
				if (document.readyState !== 'loading') {
					fn();
				} else {
					document.addEventListener('DOMContentLoaded', fn, { once: true });
				}
			} else if (type === 'load' && element === window) {
				if (document.readyState === 'complete') {
					fn();
				} else {
					window.addEventListener('load', fn, { once: true });
				}
			} else {
				let el = element;
				if (typeof(element) === 'string') {
					el = document.getElementById(element);
				}

				if (!el && Wicket.Log) {
					Wicket.Log.error("Cannot bind a listener for event '%s' because the element is not in the DOM", type, element);
					return element;
				}

				const types = type.split(/\s+/);
				/* jshint loopfunc: true */
				// the IIFE below intentionally captures each iteration's eventType by value
				for (let t = 0; t < types.length; t++) {
					(function (eventType) {
						const wrapper = function (evt) {
							const extraData = (typeof(evt.wicketExtraData) !== 'undefined') ? evt.wicketExtraData : data;
							if (selector) {
								const target = evt.target && evt.target.closest ? evt.target.closest(selector) : null;
								if (target && el.contains(target)) {
									fn.call(target, evt, extraData);
								}
							} else {
								fn.call(el, evt, extraData);
							}
						};
						el.addEventListener(eventType, wrapper);
						if (Wicket.Event._listenerRegistry) {
							let map = Wicket.Event._listenerRegistry.get(el);
							if (!map) {
								map = {};
								Wicket.Event._listenerRegistry.set(el, map);
							}
							if (!map[eventType]) {
								map[eventType] = [];
							}
							map[eventType].push({ fn: fn, wrapper: wrapper });
						}
					})(types[t]);
				}
			}
			return element;
		},

		/**
		 * Unbinds an event listener for an element
		 *
		 * @param element {HTMLElement} The host HTML element
		 * @param type {String} The type of the DOM event. Several space separated
		 *      event types can be given at once
		 * @param fn {Function} The event handler to unbind. If omitted, every listener
		 *      bound through Wicket.Event.add() for this type is removed.
		 */
		remove: function (element, type, fn) {
			let el = element;
			if (typeof(element) === 'string') {
				el = document.getElementById(element);
			}
			if (!el || !Wicket.Event._listenerRegistry) {
				return;
			}
			const map = Wicket.Event._listenerRegistry.get(el);
			if (!map) {
				return;
			}
			const types = type ? type.split(/\s+/) : Object.keys(map);
			for (let t = 0; t < types.length; t++) {
				const entries = map[types[t]];
				if (!entries) {
					continue;
				}
				for (let i = entries.length - 1; i >= 0; i--) {
					if (!fn || entries[i].fn === fn) {
						el.removeEventListener(types[t], entries[i].wrapper);
						entries.splice(i, 1);
					}
				}
			}
		},

		/**
		* Adds a subscriber for the passed topic.
		*
		* @param topic {String} - the channel name for which this subscriber will be notified
		*        If '*' then it will be notified for all topics
		* @param subscriber {Function} - the callback to call when an event with this type is published
		*/
		subscribe: function (topic, subscriber) {
			if (topic) {
				const topics = Wicket.Event._topics;
				if (!topics[topic]) {
					topics[topic] = [];
				}
				topics[topic].push(subscriber);
			}
		},

		/**
		 * Un-subscribes a subscriber from a topic.
		 * @param topic {String} - the topic name. If omitted un-subscribes all
		 *      subscribers from all topics
		 * @param subscriber {Function} - the handler to un-subscribe. If omitted then
		 *      all subscribers are removed from this topic
		 */
		unsubscribe: function(topic, subscriber) {
			const topics = Wicket.Event._topics;
			if (topic) {
				if (subscriber) {
					const subscribers = topics[topic];
					if (subscribers) {
						const idx = subscribers.indexOf(subscriber);
						if (idx > -1) {
							subscribers.splice(idx, 1);
						}
					}
				} else {
					delete topics[topic];
				}
			} else {
				Wicket.Event._topics = {};
			}
		},

		/**
		* Sends a notification to all subscribers for the given topic.
		* Subscribers for topic '*' receive the actual topic as first parameter,
		* otherwise the topic is not passed to subscribers which listen for specific
		* event types.
		*
		* @param topic {String} - the channel name for which all subscribers will be notified.
		*/
		publish: function (topic) {
			if (topic) {
				// cut the topic argument
				const args = Array.prototype.slice.call(arguments).slice(1);
				const event = { type: topic };

				const subscribers = (Wicket.Event._topics[topic] || []).slice();
				for (let i = 0; i < subscribers.length; i++) {
					subscribers[i].apply(document, [event].concat(args));
				}

				const wildcardEvent = { type: '*' };
				const wildcardSubscribers = (Wicket.Event._topics['*'] || []).slice();
				for (let w = 0; w < wildcardSubscribers.length; w++) {
					wildcardSubscribers[w].apply(document, [wildcardEvent].concat(args));
				}
			}
		},

		/**
		 * Submits the given form using, if available, standard form processing
		 * including client-side validation and firing of SubmitEvent.
		 *
		 * @param form {HTMLFormElement} form to submit
		 */
		requestSubmit: function(form) {
			if (form.requestSubmit) {
				form.requestSubmit();
			} else {
				const evt = new Event('submit', { bubbles: true, cancelable: true });
				if (form.dispatchEvent(evt)) {
					form.submit();
				}
			}
		},

		/**
		 * The names of the topics on which Wicket notifies
		 */
		Topic: {
			DOM_NODE_REMOVING      : '/dom/node/removing',
			DOM_NODE_ADDED         : '/dom/node/added',
			AJAX_CALL_INIT         : '/ajax/call/init',
			AJAX_CALL_BEFORE       : '/ajax/call/before',
			AJAX_CALL_PRECONDITION : '/ajax/call/precondition',
			AJAX_CALL_BEFORE_SEND  : '/ajax/call/beforeSend',
			AJAX_CALL_SUCCESS      : '/ajax/call/success',
			AJAX_CALL_COMPLETE     : '/ajax/call/complete',
			AJAX_CALL_AFTER        : '/ajax/call/after',
			AJAX_CALL_FAILURE      : '/ajax/call/failure',
			AJAX_CALL_DONE         : '/ajax/call/done',
			AJAX_HANDLERS_BOUND    : '/ajax/handlers/bound'
		}
	};

	// MISC FUNCTIONS

	/**
	 * Track focussed element.
	 */
	Wicket.Event.add(window, 'focusin', Wicket.Focus.focusin);
	Wicket.Event.add(window, 'focusout', Wicket.Focus.focusout);

	/**
	 * Clear any scheduled Ajax timers when leaving the current page
	 */
	Wicket.Event.add(window, "unload", function() {
		Wicket.Timer.clearAll();
	});

})();
