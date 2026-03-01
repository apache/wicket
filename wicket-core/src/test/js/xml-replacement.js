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

	Or start StartJavaScriptTests.java in project wicket-js-tests.
 */

/*global ok: true, start: true, test: true, equal: true, deepEqual: true,
 QUnit: true, expect: true, console: true  */
jQuery(document).ready(function() {
	"use strict";

	const {module, test} = QUnit;

	var execute = function (attributes, assert, done) {
		const done2 = done || assert.async();
		Wicket.testDone = done2;

		var defaults = {
			fh: [
				function () {
					done2();
					assert.ok(false, 'Failure handler should not be executed!');
				}
			],
			ch: '0|s',
			sh: [
				function () {
					assert.ok(true, 'Success handler is executed');
				}
			]
		};
		var attrs = jQuery.extend({}, defaults, attributes);
		var call = new Wicket.Ajax.Call();
		call.ajax(attrs);

	};

	// Ajax tests are executed only when run with Web Server
	if (!QUnit.isLocal) {

		module('Wicket.Ajax', {
			beforeEach: function () {
				// unsubscribe all global listeners
				Wicket.Event.unsubscribe();
			}
		});

		test('processComponent(), XML, simple case.', assert => {
			const done = assert.async();
			assert.expect(3);

			assert.equal(jQuery('#simpleXml').text(), '1', 'The component is existing and has the old innerHTML');

			var attrs = {
				u: 'data/ajax/componentIdXml.xml',
				c: 'componentIdXml',
				sh: [
					function () {
						done();
						var simpleXml = jQuery('#simpleXml');
						assert.equal(simpleXml.text(), '2', 'The component must be replaced');
						assert.equal(simpleXml[0].namespaceURI, 'http://www.w3.org/1998/Math/MathML',
							'The namespace is set correctly');
					}
				]
			};
			execute(attrs, assert, done);
		});

		test('processComponent(), XML, complex case.', assert => {
			const done = assert.async();
			Wicket.testDone = done;
			Wicket.assert = assert;
			assert.expect(2);

			var attrs = {
				u: 'data/ajax/complexComponentIdXml.xml',
				c: 'complexComponentIdXml',
				sh: [
					function () {
						done();
						var complexXml = jQuery('#complexXml');
						assert.equal(complexXml.html().trim(), '<mrow>\n' +
							'<mn>2</mn>\n' +
							'<mo>⁢</mo>\n' +
							'<mn>3</mn>\n' +
							'</mrow>\n' +
							'<mo>=</mo>\n' +
							'<mn>6</mn>', 'The component must be replaced');
						assert.equal(complexXml[0].namespaceURI, 'http://www.w3.org/1998/Math/MathML',
							'The namespace is set correctly');
					}
				]
			};
			execute(attrs, assert, done);
		});
	}
});
