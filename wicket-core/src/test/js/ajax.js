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

jQuery(document).ready(function() {

	/**
	 * Emulates an Ajax response from <script> element's body.
	 * Using script element's body is cross browser compatible
	 */
	var loadData = function (id) {
		var node = Wicket.$(id);
		var text = "", i;

		if (node.hasChildNodes()) {
			for (i = 0; i < node.childNodes.length; i++) {
				text = text + node.childNodes[i].nodeValue;
			}
		}
		return text;
	}

	execute = function (dataElementId, options) {
		
		var defaults = {
				parseResponse: true,
				randomUrl: false,
				failureHandler: function () {
					start();
					ok(false, 'Failure handler should not be called!');
				},
				channel: '0|s',
				successHandler: function () {
					start();
					ok(true, 'Success handler is executed');
				},
				url: 'dummy/url',
				transport: {
					abort: function() {},
					responseText: loadData(dataElementId),  // emulates Ajax response
					responseXML: Wicket.Xml.parse(loadData(dataElementId)),  // emulates Ajax response (IE)
					status: 200,
					readyState: 4
				}
		};
		var opts = jQuery.extend({}, defaults, options);
		var call = new Wicket.Ajax.Call(opts.url, opts.successHandler, opts.failureHandler, opts.channel);
		var request = new Wicket.Ajax.Request(opts.url, call.loadedCallback.bind(call), opts.parseResponse, opts.randomUrl, opts.channel, opts.successHandler);

		request.transport = opts.transport;

		request.stateChangeCallback();
	};

	module('Wicket.Ajax.stateChangeCallback');

	asyncTest('Wicket.Ajax - processEvaluation with mock data.', function () {

		expect(2);

		execute('evaluationId');
	});

	asyncTest('Wicket.Ajax - processEvaluation with mock data (priority-evaluate).', function () {

		expect(2);

		execute('priorityEvaluationId');
	});
	
	asyncTest('Wicket.Ajax - processEvaluation with identifier|code.', function () {

		expect(2);

		execute('evaluationIdentifierAndCodeId');
	});

	asyncTest('Wicket.Ajax - processComponent, normal case.', function () {

		expect(2);

		var options = {
			successHandler: function() {
				start();
				
				equal(jQuery('#componentToReplace').text(), 'new body', 'The component must be replaced');
			}
		};

		equal(jQuery('#componentToReplace').text(), 'old body', 'The component is existing and has the old innerHTML');

		execute('componentId', options);
	});


	asyncTest('Wicket.Ajax - processComponent() but the old component doesn\'t exist.', function () {

		expect(2);

		var oldWicketLogError = Wicket.Log.error;
		
		Wicket.Log.error = function(msg) {
			start();
			equals(msg, 'Wicket.Ajax.Call.processComponent: Component with id [[componentToReplaceDoesNotExist]] was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.');

			// restore the original method
			Wicket.Log.error = oldWicketLogError;
		};

		var options = {
			successHandler: function() {
				equals(jQuery('#componentToReplaceDoesNotExist').length, 0, 'A component with id \'componentToReplaceDoesNotExist\' must not exist!');
			}
		};

		execute('componentDoesNotExistsId', options);
	});

	asyncTest('Wicket.Ajax - processComponent() replace a component with a table with scripts inside.', function () {

		expect(4);

		var options = {
			successHandler: function() {
				start();
				equals(jQuery('#componentToReplace')[0].tagName.toLowerCase(), 'table', 'A component with id \'componentToReplace\' must be a table now!');
			}
		};

		execute('complexComponentId', options);
	});

	
	asyncTest('Wicket.Ajax - processComponent() replace title\'s text.', function () {

		expect(1);

		var oldTitle = jQuery('title').text();

		var options = {
			successHandler: function() {
				start();
				var $title = jQuery('title');
				equal($title.text(), 'new title', 'The title text should be updated!');
				$title.text(oldTitle);
			}
		};

		execute('componentToReplaceTitle', options);
	});
});
