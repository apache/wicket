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
		equal(false, Wicket.Head.containsElement(scriptElement, 'unknown'), 'There shouldn\'t be an element with such attribute name');
	});

	test('Wicket.Head.containsElement - check existence of wicket-ajax-debug.js with "src"', function() {
		var scriptElement = Wicket.Head.createElement('script');
		scriptElement.src = "../../main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery-debug.js";
		ok(Wicket.Head.containsElement(scriptElement, 'src'), 'There should be an element for wicket-ajax-debug.js');
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
		ok(Wicket.Head.containsElement(script, 'src'), 'There should be an element for wicket-ajax-debug.js');
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

		equal(true, Wicket.Head.containsElement(script1, 'src'), 'The jsessionid part of the URL must be ignored.');
		equal(false, Wicket.Head.containsElement(script2, 'src'), 'The jsessionid part of the URL must be ignored.');
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
		ok(Wicket.Head.containsElement(script, 'src'));
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


	module("Contributor.decode");

	test('Wicket.Head.Contributor.decode - remove trailing ^ from closing CDATA', function() {
		var expected = '<![CDATA[some data]]>',
			input = '<![CDATA[some data]]^>',
			encoding = 'wicket1',
			actual = Wicket.Head.Contributor.decode(encoding, input);
			
		equal(actual, expected);
	});

	test('Wicket.Head.Contributor.decode - no decoding because of wrong encoding', function() {
		var expected = '<![CDATA[some data]]^>',
			encoding = 'somethingWrong',
			actual = Wicket.Head.Contributor.decode(encoding, expected);
			
		equal(actual, expected);
	});

	module('Contributor.parse');

	test('Wicket.Head.Contributor.parse - parse head element with three script elements inside', function() {
		
		var xmlDocument = Wicket.Xml.parse('<header-contribution encoding="wicket1"><![CDATA[<head><script type="text/javascript" src="data/test.js"></script><script type="text/javascript" id="wicket-ajax-debug-enable">/*<![CDATA[*/wicketAjaxDebugEnable=true;/*]^]^>*/</script><script type="text/javascript" id="wicket-ajax-base-url">/*<![CDATA[*/Wicket.Ajax.baseUrl="clock";/*]^]^>*/</script></head>]]></header-contribution>');
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
