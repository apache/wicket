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

/*global QUnit: true, MultiSelector: true */

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	QUnit.config.testTimeout = 3000;

	let logError, errors;

	// builds a node of an Ajax response without a parser, which would need a TrustedHTML itself
	const xmlElement = function (tagName, attributes, text) {
		const xml = document.implementation.createDocument(null, 'head', null);
		const element = xml.createElement(tagName);
		for (const name in attributes) {
			element.setAttribute(name, attributes[name]);
		}
		if (text) {
			element.appendChild(xml.createTextNode(text));
		}
		return element;
	};

	/*
	 * The page requires Trusted Types for every script sink and allows the policy 'wicket' only,
	 * so a string that reaches a sink fails the test with a TypeError.
	 */
	module('Trusted Types', {
		beforeEach: function () {
			errors = [];
			logError = Wicket.Log.error;
			Wicket.Log.error = function () {
				errors.push(Array.prototype.join.call(arguments, ' '));
			};
		},
		afterEach: function (assert) {
			Wicket.Log.error = logError;
			assert.deepEqual(errors, [], 'No error is logged');
		}
	});

	test('the page enforces Trusted Types', assert => {
		assert.throws(function () {
			document.createElement('div').innerHTML = '<b>string</b>';
		}, TypeError);
	});

	test('Wicket.Xml.parse', assert => {
		const xml = Wicket.Xml.parse('<ajax-response><evaluate>1</evaluate></ajax-response>');

		assert.equal(xml.documentElement.tagName, 'ajax-response');
	});

	test('strings are passed on when the page does not allow the policy', assert => {
		const done = assert.async();
		const frame = document.createElement('iframe');
		Wicket.$('qunit-fixture').appendChild(frame);
		const frameWindow = frame.contentWindow;
		// the frame inherits the policy of the page, which allows the name 'wicket' only once
		const taken = frameWindow.trustedTypes.createPolicy('wicket', {
			createScriptURL: function (url) {
				return url;
			}
		});
		const sources = Array.prototype.map.call(document.querySelectorAll('script[src*="jquery"], script[src*="wicket-ajax"]'), function (script) {
			return script.src;
		});

		const flag = frameWindow.document.createElement('meta');
		flag.name = 'wicket-trusted-types';
		frameWindow.document.head.appendChild(flag);

		const load = function () {
			if (sources.length > 0) {
				const script = frameWindow.document.createElement('script');
				script.src = taken.createScriptURL(sources.shift());
				script.onload = load;
				frameWindow.document.head.appendChild(script);
				return;
			}
			const frameErrors = [];
			frameWindow.Wicket.Log.error = function (message) {
				frameErrors.push(message);
			};

			assert.throws(function () {
				frameWindow.Wicket.Xml.parse('<ajax-response/>');
			}, frameWindow.TypeError);
			assert.deepEqual(frameErrors, ["Wicket.TrustedTypes: Cannot create the policy 'wicket'"]);
			done();
		};
		load();
	});

	test('Wicket.DOM.replace', assert => {
		Wicket.DOM.replace(Wicket.$('componentToReplace'), '<span id="componentToReplace">new <b>body</b></span>');
		Wicket.DOM.replace(Wicket.$('rowToReplace'), '<tr id="rowToReplace"><td>new cell</td></tr>');

		assert.equal(Wicket.$('componentToReplace').tagName.toLowerCase(), 'span');
		assert.equal(Wicket.$('componentToReplace').innerHTML, 'new <b>body</b>');
		assert.equal(Wicket.$('rowToReplace').cells[0].textContent, 'new cell');
	});

	test('Wicket.DOM.replace - markup followed by text', assert => {
		Wicket.DOM.replace(Wicket.$('withTextAfter'), '<span id="withTextAfter">new body</span> text');

		assert.equal(Wicket.$('withTextAfter').tagName.toLowerCase(), 'span');
		assert.equal(Wicket.$('withTextAfter').textContent, 'new body');
		assert.equal(Wicket.$('withTextAfter').nextSibling.nodeValue, ' text');
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
			'<script src="data/start.js?markup"></script></div>');

		assert.equal(Wicket.$('withScripts').firstChild.nodeValue, 'new body');
	});

	test('Wicket.DOM.replace - the scripts run in the order of the jQuery-free implementation', assert => {
		const done = assert.async();
		const old = Wicket.$('withScripts');
		Wicket.testSteps = [];
		Wicket.testDone = function () {
			Wicket.testSteps.push('external');
			assert.deepEqual(Wicket.testSteps, ['first: DIV, old element removed: true', 'second: P', 'replaced', 'external']);
			done();
		};
		Wicket.testOld = old;

		Wicket.DOM.replace(old, '<div id="withScripts">' +
			'<script>Wicket.testSteps.push("first: " + Wicket.$("withScripts").tagName + ", old element removed: " + !Wicket.testOld.isConnected);</script>' +
			'<script src="data/start.js?order"></script>' +
			'<p id="afterScripts"><script>Wicket.testSteps.push("second: " + Wicket.$("afterScripts").tagName);</script></p></div>');
		Wicket.testSteps.push('replaced');
	});

	test('Wicket.DOM.add - inline and external scripts in the markup', assert => {
		const done = assert.async();
		Wicket.testSteps = [];
		Wicket.testDone = function () {
			Wicket.testSteps.push('external');
			assert.deepEqual(Wicket.testSteps, ['inline: 1', 'added', 'external']);
			done();
		};

		Wicket.DOM.add(Wicket.$('addToWithScripts'), '<li>one' +
			'<script>Wicket.testSteps.push("inline: " + Wicket.$("addToWithScripts").children.length);</script>' +
			'<script src="data/start.js?add"></script></li>');
		Wicket.testSteps.push('added');
	});

	test.if('Wicket.DOM.replace - jQuery data and event handlers of the removed elements are cleaned up', typeof(jQuery) !== 'undefined', assert => {
		const old = Wicket.$('withJQueryData');
		const child = old.firstChild;
		let handled = 0;
		const handler = function () {
			handled++;
		};
		jQuery(old).on('click', handler).data('key', 'value');
		jQuery(child).on('click', handler).data('key', 'value');
		jQuery(child).trigger('click');
		assert.equal(handled, 2, 'Both handlers run before the replacement');
		assert.ok(jQuery.hasData(old) && jQuery.hasData(child));

		Wicket.DOM.replace(old, '<div id="withJQueryData">new body</div>');

		assert.notOk(old.isConnected, 'The old element is removed');
		assert.notOk(jQuery.hasData(old), 'No jQuery data is left at the old element');
		assert.notOk(jQuery.hasData(child), 'No jQuery data is left at its child');
		assert.strictEqual(jQuery(old).data('key'), undefined);
		jQuery(child).trigger('click');
		assert.equal(handled, 2, 'No handler is left at the old elements');
		assert.equal(Wicket.$('withJQueryData').textContent, 'new body');
	});

	test('Wicket.DOM.add', assert => {
		Wicket.DOM.add(Wicket.$('addTo'), '<li>one</li><li>two</li>');

		assert.equal(Wicket.$('addTo').children.length, 2);
		assert.equal(Wicket.$('addTo').lastChild.textContent, 'two');
	});

	test('Wicket.Head.Contributor.processScript - external script', assert => {
		const done = assert.async();
		assert.expect(2);
		Wicket.testDone = function () {
			assert.ok(true, 'The external script is executed');
		};
		const context = { steps: [] };

		Wicket.Head.Contributor.processScript(context, xmlElement('script', {
			type: 'text/javascript',
			src: 'data/start.js?contribution'
		}));
		context.steps[0](done);
	});

	test('Wicket.Head.Contributor.processScript - inline script', assert => {
		Wicket.assert = assert;
		assert.expect(2);
		const context = { steps: [] };

		Wicket.Head.Contributor.processScript(context, xmlElement('script', {
			type: 'text/javascript'
		}, 'Wicket.assert.ok(true, "The inline script is executed");'));
		context.steps[0](function () {});
	});

	test('Ajax request that replaces a component', assert => {
		const done = assert.async();
		assert.expect(3);

		new Wicket.Ajax.Call().ajax({
			u: 'data/ajax/componentId.xml',
			c: 'componentToReplace',
			sh: [
				function () {
					assert.ok(true, 'Success handler is executed');
				}
			],
			fh: [
				function (attrs, jqXHR, errorMessage) {
					assert.ok(false, 'Failure handler should not be executed: ' + errorMessage);
				}
			],
			coh: [
				function () {
					assert.equal(Wicket.$('componentToReplace').textContent, 'new body');
					done();
				}
			]
		});
	});

	test('Ajax response with header contribution, priority evaluation, component and evaluation', assert => {
		const done = assert.async();
		Wicket.testSteps = [];
		Wicket.testDone = function () {
			Wicket.testSteps.push('external script: ' + document.currentScript.src.split('?')[1]);
			if (document.currentScript.src.split('?')[1] === 'component') {
				assert.deepEqual(Wicket.testSteps, [
					'external script: header-contribution',
					'header-contribution',
					'priority-evaluate: old body',
					'component: SPAN',
					'evaluate: new body',
					'success handler',
					'complete handler',
					'external script: component'
				]);
				assert.ok(document.querySelector('head link[href="data/test.css?trusted-types"]'), 'The stylesheet is added');
				assert.equal(Wicket.$('trusted-types-header-contribution').tagName.toLowerCase(), 'script');
				done();
			}
		};

		Wicket.Ajax.ajax({
			u: 'data/ajax/trustedTypes.xml',
			c: 'componentToReplace',
			sh: [
				function () {
					Wicket.testSteps.push('success handler');
				}
			],
			fh: [
				function (attrs, jqXHR, errorMessage) {
					assert.ok(false, 'Failure handler should not be executed: ' + errorMessage);
				}
			],
			coh: [
				function () {
					Wicket.testSteps.push('complete handler');
				}
			]
		});
	});

	test('Wicket.AutoComplete - choices', assert => {
		const done = assert.async();
		const ajax = Wicket.Ajax.ajax;
		const input = Wicket.$('autoComplete');

		Wicket.Ajax.ajax = function (attrs) {
			Wicket.Ajax.ajax = ajax;
			try {
				attrs.sh[attrs.sh.length - 1](attrs, null, '<ul><li textvalue="Amsterdam">Amsterdam</li><li textvalue="Antwerp">Antwerp</li></ul>', 'success');

				const menu = Wicket.$('autoComplete-autocomplete');
				assert.equal(menu.getElementsByTagName('li').length, 2);
				menu.parentNode.remove();
			} catch (exception) {
				assert.ok(false, exception);
			}
			done();
		};
		new Wicket.AutoComplete({ c: 'autoComplete', u: 'autocomplete' }, { throttleDelay: 1 });

		input.focus();
		input.value = 'A';
		input.dispatchEvent(new Event('input', { bubbles: true }));
	});

	test('Wicket.WUPB.setStatus - status with markup', assert => {
		const progressBar = new Wicket.WUPB(null, 'uploadStatus', null, null, null, null);

		progressBar.setStatus('<b>50%</b> uploaded');

		assert.equal(Wicket.$('uploadStatus').firstChild.tagName.toLowerCase(), 'label');
		assert.equal(Wicket.$('uploadStatus').firstChild.innerHTML, '<b>50%</b> uploaded');

		progressBar.setStatus(null);

		assert.equal(Wicket.$('uploadStatus').firstChild.innerHTML, '');
	});

	test('MultiSelector.addListRow - file names are shown as text', assert => {
		const selector = new MultiSelector('upload', Wicket.$('uploadList'), 2, true, 'Remove');

		selector.addListRow({ files: [ { name: 'C:\\files\\<b>R&D "1".txt' }, { name: "it's.png" } ] });
		selector.addListRow({ value: '/home/files/a&amp;b.txt' });

		const rows = Wicket.$('uploadList').rows;
		assert.equal(rows[0].cells[0].innerHTML, '&lt;b&gt;R&amp;D "1".txt<br>it\'s.png');
		assert.equal(rows[1].cells[0].textContent, 'a&amp;b.txt');
	});
});
