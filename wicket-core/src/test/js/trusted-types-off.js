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

/*global QUnit: true, MultiSelector: true, createPolicyCalls: true */

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	QUnit.config.testTimeout = 3000;

	const withJQuery = typeof(jQuery) !== 'undefined';

	/*
	 * The browser supports Trusted Types, but the page has no <meta name="wicket-trusted-types">,
	 * so Wicket must neither create a policy nor hand anything but strings to its callers.
	 */
	module('Trusted Types - not enabled', {
		afterEach: function (assert) {
			assert.deepEqual(createPolicyCalls, [], 'trustedTypes.createPolicy() is never called');
		}
	});

	test('the browser supports Trusted Types and the page does not require them', assert => {
		assert.equal(typeof(window.trustedTypes.createPolicy), 'function');

		document.createElement('div').innerHTML = '<b>string</b>';
	});

	test('Wicket.TrustedTypes returns its input unchanged', assert => {
		assert.equal(typeof(Wicket.TrustedTypes.html('<b>markup</b>')), 'string');
		assert.equal(typeof(Wicket.TrustedTypes.script('var a = 1;')), 'string');
		assert.equal(typeof(Wicket.TrustedTypes.scriptURL('data/start.js')), 'string');
		assert.strictEqual(Wicket.TrustedTypes.html('<b>markup</b>'), '<b>markup</b>');
		assert.strictEqual(Wicket.TrustedTypes.html(null), null);
		assert.strictEqual(Wicket.TrustedTypes.script('var a = 1;'), 'var a = 1;');
		assert.strictEqual(Wicket.TrustedTypes.scriptURL('data/start.js'), 'data/start.js');
	});

	test('Wicket.Xml.parse', assert => {
		const xml = Wicket.Xml.parse('<ajax-response><evaluate>1</evaluate></ajax-response>');

		assert.equal(xml.documentElement.tagName, 'ajax-response');
	});

	test.if('Wicket.DOM.replace and add - jQuery.htmlPrefilter gets strings', withJQuery, assert => {
		const htmlPrefilter = jQuery.htmlPrefilter;
		const types = [];
		// as jquery-migrate does with its self-closed-tags patch
		jQuery.htmlPrefilter = function (html) {
			types.push(typeof(html));
			return html.replace(/<(?!area|br|col|embed|hr|img|input|link|meta|param)(([a-z][^\/\0>\x20\t\r\n\f]*)[^>]*)\/>/gi, '<$1></$2>');
		};

		try {
			Wicket.DOM.replace(Wicket.$('componentToReplace'), '<span id="componentToReplace"><b/>new body</span>');
			Wicket.DOM.add(Wicket.$('addTo'), '<li>one</li><li>two</li>');
		} finally {
			jQuery.htmlPrefilter = htmlPrefilter;
		}

		assert.ok(types.length >= 2, 'jQuery.htmlPrefilter is called');
		assert.deepEqual(types.filter(function (type) {
			return type !== 'string';
		}), []);
		assert.equal(Wicket.$('componentToReplace').innerHTML, '<b></b>new body');
		assert.equal(Wicket.$('addTo').children.length, 2);
	});

	test('Wicket.DOM.replace - inline and external scripts in the markup', assert => {
		const done = assert.async();
		assert.expect(4);
		Wicket.assert = assert;
		Wicket.testDone = function () {
			assert.ok(true, 'The external script is executed');
			done();
		};

		Wicket.DOM.replace(Wicket.$('withScripts'), '<div id="withScripts">new body' +
			'<script>Wicket.assert.ok(true, "The inline script is executed");</script>' +
			'<script src="data/start.js?markup-off"></script></div>');

		assert.equal(Wicket.$('withScripts').firstChild.nodeValue, 'new body');
	});

	test('Ajax request that replaces a component', assert => {
		const done = assert.async();
		const htmlPrefilter = withJQuery ? jQuery.htmlPrefilter : null;
		if (withJQuery) {
			jQuery.htmlPrefilter = function (html) {
				return html.replace(/<b\/>/g, '<b></b>');
			};
		}

		Wicket.Ajax.ajax({
			u: 'data/ajax/componentId.xml',
			c: 'componentToReplace',
			bsh: [
				function (attrs, jqXHR, settings) {
					if (withJQuery) {
						assert.strictEqual(settings.converters['text xml'], jQuery.parseXML, 'The converter of jQuery parses the response');
					}
				}
			],
			fh: [
				function (attrs, jqXHR, errorMessage) {
					assert.ok(false, 'Failure handler should not be executed: ' + errorMessage);
				}
			],
			coh: [
				function () {
					if (withJQuery) {
						jQuery.htmlPrefilter = htmlPrefilter;
					}
					assert.equal(Wicket.$('componentToReplace').tagName.toLowerCase(), 'span');
					assert.equal(Wicket.$('componentToReplace').textContent, 'new body');
					done();
				}
			]
		});
	});

	test('MultiSelector - getOnlyFileNames() and getOnlyFileName() return escaped markup', assert => {
		const selector = new MultiSelector('upload', Wicket.$('uploadList'), 2, true, 'Remove');
		const input = { files: [ { name: 'C:\\files\\<b>R&D "1".txt' }, { name: "it's.png" } ] };

		assert.strictEqual(selector.getOnlyFileNames(input), '&lt;b&gt;R&amp;D &quot;1&quot;.txt<br>it&#39;s.png');
		assert.strictEqual(selector.getOnlyFileNames({ value: '/home/files/a&b.txt' }), 'a&amp;b.txt');
		assert.strictEqual(selector.getOnlyFileName('C:\\files\\<b>.txt'), '&lt;b&gt;.txt');
		assert.deepEqual(selector.getOnlyFileNamesAsArray(input), ['<b>R&D "1".txt', "it's.png"]);
		assert.deepEqual(selector.getOnlyFileNamesAsArray({ value: '/home/files/a&b.txt' }), ['a&b.txt']);
	});

	test('MultiSelector.addListRow - file names are shown as text', assert => {
		const selector = new MultiSelector('upload', Wicket.$('uploadList'), 2, true, 'Remove');

		selector.addListRow({ files: [ { name: 'C:\\files\\<b>R&D "1".txt' }, { name: "it's.png" } ] });

		assert.equal(Wicket.$('uploadList').rows[0].cells[0].innerHTML, '&lt;b&gt;R&amp;D "1".txt<br>it\'s.png');
	});
});
