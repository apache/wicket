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

/*global ok: true, start: true, test: true, asyncTest: true, equal: true, deepEqual: true,
 QUnit: true, module: true, expect: true, stop: true */

jQuery(document).ready(function() {
	"use strict";

	module('Wicket.Ajax.ChanneledAjax');

	test('schedules ajax request with the ChannelManager', function () {

		expect(2);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			url = 'URL',
			options = {
				success: function() {}
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(url, options);

		equal(cm.scheduled.channel, 'my-channel|d');
		notEqual(cm.scheduled.handler, undefined);

	});

	test('schedules a callback that calls the used transport when executed', function () {

		expect(2);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			url = 'URL',
			options = {
				success: function() { console.log("SUCCESS"); }
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(url, options);
		cm.scheduled.handler();

		equal(ajaxHandler.calledWith.url, url);
		equal(ajaxHandler.calledWith.options.success, options.success);
	});

	test('has a complete callback that is forwarded to the ajax transport and that calls done on the ChannelManager', function () {

		expect(2);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			url = 'URL',
			options = {
				success: function() { console.log("SUCCESS"); }
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(url, options);
		cm.scheduled.handler();

		notEqual(ajaxHandler.calledWith.options.complete, undefined);

		ajaxHandler.calledWith.options.complete(undefined, 'all-right');

		equal(cm.doneChannel, 'my-channel|d');
	});

	test('has a complete callback that calls the original complete callback', function () {

		expect(2);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			originalCompleted = false,

			url = 'URL',
			options = {
				success: function() { console.log("SUCCESS"); },
				complete: function() { originalCompleted = true }
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(url, options);
		cm.scheduled.handler();

		ajaxHandler.calledWith.options.complete(undefined, 'all-right');

		equal(cm.doneChannel, 'my-channel|d');
		equal(originalCompleted, true);
	});

	test('can be called only with options when the URL is named "url"', function () {

		expect(3);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			options = {
				url: 'TEST-URL',
				success: function() { console.log("SUCCESS"); }
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(options);
		cm.scheduled.handler();

		equal(ajaxHandler.calledWith.url, options.url);
		equal(ajaxHandler.calledWith.options.url, options.url);
		equal(ajaxHandler.calledWith.options.success, options.success);
	});

	test('can be called only with options when the URL is named "u"', function () {

		expect(3);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			options = {
				u: 'TEST-URL',
				success: function() { console.log("SUCCESS"); }
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(options);
		cm.scheduled.handler();

		equal(ajaxHandler.calledWith.url, options.u);
		equal(ajaxHandler.calledWith.options.u, options.u);
		equal(ajaxHandler.calledWith.options.success, options.success);
	});

	test('can be called with two option objects', function () {

		expect(3);

		var cm = new TestChannelManager(),
			ajaxHandler = new TestAjaxHandler(),

			name = 'my-channel',
			type = 'd',

			urlOptions = {
				url: 'TEST-URL'
			},
			options = {
				success: function() { console.log("SUCCESS"); }
			},

			channeledAjax = new Wicket.Ajax.ChanneledAjax(name, type, cm, ajaxHandler.ajax);

		channeledAjax.ajax(urlOptions, options);
		cm.scheduled.handler();

		equal(ajaxHandler.calledWith.url, urlOptions.url);
		equal(ajaxHandler.calledWith.options.url, urlOptions.url);
		equal(ajaxHandler.calledWith.options.success, options.success);
	});

	// Ajax tests are executed only when run with Web Server
	if ( !QUnit.isLocal ) {

		asyncTest('can create a replacement handler for $.ajax that uses the default channelManager', function () {

			expect(5);

			var name = 'my-channel',
				type = 'd',

				channeledAjax = Wicket.Ajax.channeledAjax(name, type),

				url = 'data/ajax/evaluationId.xml',
				options = {
					success: function() {
						notEqual(Wicket.channelManager.channels[name], undefined);
						ok(true, 'Success handler is executed');
					},

					complete: function() {
						equal(Wicket.channelManager.channels[name], undefined);
						ok(true, 'Complete handler is executed');
					}
				};

			equal(Wicket.channelManager.channels[name], undefined);

			channeledAjax(url, options);
		});
	}

	function TestChannelManager() {

		this.schedule = function (channel, fn) {
			this.scheduled = {
				channel: channel,
				handler: fn
			};
		};

		this.done = function(channel) {
			this.doneChannel = channel;
		};

	}

	function TestAjaxHandler() {
		var self = this;
		this.ajax = function(url, options) {
			self.calledWith = {
				url: url,
				options: options
			};
		}
	}
});
