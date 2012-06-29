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

/*jshint evil: true, nomen: false, onevar: false, regexp: false, strict: true, boss: true, undef: true, maxlen: 160, curly: true, eqeqeq: true */
/*global document: false, jQuery:false, DOMParser: true, window: false, Wicket: true, ActiveXObject: true */


/*
 * Wicket Ajax Support
 *
 * @author Igor Vaynberg
 * @author Matej Knopp
 */

;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'object' && typeof(Wicket.Ajax) === 'object') {
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
		var regex = '';
		eval('regex = /' + from.replace( /\W/g ,'\\$&' ) + '/g ;');
		return str.replace(regex,to);
	};

	/**
	 * Creates an iframe that can be used to load data asynchronously or as a
	 * target for Ajax form submit.
	 *
	 * @param {String} the value of the iframe's name attribute
	 */
	createIFrame = function (iframeName) {
		var $iframe = jQuery('<iframe>', {
			name: iframeName,
			id: iframeName,
			css: {
				position: 'absolute',
				top: '-9999px',
				left: '-9999px'
			},
			src: 'about:blank'
		});
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
	 * @param envelope (DispHtmlDocument) the document object created by IE from the XML response in the iframe
	 */
	htmlToDomDocument = function (htmlDocument) {
		var xmlAsString = htmlDocument.body.outerText;
		xmlAsString = xmlAsString.replace(/^\s+|\s+$/g, ''); // trim
		xmlAsString = xmlAsString.replace(/(\n|\r)-*/g, ''); // remove '\r\n-'. The dash is optional.
		var xmldoc = Wicket.Xml.parse(xmlAsString);
		return xmldoc;
	};

	/**
	 * Functions executer takes array of functions and executes them. Each function gets
	 * the notify object, which needs to be called for the next function to be executed.
	 * This way the functions can be executed synchronously. Each function has to call
	 * the notify object at some point, otherwise the functions after it wont be executed.
	 * After the FunctionExecuter is initiatialized, the start methods triggers the
	 * first function.
	 */
	var FunctionsExecuter = function (functions) {

		this.functions = functions;

		this.current = 0;

		this.depth = 0; // we need to limit call stack depth

		this.processNext = function () {
			if (this.current < this.functions.length) {
				var f, run;

				f = this.functions[this.current];
				run = function () {
					try {
						var n = jQuery.proxy(this.notify, this);
						f(n);
					}
					catch (e) {
						Wicket.Log.error("FunctionsExecuter.processNext: " + e);
						jQuery.proxy(this.notify, this);
					}
				};
				run = jQuery.proxy(run, this);
				this.current++;

				if (this.depth > 50 || Wicket.Browser.isKHTML() || Wicket.Browser.isSafari()) {
					// to prevent khtml bug that crashes entire browser
					// or to prevent stack overflow (safari has small call stack)
					this.depth = 0;
					window.setTimeout(run, 1);
				} else {
					this.depth ++;
					run();
				}
			}
		};

		this.start = function () {
			this.processNext();
		};

		this.notify = function () {
			this.processNext();
		};
	};


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
			var res = name.match(/^([^|]+)\|(d|s)$/);
			if (isUndef(res)) {
				this.name = '';
				this.type = 's'; // default to stack
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
				Wicket.Log.info("Channel busy - postponing...");
				if (this.type === 's') { // stack
					this.callbacks.push(callback);
				}
				else { /* drop */
					this.callbacks = [];
					this.callbacks[0] = callback;
				}
				return null;
			}
		},

		done: function () {
			var c = null;

			if (this.callbacks.length > 0) {
				c = this.callbacks.shift();
			}

			if (c !== null && typeof(c) !== "undefined") {
				Wicket.Log.info("Calling postponed function...");
				// we can't call the callback from this call-stack
				// therefore we set it on timer event
				window.setTimeout(c, 1);
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
		},

		/**
		 * A helper function that executes an array of handlers (before, success, failure)
		 *
		 * @param {Array[FunctionBody]} handlers - the handlers to execute
		 */
		_executeHandlers: function (handlers) {
			if (jQuery.isArray(handlers)) {

				// cut the handlers argument
				var args = Array.prototype.slice.call(arguments).slice(1);

				for (var i = 0; i < handlers.length; i++) {
					var handler = handlers[i];
					if (jQuery.isFunction(handler)) {
						handler.apply(this, args);
					} else {
						new Function(handler).apply(this, args);
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
		 */
		_asParamArray: function(parameters) {
			var result = [];
			if (jQuery.isArray(parameters)) {
				result = parameters;
			}
			else if (jQuery.isPlainObject(parameters)) {
				for (var name in parameters) {
					var value = parameters[name];
					result.push({name: name, value: value});
				}
			}

			return result;
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

			var
				// the headers to use for each Ajax request
				headers = {
					'Wicket-Ajax': 'true',
					'Wicket-Ajax-BaseURL': getAjaxBaseUrl()
				},

				// the request (extra) parameters
				data = this._asParamArray(attrs.ep),

				// keep a reference to the current context
				self = this,

				// the precondition to use if there are no explicit ones
				defaultPrecondition = [ function () {
					if (attrs.c) {
						if (attrs.f) {
							return Wicket.$$(attrs.c) && Wicket.$$(attrs.f);
						} else {
							return Wicket.$$(attrs.c);
						}
					}
				}];

			if (Wicket.Focus.lastFocusId) {
				headers["Wicket-FocusedElementId"] = Wicket.Focus.lastFocusId;
			}

			if (attrs.mp) { // multipart form. jQuery doesn't help here ...
				// TODO Wicket.next - should we execute all handlers ?!
				// Wicket 1.5 didn't support success/failure handlers for this, but we can do it
				return this.submitMultipartForm(attrs);
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

					var preconditions = attrs.pre || defaultPrecondition;
					if (jQuery.isArray(preconditions)) {
						for (var p = 0; p < preconditions.length; p++) {

							var precondition = preconditions[p];
							var result;
							if (jQuery.isFunction(precondition)) {
								result = precondition(attrs, jqXHR, settings);
							} else {
								result = new Function('attrs', 'jqXHR', 'settings', precondition)(attrs, jqXHR, settings);
							}
							if (result === false) {
								Wicket.Log.info("Ajax request stopped because of precondition check, url: " + attrs.u);
								self.done();
								return false;
							}
						}
					}

					// collect the dynamic extra parameters
					if (jQuery.isArray(attrs.dep)) {
						var deps = attrs.dep,
							params = [],
							queryString,
							separator;

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
						queryString = jQuery.param(params);
						if (settings.type.toLowerCase() === 'post') {
							separator = settings.data.length > 0 ? '&' : '';
							settings.data = settings.data + separator + queryString;
						} else {
							separator = settings.url.indexOf('?') > -1 ? '&' : '?';
							settings.url = settings.url + separator + queryString;
						}
					}

					Wicket.Event.publish('/ajax/call/before', attrs, jqXHR, settings);
					self._executeHandlers(attrs.bh, attrs, jqXHR, settings);

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
						self.processAjaxResponse(data, textStatus, jqXHR, attrs);
					} else {
						self._executeHandlers(attrs.sh, attrs, jqXHR, data, textStatus);
					}
					Wicket.Event.publish('/ajax/call/success', attrs, jqXHR, data, textStatus);

				},
				error: function(jqXHR, textStatus, errorMessage) {

					self.failure(attrs, jqXHR, errorMessage, textStatus);
					Wicket.Event.publish('/ajax/call/failure', attrs, jqXHR, errorMessage, textStatus);

				},
				complete: function (jqXHR, textStatus) {
					if (attrs.i) {
						Wicket.DOM.hideIncrementally(attrs.i);
					}

					self._executeHandlers(attrs.coh, attrs, jqXHR, textStatus);
					Wicket.Event.publish('/ajax/call/complete', attrs, jqXHR, textStatus);

					this.done();
				}
			});

			// execute after handlers right after the Ajax request is fired
			self._executeHandlers(attrs.ah, attrs);
			Wicket.Event.publish('/ajax/call/after', attrs);

			if (!attrs.ad && attrs.event) {
				attrs.event.preventDefault();
			}

			return jqXHR;
		},

		/**
		 * Method that processes the <ajax-response>.
		 *
		 * @param {XmlDocument} data - the <ajax-response> XML document
		 * @param {String} textStatus - the response status as text (e.g. 'success', 'parsererror', etc.)
		 * @param {Object} jqXHR - the jQuery wrapper around XMLHttpRequest
		 * @param {Object} attrs - the Ajax request attributes
		 */
		processAjaxResponse: function (data, textStatus, jqXHR, attrs) {

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
						var responseAsText = jQuery(data).text();
						Wicket.Log.info("Received ajax response (" + responseAsText.length + " characters)");
						Wicket.Log.info("\n" + responseAsText);
					}

					// invoke the loaded callback with an xml document
					return this.loadedCallback(data, attrs);
				}
			}
		},

		/**
		 * This method serializes a form and sends it as POST body. If the form contains multipart content
		 * this function will post the form using an iframe instead of the regular ajax call
		 * and bridge the output - transparently making this work  as if it was an ajax call.
		 *
		 * @param {Object} attrs - the ajax request attributes
		 */
		submitMultipartForm: function (attrs) {

			var form = Wicket.$(attrs.f);
			if (!form) {
				Wicket.Log.error("Wicket.Ajax.Call.submitForm: Trying to submit form with id '" + attrs.f + "' that is not in document.");
				return;
			}

			var submittingAttribute = 'data-wicket-submitting';

			if (form.onsubmit && !form.getAttribute(submittingAttribute)) {
				form.setAttribute(submittingAttribute, submittingAttribute);
				var retValue = form.onsubmit();
				if (typeof(retValue) === "undefined") {
					retValue = true;
				}
				form.removeAttribute(submittingAttribute);
				if (!retValue) {
					return;
				}
			}

			var multipart = true;

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
			form.method = "post";
			form.enctype = "multipart/form-data";
			form.encoding = "multipart/form-data";

			// create submitting button element
			if (attrs.sc) {
				var $btn = jQuery("<input type='hidden' name='" + attrs.sc + "' id='" + iframe.id + "-btn' value='1'/>");
				form.appendChild($btn[0]);
			}

			//submit the form into the iframe, response will be handled by the onload callback
			form.submit();

			// install handler to deal with the ajax response
			// ... we add the onload event after form submit because chrome fires it prematurely
			Wicket.Event.add(iframe, "load.handleMultipartComplete", jQuery.proxy(this.handleMultipartComplete, this), attrs);

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

			var iframe = event.target;

			var envelope = iframe.contentWindow.document;
			if (envelope.XMLDocument) {
				envelope = envelope.XMLDocument;
			}

			// process the response
			var attrs = event.data;
			this.loadedCallback(envelope, attrs);

			// stop the event
			event.stopPropagation();

			// remove the event
			jQuery(iframe).off("load.handleMultipartComplete");

			// remove the iframe and button elements
			window.setTimeout(function () {
				jQuery('#'+iframe.id + '-btn').remove();
				jQuery(iframe).remove();
			}, 250);
		},

		// Processes the response
		loadedCallback: function (envelope, attrs) {
			// To process the response, we go through the xml document and add a function for every action (step).
			// After this is done, a FunctionExecuter object asynchronously executes these functions.
			// The asynchronous execution is necessary, because some steps might involve loading external javascript,
			// which must be asynchronous, so that it doesn't block the browser, but we also have to maintain
			// the order in which scripts are loaded and we have to delay the next steps until the script is
			// loaded.
			try {
				var root = envelope.getElementsByTagName("ajax-response")[0];

				if (root === null && envelope.compatMode === 'BackCompat') {
					envelope = htmlToDomDocument(envelope);
					root = envelope.getElementsByTagName("ajax-response")[0];
				}

				// the root element must be <ajax-response
				if (isUndef(root) || root.tagName !== "ajax-response") {
					this.failure("Could not find root <ajax-response> element");
					return;
				}

				// initialize the array for steps (closures that execute each action)
				var steps = [];

				// start it a bit later so that the browser does handle the next event
				// before the component is or can be replaced. We could do (if (!posponed))
				// because if there is already something in the queue then we could execute that immedietly
				steps.push(jQuery.proxy(function (notify) {
					window.setTimeout(notify, 2);
				}, this));

				// go through the ajax response and execute all priority-invocations first
				for (var i = 0; i < root.childNodes.length; ++i) {
					var node = root.childNodes[i];
					if (node.tagName === "priority-evaluate") {
						this.processEvaluation(steps, node, attrs);
					}
				}

				// go through the ajax response and for every action (component, js evaluation, header contribution)
				// ad the proper closure to steps
				var stepIndexOfLastReplacedComponent = -1;
				for (var i = 0; i < root.childNodes.length; ++i) {
					var node = root.childNodes[i];

					if (node.tagName === "component") {
						if (stepIndexOfLastReplacedComponent === -1) {
							this.processFocusedComponentMark(steps);
						}
						stepIndexOfLastReplacedComponent = steps.length;
						this.processComponent(steps, node);
					} else if (node.tagName === "evaluate") {
						this.processEvaluation(steps, node, attrs, attrs.event);
					} else if (node.tagName === "header-contribution") {
						this.processHeaderContribution(steps, node);
					} else if (node.tagName === "redirect") {
						this.processRedirect(steps, node);
					}

				}
				if (stepIndexOfLastReplacedComponent !== -1) {
					this.processFocusedComponentReplaceCheck(steps, stepIndexOfLastReplacedComponent);
				}

				// add the last step, which should trigger the success call the done method on request
				this.success(steps, attrs);

				Wicket.Log.info("Response parsed. Now invoking steps...");
				var executer = new FunctionsExecuter(steps);
				executer.start();
			} catch (exception) {
				this.failure(attrs, null, exception);
			}
		},

		// Adds a closure to steps that should be invoked after all other steps have been successfully executed
		success: function (steps, attrs) {
			steps.push(jQuery.proxy(function (notify) {
				Wicket.Log.info("Response processed successfully.");

				this._executeHandlers(attrs.sh,
						["data", "textStatus", "jqXHR", "attrs"],
						null, 'success', null, attrs);

				// re-attach the events to the new components (a bit blunt method...)
				// This should be changed for IE See comments in wicket-event.js add (attachEvent/detachEvent)
				// IE this will cause double events for everything.. (mostly because of the jQuery.proxy(element))
				Wicket.Focus.attachFocusEvent();

				// set the focus to the last component
				window.setTimeout("Wicket.Focus.requestFocus();", 0);

				this.done();

				// continue to next step (which should make the processing stop, as success should be the final step)
				notify();
			}, this));
		},

		// On ajax request failure
		failure: function (attrs, jqXHR, errorMessage, textStatus) {
			if (errorMessage) {
				Wicket.Log.error("Wicket.Ajax.Call.failure: Error while parsing response: " + errorMessage);
			}
			this._executeHandlers(attrs.fh, attrs, errorMessage);
		},

		done: function () {
			Wicket.channelManager.done(this.channel);
		},

		// Adds a closure that replaces a component
		processComponent: function (steps, node) {
			steps.push(function (notify) {
				// get the component id
				var compId = node.getAttribute("id");
				var text = jQuery(node).text();

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
				notify();
			});
		},

		/**
		 * Adds a closure that evaluates javascript code.
		 * @param steps {Array} - the steps for FunctionExecutor
		 * @param node {XmlElement} - the <[priority-]evaluate> element with the script to evaluate
		 * @param attrs {Object} - the attributes used for the Ajax request
		 * @param event {jQuery.Event} - the event that caused this Ajax call
 		 */
		processEvaluation: function (steps, node, attrs, event) {
			steps.push(function (notify) {
				// get the javascript body
				var text = jQuery(node).text();

				// unescape it if necessary
				var encoding = node.getAttribute("encoding");
				if (encoding) {
					text = Wicket.Head.Contributor.decode(encoding, text);
				}

				// test if the javascript is in form of identifier|code
				// if it is, we allow for letting the javascript decide when the rest of processing will continue
				// by invoking identifier();
				var res = text.match(new RegExp("^([a-z|A-Z_][a-z|A-Z|0-9_]*)\\|((.|\\n)*)$"));

				if (res !== null) {
					var f = jQuery.noop;
					text = "f = function(" + res[1] + ") {" + res[2] + "};";

					try {
						// do the evaluation
						eval(text);
						f(notify);
					} catch (exception) {
						Wicket.Log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text);
					}

				} else {
					// just evaluate the javascript
					try {
						// do the evaluation
						eval(text);
					} catch (exception) {
						Wicket.Log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text);
					}
					// continue to next step
					notify();
				}
			});
		},

		// Adds a closure that processes a header contribution
		processHeaderContribution: function (steps, node) {
			var c = Wicket.Head.Contributor;
			c.processContribution(steps, node);
		},

		// Adds a closure that processes a redirect
		processRedirect: function (steps, node) {
			var text = jQuery(node).text();
			Wicket.Log.info("Redirecting to: " + text);
			window.location = text;
		},

		// mark the focused component so that we know if it has been replaced by response
		processFocusedComponentMark: function (steps) {
			steps.push(function (notify) {
				Wicket.Focus.markFocusedComponent();

				// continue to next step
				notify();
			});
		},

		// detect if the focused component was replaced
		processFocusedComponentReplaceCheck: function (steps, lastReplaceComponentStep) {
			// add this step imediately after all components have been replaced
			steps.splice(lastReplaceComponentStep + 1, 0, function (notify) {
				Wicket.Focus.checkFocusedComponentReplaced();

				// continue to next step
				notify();
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
			 * @param {HTMLFormSelectElement} select - the form element to serialize
			 * @return an object of key -> value pair where 'value' can be an array of Strings if the select is .multiple,
			 *		or empty object if the form element is disabled.
			 */
			serializeSelect: function (select){
				var result = [];
				if (select) {
					var $select = jQuery(select);
					if ($select.length > 0 && $select.prop('disabled') === false) {
						var name = $select.attr('name');
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
			 * Serializes a form element to a key=value string in URL encoded notation.
			 * Note: this function intentionally ignores image and submit inputs.
			 *
			 * @param {HtmlFormElement} input - the form element to serialize
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

			//list of item to exclude from serialization
			excludeFromAjaxSerialization: {
			},

			// Returns url/post-body fragment representing element (e)
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

			/** shows an element */
			show: function (e) {
				e = Wicket.$(e);
				if (e !== null) {
					e.style.display = "";
				}
			},

			/** hides an element */
			hide: function (e) {
				e = Wicket.$(e);
				if (e !== null) {
					e.style.display = "none";
				}
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
						e.push(Wicket.$(arguments[i]));
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
				if (typeof(element) === "string") {
					element = Wicket.$(element);
				}
				if (isUndef(element) || isUndef(element.tagName)) {
					return true;
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

				Wicket.Event.publish('/dom/node/removing', element);

				if (element.tagName.toLowerCase() === "title") {
					// match the text between the tags
					var titleText = />(.*?)</.exec(text)[1];
					document.title = titleText;
					return;
				} else {
					var $newElement = jQuery(text);
					// WICKET-4236
					jQuery(element).after($newElement).remove();
				}

				var newElement = Wicket.$(element.id);
				Wicket.Event.publish('/dom/node/added', newElement);
			},

			// Method for serializing DOM nodes to string
			// original taken from Tacos (http://tacoscomponents.jot.com)
			serializeNodeChildren: function (node) {
				if (isUndef(node)) {
					return "";
				}
				var result = "";

				if (node.childNodes.length > 0) {
					for (var i = 0; i < node.childNodes.length; i++) {
						var thisNode = node.childNodes[i];
						switch (thisNode.nodeType) {
							case 1: // ELEMENT_NODE
							case 5: // ENTITY_REFERENCE_NODE
								result += this.serializeNode(thisNode);
								break;
							case 8: // COMMENT
								result += "<!--" + thisNode.nodeValue + "-->";
								break;
							case 4: // CDATA_SECTION_NODE
								result += "<![CDATA[" + thisNode.nodeValue + "]]>";
								break;
							case 3: // TEXT_NODE
							case 2: // ATTRIBUTE_NODE
								result += thisNode.nodeValue;
								break;
							default:
								break;
						}
					}
				} else {
					result += node.textContent || node.text;
				}
				return result;
			},

			serializeNode: function (node){
				if (isUndef(node)) {
					return "";
				}
				var result = "";
				result += '<' + node.nodeName;

				if (node.attributes && node.attributes.length > 0) {

					for (var i = 0; i < node.attributes.length; i++) {
						// serialize the attribute only if it has meaningful value that is not inherited
						if (node.attributes[i].nodeValue && node.attributes[i].specified) {
							result += " " + node.attributes[i].name +
								"=\"" + node.attributes[i].value + "\"";
						}
					}
				}

				result += '>';
				result += Wicket.DOM.serializeNodeChildren(node);
				result += '</' + node.nodeName + '>';
				return result;
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
			}
		},

		/**
		 * The Ajax class handles low level details of creating XmlHttpRequest objects,
		 * as well as registering and execution of pre-call, post-call and failure handlers.
		 */
		 Ajax: {

			Call: Wicket.Ajax.Call,

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
					Wicket.Event.add(attrs.c, evt, function (jqEvent) {
						var call = new Wicket.Ajax.Call();
						attrs.event = Wicket.Event.fix(jqEvent);

						var throttlingSettings = attrs.tr;
						if (throttlingSettings) {
							var postponeTimerOnUpdate = throttlingSettings.p || false;
							var throttler = new Wicket.Throttler(postponeTimerOnUpdate);
							throttler.throttle(throttlingSettings.id, throttlingSettings.d,
								Wicket.bind(function () {
									call.ajax(attrs);
									return attrs.ad;
								}, this));
						}
						else {
							call.ajax(attrs);
							return attrs.ad;
						}
					});
				});
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
					var text = jQuery(headerNode).text();
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
				processContribution: function (steps, headerNode) {
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
								this.processLink(steps, node);
							} else if (name === "script") {
								this.processScript(steps, node);
							} else if (name === "style") {
								this.processStyle(steps, node);
							}
						} else if (node.nodeType === 8) { // comment type
							this.processComment(steps, node);
						}
					}
				},

				// Process an external stylesheet element
				processLink: function (steps, node) {
					steps.push(function (notify) {
						// if the element is already in head, skip it
						if (Wicket.Head.containsElement(node, "href")) {
							notify();
							return;
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
					});
				},

				// Process an inline style element
				processStyle: function (steps, node) {
					steps.push(function (notify) {
						// if element with same id is already in document, skip it
						if (Wicket.DOM.containsElement(node)) {
							notify();
							return;
						}
						// serialize the style to string
						var content = Wicket.DOM.serializeNodeChildren(node);

						// create style element
						var style = Wicket.Head.createElement("style");

						// copy id attribute
						style.id = node.getAttribute("id");

						// create stylesheet
						if (Wicket.Browser.isIE()) {
							try  {
								document.createStyleSheet().cssText = content;
							}
							catch (ignore) {
								var run = function() {
									try {
										document.createStyleSheet().cssText = content;
									}
									catch(e) {
										Wicket.Log.error("Wicket.Head.Contributor.processStyle: " + e);
									}
								};
								window.setTimeout(run, 1);
							}
						} else {
							var textNode = document.createTextNode(content);
							style.appendChild(textNode);
						}
						Wicket.Head.addElement(style);

						// continue to next step
						notify();
					});
				},

				// Process a script element (both inline and external)
				processScript: function (steps, node) {
					steps.push(function (notify) {
						// if element with same id is already in document,
						// or element with same src attribute is in document, skip it
						if (Wicket.DOM.containsElement(node) ||
							Wicket.Head.containsElement(node, "src")) {
							notify();
							return;
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

						} else {
							// serialize the element content to string
							var text = Wicket.DOM.serializeNodeChildren(node);
							// get rid of prefix and suffix, they are not eval-d correctly
							text = text.replace(/^\n\/\*<!\[CDATA\[\*\/\n/, "");
							text = text.replace(/\n\/\*\]\]>\*\/\n$/, "");

							var id = node.getAttribute("id");

							if (typeof(id) === "string" && id.length > 0) {
								// add javascript to document head
								Wicket.Head.addJavascript(text, id);
							} else {
								try {
									eval(text);
								} catch (e) {
									Wicket.Log.error("Wicket.Head.Contributor.processScript: " + e + ": eval -> " + text);
								}
							}

							// continue to next step
							notify();
						}
					});
				},

				// process (conditional) comments
				processComment: function (steps, node) {
					steps.push(function (notify) {
						var comment = document.createComment(node.nodeValue);
						Wicket.Head.addElement(comment);
						notify();
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
			addJavascript: function (content, id, fakeSrc) {
				content = 'try{'+content+'}catch(e){Wicket.Log.error(e);}';
				var script = Wicket.Head.createElement("script");
				if (id) {
					script.id = id;
				}
				script.setAttribute("src_", fakeSrc);
				script.setAttribute("type", "text/javascript");

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

					// if it is a reference, just add it to head
					if (src !== null && src.length > 0) {
						var e = document.createElement("script");
						e.setAttribute("type","text/javascript");
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

						Wicket.Head.addJavascript(content, element.id);
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

				// HACK - for safari stopPropagation doesn't work well because
				// it also prevents scrollbars and form components getting the
				// event. Therefore for safari the 'ignore' flag is set on event.
				if (typeof(e.ignore) === "undefined") {

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
				}
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
			 *
			 * @param {Event} e
			 */
			mouseUp: function (e) {
				e = Wicket.Event.fix(e);
				var o = Wicket.Drag.current;

				if (o !== null && typeof(o) !== "undefined") {
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

				// IE doesn't have the property "target".
				// Use "srcElement" instead.
				var target = event.target ? event.target : event.srcElement;
				if (target) {
					Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
					Wicket.Focus.lastFocusId = target.id;
					Wicket.Log.info("focus set on " + Wicket.Focus.lastFocusId);
				}
			},

			blur: function (event) {
				event = Wicket.Event.fix(event);

				// IE doesn't have the property "target".
				// Use "srcElement" instead.
				var target = event.target ? event.target : event.srcElement;
				if (target && Wicket.Focus.lastFocusId === target.id) {
					if (Wicket.Focus.refocusLastFocusedComponentAfterResponse) {
						// replaced components seem to blur when replaced only on Safari - so do not modify lastFocusId so it gets refocused
						Wicket.Log.info("focus removed from " + target.id + " but ignored because of component replacement");
					} else {
						Wicket.Focus.lastFocusId=null;
						Wicket.Log.info("focus removed from " + target.id);
					}
				}
			},

			getFocusedElement: function () {
				if (typeof(Wicket.Focus.lastFocusId) !== "undefined" && Wicket.Focus.lastFocusId !== "" && Wicket.Focus.lastFocusId !== null)
				{
					Wicket.Log.info("returned focused element: " + Wicket.$(Wicket.Focus.lastFocusId));
					return Wicket.$(Wicket.Focus.lastFocusId);
				}
				return;
			},

			setFocusOnId: function (id) {
				if (typeof(id) !== "undefined" && id !== "" && id !== null) {
					Wicket.Focus.refocusLastFocusedComponentAfterResponse = true;
					Wicket.Focus.focusSetFromServer = true;
					Wicket.Focus.lastFocusId = id;
					Wicket.Log.info("focus set on " + Wicket.Focus.lastFocusId + " from serverside");
				} else {
					Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
					Wicket.Log.info("refocus focused component after request stopped from serverside");
				}
			},

			// mark the focused component so that we know if it has been replaced or not by response
			markFocusedComponent: function () {
				var focusedElement = Wicket.Focus.getFocusedElement();
				if (typeof(focusedElement) !== "undefined" && focusedElement !== null) {
					// create a property of the focused element that would not remain there if component is replaced
					focusedElement.wasFocusedBeforeComponentReplacements = true;
					Wicket.Focus.refocusLastFocusedComponentAfterResponse = true;
					Wicket.Focus.focusSetFromServer = false;
				} else {
					Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
				}
			},

			// detect if the focused component was replaced
			checkFocusedComponentReplaced: function () {
				var focusedElement = Wicket.Focus.getFocusedElement();
				if (Wicket.Focus.refocusLastFocusedComponentAfterResponse === true)
				{
					if (typeof(focusedElement) !== "undefined" && focusedElement !== null) {
						if (typeof(focusedElement.wasFocusedBeforeComponentReplacements) !== "undefined")
						{
							// focus component was not replaced - no need to refocus it
							Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
						}
					} else {
						// focused component dissapeared completely - no use to try to refocus it
						Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
						Wicket.Focus.lastFocusId = "";
					}
				}
			},

			requestFocus: function() {
				// if the focused component is replaced by the ajax response, a re-focus might be needed
				// (if focus was not changed from server) but if not, and the focus component should
				// remain the same, do not re-focus - fixes problem on IE6 for combos that have
				// the popup open (refocusing closes popup)
				if (Wicket.Focus.refocusLastFocusedComponentAfterResponse &&
					typeof(Wicket.Focus.lastFocusId) !== "undefined" &&
					Wicket.Focus.lastFocusId !== "" &&
					Wicket.Focus.lastFocusId !== null)
				{
					var toFocus = Wicket.$(Wicket.Focus.lastFocusId);

					if (toFocus !== null && typeof(toFocus) !== "undefined") {
						Wicket.Log.info("Calling focus on " + Wicket.Focus.lastFocusId);
						try {
							if (Wicket.Focus.focusSetFromServer) {
								toFocus.focus();
							} else {
								// avoid loops like - onfocus triggering an event the modifies the tag => refocus => the event is triggered again
								var temp = toFocus.onfocus;
								toFocus.onfocus = null;
								toFocus.focus();
								// IE needs setTimeout (it seems not to call onfocus sync. when focus() is called
								window.setTimeout(function () {toFocus.onfocus = temp; }, 0);
							}
						} catch (ignore) {
						}
					}
					else
					{
						Wicket.Focus.lastFocusId = "";
						Wicket.Log.info("Couldn't set focus on " + Wicket.Focus.lastFocusId + " not on the page anymore");
					}
				}
				else if (Wicket.Focus.refocusLastFocusedComponentAfterResponse)
				{
					Wicket.Log.info("last focus id was not set");
				}
				else
				{
					Wicket.Log.info("refocus last focused component not needed/allowed");
				}
				Wicket.Focus.refocusLastFocusedComponentAfterResponse = false;
			},

			setFocusOnElements: function (elements) {
				// we need to cache array length because IE will try to recalculate
				// the collection of elements every time length() is called which can be quiet expensive
				// if the collection is a result of getElementsByTagName or a similar function.
				var len = elements.length;
				for (var i = 0; i < len; i++)
				{
					if (elements[i].wicketFocusSet !== true)
					{
						 Wicket.Event.add(elements[i], 'focus', Wicket.Focus.setFocus);
						 Wicket.Event.add(elements[i], 'blur', Wicket.Focus.blur);
						 elements[i].wicketFocusSet = true;
					}
				}
			},

			attachFocusEvent: function () {
				Wicket.Focus.setFocusOnElements(document.getElementsByTagName("input"));
				Wicket.Focus.setFocusOnElements(document.getElementsByTagName("select"));
				Wicket.Focus.setFocusOnElements(document.getElementsByTagName("textarea"));
				Wicket.Focus.setFocusOnElements(document.getElementsByTagName("button"));
				Wicket.Focus.setFocusOnElements(document.getElementsByTagName("a"));
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

			if (Wicket.Browser.isIE()) {

				jQuery(this).on('keydown', function (event) {
					jQuery.event.special.inputchange.keyDownPressed = true;
				});

				jQuery(this).on("cut paste", function (event) {

					var self = this;

					if (false === jQuery.event.special.inputchange.keyDownPressed) {
						window.setTimeout(function() {
							jQuery.event.special.inputchange.handler.apply(self, arguments);
						}, 10);
					}
				});

				jQuery(this).on("keyup", function (event) {
					jQuery.event.special.inputchange.keyDownPressed = false; // reset
					jQuery.event.special.inputchange.handler.apply(this, arguments);
				});

			} else {

				jQuery(this).on("input", jQuery.event.special.inputchange.handler);
			}
		},

		teardown: function() {
			jQuery(this).off("input keyup cut paste", jQuery.event.special.inputchange.handler);
		},

		handler: function( event ) {
			var WE = Wicket.Event;
			var k = jQuery.event.special.inputchange.keys;
			var kc = WE.keyCode(WE.fix(event));
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
					return WE.stop(event);
				default:
					event.type = "inputchange";
					var args = Array.prototype.slice.call( arguments, 0 );
					return jQuery.event.handle.apply( event.target, args );
			}
		}
	};

	// MISC FUNCTIONS

	Wicket.Event.add(window, 'domready', Wicket.Focus.attachFocusEvent);

})();
