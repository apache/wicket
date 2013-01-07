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
					url,
					hashIdx,
					delim;

				url = document.location.toString()
					.replace('http://', 'ws://')
					.replace('https://', 'wss://');
				hashIdx = url.indexOf('#');
				if (hashIdx > -1) {
					url = url.substring(0, hashIdx);
				}
				delim = url.indexOf('?') > -1 ? '&' : '?';
				url += delim + 'pageId=' + Wicket.WebSocket.pageId;
				url += '&wicket-ajax-baseurl=' + Wicket.WebSocket.baseUrl;
				self.ws = new WebSocket(url);

				self.ws.onopen = function (evt) {
					Wicket.Event.publish('/websocket/open', evt);
				};

				self.ws.onmessage = function (event) {

					var message = event.data;
					if (message && message.indexOf('<ajax-response>') > -1) {
						var call = new Wicket.Ajax.Call();
						call.process(message);
					}
					else {
						Wicket.Event.publish('/websocket/message', message);
					}
				};

				self.ws.onclose = function (evt) {
					if (self.ws) {
						self.ws.close();
						self.ws = null;
						Wicket.Event.publish('/websocket/closed', evt);
					}
				};

				self.ws.onerror = function (evt) {
					if (self.ws) {
						self.ws.close();
						self.ws = null;
						Wicket.Event.publish('/websocket/error', evt);
					}
				};
			} else {
				var errMessage = '[WebSocket.initialize] WebSocket is not supported in your browser!';
				Wicket.Log.error(errMessage);
				throw errMessage;
			}
		},

		send: function (text) {
			if (this.ws && text) {
				Wicket.Log.info("[WebSocket.send] Sending: " + text);
				this.ws.send(text);
			} else if (!text) {
				Wicket.Log.error("[WebSocket.send] Cannot send an empty text message!");
			} else {
				Wicket.Log.error("[WebSocket.send] No open WebSocket connection! Cannot send text message: " + text);
			}
		},

		close: function () {
			if (this.ws) {
				this.ws.close();
				Wicket.Log.info("[WebSocket.close] Connection closed.");
			} else {
				Wicket.Log.info("[WebSocket.close] Connection already closed.");
			}
		}
	};

	Wicket.WebSocket.createDefaultConnection = function () {
		if (!Wicket.WebSocket.INSTANCE) {
			Wicket.WebSocket.INSTANCE = new Wicket.WebSocket();
		}
	};

	Wicket.WebSocket.send = function (text) {
		if (Wicket.WebSocket.INSTANCE) {
			Wicket.WebSocket.INSTANCE.send(text);
		} else {
			Wicket.Log.error("[WebSocket.send] No default connection available!");
		}
	};

	Wicket.WebSocket.close = function () {
		if (Wicket.WebSocket.INSTANCE) {
			Wicket.WebSocket.INSTANCE.close();
			delete Wicket.WebSocket.INSTANCE;
		} else {
			Wicket.Log.info("[WebSocket.close] No default connection to close.");
		}
	};

})();