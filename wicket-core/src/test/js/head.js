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
 QUnit: true, module: true, expect: true */

jQuery(document).ready(function() {
	"use strict";

	module("addElement");

	test("Wicket.Head.addElement - add link element", function() {

		var css = jQuery('<link>', {
			type: 'text/stylesheet',
			rel: 'stylesheet',
			href: 'data/test.css'
		}),

		initialHeadElementsNumber = jQuery('head').children().length;

		Wicket.Head.addElement(css[0]);

		var newHeadElementsNumber = jQuery('head').children().length;

		equal(newHeadElementsNumber, initialHeadElementsNumber + 1);
	});


	test("Wicket.Head.addElement - add script element", function() {

		var script = jQuery('<script>', {
			type: 'text/javascript',
			src: 'data/test.js'
		}),

		initialHeadElementsNumber = jQuery('head').children().length;

		Wicket.Head.addElement(script[0]);

		var newHeadElementsNumber = jQuery('head').children().length;

		equal(newHeadElementsNumber, initialHeadElementsNumber + 1);
	});

	test("Wicket.Head.addElement - add style element", function() {
		expect(1);

		var $style = jQuery('<style> body: {font-family: bold;} </style>'),

		initialHeadElementsNumber = jQuery('head').children().length;

		Wicket.Head.addElement($style[0]);

		var newHeadElementsNumber = jQuery('head').children().length;

		equal(newHeadElementsNumber, initialHeadElementsNumber + 1);
	});

	module('createElement');

	test('Wicket.Head.createElement', function() {
		var span = Wicket.Head.createElement('span');

		equal(span.nodeType, 1, 'Wicket.Head.createElement should create a DOM element');
		equal(span.tagName.toLowerCase(), 'span', 'Wicket.Head.createElement should create a DOM element');
	});

	module('containsElement');

	test('Wicket.Head.containsElement - unknown attribute', function() {
		var scriptElement = Wicket.Head.createElement('script');
		equal(false, Wicket.Head.containsElement(scriptElement, 'unknown').contains, 'There shouldn\'t be an element with such attribute name');
	});

	test('Wicket.Head.containsElement - check existence of wicket-ajax-debug.js with "src"', function() {
		var scriptElement = Wicket.Head.createElement('script');
		scriptElement.src = "../../main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery-debug.js";
		ok(Wicket.Head.containsElement(scriptElement, 'src').contains, 'There should be an element for wicket-ajax-debug.js');
	});

	test('Wicket.Head.containsElement - check existence of data/test.js with "src_"', function() {
		var $script = jQuery('<script>', {
			type: 'text/javascript',
			src_: 'data/test.js'
		}),
		script = $script[0];
		// add <script src_="..."/>
		Wicket.Head.addElement(script);

		script.src = script.src_;
		// check for existence by 'src' attribute
		ok(Wicket.Head.containsElement(script, 'src').contains, 'There should be an element for wicket-ajax-debug.js');
	});

	test('Wicket.Head.containsElement - check existence of data/test.js with jsessionid in the url', function() {
		var
			script1 = jQuery('<script>', {
				type: 'text/javascript',
				src: 'data/test.js;jsessionid=1'
			})[0],
			script2 = jQuery('<script>', {
				type: 'text/javascript',
				src: 'data/test.js;jsessionid=2' // different jsessionid
			})[0];

		// add just jsessionid=1
		Wicket.Head.addElement(script1);

		ok(Wicket.Head.containsElement(script1, 'src').contains, 'The jsessionid part of the URL must be ignored.');
		equal(false, Wicket.Head.containsElement(script2, 'src').contains, 'The jsessionid part of the URL must be ignored.');
	});

	test('Wicket.Head.containsElement - check replacement of SCRIPT elements with same id', function() {
		var
			script1 = jQuery('<script>', {
				type: 'text/javascript',
				src: 'data/one.js',
				id: 'testId'
			})[0],
			script2 = jQuery('<script>', {
				type: 'text/javascript',
				src: 'data/two.js',
				id: 'testId'
			})[0],
			context = {
				steps: []
			};

		Wicket.Head.addElement(script1);
		ok(Wicket.Head.containsElement(script1, 'src').contains, 'script1 should be in the DOM.');

		Wicket.Head.Contributor.processScript(context, script2);
		ok(Wicket.Head.containsElement(script1, 'src').contains, 'script1 should be in the DOM - 2.');

		// poor man's FunctionExecuter
		jQuery.each(context.steps, function(idx, step) {
			step(function() {});
		});

		ok(Wicket.Head.containsElement(script2, 'src').contains, 'script2 should be in the DOM.');
		equal(Wicket.Head.containsElement(script1, 'src').contains, false,
				'script1 should have been removed from the DOM because a new element with the same id and' +
				'different "src" has been added');
	});

	test('Wicket.Head.containsElement - check replacement of <link> elements with same id', function() {
		var
			css1 = jQuery('<link>', {
				type: 'text/css',
				href: 'data/one.css',
				id: 'testId'
			})[0],
			css2 = jQuery('<link>', {
				type: 'text/css',
				href: 'data/two.css',
				id: 'testId'
			})[0],
			context = {
				steps: []
			};

			Wicket.Head.addElement(css1);
			var containsCss1 = Wicket.Head.containsElement(css1, 'href');
			ok(containsCss1.contains, 'css1 should be in the DOM.');

			Wicket.Head.Contributor.processLink(context, css2);
			var containsCss2 = Wicket.Head.containsElement(css1, 'href');
			ok(containsCss2.contains, 'css1 should be still in the DOM');

			// poor man's FunctionExecuter
			jQuery.each(context.steps, function(idx, step) {
				step(function() {});
			});

			ok(Wicket.Head.containsElement(css2, 'href').contains, 'css2 should be in the DOM.');
			var containsCss3 = Wicket.Head.containsElement(css1, 'href');
			equal(containsCss3.contains, false,
					'css1 should have been removed from the DOM because a new element with the same id and' +
					'different "href" has been added');
		});
	module('addJavascript');
	
	test('Wicket.Head.addJavascript - add script with text content', function() {
		expect(2);
		
		var content = 'ok(true, "Added JavaScript must be executed!")',
			url = 'some/fake.js';
		Wicket.Head.addJavascript(content, 'someId', url);

		var $script = jQuery('<script>', {
			type: 'text/javascript',
			src: url
		}),
		script = $script[0];
		ok(Wicket.Head.containsElement(script, 'src').contains);
	});

	module('addJavascripts');

	test('Wicket.Head.addJavascripts - no script tags', function() {
		var $element = jQuery('<div>DIV TEXT<span>SPAN TEXT<a href="#anchor">ANCHOR</a></span></div>'),
			initialHeadElementsNumber = jQuery('head').children().length;

		Wicket.Head.addJavascripts($element[0]);

		equal(initialHeadElementsNumber, jQuery('head').children().length, 'No script elements in the added element, so nothing is added');
	});

	test('Wicket.Head.addJavascripts - direct script tag', function() {
		expect(2);
		
		var $element = jQuery('<script>ok(true);</script>'),
			initialHeadElementsNumber = jQuery('head').children().length;

		Wicket.Head.addJavascripts($element[0]);

		equal(jQuery('head').children().length, initialHeadElementsNumber + 1, 'A script element must be added');
	});

	test('Wicket.Head.addJavascripts - child with script tags inside', function() {
		
		expect(2);
		
		var $element = jQuery('<div/>'),
			script = document.createElement('script'),
			initialHeadElementsNumber = jQuery('head').children().length;

		script.type = 'text/javascript';
		script.textContent = 'ok(true, "Script text executed");'; // 1
		// cannot use jQuery.append() here - see http://stackoverflow.com/questions/610995/jquery-cant-append-script-element
		$element[0].appendChild(script);
		
		Wicket.Head.addJavascripts($element[0]);

		var newNumber = jQuery('head').children().length;
		equal(newNumber, initialHeadElementsNumber + 1, 'A script element in the added element should be added and executed'); // 2
	});

	module('Contributor.parse');

	test('Wicket.Head.Contributor.parse - parse head element with three script elements inside', function() {
		
		var xmlDocument = Wicket.Xml.parse('<header-contribution><![CDATA[<head><script type="text/javascript" src="data/test.js"></script><script type="text/javascript" id="wicket-ajax-debug-enable">/*<![CDATA[*/wicketAjaxDebugEnable=true;/*]]]]><![CDATA[>*/</script><script type="text/javascript" id="wicket-ajax-base-url">/*<![CDATA[*/Wicket.Ajax.baseUrl="clock";/*]]]]><![CDATA[>*/</script></head>]]></header-contribution>');
		var xmlRootElement = xmlDocument.documentElement;
		var xmlElement   = Wicket.Head.Contributor.parse(xmlRootElement);
		var isXml = jQuery.isXMLDoc(xmlElement);

		ok(isXml, 'The result must be XML document');
		equal(xmlElement.documentElement.childNodes.length, 3, "There must be 3 children nodes.");
		var baseUrlElement = xmlElement.documentElement.childNodes.item(2);
		var baseUrlText = baseUrlElement.text || baseUrlElement.textContent;
		equal(baseUrlText, '/**/Wicket.Ajax.baseUrl=\"clock\";/**/', "Wicket.Ajax.baseUrl must be the third item's content");
	});

	/**
	 * Wicket.Head.Contributor.processXYZ method will be tested in ajax.js
	 */
});
