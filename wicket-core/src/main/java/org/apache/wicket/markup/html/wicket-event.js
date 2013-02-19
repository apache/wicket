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

if (!Function.prototype.bind) {
	// taken from https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Function/bind
	Function.prototype.bind = function (oThis) {
		if (typeof this !== "function") {
			// closest thing possible to the ECMAScript 5 internal IsCallable function
			throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
		}

		var aArgs = Array.prototype.slice.call(arguments, 1),
			fToBind = this,
			fNOP = function () {},
			fBound = function () {
				return fToBind.apply(this instanceof fNOP && oThis
					? this
					: oThis,
					aArgs.concat(Array.prototype.slice.call(arguments)));
			};

		fNOP.prototype = this.prototype;
		fBound.prototype = new fNOP();

		return fBound;
	};
}

// Wicket Namespace

if (typeof(Wicket) == "undefined")
	Wicket = { };



/**
 * Browser types
 */
if (typeof(Wicket.Browser) == "undefined") {
	Wicket.Browser = { 
		isKHTML: function() {
			return /Konqueror|KHTML/.test(navigator.userAgent) && !/Apple/.test(navigator.userAgent);
		},
		
		isSafari: function() {
			return !/Chrome/.test(navigator.userAgent) && /KHTML/.test(navigator.userAgent) && /Apple/.test(navigator.userAgent);
		},
		
		isChrome: function() {
			return /KHTML/.test(navigator.userAgent) && /Apple/.test(navigator.userAgent) && /Chrome/.test(navigator.userAgent);
		},
		
		isOpera: function() {
			return !Wicket.Browser.isSafari() && typeof(window.opera) != "undefined";
		},
	
		isIE: function() {
			return !Wicket.Browser.isSafari() && typeof(document.all) != "undefined" && typeof(window.opera) == "undefined";
		},
		
		isIEQuirks: function() {
			// is the browser internet explorer in quirks mode (we could use document.compatMode too)		
			return Wicket.Browser.isIE() && document.documentElement.clientHeight == 0;
		},		
		
		isIELessThan7: function() {
			var index = navigator.userAgent.indexOf("MSIE");
			var version = parseFloat(navigator.userAgent.substring(index + 5));
			return Wicket.Browser.isIE() && version < 7;
		},
		
		isIE7: function() {
			var index = navigator.userAgent.indexOf("MSIE");
			var version = parseFloat(navigator.userAgent.substring(index + 5));
			return Wicket.Browser.isIE() && version >= 7;
		},
		

		isIELessThan9: function() {
			var index = navigator.userAgent.indexOf("MSIE");
			var version = parseFloat(navigator.userAgent.substring(index + 5));
			return Wicket.Browser.isIE() && version < 9;
		},
		
		isGecko: function() {
			return /Gecko/.test(navigator.userAgent) && !Wicket.Browser.isSafari();
		}
	};
}




/**
 * Events related code
 * Based on code from Mootools (http://mootools.net)
 */

