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
	Note: these tests run only through Web Server.
	Here is a possible setup for Apache HTTPD:

		 Alias /ajax-tests "/path/to/wicket/wicket-core/src"

		 <Directory "/path/to/wicket/wicket-core/src">

		 Options Indexes
		 AllowOverride None AuthConfig

		 Order allow,deny
		 Allow from all

		 </Directory>

	then run it by opening "http://localhost/ajax-tests/test/js/all.html" in the browser

 */

jQuery(document).ready(function() {

	execute = function (attributes) {
		
		var defaults = {
				fh: [
					function () {
						start();
						ok(false, 'Failure handler should not be executed!');
					}
				],
				ch: '0|s',
				sh: [
					function () {
						ok(true, 'Success handler is executed');
					}
				]
		};
		var attrs = jQuery.extend({}, defaults, attributes);
		var call = new Wicket.Ajax.Call();
		call.ajax(attrs);

	};

	// Ajax tests are executed only when run with Web Server
	if ( !isLocal ) {

		module('Wicket.Ajax.stateChangeCallback');

		asyncTest('Wicket.Ajax - processEvaluation with mock data.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/evaluationId.xml',
				c: 'evaluationId'
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - processEvaluation with mock data (priority-evaluate).', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/priorityEvaluationId.xml',
				c: 'priorityEvaluationId'
			}
			execute(attrs);
		});

		/**
		 * Executes the second part of 'something|functionBody' by passing 'notify' function as parameter
		 */
		asyncTest('Wicket.Ajax - processEvaluation with identifier|code.', function () {

			expect(2);

			var attrs = {
				u: 'data/ajax/evaluationIdentifierAndCodeId.xml',
				c: 'evaluationIdentifierAndCodeId'
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - processComponent, normal case.', function () {

			expect(2);

			equal(jQuery('#componentToReplace').text(), 'old body', 'The component is existing and has the old innerHTML');

			var attrs = {
				u: 'data/ajax/componentId.xml',
				c: 'componentId',
				sh: [
					function() {
						start();
						equal(jQuery('#componentToReplace').text(), 'new body', 'The component must be replaced');
					}
				]
			}
			execute(attrs);
		});


		asyncTest('Wicket.Ajax - processComponent() but the old component doesn\'t exist.', function () {

			expect(2);

			var oldWicketLogError = Wicket.Log.error;

			Wicket.Log.error = function(msg) {
				start();
				equal(msg, 'Wicket.Ajax.Call.processComponent: Component with id [[componentToReplaceDoesNotExist]] was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.');

				// restore the original method
				Wicket.Log.error = oldWicketLogError;
			};

			var attrs = {
				u: 'data/ajax/componentDoesNotExistsId.xml',
				c: 'componentDoesNotExistsId',
				sh: [
					function() {
						start();
						equal(jQuery('#componentToReplaceDoesNotExist').length, 0, 'A component with id \'componentToReplaceDoesNotExist\' must not exist!');
					}
				]
			}
			execute(attrs);
		});

		asyncTest('Wicket.Ajax - processComponent() replace a component with a table with scripts inside.', function () {

			expect(4);

			var attrs = {
				u: 'data/ajax/complexComponentId.xml',
				c: 'complexComponentId',
				sh: [
					function() {
						start();
						equal(jQuery('#componentToReplace')[0].tagName.toLowerCase(), 'table', 'A component with id \'componentToReplace\' must be a table now!');
					}
				]
			}
			execute(attrs);

		});


		asyncTest('Wicket.Ajax - processComponent() replace title\'s text.', function () {

			expect(1);

			var oldTitle = jQuery('title').text();

			var attrs = {
				u: 'data/ajax/componentToReplaceTitle.xml',
				c: 'componentToReplaceTitle',
				sh: [
					function() {
						start();
						var $title = jQuery('title');
						equal($title.text(), 'new title', 'The title text should be updated!');
						$title.text(oldTitle);
					}
				]
			}
			execute(attrs);
		});
	}
});
