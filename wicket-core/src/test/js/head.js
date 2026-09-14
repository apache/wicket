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
 QUnit: true, expect: true */

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	// creates an element with the given tag name and attributes, without depending on jQuery
	var createElement = function (tagName, attributes) {
		var el = document.createElement(tagName);
		for (var name in attributes) {
			if (attributes.hasOwnProperty(name)) {
				el[name] = attributes[name];
			}
		}
		return el;
	};

	module("addElement");

	test("Wicket.Head.addElement - add link element", assert => {

		var css = createElement('link', {
			type: 'text/stylesheet',
			rel: 'stylesheet',
			href: 'data/test.css'
		}),

		initialHeadElementsNumber = document.head.children.length;

		Wicket.Head.addElement(css);

		var newHeadElementsNumber = document.head.children.length;

		assert.equal(newHeadElementsNumber, initialHeadElementsNumber + 1);
	});


	test("Wicket.Head.addElement - add script element", assert => {
		const done = assert.async();
		Wicket.testDone = done;
		assert.expect(1);

		var script = createElement('script', {
			type: 'text/javascript',
			src: 'data/start.js'
		}),

		initialHeadElementsNumber = document.head.children.length;

		Wicket.Head.addElement(script);

		var newHeadElementsNumber = document.head.children.length;

		assert.equal(newHeadElementsNumber, initialHeadElementsNumber + 1);
	});

	test("Wicket.Head.addElement - add style element", assert => {
		assert.expect(1);

		var style = createElement('style', {
			textContent: ' body: {font-family: bold;} '
		}),

		initialHeadElementsNumber = document.head.children.length;

		Wicket.Head.addElement(style);

		var newHeadElementsNumber = document.head.children.length;

		assert.equal(newHeadElementsNumber, initialHeadElementsNumber + 1);
	});

	test('Wicket.Head.createElement', assert => {
		var span = Wicket.Head.createElement('span');

		assert.equal(span.nodeType, 1, 'Wicket.Head.createElement should create a DOM element');
		assert.equal(span.tagName.toLowerCase(), 'span', 'Wicket.Head.createElement should create a DOM element');
	});

	test('Wicket.Head.containsElement - unknown attribute', assert => {
		var scriptElement = Wicket.Head.createElement('script');
		assert.equal(false, Wicket.Head.containsElement(scriptElement, 'unknown').contains, 'There shouldn\'t be an element with such attribute name');
	});

	test('Wicket.Head.containsElement - check existence of data/test.js with jsessionid in the url', assert => {
		var
			script1 = createElement('script', {
				type: 'text/javascript',
				src: 'data/test.js;jsessionid=1'
			}),
			script2 = createElement('script', {
				type: 'text/javascript',
				src: 'data/test.js;jsessionid=2' // different jsessionid
			});

		// add just jsessionid=1
		Wicket.Head.addElement(script1);

		assert.ok(Wicket.Head.containsElement(script1, 'src').contains, 'The jsessionid part of the URL must be ignored.');
		assert.equal(false, Wicket.Head.containsElement(script2, 'src').contains, 'The jsessionid part of the URL must be ignored.');
	});

	test('Wicket.Head.containsElement - check replacement of SCRIPT elements with same id', assert => {
		var
			script1 = createElement('script', {
				type: 'text/javascript',
				src: 'data/one.js',
				id: 'testId'
			}),
			script2 = createElement('script', {
				type: 'text/javascript',
				src: 'data/two.js',
				id: 'testId'
			}),
			context = {
				steps: []
			};

		Wicket.Head.addElement(script1);
		assert.ok(Wicket.Head.containsElement(script1, 'src').contains, 'script1 should be in the DOM.');

		Wicket.Head.Contributor.processScript(context, script2);
		assert.ok(Wicket.Head.containsElement(script1, 'src').contains, 'script1 should be in the DOM - 2.');

		// poor man's FunctionExecuter
		context.steps.forEach(function(step) {
			step(function() {});
		});

		assert.ok(Wicket.Head.containsElement(script2, 'src').contains, 'script2 should be in the DOM.');
		assert.equal(Wicket.Head.containsElement(script1, 'src').contains, false,
				'script1 should have been removed from the DOM because a new element with the same id and' +
				'different "src" has been added');
	});

	test('Wicket.Head.containsElement - check replacement of <link> elements with same id', assert => {
		var
			css1 = createElement('link', {
				type: 'text/css',
				href: 'data/one.css',
				id: 'testId'
			}),
			css2 = createElement('link', {
				type: 'text/css',
				href: 'data/two.css',
				id: 'testId'
			}),
			context = {
				steps: []
			};

			Wicket.Head.addElement(css1);
			var containsCss1 = Wicket.Head.containsElement(css1, 'href');
			assert.ok(containsCss1.contains, 'css1 should be in the DOM.');

			Wicket.Head.Contributor.processLink(context, css2);
			var containsCss2 = Wicket.Head.containsElement(css1, 'href');
			assert.ok(containsCss2.contains, 'css1 should be still in the DOM');

			// poor man's FunctionExecuter
			context.steps.forEach(function(step) {
				step(function() {});
			});

			assert.ok(Wicket.Head.containsElement(css2, 'href').contains, 'css2 should be in the DOM.');
			var containsCss3 = Wicket.Head.containsElement(css1, 'href');
			assert.equal(containsCss3.contains, false,
					'css1 should have been removed from the DOM because a new element with the same id and' +
					'different "href" has been added');
		});

	test('Wicket.Head.Contributor.parse - parse head element with three script elements inside', assert => {

		var xmlDocument = Wicket.Xml.parse('<header-contribution><![CDATA[<head><script type="text/javascript" src="data/test.js"></script><script type="text/javascript" id="wicket-ajax-debug-enable">/*<![CDATA[*/wicketAjaxDebugEnable=true;/*]]]]><![CDATA[>*/</script><script type="text/javascript" id="wicket-ajax-base-url">/*<![CDATA[*/Wicket.Ajax.baseUrl="clock";/*]]]]><![CDATA[>*/</script></head>]]></header-contribution>');
		var xmlRootElement = xmlDocument.documentElement;
		var xmlElement   = Wicket.Head.Contributor.parse(xmlRootElement);
		var isXml = xmlElement.documentElement != null && xmlElement.contentType !== 'text/html';

		assert.ok(isXml, 'The result must be XML document');
		assert.equal(xmlElement.documentElement.childNodes.length, 3, "There must be 3 children nodes.");
		var baseUrlElement = xmlElement.documentElement.childNodes.item(2);
		var baseUrlText = baseUrlElement.text || baseUrlElement.textContent;
		assert.equal(baseUrlText, '/**/Wicket.Ajax.baseUrl=\"clock\";/**/', "Wicket.Ajax.baseUrl must be the third item's content");
	});

	/**
	 * Wicket.Head.Contributor.processXYZ method will be tested in ajax.js
	 */
});