if (typeof(Wicket.Event) == "undefined") {
	Wicket.Event = {
		idCounter: 0,
		
		getId: function(element) {
			var current = element.getAttribute("id");
			if (typeof(current) == "string" && current.length > 0) {
				return current;
			} else {
				current = "wicket-generated-id-" + Wicket.Event.idCounter++;
				element.setAttribute("id", current);
				return current;
			}
		},
		
		handler: function() {
			var id = this[0];
			var original = this[1];
			var element = Wicket.$(id);
			original.bind(element)();
		},
	
		fire: function(element, event) {
			if (document.createEvent) {
				var e=document.createEvent("Event");
				e.initEvent(event, true, true);
				return element.dispatchEvent(e);
			} else {
				return element.fireEvent("on"+event);
			}
		},
		
		// adds an event of specified type to the element
		// also supports the domready event on window
		// domready is event fired when the DOM is complete, but before loading external resources (images, ...)
		add: function(element, type, fn) {
			// is the event domready?
			if (element == window && type == "domready") {
				Wicket.Event.addDomReadyEvent(fn);
			} else {
				if (element.addEventListener){
					element.addEventListener((type == 'mousewheel' && Wicket.Browser.isGecko()) ? 'DOMMouseScroll' : type, fn, false);
				} else {
					if (element == window || element == document)   {
						fn = fn.bind(element);
					}
					else {
						fn = Wicket.Event.handler.bind([Wicket.Event.getId(element), fn]);					
					}				
					// Because of the fn.bind (returning a new function object)
					// you can't detach the event first to be sure that there are no doubles :(
					//element.detachEvent('on'+type, fn);
					element.attachEvent('on'+type, fn);
				}
			}
			return element;
		},
		
		// handlers that will be fired on dom ready event
		domReadyHandlers : new Array(),
		
		// fires the dom ready event and cleanup the handlers
		fireDomReadyHandlers : function() {
			var h = Wicket.Event.domReadyHandlers;
			while (h.length > 0) {
				var c = h.shift();
				c();
			}
			Wicket.Event.domReadyHandlers = null;
		},
		
		// adds the dom ready event 
		addDomReadyEvent : function(fn) {
			// is the window already loaded?
			if (window.loaded)  {
				fn();
			} else if (!window.events || !window.events.domready) {
				// register the handler
				Wicket.Event.domReadyHandlers.push(fn);
			
				// callback
				var domReady = function() {
					if (window.loaded) 
						return;
					window.loaded = true;
					
					// invoke the handlers
					Wicket.Event.fireDomReadyHandlers();
				}.bind(this);
				
				if (document.readyState && 
				    (Wicket.Browser.isKHTML() || 
				    Wicket.Browser.isSafari() ||
				    Wicket.Browser.isChrome())
				) { 
				  // chrome, safari and konqueror don't support the event - simulate it through a timeout
					var domCheck = function() {
						if (document.readyState == "loaded" ||
						    document.readyState == "complete") {
						    domReady();
						} else {
							// dom not yet ready, set timer to check later
							window.setTimeout(domCheck, 10);
						}
					}
					window.setTimeout(domCheck, 10);
				} else if (document.readyState && Wicket.Browser.isIE()) { 
					if (document.getElementById('ie_ready') == null) {
						// for internet explorer we need to load a "dummy" scrip from ::/ to get the 
						// readystatechangeevernt - that means the main page being loaded and now the browser
						// is loading dependencies
						var src = (window.location.protocol == 'https:') ? '\/\/:' : 'javascript:void(0)';
						document.write('<script id="ie_ready" defer src="' + src + '"><\/script>');
						document.getElementById('ie_ready').onreadystatechange = function() {
							if (this.readyState == 'complete') domReady();
						};
					}
				} else { 
					// other browsers
					Wicket.Event.add(document, "DOMContentLoaded", domReady);
				}
			} else {
				window.addEventListener("domready", fn, false);
			}
		},
       
		/**
		* A hash of subscribers per topic.
		* String -> Array[Function].
		*/
		subscribers : {},

		/**
		* Adds a subscriber for the passed topic.
		* 
		* @param String topic - the channel name for which this subscriber will be notified
		*        If '*' then it will be notified for all topics
		* @param Function subscriber - the callback to call when an event with this type is published
		*/    
		subscribe: function(topic, subscriber) {
		   if (typeof(topic) === 'undefined' || topic === null) {
			   return;
		   }
		   
		   var subscribers;
		   
		   subscribers = Wicket.Event.subscribers[topic];
		   if (typeof(subscribers) === 'undefined' || subscribers === null) {
			   subscribers = [];
			   Wicket.Event.subscribers[topic] = subscribers;
		   }
		   
		   subscribers.push(subscriber);
		},

		/**
		* Sends a notification to all subscribers for the given topic.
		* Subscribers for topic '*' receive the actual topic as first parameter,
		* otherwise the topic is not passed to subscribers which listen for specific 
		* event types.
		*
		* @param String topic - the channel name for which all subscribers will be notified.
		*/
		publish: function(topic) {
		   if (typeof(topic) === 'undefined' || topic === null) {
			   return;
		   }
		   
		   var subscribers, subscriberArguments;

		   // notify all subscribers for this specific event type            
		   subscribers = Wicket.Event.subscribers[topic];
		   if (subscribers instanceof Array) {

			   // cut the topic argument
			   subscriberArguments = Array.prototype.slice.call(arguments).slice(1);
		   
			   for (var i = 0; i < subscribers.length; ++i) {
				   subscribers[i].apply(this, subscriberArguments);
			   }
		   }
		   
		   // notify all subscribers which listen for any kind of events
		   // and pass them the event type as first parameter
		   subscribers = Wicket.Event.subscribers['*'];
		   if (subscribers instanceof Array) {

			   for (var i = 0; i < subscribers.length; ++i) {
				   subscribers[i].apply(this, arguments);
			   }
		   }
		}           
	};
}
