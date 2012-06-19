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
/*global document: false, jQuery:false, DOMParser: true, window: false, Wicket: true */

;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'undefined' || typeof(Wicket.Ajax) === 'undefined') {
		throw "Wicket.WebSocket needs wicket-ajax.js as prerequisite.";
	}

	Wicket.WebSocket = Wicket.Class.create();

	Wicket.WebSocket.prototype = {

		ws: null,

		initialize: function () {

			if (('WebSocket' in window)) {

				var self = this,
					url;

				url = document.location.toString().replace('http://', 'ws://');
				url += '&pageId=' + Wicket.WebSocket.pageId;
				self.ws = new WebSocket(url);

				self.ws.onopen = function () {
					Wicket.Event.publish('/websocket/open');
				};

				self.ws.onmessage = function (event) {

					var call = new Wicket.Ajax.Call();
					var attrs = {};
					call._initializeDefaults(attrs);
					attrs.event = event;
					var jqXHR = {
						readyState : 4
					};
					var message = event.data;
					if (message && message.indexOf('<ajax-response>') > -1) {
						var xmlDocument = Wicket.Xml.parse(event.data);
						call.processAjaxResponse(xmlDocument, "success", jqXHR, attrs);
					}
					else {
						Wicket.Event.publish('/websocket/message', message);
					}
				};

				self.ws.onclose = function (event) {
					if (self.ws) {
						self.ws.close();
						self.ws = null;
						Wicket.Event.publish('/websocket/closed');
					}
				};

				self.ws.onerror = function (e) {
					if (self.ws) {
						self.ws.close();
						self.ws = null;
						Wicket.Event.publish('/websocket/error');
					}
				};
			} else {
				Wicket.Log.error('WebSocket not supported in your browser!');

			}
		},

		send: function (text) {
			if (this.ws) {
				Wicket.Log.info("WebSocket.send: " + text);
				this.ws.send(text);
			}
		},

		close: function () {
			if (this.ws) {
				this.ws.close();
				Wicket.Log.info("WebSocket closed");
			}
		}
	};

})();