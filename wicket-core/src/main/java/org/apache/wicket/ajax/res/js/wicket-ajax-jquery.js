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

/*global DOMParser: true, ActiveXObject: true, console: true */

/*
 * Wicket Ajax Support
 *
 * @author Igor Vaynberg
 * @author Matej Knopp
 */

;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'object' && typeof(Wicket.Head) === 'object') {
		return;
	}

	/**
	 * Add a check for old Safari. It should not be our responsibility to check the
	 * browser's version, but it's a minor version that makes a difference here,
	 * so we try to be at least user friendly.
	 */
	if (typeof(DOMParser) === "undefined" && Wicket.Browser.isSafari()) {
		DOMParser = function () {};

		DOMParser.prototype.parseFromString = function () {
			window.alert('You are using an old version of Safari.\nTo be able to use this page you need at least version 2.0.1.');
		};
	}

	var createIFrame,
		getAjaxBaseUrl,
		isUndef,
		replaceAll,
		htmlToDomDocument;

	isUndef = function (target) {
		return (typeof(target) === 'undefined' || target === null);
	};

	replaceAll = function (str, from, to) {
		var regex = new RegExp(from.replace( /\W/g ,'\\$&' ), 'g');
		return str.replace(regex,to);
	};

	/**
	 * Creates an iframe that can be used to load data asynchronously or as a
	 * target for Ajax form submit.
	 *
	 * @param iframeName {String} the value of the iframe's name attribute
	 */
	createIFrame = function (iframeName) {
		var $iframe = jQuery('<iframe name="'+iframeName+'" id="'+iframeName+
			'" src="about:blank" style="position: absolute; top: -9999px; left: -9999px;">');
		return $iframe[0];
	};

	/**
	 * A safe getter for Wicket's Ajax base URL.
	 * If the value is not defined or is empty string then
	 * return '.' (current folder) as base URL.
	 * Used for request header and parameter
	 */
	getAjaxBaseUrl = function () {
		var baseUrl = Wicket.Ajax.baseUrl || '.';
		return baseUrl;
	};

	/**
	 * Helper method that serializes HtmlDocument to string and then
	 * creates a DOMDocument by parsing this string.
	 * It is used as a workaround for the problem described at https://issues.apache.org/jira/browse/WICKET-4332
	 * @param htmlDocument (DispHtmlDocument) the document object created by IE from the XML response in the iframe
	 */
	htmlToDomDocument = function (htmlDocument) {
		var xmlAsString = htmlDocument.body.outerText;
		xmlAsString = xmlAsString.replace(/^\s+|\s+$/g, ''); // trim
		xmlAsString = xmlAsString.replace(/(\n|\r)-*/g, ''); // remove '\r\n-'. The dash is optional.
		var xmldoc = Wicket.Xml.parse(xmlAsString);
		return xmldoc;
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
	var FunctionsExecuter = function (functions) {

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
		 * another instance of FunctionExecuter to run its functions
		 * @type {number}
		 */
		this.depth = 0; // we need to limit call stack depth

		this.processNext = function () {
			if (this.current < this.functions.length) {
				var f, run;

				f = this.functions[this.current];
				run = function () {
					try {
						var n = jQuery.proxy(this.notify, this);
						return f(n);
					}
					catch (e) {
						Wicket.Log.error("FunctionsExecuter.processNext: " + e);
						return FunctionsExecuter.FAIL;
					}
				};
				run = jQuery.proxy(run, this);
				this.current++;

				if (this.depth > FunctionsExecuter.DEPTH_LIMIT) {
					// to prevent stack overflow (see WICKET-4675)
					this.depth = 0;
					window.setTimeout(run, 1);
				} else {
					var retValue = run();
					if (isUndef(retValue) || retValue === FunctionsExecuter.ASYNC) {
						this.depth++;
					}
					return retValue;
				}
			}
		};

		this.start = function () {
			var retValue = FunctionsExecuter.DONE;
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

	/**
	 * Logging functionality.
	 */
	Wicket.Log = {

		enabled: function () {
			return Wicket.Ajax.DebugWindow && Wicket.Ajax.DebugWindow.enabled;
		},

		info: function (msg) {
			if (Wicket.Log.enabled()) {
				Wicket.Ajax.DebugWindow.logInfo(msg);
			}
		},

		error: function (msg) {
			if (Wicket.Log.enabled()) {
				Wicket.Ajax.DebugWindow.logError(msg);
			} else if (typeof(console) !== "undefined" && typeof(console.error) === 'function') {
				console.error('Wicket.Ajax: ', msg);
			}
		},

		log: function (msg) {
			if (Wicket.Log.enabled()) {
				Wicket.Ajax.DebugWindow.log(msg);
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
			var res = name.match(/^([^|]+)\|(d|s|a)$/);
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
					Wicket.Log.error("An error occurred while executing Ajax request:" + exception);
				}
			} else {
				var busyChannel = "Channel '"+ this.name+"' is busy";
				if (this.type === 's') { // stack/queue
					Wicket.Log.info(busyChannel + " - scheduling the callback to be executed when the previous request finish.");
					this.callbacks.push(callback);
				}
				else if (this.type === 'd') { // drop
					Wicket.Log.info(busyChannel + " - dropping all previous scheduled callbacks and scheduling a new one to be executed when the current request finish.");
					this.callbacks = [];
					this.callbacks.push(callback);
				} else if (this.type === 'a') { // active
					Wicket.Log.info(busyChannel + " - ignoring the Ajax call because there is a running request.");
				}
				return null;
			}
		},

		done: function () {
			var callback = null;

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
			var parsed = new Wicket.Channel(channel);
			var c = this.channels[parsed.name];
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
			var parsed = new Wicket.Channel(channel);
			var c = this.channels[parsed.name];
			if (!isUndef(c)) {
				c.done();
				if (!c.busy) {
					delete this.channels[parsed.name];
				}
			}
		}
	};

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

	Wicket.Ajax.Call.prototype = {

		initialize: jQuery.noop,

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

			if (!jQuery.isNumeric(attrs.rt)) {
				attrs.rt = 0;
			}

			if (attrs.ad !== true) {
				attrs.ad = false;
			}

			if (!attrs.sp) {
				attrs.sp = "stop";
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
			var target;
			if (attrs.event) {
				target = attrs.event.target;
			} else if (!jQuery.isWindow(attrs.c)) {
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
			if (jQuery.isArray(handlers)) {

				// cut the handlers argument
				var args = Array.prototype.slice.call(arguments).slice(1);

				// assumes that the Ajax attributes is always the first argument
				var attrs = args[0];
				var that = this._getTarget(attrs);

				for (var i = 0; i < handlers.length; i++) {
					var handler = handlers[i];
					if (jQuery.isFunction(handler)) {
						handler.apply(that, args);
					} else {
						new Function(handler).apply(that, args);
					}
				}
			}
		},

		/**
		 * Converts an object (hash) to an array suitable for consumption
		 * by jQuery.param()
		 *
		 * @param {Object} parameters - the object to convert to an array of
		 *      name -> value pairs.
		 * @see jQuery.param
		 * @see jQuery.serializeArray
		 * @private
		 */
		_asParamArray: function(parameters) {
			var result = [],
				value,
				name;
			if (jQuery.isArray(parameters)) {
				result = parameters;
			}
			else if (jQuery.isPlainObject(parameters)) {
				for (name in parameters) {
					if (name && parameters.hasOwnProperty(name)) {
						value = parameters[name];
						result.push({name: name, value: value});
					}
				}
			}

			for (var i = 0; i < result.length; i++) {
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
			var deps = attrs.dep,
				params = [];

			for (var i = 0; i < deps.length; i++) {
				var dep = deps[i],
					extraParam;
				if (jQuery.isFunction(dep)) {
					extraParam = dep(attrs);
				} else {
					extraParam = new Function('attrs', dep)(attrs);
				}
				extraParam = this._asParamArray(extraParam);
				params = params.concat(extraParam);
			}
			return jQuery.param(params);
		},

		/**
		 * Executes or schedules for execution #doAjax()
		 *
		 * @param {Object} attrs - the Ajax request attributes configured at the server side
		 */
		ajax: function (attrs) {
			this._initializeDefaults(attrs);

			var res = Wicket.channelManager.schedule(attrs.ch, Wicket.bind(function () {
				this.doAjax(attrs);
			}, this));
			return res !== null ? res: true;
		},

		/**
		 * Handles execution of Ajax calls.
		 *
		 * @param {Object} attrs - the Ajax request attributes configured at the server side
		 */
		doAjax: function (attrs) {

			// keep channel for done()
			this.channel = attrs.ch;

			var
				// the headers to use for each Ajax request
				headers = {
					'Wicket-Ajax': 'true',
					'Wicket-Ajax-BaseURL': getAjaxBaseUrl()
				},

				// the request (extra) parameters
				data = this._asParamArray(attrs.ep),

				self = this,

				// the precondition to use if there are no explicit ones
				defaultPrecondition = [ function (attributes) {
					if (attributes.c) {
						if (attributes.f) {
							return Wicket.$$(attributes.c) && Wicket.$$(attributes.f);
						} else {
							return Wicket.$$(attributes.c);
						}
					}
					return true;
				}],

				// a context that brings the common data for the success/fialure/complete handlers
				context = {
					attrs: attrs,

					// initialize the array for steps (closures that execute each action)
					steps: []
				},
				we = Wicket.Event,
				topic = we.Topic;

			if (Wicket.Focus.lastFocusId) {
				headers["Wicket-FocusedElementId"] = Wicket.Focus.lastFocusId;
			}

			self._executeHandlers(attrs.bh, attrs);
			we.publish(topic.AJAX_CALL_BEFORE, attrs);

			var preconditions = attrs.pre || [];
			preconditions = defaultPrecondition.concat(preconditions);
			if (jQuery.isArray(preconditions)) {

				var that = this._getTarget(attrs);

				for (var p = 0; p < preconditions.length; p++) {

					var precondition = preconditions[p];
					var result;
					if (jQuery.isFunction(precondition)) {
						result = precondition.call(that, attrs);
					} else {
						result = new Function(precondition).call(that, attrs);
					}
					if (result === false) {
						Wicket.Log.info("Ajax request stopped because of precondition check, url: " + attrs.u);
						self.done();
						return false;
					}
				}
			}

			we.publish(topic.AJAX_CALL_PRECONDITION, attrs);

			if (attrs.mp) { // multipart form. jQuery.ajax() doesn't help here ...
				var ret = self.submitMultipartForm(context);
				return ret;
			}

			if (attrs.f) {
				// serialize the form with id == attrs.f
				var form = Wicket.$(attrs.f);
				data = data.concat(Wicket.Form.serializeForm(form));

				// set the submitting component input name
				if (attrs.sc) {
					var scName = attrs.sc;
					data = data.concat({name: scName, value: 1});
				}

			} else if (attrs.c && !jQuery.isWindow(attrs.c)) {
				// serialize just the form component with id == attrs.c
				var el = Wicket.$(attrs.c);
				data = data.concat(Wicket.Form.serializeElement(el));
			}

			// convert to URL encoded string
			data = jQuery.param(data);

			// execute the request
			var jqXHR = jQuery.ajax({
				url: attrs.u,
				type: attrs.m,
				context: self,
				beforeSend: function (jqXHR, settings) {

					// collect the dynamic extra parameters
					if (jQuery.isArray(attrs.dep)) {
						var queryString,
							separator;

						queryString = this._calculateDynamicParameters(attrs);
						if (settings.type.toLowerCase() === 'post') {
							separator = settings.data.length > 0 ? '&' : '';
							settings.data = settings.data + separator + queryString;
							jqXHR.setRequestHeader("Content-Type", settings.contentType);
						} else {
							separator = settings.url.indexOf('?') > -1 ? '&' : '?';
							settings.url = settings.url + separator + queryString;
						}
					}

					self._executeHandlers(attrs.bsh, attrs, jqXHR, settings);
					we.publish(topic.AJAX_CALL_BEFORE_SEND, attrs, jqXHR, settings);

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
				success: function(data, textStatus, jqXHR) {
					if (attrs.wr) {
						self.processAjaxResponse(data, textStatus, jqXHR, context);
					} else {
						self._executeHandlers(attrs.sh, attrs, jqXHR, data, textStatus);
						we.publish(topic.AJAX_CALL_SUCCESS, attrs, jqXHR, data, textStatus);
					}
				},
				error: function(jqXHR, textStatus, errorMessage) {
					self.failure(context, jqXHR, errorMessage, textStatus);
				},
				complete: function (jqXHR, textStatus) {

					context.steps.push(jQuery.proxy(function (notify) {
						if (attrs.i) {
							Wicket.DOM.hideIncrementally(attrs.i);
						}

						self._executeHandlers(attrs.coh, attrs, jqXHR, textStatus);
						we.publish(topic.AJAX_CALL_COMPLETE, attrs, jqXHR, textStatus);

						self.done();
						return FunctionsExecuter.DONE;
					}, self));

					var executer = new FunctionsExecuter(context.steps);
					executer.start();
				}
			});

			// execute after handlers right after the Ajax request is fired
			self._executeHandlers(attrs.ah, attrs);
			we.publish(topic.AJAX_CALL_AFTER, attrs);

			return jqXHR;
		},

		/**
		 * Method that processes a manually supplied <ajax-response>.
		 *
		 * @param data {XmlDocument} - the <ajax-response> XML document
		 */
		process: function(data) {
			var context =  {
					attrs: {},
					steps: []
				};
			var xmlDocument = Wicket.Xml.parse(data);
			this.loadedCallback(xmlDocument, context);
			var executer = new FunctionsExecuter(context.steps);
			executer.start();
		},

		/**
		 * Method that processes the <ajax-response> in the context of an XMLHttpRequest.
		 *
		 * @param data {XmlDocument} - the <ajax-response> XML document
		 * @param textStatus {String} - the response status as text (e.g. 'success', 'parsererror', etc.)
		 * @param jqXHR {Object} - the jQuery wrapper around XMLHttpRequest
		 * @param context {Object} - the request context with the Ajax request attributes and the FunctionExecuter's steps
		 */
		processAjaxResponse: function (data, textStatus, jqXHR, context) {

			if (jqXHR.readyState === 4) {

				// first try to get the redirect header
				var redirectUrl;
				try {
					redirectUrl = jqXHR.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}

				// the redirect header was set, go to new url
				if (typeof(redirectUrl) !== "undefined" && redirectUrl !== null && redirectUrl !== "") {

					// In case the page isn't really redirected. For example say the redirect is to an octet-stream.
					// A file download popup will appear but the page in the browser won't change.
					this.success(context);
					this.done();

					var rhttp  = /^http:\/\//,  // checks whether the string starts with http://
					    rhttps = /^https:\/\//; // checks whether the string starts with https://

					// support/check for non-relative redirectUrl like as provided and needed in a portlet context
					if (redirectUrl.charAt(0) === '/' || rhttp.test(redirectUrl) || rhttps.test(redirectUrl)) {
						window.location = redirectUrl;
					}
					else {
						var urlDepth = 0;
						while (redirectUrl.substring(0, 3) === "../") {
							urlDepth++;
							redirectUrl = redirectUrl.substring(3);
						}
						// Make this a string.
						var calculatedRedirect = window.location.pathname;
						while (urlDepth > -1) {
							urlDepth--;
							var i = calculatedRedirect.lastIndexOf("/");
							if (i > -1) {
								calculatedRedirect = calculatedRedirect.substring(0, i);
							}
						}
						calculatedRedirect += "/" + redirectUrl;

						if (Wicket.Browser.isGecko()) {
							// firefox 3 has problem with window.location setting relative url
							calculatedRedirect = window.location.protocol + "//" + window.location.host + calculatedRedirect;
						}

						window.location = calculatedRedirect;
					}
				}
				else {
					// no redirect, just regular response
					if (Wicket.Log.enabled()) {
						var responseAsText = jqXHR.responseText;
						Wicket.Log.info("Received ajax response (" + responseAsText.length + " characters)");
						Wicket.Log.info("\n" + responseAsText);
					}

					// invoke the loaded callback with an xml document
					return this.loadedCallback(data, context);
				}
			}
		},

		/**
		 * This method serializes a form and sends it as POST body. If the form contains multipart content
		 * this function will post the form using an iframe instead of the regular ajax call
		 * and bridge the output - transparently making this work  as if it was an ajax call.
		 *
		 * @param {Object} context - the context for the ajax call (request attributes + steps)
		 */
		submitMultipartForm: function (context) {

			var attrs = context.attrs;

			var form = Wicket.$(attrs.f);
			if (!form) {
				Wicket.Log.error("Wicket.Ajax.Call.submitForm: Trying to submit form with id '" + attrs.f + "' that is not in document.");
				return;
			}

			// find root form
			if (form.tagName.toLowerCase() !== "form") {
				do {
					form = form.parentNode;
				} while(form.tagName.toLowerCase() !== "form" && form !== document.body);
			}

			if (form.tagName.toLowerCase() !== "form") {
				Wicket.Log.error("Cannot submit form with id " + attrs.f + " because there is no form element in the hierarchy.");
				return false;
			}

			var submittingAttribute = 'data-wicket-submitting';

			if (form.onsubmit && !form.getAttribute(submittingAttribute)) {
				form.setAttribute(submittingAttribute, submittingAttribute);
				var retValue = true;
				try {
					retValue = form.onsubmit();
				} finally {
					form.removeAttribute(submittingAttribute);
				}
				if (!retValue) {
					return;
				}
			}

			var originalFormAction = form.action;
			var originalFormTarget = form.target;
			var originalFormMethod = form.method;
			var originalFormEnctype = form.enctype;
			var originalFormEncoding = form.encoding;

			var iframeName = "wicket-submit-" + ("" + Math.random()).substr(2);

			var iframe = createIFrame(iframeName);

			document.body.appendChild(iframe);

			// reconfigure the form
			form.target = iframe.name;
			var separator = (attrs.u.indexOf("?")>-1 ? "&" : "?");
			form.action = attrs.u + separator + "wicket-ajax=true&wicket-ajax-baseurl=" + Wicket.Form.encode(getAjaxBaseUrl());

			// add the static extra parameters
			if (attrs.ep) {
				var extraParametersArray = this._asParamArray(attrs.ep);
				if (extraParametersArray.length > 0) {
					var extraParametersQueryString = jQuery.param(extraParametersArray);
					form.action = form.action + '&' + extraParametersQueryString;
				}
			}

			// add the dynamic extra parameters
			if (jQuery.isArray(attrs.dep)) {
				var dynamicExtraParameters = this._calculateDynamicParameters(attrs);
				if (dynamicExtraParameters) {
					form.action = form.action + '&' + dynamicExtraParameters;
				}
			}
			form.method = 'post';
			form.enctype = "multipart/form-data";
			form.encoding = "multipart/form-data";

			// create submitting button element
			if (attrs.sc) {
				var $btn = jQuery("<input type='hidden' name='" + attrs.sc + "' id='" + iframe.id + "-btn' value='1'/>");
				form.appendChild($btn[0]);
			}

			var we = Wicket.Event;
			var topic = we.Topic;

			this._executeHandlers(attrs.bsh, attrs, null, null);
			we.publish(topic.AJAX_CALL_BEFORE_SEND, attrs, null, null);

			if (attrs.i) {
				// show the indicator
				Wicket.DOM.showIncrementally(attrs.i);
			}

			//submit the form into the iframe, response will be handled by the onload callback
			form.submit();

			this._executeHandlers(attrs.ah, attrs);
			we.publish(topic.AJAX_CALL_AFTER, attrs);

			// install handler to deal with the ajax response
			// ... we add the onload event after form submit because chrome fires it prematurely
			we.add(iframe, "load.handleMultipartComplete", jQuery.proxy(this.handleMultipartComplete, this), context);

			// handled, restore state and return true
			form.action = originalFormAction;
			form.target = originalFormTarget;
			form.method = originalFormMethod;
			form.enctype = originalFormEnctype;
			form.encoding = originalFormEncoding;

			return true;
		},

		/**
		 * Completes the multipart ajax handling started via handleMultipart()
		 * @param {jQuery.Event} event
		 */
		handleMultipartComplete: function (event) {

			var context = event.data,
				iframe = event.target,
				envelope;

			// stop the event
			event.stopPropagation();

			// remove the event
			jQuery(iframe).off("load.handleMultipartComplete");

			try {
				envelope = iframe.contentWindow.document;
			} catch (e) {
				Wicket.Log.error("Cannot read Ajax response for multipart form submit: " + e);
			}

			if (isUndef(envelope)) {
				this.failure(context, null, "No XML response in the IFrame document", "Failure");
			}
			else {
				if (envelope.XMLDocument) {
					envelope = envelope.XMLDocument;
				}

				// process the response
				this.loadedCallback(envelope, context);
			}

			context.steps.push(jQuery.proxy(function(notify) {
				// remove the iframe and button elements
				setTimeout(function() {
					jQuery('#'+iframe.id + '-btn').remove();
					jQuery(iframe).remove();
				}, 0);

				var attrs = context.attrs;
				if (attrs.i) {
					// hide the indicator
					Wicket.DOM.hideIncrementally(attrs.i);
				}

				this._executeHandlers(attrs.coh, attrs, null, null);
				Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_COMPLETE, attrs, null, null);

				this.done();
				return FunctionsExecuter.DONE;
			}, this));

			var executer = new FunctionsExecuter(context.steps);
			executer.start();
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
				var root = envelope.getElementsByTagName("ajax-response")[0];

				if (isUndef(root) && envelope.compatMode === 'BackCompat') {
					envelope = htmlToDomDocument(envelope);
					root = envelope.getElementsByTagName("ajax-response")[0];
				}

				// the root element must be <ajax-response
				if (isUndef(root) || root.tagName !== "ajax-response") {
					this.failure(context, null, "Could not find root <ajax-response> element", null);
					return;
				}

				var steps = context.steps;

				// go through the ajax response and execute all priority-invocations first
				for (var i = 0; i < root.childNodes.length; ++i) {
					var childNode = root.childNodes[i];
					if (childNode.tagName === "header-contribution") {
						this.processHeaderContribution(context, childNode);
					} else if (childNode.tagName === "priority-evaluate") {
						this.processEvaluation(context, childNode);
					}
				}

				// go through the ajax response and for every action (component, js evaluation, header contribution)
				// ad the proper closure to steps
				var stepIndexOfLastReplacedComponent = -1;
				for (var c = 0; c < root.childNodes.length; ++c) {
					var node = root.childNodes[c];

					if (node.tagName === "component") {
						if (stepIndexOfLastReplacedComponent === -1) {
							this.processFocusedComponentMark(context);
						}
						stepIndexOfLastReplacedComponent = steps.length;
						this.processComponent(context, node);
					} else if (node.tagName === "evaluate") {
						this.processEvaluation(context, node);
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
			context.steps.push(jQuery.proxy(function (notify) {
				Wicket.Log.info("Response processed successfully.");

				var attrs = context.attrs;
				this._executeHandlers(attrs.sh, attrs, null, null, 'success');
				Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_SUCCESS, attrs, null, null, 'success');

				// re-attach the events to the new components (a bit blunt method...)
				// This should be changed for IE See comments in wicket-event.js add (attachEvent/detachEvent)
				// IE this will cause double events for everything.. (mostly because of the jQuery.proxy(element))
				Wicket.Focus.attachFocusEvent();

				// set the focus to the last component
				if (Wicket.Browser.isIELessThan9()) {
					// WICKET-5755
					window.setTimeout("Wicket.Focus.requestFocus();", 0);
				} else {
					Wicket.Focus.requestFocus();
				}

				// continue to next step (which should make the processing stop, as success should be the final step)
				return FunctionsExecuter.DONE;
			}, this));
		},

		// On ajax request failure
		failure: function (context, jqXHR, errorMessage, textStatus) {
			context.steps.push(jQuery.proxy(function (notify) {
				if (errorMessage) {
					Wicket.Log.error("Wicket.Ajax.Call.failure: Error while parsing response: " + errorMessage);
				}
				var attrs = context.attrs;
				this._executeHandlers(attrs.fh, attrs, errorMessage);
				Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_FAILURE, attrs, jqXHR, errorMessage, textStatus);

				return FunctionsExecuter.DONE;
			}, this));
		},

		done: function () {
			Wicket.channelManager.done(this.channel);
		},

		// Adds a closure that replaces a component
		processComponent: function (context, node) {
			context.steps.push(function (notify) {
				// get the component id
				var compId = node.getAttribute("id");
				var text = Wicket.DOM.text(node);

				// if the text was escaped, unascape it
				// (escaping is done when the component body contains a CDATA section)
				var encoding = node.getAttribute("encoding");
				if (encoding) {
					text = Wicket.Head.Contributor.decode(encoding, text);
				}

				// get existing component
				var element = Wicket.$(compId);

				if (isUndef(element)) {
					Wicket.Log.error("Wicket.Ajax.Call.processComponent: Component with id [[" +
						compId + "]] was not found while trying to perform markup update. " +
						"Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.");
				} else {
					// replace the component
					Wicket.DOM.replace(element, text);
				}
				// continue to next step
				return FunctionsExecuter.DONE;
			});
		},

		/**
		 * Adds a closure that evaluates javascript code.
		 * @param context {Object} - the object that brings the executer's steps and the attributes
		 * @param node {XmlElement} - the <[priority-]evaluate> element with the script to evaluate
		 */
		processEvaluation: function (context, node) {

			// used to match evaluation scripts which manually call FunctionsExecuter's notify() when ready
			var scriptWithIdentifierR = new RegExp("^\\(function\\(\\)\\{([a-zA-Z_]\\w*)\\|((.|\\n)*)?\\}\\)\\(\\);$");

			/**
			 * A regex used to split the text in (priority-)evaluate elements in the Ajax response
			 * when there are scripts which require manual call of 'FunctionExecutor#notify()'
			 * @type {RegExp}
			 */
			var scriptSplitterR = new RegExp("\\(function\\(\\)\\{[\\s\\S]*?}\\)\\(\\);", 'gi');

			// get the javascript body
			var text = Wicket.DOM.text(node);

			// unescape it if necessary
			var encoding = node.getAttribute("encoding");
			if (encoding) {
				text = Wicket.Head.Contributor.decode(encoding, text);
			}

			// aliases to improve performance
			var steps = context.steps;
			var log = Wicket.Log;

			var evaluateWithManualNotify = function (parameters, body) {
				return function(notify) {
					var f = jQuery.noop;
					var toExecute = "f = function(" + parameters + ") {" + body + "};";

					try {
						// do the evaluation
						eval(toExecute);
						f(notify);
					} catch (exception) {
						log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text);
					}
					return FunctionsExecuter.ASYNC;
				};
			};

			var evaluate = function (script) {
				return function(notify) {
					// just evaluate the javascript
					try {
						// do the evaluation
						eval(script);
					} catch (exception) {
						log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text);
					}
					// continue to next step
					return FunctionsExecuter.DONE;
				};
			};

			// test if the javascript is in form of identifier|code
			// if it is, we allow for letting the javascript decide when the rest of processing will continue
			// by invoking identifier();. This allows usage of some asynchronous/deferred logic before the next script
			// See WICKET-5039
			if (scriptWithIdentifierR.test(text)) {
				var scripts = [];
				var scr;
				while ( (scr = scriptSplitterR.exec(text) ) != null ) {
					scripts.push(scr[0]);
				}

				for (var s = 0; s < scripts.length; s++) {
					var script = scripts[s];
					if (script) {
						var scriptWithIdentifier = script.match(scriptWithIdentifierR);
						if (scriptWithIdentifier) {
							steps.push(evaluateWithManualNotify(scriptWithIdentifier[1], scriptWithIdentifier[2]));
						}
						else {
							steps.push(evaluate(script));
						}
					}
				}
			} else {
				steps.push(evaluate(text));
			}
		},

		// Adds a closure that processes a header contribution
		processHeaderContribution: function (context, node) {
			var c = Wicket.Head.Contributor;
			c.processContribution(context, node);
		},

		// Adds a closure that processes a redirect
		processRedirect: function (context, node) {
			var text = Wicket.DOM.text(node);
			Wicket.Log.info("Redirecting to: " + text);
			window.location = text;
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
			var entries = Wicket.Throttler.entries;
			var entry = entries[id];
			var me = this;
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
			var entries = Wicket.Throttler.entries;
			var entry = entries[id];
			if (typeof(entry) !== 'undefined') {
				var func = entry.getFunc();
				entries[id] = undefined;
				return func();
			}
		}
	};



	jQuery.extend(true, Wicket, {

		channelManager: new Wicket.ChannelManager(),

		throttler: new Wicket.Throttler(),

		$: function (arg) {
			return Wicket.DOM.get(arg);
		},

		/**
		 * returns if the element belongs to current document
		 * if the argument is not element, function returns true
		 */
		$$: function (element) {
			return Wicket.DOM.inDoc(element);
		},

		/**
		 * Merges two objects. Values of the second will overwrite values of the first.
		 *
		 * @param {Object} object1 - the first object to merge
		 * @param {Object} object2 - the second object to merge
		 * @return {Object} a new object with the values of object1 and object2
		 */
		merge: function(object1, object2) {
			return jQuery.extend({}, object1, object2);
		},

		/**
		 * Takes a function and returns a new one that will always have a particular context, i.e. 'this' will be the passed context.
		 *
		 * @param {Function} fn - the function which context will be set
		 * @param {Object} context - the new context for the function
		 * @return {Function} the original function with the changed context
		 */
		bind: function(fn, context) {
			return jQuery.proxy(fn, context);
		},

		Xml: {
			parse: function (text) {
				var xmlDocument;
				if (window.DOMParser) {
					var parser = new DOMParser();
					xmlDocument = parser.parseFromString(text, "text/xml");
				} else if (window.ActiveXObject) {
					try {
						xmlDocument = new ActiveXObject("Msxml2.DOMDocument.6.0");
					} catch (err6) {
						try {
							xmlDocument = new ActiveXObject("Msxml2.DOMDocument.5.0");
						} catch (err5) {
							try {
								xmlDocument = new ActiveXObject("Msxml2.DOMDocument.4.0");
							} catch (err4) {
								try {
									xmlDocument = new ActiveXObject("MSXML2.DOMDocument.3.0");
								} catch (err3) {
									try {
										xmlDocument = new ActiveXObject("Microsoft.XMLDOM");
									} catch (err2) {
										Wicket.Log.error("Cannot create DOM document: " + err2);
									}
								}
							}
						}
					}

					if (xmlDocument) {
						xmlDocument.async = "false";
						if (!xmlDocument.loadXML(text)) {
							Wicket.Log.error("Error parsing response: "+text);
						}
					}
				}

				return xmlDocument;
			}
		},

		/**
		 * Form serialization
		 *
		 * To post a form using Ajax Wicket first needs to serialize it, which means composing a string
		 * from form elments names and values. The string will then be set as body of POST request.
		 */

		Form: {
			encode: function (text) {
				if (window.encodeURIComponent) {
					return window.encodeURIComponent(text);
				} else {
					return window.escape(text);
				}
			},

			/**
			 * Serializes HTMLFormSelectElement to URL encoded key=value string.
			 *
			 * @param select {HTMLFormSelectElement} - the form element to serialize
			 * @return an object of key -> value pair where 'value' can be an array of Strings if the select is .multiple,
			 *		or empty object if the form element is disabled.
			 */
			serializeSelect: function (select){
				var result = [];
				if (select) {
					var $select = jQuery(select);
					if ($select.length > 0 && $select.prop('disabled') === false) {
						var name = $select.prop('name');
						var values = $select.val();
						if (jQuery.isArray(values)) {
							for (var v = 0; v < values.length; v++) {
								var value = values[v];
								result.push( { name: name, value: value } );
							}
						} else {
							result.push( { name: name, value: values } );
						}
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
				var result = [];
				if (input && input.type && !(input.type === 'image' || input.type === 'submit')) {
					var $input = jQuery(input);
					result = $input.serializeArray();
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
			 * @return An array with a single element - an object with two keys - <em>name</em> and <em>value</em>.
			 */
			serializeElement: function(element) {

				if (!element) {
					return [];
				}
				else if (typeof(element) === 'string') {
					element = Wicket.$(element);
				}

				if (Wicket.Form.excludeFromAjaxSerialization && element.id && Wicket.Form.excludeFromAjaxSerialization[element.id] === "true") {
					return [];
				}

				var tag = element.tagName.toLowerCase();
				if (tag === "select") {
					return Wicket.Form.serializeSelect(element);
				} else if (tag === "input" || tag === "textarea") {
					return Wicket.Form.serializeInput(element);
				} else {
					return [];
				}
			},

			serializeForm: function (form) {
				var result = [],
					elements,
					nodeListToArray,
					nodeId;

				nodeListToArray = function (nodeList) {
					var arr = [];
					if (nodeList && nodeList.length) {
						for (nodeId = 0; nodeId < nodeList.length; nodeId++) {
							arr.push(nodeList.item(nodeId));
						}
					}
					return arr;
				};

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

				for (var i = 0; i < elements.length; ++i) {
					var el = elements[i];
					if (el.name && el.name !== "") {
						result = result.concat(Wicket.Form.serializeElement(el));
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
					var elementBck = element;

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
						var form = document.createElement("form");
						var parent = elementBck.parentNode;

						parent.replaceChild(form, elementBck);
						form.appendChild(elementBck);
						var result = Wicket.Form.serializeForm(form);
						parent.replaceChild(elementBck, form);

						return result;
					}
				}
			}
		},

		/**
		 * DOM nodes serialization functionality
		 *
		 * The purpose of these methods is to return a string representation
		 * of the DOM tree.
		 */
		DOM: {

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
						// no explicit 'display' value is requested so
						// use jQuery. It has special logic to decide which is the
						// best value for an HTMLElement
						jQuery(e).show();
					} else {
						e.style.display = display;
					}
				}
			},

			/** hides an element */
			hide: function (e) {
				e = Wicket.$(e);
				if (e !== null) {
					jQuery(e).hide();
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
				jQuery('#'+elementId).toggleClass(cssClass, Switch);
			},

			/** call-counting implementation of Wicket.DOM.show() */
			showIncrementally: function (e) {
				e = Wicket.$(e);
				if (e === null) {
					return;
				}
				var count = e.getAttribute("showIncrementallyCount");
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
				var count = e.getAttribute("showIncrementallyCount");
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
					var e = [];
					for (var i = 0; i < arguments.length; i++) {
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

				var id = element.getAttribute('id');
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
			 * on browser where it's not supported when using outerHTML (IE).
			 *
			 * This method sends notifications to all subscribers for channels with names
			 * '/dom/node/removing' with the element that is going to be replaced and
			 * '/dom/node/added' with the newly created element (the replacement).
			 *
			 * Note: the 'to be replaced' element must have an 'id' attribute
			 */
			replace: function (element, text) {

				var we = Wicket.Event;
				var topic = we.Topic;

				we.publish(topic.DOM_NODE_REMOVING, element);

				if (element.tagName.toLowerCase() === "title") {
					// match the text between the tags
					var titleText = />(.*?)</.exec(text)[1];
					document.title = titleText;
					return;
				} else {
					// jQuery 1.9+ expects '<' as the very first character in text
					var cleanedText = jQuery.trim(text);

					var $newElement = jQuery(cleanedText);
					jQuery(element).replaceWith($newElement);
				}

				var newElement = Wicket.$(element.id);
				if (newElement) {
					we.publish(topic.DOM_NODE_ADDED, newElement);
				}
			},

			// Method for serializing DOM nodes to string
			// original taken from Tacos (http://tacoscomponents.jot.com)
			serializeNodeChildren: function (node) {
				if (isUndef(node)) {
					return "";
				}
				var result = [];

				if (node.childNodes.length > 0) {
					for (var i = 0; i < node.childNodes.length; i++) {
						var thisNode = node.childNodes[i];
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
				var result = [];
				result.push("<");
				result.push(node.nodeName);

				if (node.attributes && node.attributes.length > 0) {

					for (var i = 0; i < node.attributes.length; i++) {
						// serialize the attribute only if it has meaningful value that is not inherited
						if (node.attributes[i].nodeValue && node.attributes[i].specified) {
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
				var id = element.getAttribute("id");
				if (id) {
					return Wicket.$(id) !== null;
				}
				else {
					return false;
				}
			},

			/**
			 * Reads the text from the node's children nodes.
			 * Used instead of jQuery.text() because it is very slow in IE10/11.
			 * WICKET-5132, WICKET-5510
			 * @param node {DOMElement} the root node
			 */
			text: function (node) {
				if (isUndef(node)) {
					return "";
				}

				var result = [];

				if (node.childNodes.length > 0) {
					for (var i = 0; i < node.childNodes.length; i++) {
						var thisNode = node.childNodes[i];
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
		},

		/**
		 * The Ajax class handles low level details of creating XmlHttpRequest objects,
		 * as well as registering and execution of pre-call, post-call and failure handlers.
		 */
		 Ajax: {

			Call: Wicket.Ajax.Call,

			/**
			 * Aborts the default event if attributes request it
			 *
			 * @param {Object} attrs - the Ajax request attributes configured at the server side
			 */
			_handleEventCancelation: function(attrs) {
				var evt = attrs.event;
				if (evt) {
					if (!attrs.ad) {
						try {
							evt.preventDefault();
						} catch (ignore) {
							// WICKET-4986
							// jquery fails 'member not found' with calls on busy channel
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

				if (!jQuery.isArray(attrs.e)) {
					attrs.e = [ attrs.e ];
				}

				jQuery.each(attrs.e, function (idx, evt) {
					Wicket.Event.add(attrs.c, evt, function (jqEvent, data) {
						var call = new Wicket.Ajax.Call();
						var attributes = jQuery.extend({}, attrs);
						attributes.event = Wicket.Event.fix(jqEvent);
						if (data) {
							attributes.event.extraData = data;
						}

						var throttlingSettings = attributes.tr;
						if (throttlingSettings) {
							var postponeTimerOnUpdate = throttlingSettings.p || false;
							var throttler = new Wicket.Throttler(postponeTimerOnUpdate);
							throttler.throttle(throttlingSettings.id, throttlingSettings.d,
								Wicket.bind(function () {
									call.ajax(attributes);
								}, this));
						}
						else {
							call.ajax(attributes);
						}
						Wicket.Ajax._handleEventCancelation(attributes);
					}, null, attrs.sel);
				});
			},
			
			process: function(data) {
				var call = new Wicket.Ajax.Call();
				call.process(data);
			}
		},

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
		Head: {
			Contributor: {

				/**
				 * Decoding functionality
				 *
				 * Wicket sends rendered components and javascript as CDATA section of XML document. When the
				 * component body itself contains a CDATA section, Wicket needs to escape it properly.
				 */
				decode: function (encoding, text) {

					var decode1 = function (text) {
						return replaceAll(text, "]^", "]");
					};

					if (encoding === "wicket1") {
						text = decode1(text);
					}
					return text;
				},

				// Parses the header contribution element (returns a DOM tree with the contribution)
				parse: function (headerNode) {
					// the header contribution is stored as CDATA section in the header-contribution element.
					// even though we need to parse it (and we have aleady parsed the response), header
					// contribution needs to be treated separately. The reason for this is that
					// Konqueror crashes when it there is a <script element in the parsed string. So we
					// need to replace that first

					// get the header contribution text and unescape it if necessary
					var text = Wicket.DOM.text(headerNode);
					var encoding = headerNode.getAttribute("encoding");

					if (encoding !== null && encoding !== "") {
						text = this.decode(encoding, text);
					}

					if (Wicket.Browser.isKHTML()) {
						// konqueror crashes if there is a <script element in the xml, but <SCRIPT is fine.
						text = text.replace(/<script/g, "<SCRIPT");
						text = text.replace(/<\/script>/g, "</SCRIPT>");
					}

					// build a DOM tree of the contribution
					var xmldoc = Wicket.Xml.parse(text);
					return xmldoc;
				},

				// checks whether the passed node is the special "parsererror"
				// created by DOMParser if there is a error in XML parsing
				// TODO: move out of the API section
				_checkParserError: function (node) {
					var result = false;

					if (!isUndef(node.tagName) && node.tagName.toLowerCase() === "parsererror") {
						Wicket.Log.error("Error in parsing: " + node.textContent);
						result = true;
					}
					return result;
				},

				// Processes the parsed header contribution
				processContribution: function (context, headerNode) {
					var xmldoc = this.parse(headerNode);
					var rootNode = xmldoc.documentElement;

					// Firefox and Opera reports the error in the documentElement
					if (this._checkParserError(rootNode)) {
						return;
					}

					// go through the individual elements and process them according to their type
					for (var i = 0; i < rootNode.childNodes.length; i++) {
						var node = rootNode.childNodes[i];

						// Chromium reports the error as a child node
						if (this._checkParserError(node)) {
							return;
						}

						if (!isUndef(node.tagName)) {
							var name = node.tagName.toLowerCase();

							// it is possible that a reference is surrounded by a <wicket:link
							// in that case, we need to find the inner element
							if (name === "wicket:link") {
								for (var j = 0; j < node.childNodes.length; ++j) {
									var childNode = node.childNodes[j];
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
							}
						} else if (node.nodeType === 8) { // comment type
							this.processComment(context, node);
						}
					}
				},

				// Process an external stylesheet element
				processLink: function (context, node) {
					context.steps.push(function (notify) {
						// if the element is already in head, skip it
						if (Wicket.Head.containsElement(node, "href")) {
							return FunctionsExecuter.DONE;
						}
						// create link element
						var css = Wicket.Head.createElement("link");

						// copy required attributes
						css.id = node.getAttribute("id");
						css.rel = node.getAttribute("rel");
						css.href = node.getAttribute("href");
						css.type = node.getAttribute("type");

						// add element to head
						Wicket.Head.addElement(css);

						// cross browser way to check when the css is loaded
						// taked from http://www.backalleycoder.com/2011/03/20/link-tag-css-stylesheet-load-event/
						// this makes a second GET request to the css but it gets it either from the cache or
						// downloads just the first several bytes and realizes that the MIME is wrong and ignores the rest
						var img = document.createElement('img');
						var notifyCalled = false;
						img.onerror = function () {
							if (!notifyCalled) {
								notifyCalled = true;
								notify();
							}
						};
						img.src = css.href;
						if (img.complete) {
						  if (!notifyCalled) {
							notifyCalled = true;
							notify();
						  }
						}

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
						var content = Wicket.DOM.serializeNodeChildren(node);

						// create stylesheet
						if (Wicket.Browser.isIELessThan11()) {
							try  {
								document.createStyleSheet().cssText = content;
								return FunctionsExecuter.DONE;
							}
							catch (ignore) {
								var run = function() {
									try {
										document.createStyleSheet().cssText = content;
									}
									catch(e) {
										Wicket.Log.error("Wicket.Head.Contributor.processStyle: " + e);
									}
									notify();
								};
								window.setTimeout(run, 1);
								return FunctionsExecuter.ASYNC;
							}
						} else {
							// create style element
							var style = Wicket.Head.createElement("style");

							// copy id attribute
							style.id = node.getAttribute("id");

							var textNode = document.createTextNode(content);
							style.appendChild(textNode);

							Wicket.Head.addElement(style);
						}

						// continue to next step
						return FunctionsExecuter.DONE;
					});
				},

				// Process a script element (both inline and external)
				processScript: function (context, node) {
					context.steps.push(function (notify) {
						// if element with same id is already in document,
						// or element with same src attribute is in document, skip it
						if (Wicket.DOM.containsElement(node) ||
							Wicket.Head.containsElement(node, "src")) {
							return FunctionsExecuter.DONE;
						}

						// determine whether it is external javascript (has src attribute set)
						var src = node.getAttribute("src");

						if (src !== null && src !== "") {

							// convert the XML node to DOM node
							var scriptDomNode = document.createElement("script");

							var attrs = node.attributes;
							for (var a = 0; a < attrs.length; a++) {
								var attr = attrs[a];
								scriptDomNode[attr.name] = attr.value;
							}

							var onScriptReady = function () {
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
							} else if (Wicket.Browser.isGecko()) {
								// Firefox doesn't react on the checks above but still supports 'onload'
								scriptDomNode.onload = onScriptReady;
							} else {
								// as a final resort notify after the current function execution
								window.setTimeout(onScriptReady, 10);
							}

							Wicket.Head.addElement(scriptDomNode);

							return FunctionsExecuter.ASYNC;
						} else {
							// serialize the element content to string
							var text = Wicket.DOM.serializeNodeChildren(node);
							// get rid of prefix and suffix, they are not eval-d correctly
							text = text.replace(/^\n\/\*<!\[CDATA\[\*\/\n/, "");
							text = text.replace(/\n\/\*\]\]>\*\/\n$/, "");

							var id = node.getAttribute("id");
							var type = node.getAttribute("type");

							if (typeof(id) === "string" && id.length > 0) {
								// add javascript to document head
								Wicket.Head.addJavascript(text, id, "", type);
							} else {
								try {
									eval(text);
								} catch (e) {
									Wicket.Log.error("Wicket.Head.Contributor.processScript: " + e + ": eval -> " + text);
								}
							}

							// continue to next step
							return FunctionsExecuter.DONE;
						}
					});
				},

				// process (conditional) comments
				processComment: function (context, node) {
					context.steps.push(function (notify) {
						var comment = document.createComment(node.nodeValue);
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
			addElement: function (element) {
				var head = document.getElementsByTagName("head");

				if (head[0]) {
					head[0].appendChild(element);
				}
			},

			// Returns true, if the page head contains element that has attribute with
			// name mandatoryAttribute same as the given element and their names match.
			//
			// e.g. Wicket.Head.containsElement(myElement, "src") return true, if there
			// is an element in head that is of same type as myElement, and whose src
			// attribute is same as myElement.src.
			containsElement: function (element, mandatoryAttribute) {
				var attr = element.getAttribute(mandatoryAttribute);
				if (isUndef(attr) || attr === "") {
					return false;
				}

				var head = document.getElementsByTagName("head")[0];

				if (element.tagName === "script") {
					head = document;
				}

				var nodes = head.getElementsByTagName(element.tagName);

				for (var i = 0; i < nodes.length; ++i) {
					var node = nodes[i];

					// check node names and mandatory attribute values
					// we also have to check for attribute name that is suffixed by "_".
					// this is necessary for filtering script references
					if (node.tagName.toLowerCase() === element.tagName.toLowerCase()) {

						var loadedUrl = node.getAttribute(mandatoryAttribute);
						var loadedUrl_ = node.getAttribute(mandatoryAttribute+"_");
						if (loadedUrl === attr || loadedUrl_ === attr) {
							return true;
						}
					}
				}
				return false;
			},

			// Adds a javascript element to page header.
			// The fakeSrc attribute is used to filter out duplicate javascript references.
			// External javascripts are loaded using xmlhttprequest. Then a javascript element is created and the
			// javascript body is used as text for the element. For javascript references, wicket uses the src
			// attribute to filter out duplicates. However, since we set the body of the element, we can't assign
			// also a src value. Therefore we put the url to the src_ (notice the underscore)  attribute.
			// Wicket.Head.containsElement is aware of that and takes also the underscored attributes into account.
			addJavascript: function (content, id, fakeSrc, type) {
				var script = Wicket.Head.createElement("script");
				if (id) {
					script.id = id;
				}

				// WICKET-5047: encloses the content with a try...catch... block if the content is javascript
				// content is considered javascript if mime-type is empty (html5's default) or is 'text/javascript'
				if (!type || type.toLowerCase() === "text/javascript") {
					type = "text/javascript";
					content = 'try{'+content+'}catch(e){Wicket.Log.error(e);}';
				}

				script.setAttribute("src_", fakeSrc);
				script.setAttribute("type", type);

				// set the javascript as element content
				if (null === script.canHaveChildren || script.canHaveChildren) {
					var textNode = document.createTextNode(content);
					script.appendChild(textNode);
				} else {
					script.text = content;
				}
				Wicket.Head.addElement(script);
			},

			// Goes through all script elements contained by the element and add them to head
			addJavascripts: function (element, contentFilter) {
				function add(element) {
					var src = element.getAttribute("src");
					var type = element.getAttribute("type");

					// if it is a reference, just add it to head
					if (src !== null && src.length > 0) {
						var e = document.createElement("script");
						if (type) {
							e.setAttribute("type",type);
						}
						e.setAttribute("src", src);
						Wicket.Head.addElement(e);
					} else {
						var content = Wicket.DOM.serializeNodeChildren(element);
						if (isUndef(content) || content === "") {
							content = element.text;
						}

						if (typeof(contentFilter) === "function") {
							content = contentFilter(content);
						}

						Wicket.Head.addJavascript(content, element.id, "", type);
					}
				}
				if (typeof(element) !== "undefined" &&
					typeof(element.tagName) !== "undefined" &&
					element.tagName.toLowerCase() === "script") {
					add(element);
				} else {
					// we need to check if there are any children, because Safari
					// aborts when the element is a text node
					if (element.childNodes.length > 0) {
						var scripts = element.getElementsByTagName("script");
						for (var i = 0; i < scripts.length; ++i) {
							add(scripts[i]);
						}
					}
				}
			}
		},

		/**
		 * Flexible dragging support.
		 */
		Drag: {

			/**
			 * Initializes the dragging on the specified element.
			 * Element's onmousedown will be replaced by generated handler.
			 *
			 * @param {Element} element - element clicking on which the drag should begin
			 * @param {Function} onDragBegin - handler called at the begin on dragging - passed element as first parameter
			 * @param {Function} onDragEnd - handler called at the end of dragging - passed element as first parameter
			 * @param {Function} onDrag - handler called during dragging - passed element and mouse deltas
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

				Wicket.Event.stop(e);

				if (e.preventDefault) {
					e.preventDefault();
				}

				element.wicketOnDragBegin(element);

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

				return false;
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

					if (isUndef(res)) {
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
				if (false && Wicket.Browser.isGecko()) {
					// other browsers handle this more gracefully
					e = Wicket.Event.fix(e);

					if (e.target.tagName === "HTML") {
						Wicket.Drag.mouseUp(e);
					}
				}
			}
		},

		// FOCUS FUNCTIONS

		Focus: {
			lastFocusId : "",
			refocusLastFocusedComponentAfterResponse : false,
			focusSetFromServer : false,

			setFocus: function (event) {
				event = Wicket.Event.fix(event);

				var target = event.target;
				if (target) {
					var WF = Wicket.Focus;
					WF.refocusLastFocusedComponentAfterResponse = false;
					var id = target.id;
					WF.lastFocusId = id;
					Wicket.Log.info("focus set on " + id);
				}
			},

			blur: function (event) {
				event = Wicket.Event.fix(event);

				var target = event.target;
				var WF = Wicket.Focus;
				if (target && WF.lastFocusId === target.id) {
					var id = target.id;
					if (WF.refocusLastFocusedComponentAfterResponse) {
						// replaced components seem to blur when replaced only on Safari - so do not modify lastFocusId so it gets refocused
						Wicket.Log.info("focus removed from " + id + " but ignored because of component replacement");
					} else {
						WF.lastFocusId = null;
						Wicket.Log.info("focus removed from " + id);
					}
				}
			},

			getFocusedElement: function () {
				var lastFocusId = Wicket.Focus.lastFocusId;
				if (lastFocusId) {
					var focusedElement = Wicket.$(lastFocusId);
					Wicket.Log.info("returned focused element: " + focusedElement);
					return  focusedElement;
				}
			},

			setFocusOnId: function (id) {
				var WF = Wicket.Focus;
				if (id) {
					WF.refocusLastFocusedComponentAfterResponse = true;
					WF.focusSetFromServer = true;
					WF.lastFocusId = id;
					Wicket.Log.info("focus set on " + id + " from server side");
				} else {
					WF.refocusLastFocusedComponentAfterResponse = false;
					Wicket.Log.info("refocus focused component after request stopped from server side");
				}
			},

			// mark the focused component so that we know if it has been replaced or not by response
			markFocusedComponent: function () {
				var WF = Wicket.Focus;
				var focusedElement = WF.getFocusedElement();
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
				var WF = Wicket.Focus;
				if (WF.refocusLastFocusedComponentAfterResponse) {
					var focusedElement = WF.getFocusedElement();
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
				var WF = Wicket.Focus;
				if (WF.refocusLastFocusedComponentAfterResponse && WF.lastFocusId) {
					var toFocus = Wicket.$(WF.lastFocusId);

					if (toFocus) {
						Wicket.Log.info("Calling focus on " + WF.lastFocusId);
						try {
							if (WF.focusSetFromServer) {
								// WICKET-5858
								window.setTimeout(function () { toFocus.focus(); }, 0);
							} else {
								// avoid loops like - onfocus triggering an event the modifies the tag => refocus => the event is triggered again
								var temp = toFocus.onfocus;
								toFocus.onfocus = null;
								
								// IE needs setTimeout (it seems not to call onfocus sync. when focus() is called
								window.setTimeout(function () {toFocus.focus(); toFocus.onfocus = temp; }, 0);
							}
						} catch (ignore) {
						}
					}
					else {
						WF.lastFocusId = "";
						Wicket.Log.info("Couldn't set focus on element with id '" + WF.lastFocusId + "' because it is not in the page anymore");
					}
				}
				else if (WF.refocusLastFocusedComponentAfterResponse) {
					Wicket.Log.info("last focus id was not set");
				}
				else {
					Wicket.Log.info("refocus last focused component not needed/allowed");
				}
				WF.refocusLastFocusedComponentAfterResponse = false;
			},

			setFocusOnElements: function (elements) {
				// we need to cache array length because IE will try to recalculate
				// the collection of elements every time length() is called which can be quite expensive
				// if the collection is a result of getElementsByTagName or a similar function.
				var len = elements.length;
				var WE = Wicket.Event;
				var WF = Wicket.Focus;
				for (var i = 0; i < len; i++)
				{
					var element = elements[i];
					if (element.wicketFocusSet !== true)
					{
						WE.add(element, 'focus', WF.setFocus);
						WE.add(element, 'blur', WF.blur);
						element.wicketFocusSet = true;
					}
				}
			},

			attachFocusEvent: function () {
				var WF = Wicket.Focus;
				WF.setFocusOnElements(document.getElementsByTagName("input"));
				WF.setFocusOnElements(document.getElementsByTagName("select"));
				WF.setFocusOnElements(document.getElementsByTagName("textarea"));
				WF.setFocusOnElements(document.getElementsByTagName("button"));
				WF.setFocusOnElements(document.getElementsByTagName("a"));
			}
		},

		/**
		 * Manages the functionality needed by AbstractAjaxTimerBehavior and its subclasses
		 */
		Timer: {
			/**
			 * Schedules a timer
			 * @param {string} timerId - the identifier for the timer
			 * @param {function|string} js - the JavaScript to execute after the timeout
			 * @param {number} delay - the timeout
			 */
			'set': function(timerId, js, delay) {
				if (typeof(Wicket.TimerHandles) === 'undefined') {
					Wicket.TimerHandles = {};
				}

				Wicket.Timer.clear(timerId);
				Wicket.TimerHandles[timerId] = setTimeout(js, delay);
			},

			/**
			 * Un-schedules a timer by its id
			 * @param {string} timerId - the identifier of the timer
			 */
			clear: function(timerId) {
				if (Wicket.TimerHandles && Wicket.TimerHandles[timerId]) {
					clearTimeout(Wicket.TimerHandles[timerId]);
					delete Wicket.TimerHandles[timerId];
				}
			}
		}
	});

	/**
	 * A special event that is used to listen for immediate changes in input fields.
	 */
	jQuery.event.special.inputchange = {

		keys : {
			BACKSPACE	: 8,
			TAB			: 9,
			ENTER		: 13,
			ESC			: 27,
			LEFT		: 37,
			UP			: 38,
			RIGHT		: 39,
			DOWN		: 40,
			SHIFT		: 16,
			CTRL		: 17,
			ALT			: 18,
			END			: 35,
			HOME		: 36
		},

		keyDownPressed : false,

		setup: function () {

			if (Wicket.Browser.isIELessThan11()) {

				jQuery(this).on('keydown', function (event) {
					jQuery.event.special.inputchange.keyDownPressed = true;
				});

				jQuery(this).on("cut paste", function (evt) {

					var self = this;

					if (false === jQuery.event.special.inputchange.keyDownPressed) {
						window.setTimeout(function() {
							jQuery.event.special.inputchange.handler.call(self, evt);
						}, 10);
					}
				});

				jQuery(this).on("keyup", function (evt) {
					jQuery.event.special.inputchange.keyDownPressed = false; // reset
					jQuery.event.special.inputchange.handler.call(this, evt);
				});

			} else {

				jQuery(this).on("input", jQuery.event.special.inputchange.handler);
			}
		},

		teardown: function() {
			jQuery(this).off("input keyup cut paste", jQuery.event.special.inputchange.handler);
		},

		handler: function( evt ) {
			var WE = Wicket.Event;
			var k = jQuery.event.special.inputchange.keys;

			var kc = WE.keyCode(WE.fix(evt));
			switch (kc) {
				case k.ENTER:
				case k.UP:
				case k.DOWN:
				case k.ESC:
				case k.TAB:
				case k.RIGHT:
				case k.LEFT:
				case k.SHIFT:
				case k.ALT:
				case k.CTRL:
				case k.HOME:
				case k.END:
					return WE.stop(evt);
				default:
					evt.type = "inputchange";
					var args = Array.prototype.slice.call( arguments, 0 );
					return jQuery(this).trigger(evt.type, args);
			}
		}
	};

	// MISC FUNCTIONS

	Wicket.Event.add(window, 'domready', Wicket.Focus.attachFocusEvent);

	/**
	 * Clear any scheduled Ajax timers when leaving the current page
	 */
	Wicket.Event.add(window, "unload", function() {
		var WTH = Wicket.TimerHandles;
		if (WTH) {
			for (var th in WTH) {
				if (WTH.hasOwnProperty(th)) {
					window.clearTimeout(WTH[th]);
					delete WTH[th];
				}
			}
		}
	});

	/**
	 * Remove any scheduled timers on the removed element.
	 * This wont remove the timer for elements which are children of the removed one.
	 */
	Wicket.Event.subscribe('/dom/node/removing', function(jqEvent, element) {
		var id = element.id;
		if (Wicket.TimerHandles && Wicket.TimerHandles[id]) {
			window.clearTimeout(Wicket.TimerHandles[id]);
			delete Wicket.TimerHandles[id];
		}
	});

	/**
	 * Remove any scheduled timers on elements which are no more in the DOM document.
	 * This removes the timers for all elements which parents have been removed from the DOM.
	 */
	Wicket.Event.subscribe('/dom/node/added', function() {
		if (Wicket.TimerHandles) {
			for (var timerHandle in Wicket.TimerHandles) {
				if (Wicket.$$(timerHandle) === false) {
					window.clearTimeout(timerHandle);
					delete Wicket.TimerHandles[timerHandle];
				}
			}
		}
	});

})();
