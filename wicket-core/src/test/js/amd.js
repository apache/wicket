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

/*global ok: true, start: true, test: true, equal: true, deepEqual: true,
 QUnit: true, module: true, expect: true, define: true */

define(
	[
		"jquery",
		"Wicket",
		"WicketDebugWindow"
	],
	function ($, w) {
		"use strict";

		return {
			runTests: function () {
				test("Wicket object is successfully loaded via RequireJs", function () {

					expect(15);

					// test for one member for each Wicket.** module
					ok($.isFunction(w.Browser.isChrome), "Wicket Browser (from wicket-event-jquery.js) is imported");
					ok($.isFunction(w.Event.fix), "Wicket Event (from wicket-event-jquery.js) is imported");
					ok($.isFunction(w.Ajax.ajax), "Wicket Ajax (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(w.DOM.get), "Wicket DOM (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(w.Form.serialize), "Wicket Form (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(new w.Channel("blah|s").done), "Wicket Channel (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(new w.ChannelManager().done), "Wicket ChannelManager (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(w.Class.create), "Wicket Class (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(w.Head.addElement), "Wicket Head (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(w.Focus.attachFocusEvent), "Wicket Focus (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(w.Log.error), "Wicket Log (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(new w.Throttler().throttle), "Wicket Throttler (from wicket-ajax-jquery.js) is imported");
					ok($.isFunction(new w.ThrottlerEntry().getFunc), "Wicket ThrottlerEntry (from wicket-ajax-jquery.js) is imported");

					equal('div', w.DOM.get('amdElement').tagName.toLowerCase(), "Wicket.DOM.get() works");

					ok($.isFunction(w.Ajax.DebugWindow.logError), "Wicket Ajax.DebugWindow (from wicket-ajax-jquery-debug.js) is imported");
				});
			}
		};
	}
);