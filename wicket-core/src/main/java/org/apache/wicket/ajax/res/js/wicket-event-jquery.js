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
 * Wicket Ajax Support
 *
 * @author Igor Vaynberg
 * @author Matej Knopp
 */

;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === 'undefined') {
		window.Wicket = {};
	}

	if (typeof(Wicket.Event) === 'object') {
		return;
	}

	jQuery.extend(true, Wicket, {

		Browser: {
			_isKHTML: null,
			isKHTML: function () {
				var wb = Wicket.Browser;
				if (wb._isKHTML === null) {
					wb._isKHTML = (/Konqueror|KHTML/).test(window.navigator.userAgent) && !/Apple/.test(window.navigator.userAgent);
				}
				return wb._isKHTML;
			},

			_isSafari: null,
			isSafari: function () {
				var wb = Wicket.Browser;
				if (wb._isSafari === null) {
					wb._isSafari = !/Chrome/.test(window.navigator.userAgent) && /KHTML/.test(window.navigator.userAgent) && /Apple/.test(window.navigator.userAgent);
				}
				return wb._isSafari;
			},

			_isChrome: null,
			isChrome: function () {
				var wb = Wicket.Browser;
				if (wb._isChrome === null) {
					wb._isChrome = (/KHTML/).test(window.navigator.userAgent) && /Apple/.test(window.navigator.userAgent) && /Chrome/.test(window.navigator.userAgent);
				}
				return wb._isChrome;
			},

			_isOpera: null,
			isOpera: function () {
				var wb = Wicket.Browser;
				if (wb._isOpera === null) {
					wb._isOpera = !Wicket.Browser.isSafari() && typeof(window.opera) !== "undefined";
				}
				return wb._isOpera;
			},

			_isIE: null,
			isIE: function () {
				var wb = Wicket.Browser;
				if (wb._isIE === null) {
					wb._isIE = !Wicket.Browser.isSafari() && (typeof(document.all) !== "undefined" || window.navigator.userAgent.indexOf("Trident/")>-1) && typeof(window.opera) === "undefined";
				}
				return wb._isIE;
			},

			_isIEQuirks: null,
			isIEQuirks: function () {
				var wb = Wicket.Browser;
				if (wb._isIEQuirks === null) {
					// is the browser internet explorer in quirks mode (we could use document.compatMode too)
					wb._isIEQuirks = Wicket.Browser.isIE() && window.document.documentElement.clientHeight === 0;
				}
				return wb._isIEQuirks;
			},

			_isIELessThan7: null,
			isIELessThan7: function () {
				var wb = Wicket.Browser;
				if (wb._isIELessThan7 === null) {
					var index = window.navigator.userAgent.indexOf("MSIE");
					var version = parseFloat(window.navigator.userAgent.substring(index + 5));
					wb._isIELessThan7 = Wicket.Browser.isIE() && version < 7;
				}
				return wb._isIELessThan7;
			},

			_isIE7: null,
			isIE7: function () {
				var wb = Wicket.Browser;
				if (wb._isIE7 === null) {
					var index = window.navigator.userAgent.indexOf("MSIE");
					var version = parseFloat(window.navigator.userAgent.substring(index + 5));
					wb._isIE7 = Wicket.Browser.isIE() && version >= 7;
				}
				return wb._isIE7;
			},

			_isIELessThan9: null,
			isIELessThan9: function () {
				var wb = Wicket.Browser;
				if (wb._isIELessThan9 === null) {
					var index = window.navigator.userAgent.indexOf("MSIE");
					var version = parseFloat(window.navigator.userAgent.substring(index + 5));
					wb._isIELessThan9 = Wicket.Browser.isIE() && version < 9;
				}
				return wb._isIELessThan9;
			},

			_isIELessThan11: null,
			isIELessThan11: function () {
				var wb = Wicket.Browser;
				if (wb._isIELessThan11 === null) {
					wb._isIELessThan11 = !Wicket.Browser.isSafari() && typeof(document.all) !== "undefined" && typeof(window.opera) === "undefined";
				}
				return wb._isIELessThan11;
			},

			_isIE11: null,
			isIE11: function () {
				var wb = Wicket.Browser;
				if (wb._isIE11 === null) {
					var userAgent = window.navigator.userAgent;
					var isTrident = userAgent.indexOf("Trident") > -1;
					var is11 = userAgent.indexOf("rv:11") > -1;
					wb._isIE11 = isTrident && is11;
				}
				return wb._isIE11;
			},

			_isGecko: null,
			isGecko: function () {
				var wb = Wicket.Browser;
				if (wb._isGecko === null) {
					wb._isGecko = (/Gecko/).test(window.navigator.userAgent) && !Wicket.Browser.isSafari();
				}
				return wb._isGecko;
			}
		},

		/**
		 * Events related code
		 * Based on code from Mootools (http://mootools.net)
		 */
		Event: {
			idCounter: 0,

			getId: function (element) {
				var $el = jQuery(element),
					id = $el.prop("id");
					
				if (typeof(id) === "string" && id.length > 0) {
					return id;
				} else {
					id = "wicket-generated-id-" + Wicket.Event.idCounter++;
					$el.prop("id", id);
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
			 * If no event is given as argument (IE), window.event is returned.
			 */
			fix: function (evt) {
				var evnt = evt || window.event;
				return jQuery.event.fix(evnt);
			},

			fire: function (element, event) {
				event = (event === 'mousewheel' && Wicket.Browser.isGecko()) ? 'DOMMouseScroll' : event;
				jQuery(element).trigger(event);
			},

			/**
			 * Binds an event listener for an element
			 *
			 * Also supports the special 'domready' event on window.
			 * 'domready' is event fired when the DOM is complete, but
			 * before loading external resources (images, scripts, ...)
			 *
			 * @param element {HTMLElement} The host HTML element
			 * @param type {String} The type of the DOM event
			 * @param fn {Function} The event handler to unbind
			 * @param data {Object} Extra data for the event
			 * @param selector {String} A selector string to filter the descendants of the selected
			 *      elements that trigger the event. If the selector is null or omitted,
			 *      the event is always triggered when it reaches the selected element.
			 */
			add: function (element, type, fn, data, selector) {
				if (type === 'domready') {
					jQuery(fn);
				} else if (type === 'load' && element === window) {
					jQuery(window).on('load', function() {
						jQuery(fn);
					});
				} else {
					type = (type === 'mousewheel' && Wicket.Browser.isGecko()) ? 'DOMMouseScroll' : type;
					var el = element;
					if (typeof(element) === 'string') {
						el = document.getElementById(element);
					}

					if (!el && Wicket.Log) {
						Wicket.Log.error('Cannot bind a listener for event "' + type +
							'" on element "' + element + '" because the element is not in the DOM');
					}

					jQuery(el).on(type, selector, data, fn);
				}
				return element;
			},

			/**
			 * Unbinds an event listener for an element
			 *
			 * @param element {HTMLElement} The host HTML element
			 * @param type {String} The type of the DOM event
			 * @param fn {Function} The event handler to unbind
			 */
			remove: function (element, type, fn) {
				jQuery(element).off(type, fn);
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
					jQuery(document).on(topic, subscriber);
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
				if (topic) {
					if (subscriber) {
						jQuery(document).off(topic, subscriber);
					} else {
						jQuery(document).off(topic);
					}
				} else {
					jQuery(document).off();
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
					var args = Array.prototype.slice.call(arguments).slice(1);
			
					jQuery(document).triggerHandler(topic, args);
					jQuery(document).triggerHandler('*', args);
				}
			},

			/**
			 * The names of the topics on which Wicket notifies
			 */
			Topic: {
				DOM_NODE_REMOVING      : '/dom/node/removing',
				DOM_NODE_ADDED         : '/dom/node/added',
				AJAX_CALL_BEFORE       : '/ajax/call/before',
				AJAX_CALL_PRECONDITION : '/ajax/call/precondition',
				AJAX_CALL_BEFORE_SEND  : '/ajax/call/beforeSend',
				AJAX_CALL_SUCCESS      : '/ajax/call/success',
				AJAX_CALL_COMPLETE     : '/ajax/call/complete',
				AJAX_CALL_AFTER        : '/ajax/call/after',
				AJAX_CALL_FAILURE      : '/ajax/call/failure',
				AJAX_HANDLERS_BOUND    : '/ajax/handlers/bound'
			}
		}
	});
})();
