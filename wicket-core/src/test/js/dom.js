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

	var existingId = 'testElement',
		existingBlockId = 'testBlockElement',
		nonExistingId = 'nonExistingElement',
		iframeId = 'testInDocIFrame',
		complexElementId = 'complexElement',
		toBeReplacedByTableId = 'toBeReplacedByTable',
		toBeReplacedByScriptId = 'toBeReplacedByScript',
		toBeReplacedByDivWithChildrenId = 'toBeReplacedByDivWithChildren';

	module("Wicket.DOM");

	test("Wicket.$ existing", function() {
		var el = Wicket.$(existingId);
		equal( el.tagName.toLowerCase(), 'span', "Wicket.$ cannot find an existing element" );
	});

	test("Wicket.$ non-existing", function() {
		var el = Wicket.$(nonExistingId);
		equal( el, null, "Wicket.$ found an not existing element!" );
	});

	test("Wicket.$ is an alias to Wicket.DOM.get", function() {
		var el = Wicket.DOM.get(existingId);
		equal( el, Wicket.$(existingId), "Wicket.$ is an alias of Wicket.DOM.get" );
	});

	test("Wicket.$$ looks for existing element in the current document", function() {
		var el = Wicket.$(existingId);
		equal( Wicket.$$(el), true, "Wicket.$$ says that 'testGet' element is not in the current document." );
	});

	test("Wicket.$$ looks for non existing element", function() {
		equal( Wicket.$$(nonExistingId), false, "Wicket.$$ should return 'false' for non existing elements." );
	});

	test("Wicket.$$ looks for 'window'", function() {
		equal( Wicket.$$(window), true, "Wicket.$$ should return 'true' for 'window'." );
	});

	test("Wicket.$$ looks for element in iframe", function() {
		var iframeEl = Wicket.$(iframeId);
		var iframeDocument = (iframeEl.contentWindow || iframeEl.contentDocument);
		if (iframeDocument.document) {
			iframeDocument = iframeDocument.document;
		}
		var el = iframeDocument.createElement('span');
		equal( Wicket.$$(el), false, "Wicket.$$ should return false for elements created in different documents." );
	});

	test("containsElement looks for an existing element", function() {
		var el = jQuery('#'+existingId)[0];
		equal( Wicket.DOM.containsElement(el), true, "Wicket.DOM.containsElement should return true for existing elements." );
	});

	test("containsElement looks for an non-existing element", function() {
		var el = document.createElement('span');
		equal( Wicket.DOM.containsElement(el), false, "Wicket.DOM.containsElement should return true for existing elements." );
	});

	test("serializeNode a simple element", function() {
		var el = Wicket.$(existingId);
		var asString = Wicket.DOM.serializeNode(el);
		var $deserialized = jQuery(asString);
		equal($deserialized[0].tagName.toLowerCase() , 'span', "Wicket.DOM.serializeNode should return <span>." );
		equal($deserialized.prop('id') , existingId, "<span>'s must be "+existingId+"." );
	});

	test("serializeNode(Children) a complex element", function() {
		var el = Wicket.$(complexElementId);
		var asString = Wicket.DOM.serializeNode(el);
		var $deserialized = jQuery(asString);
		equal($deserialized[0].tagName.toLowerCase(), 'div', 'The serialized element name should be <div>');
		equal($deserialized.prop('id'), complexElementId, 'The serialized element id should be ' + complexElementId);
		equal($deserialized.children()[0].tagName.toLowerCase(), 'a', 'The serialized element should have one child <a>');
		equal(jQuery.trim($deserialized.text()), 'Link', 'The serialized element should have text "Link"');
	});

	test("show() an element", function() {
		var el = Wicket.$(existingId);
		Wicket.DOM.hide(el);
		Wicket.DOM.show(el, '');
		equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
	});

	test("show() an element by id ", function() {
		Wicket.DOM.hide(existingId);
		Wicket.DOM.show(existingId, '');
		var el = Wicket.$(existingId);
		equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
	});

	test("toggleClass() - single CSS class", function() {
		var cssClass = 'testCssClass';
		var element = jQuery('#' + existingId);
		equal(false, element.hasClass(cssClass), "The element doesn't have the CSS class");
		Wicket.DOM.toggleClass(existingId, cssClass);
		equal(true, element.hasClass(cssClass), "The element does have the CSS class");
		Wicket.DOM.toggleClass(existingId, cssClass);
		equal(false, element.hasClass(cssClass), "The element doesn't have the CSS class");
	});

	test("toggleClass() - multiple CSS classes", function() {
		var cssClass1 = 'testCssClass1';
		var cssClass2 = 'testCssClass2';
		var cssClass = cssClass1 + ' ' + cssClass2;
		var element = jQuery('#' + existingId);
		equal(false, element.hasClass(cssClass1), "The element doesn't have the CSS class");
		Wicket.DOM.toggleClass(existingId, cssClass);
		equal(true, element.hasClass(cssClass1), "The element does have the CSS class");
		Wicket.DOM.toggleClass(existingId, cssClass);
		equal(false, element.hasClass(cssClass1), "The element doesn't have the CSS class");
	});

	test("toggleClass() - switch argument", function() {
		var cssClass = 'testCssClass';
		var element = jQuery('#' + existingId);
		equal(false, element.hasClass(cssClass), "The element doesn't have the CSS class");
		Wicket.DOM.toggleClass(existingId, cssClass, true);
		equal(true, element.hasClass(cssClass), "The element does have the CSS class");
		Wicket.DOM.toggleClass(existingId, cssClass, false);
		equal(false, element.hasClass(cssClass), "The element doesn't have the CSS class");
	});

	test("(show|hide)Incrementally() an element", function() {
		var el = Wicket.$(existingId);
		Wicket.DOM.hideIncrementally(el);
		equal( el.style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(el);
		equal( el.style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(el);
		equal( el.style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(el);
		equal( el.style.display, 'inline', "Wicket.DOM.show should set .style.display to 'inline'." );
	});

	test("(show|hide)Incrementally() an element by id ", function() {
		Wicket.DOM.hideIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'inline', "Wicket.DOM.show should set .style.display to 'inline'." );
	});

	test("(show|hide)Incrementally() a block element by id ", function() {
		var elId = existingBlockId;
		Wicket.DOM.hideIncrementally(elId);
		equal( Wicket.$(elId).style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(elId);
		equal( Wicket.$(elId).style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(elId);
		equal( Wicket.$(elId).style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(elId);
		equal( Wicket.$(elId).style.display, 'block', "Wicket.DOM.show should set .style.display to 'block'." );
	});

	test("hide() an element", function() {
		var el = Wicket.$(existingId);
		Wicket.DOM.show(el);
		Wicket.DOM.hide(el);
		equal( el.style.display, 'none', "Wicket.DOM.node should set .style.display to 'none'." );
	});

	test("hide() an element by id ", function() {
		Wicket.DOM.show(existingId);
		Wicket.DOM.hide(existingId);
		var el = Wicket.$(existingId);
		equal( el.style.display, 'none', "Wicket.DOM.hide should set .style.display to 'none'." );
	});

	test("replace() an element with a table", function() {
		var el = Wicket.$(toBeReplacedByTableId);
		var tableMarkup = '<table id="'+toBeReplacedByTableId+'"><thead><tr><th>header</th></tr></thead><tbody><tr><td>data</td></tr></tbody><tfoot><tr><td>footer data</td></tr></tfoot></table>';
		Wicket.DOM.replace(el, tableMarkup);
		equal( Wicket.DOM.serializeNode(Wicket.$(toBeReplacedByTableId)).toLowerCase(), tableMarkup.toLowerCase(), "Wicket.DOM.replace replace the span with a table." );
	});

	test("replace() an element with a script", function() {
		var el = Wicket.$(toBeReplacedByScriptId);
		var counter = 0;
		Wicket.setCounter = function (newValue) { counter = newValue; };
		var scriptBody = 'Wicket.setCounter(1);';
		var scriptMarkup = '<scr'+'ipt id="'+toBeReplacedByScriptId+'" type="text/javascript">'+scriptBody+'</script>';

		Wicket.DOM.replace(el, scriptMarkup);

		equal(counter, 1, "Replacing with script should execute it." );
	});


	test("replace() an element with a complex element", function() {
		var el = Wicket.$(toBeReplacedByDivWithChildrenId);
		var complexElMarkup = '<div id="'+toBeReplacedByDivWithChildrenId+'"><div>inner div<span>inner span</span><a href="http://host/some/url">Link</a></div></div>';
		Wicket.DOM.replace(el, complexElMarkup);
		equal( Wicket.DOM.serializeNode(Wicket.$(toBeReplacedByDivWithChildrenId)).toLowerCase(), complexElMarkup.toLowerCase(), "Wicket.DOM.replace should replace the span with a complex element." );
	});

	test("replace - test event notifications", function() {

		Wicket.Event.subscribe('/dom/node/removing', function(jqEvent, elementToBeRemoved) {
			equal(elementToBeRemoved.id, "testDomEventNotifications", "The removed element id match!");
		});

		Wicket.Event.subscribe('/dom/node/added', function(jqEvent, addedElement) {
			equal(jQuery(addedElement).text(), "New One", "The added element text match!");
		});

		var toReplace = Wicket.$('testDomEventNotifications');
		var newElementMarkup = '<div id="testDomEventNotifications">New One</div>';
		Wicket.DOM.replace(toReplace, newElementMarkup);
		jQuery(document).off();
	});

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4650
	 */
	test("replace - do not publish '/dom/node/added' event notification when removing a component", function() {

		expect(1);

		Wicket.Event.subscribe('/dom/node/removing', function(jqEvent, elementToBeRemoved) {
			equal(elementToBeRemoved.id, "testDomEventNotifications", "The removed element id match!");
		});

		Wicket.Event.subscribe('/dom/node/added', function(jqEvent, addedElement) {
			ok(false, "Event '/dom/node/added' should not be published when the new markup of the component is empty text!");
		});

		var toReplace = Wicket.$('testDomEventNotifications');
		var newElementMarkup = '';
		Wicket.DOM.replace(toReplace, newElementMarkup);
		jQuery(document).off();
	});

	test("text - read text from a node with single text type child", function() {

		var node = jQuery("<div></div>")[0];
		var doc = node.ownerDocument;
		var textNode = doc.createTextNode("some text");
		node.appendChild(textNode);

		var text = Wicket.DOM.text(node);
		equal(text, "some text", "Single text child text");
	});

	test("text - read text from a node with several text type children", function() {

		var document = Wicket.Xml.parse("<root><![CDATA[text1]]>|<![CDATA[text2]]>|<![CDATA[text3]]></root>");
		var node = document.documentElement;

		var text = Wicket.DOM.text(node);
		equal(text, "text1|text2|text3", "Several text children");
	});

	test("text - read text from a node with several children (text and elements)", function() {

		var document = Wicket.Xml.parse("<root><![CDATA[text1|]]><child1>child1text|<![CDATA[text2|]]></child1><![CDATA[text3|]]><child2>child2Test</child2></root>");
		var node = document.documentElement;

		var text = Wicket.DOM.text(node);
		equal(text, "text1|child1text|text2|text3|child2Test", "Several text and element children");
	});
});
