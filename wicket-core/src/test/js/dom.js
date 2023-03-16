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

jQuery(document).ready(function() {
	"use strict";

	const { module, test } = QUnit;

	var existingId = 'testElement',
		existingBlockId = 'testBlockElement',
		nonExistingId = 'nonExistingElement',
		iframeId = 'testInDocIFrame',
		complexElementId = 'complexElement',
		toBeReplacedByTableId = 'toBeReplacedByTable',
		toBeReplacedByScriptId = 'toBeReplacedByScript',
		toBeReplacedByDivWithChildrenId = 'toBeReplacedByDivWithChildren';

	module("Wicket.DOM");

	test("Wicket.$ existing", assert => {
		var el = Wicket.$(existingId);
		assert.equal( el.tagName.toLowerCase(), 'span', "Wicket.$ cannot find an existing element" );
	});

	test("Wicket.$ non-existing", assert => {
		var el = Wicket.$(nonExistingId);
		assert.equal( el, null, "Wicket.$ found an not existing element!" );
	});

	test("Wicket.$ is an alias to Wicket.DOM.get", assert => {
		var el = Wicket.DOM.get(existingId);
		assert.equal( el, Wicket.$(existingId), "Wicket.$ is an alias of Wicket.DOM.get" );
	});

	test("Wicket.$$ looks for existing element in the current document", assert => {
		var el = Wicket.$(existingId);
		assert.equal( Wicket.$$(el), true, "Wicket.$$ says that 'testGet' element is not in the current document." );
	});

	test("Wicket.$$ looks for non existing element", assert => {
		assert.equal( Wicket.$$(nonExistingId), false, "Wicket.$$ should return 'false' for non existing elements." );
	});

	test("Wicket.$$ looks for 'window'", assert => {
		assert.equal( Wicket.$$(window), true, "Wicket.$$ should return 'true' for 'window'." );
	});

	test("Wicket.$$ looks for element in iframe", assert => {
		var iframeEl = Wicket.$(iframeId);
		var iframeDocument = (iframeEl.contentWindow || iframeEl.contentDocument);
		if (iframeDocument.document) {
			iframeDocument = iframeDocument.document;
		}
		var el = iframeDocument.createElement('span');
		assert.equal( Wicket.$$(el), false, "Wicket.$$ should return false for elements created in different documents." );
	});

	test("containsElement looks for an existing element", assert => {
		var el = jQuery('#'+existingId)[0];
		assert.equal( Wicket.DOM.containsElement(el), true, "Wicket.DOM.containsElement should return true for existing elements." );
	});

	test("containsElement looks for an non-existing element", assert => {
		var el = document.createElement('span');
		assert.equal( Wicket.DOM.containsElement(el), false, "Wicket.DOM.containsElement should return true for existing elements." );
	});

	test("serializeNode a simple element", assert => {
		var el = Wicket.$(existingId);
		var asString = Wicket.DOM.serializeNode(el);
		var $deserialized = jQuery(asString);
		assert.equal($deserialized[0].tagName.toLowerCase() , 'span', "Wicket.DOM.serializeNode should return <span>." );
		assert.equal($deserialized.prop('id') , existingId, "<span>'s must be "+existingId+"." );
	});

	test("serializeNode(Children) a complex element", assert => {
		var el = Wicket.$(complexElementId);
		var asString = Wicket.DOM.serializeNode(el);
		var $deserialized = jQuery(asString);
		assert.equal($deserialized[0].tagName.toLowerCase(), 'div', 'The serialized element name should be <div>');
		assert.equal($deserialized.prop('id'), complexElementId, 'The serialized element id should be ' + complexElementId);
		assert.equal($deserialized.children()[0].tagName.toLowerCase(), 'a', 'The serialized element should have one child <a>');
		assert.equal(jQuery.trim($deserialized.text()), 'Link', 'The serialized element should have text "Link"');
	});

	test("show() an element", assert => {
		var el = Wicket.$(existingId);
		Wicket.DOM.hide(el);
		Wicket.DOM.show(el, '');
		assert.equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
	});

	test("show() an element by id ", assert => {
		Wicket.DOM.hide(existingId);
		Wicket.DOM.show(existingId, '');
		var el = Wicket.$(existingId);
		assert.equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
	});

	test("(show|hide)Incrementally() an element", assert => {
		var el = Wicket.$(existingId);
		Wicket.DOM.hideIncrementally(el);
		assert.equal( el.style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(el);
		assert.equal( el.style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(el);
		assert.equal( el.style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(el);
		assert.equal( el.style.display, 'inline', "Wicket.DOM.show should set .style.display to 'inline'." );
	});

	test("(show|hide)Incrementally() an element by id ", assert => {
		Wicket.DOM.hideIncrementally(existingId);
		assert.equal( Wicket.$(existingId).style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(existingId);
		assert.equal( Wicket.$(existingId).style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(existingId);
		assert.equal( Wicket.$(existingId).style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(existingId);
		assert.equal(Wicket.$(existingId).style.display, 'inline', "Wicket.DOM.show should set .style.display to 'inline'.");
	});

	test("(show|hide)Incrementally() a block element by id ", assert => {
		var elId = existingBlockId;
		Wicket.DOM.hideIncrementally(elId);
		assert.equal( Wicket.$(elId).style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(elId);
		assert.equal( Wicket.$(elId).style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(elId);
		assert.equal( Wicket.$(elId).style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(elId);
		assert.equal(Wicket.$(elId).style.display, 'block', "Wicket.DOM.show should set .style.display to 'block'.");
	});

	test("hide() an element", assert => {
		var el = Wicket.$(existingId);
		Wicket.DOM.show(el);
		Wicket.DOM.hide(el);
		assert.equal( el.style.display, 'none', "Wicket.DOM.node should set .style.display to 'none'." );
	});

	test("hide() an element by id ", assert => {
		Wicket.DOM.show(existingId);
		Wicket.DOM.hide(existingId);
		var el = Wicket.$(existingId);
		assert.equal( el.style.display, 'none', "Wicket.DOM.hide should set .style.display to 'none'." );
	});

	test("replace() an element with a table", assert => {
		var el = Wicket.$(toBeReplacedByTableId);
		var tableMarkup = '<table id="'+toBeReplacedByTableId+'"><thead><tr><th>header</th></tr></thead><tbody><tr><td>data</td></tr></tbody><tfoot><tr><td>footer data</td></tr></tfoot></table>';
		Wicket.DOM.replace(el, tableMarkup);
		assert.equal( Wicket.DOM.serializeNode(Wicket.$(toBeReplacedByTableId)).toLowerCase(), tableMarkup.toLowerCase(), "Wicket.DOM.replace replace the span with a table." );
	});

	test("replace() an element with a script", assert => {
		var el = Wicket.$(toBeReplacedByScriptId);
		var counter = 0;
		Wicket.setCounter = function (newValue) { counter = newValue; };
		var scriptBody = 'Wicket.setCounter(1);';
		var scriptMarkup = '<scr'+'ipt id="'+toBeReplacedByScriptId+'" type="text/javascript">'+scriptBody+'</script>';

		Wicket.DOM.replace(el, scriptMarkup);

		assert.equal(counter, 1, "Replacing with script should execute it." );
	});


	test("replace() an element with a complex element", assert => {
		var el = Wicket.$(toBeReplacedByDivWithChildrenId);
		var complexElMarkup = '<div id="'+toBeReplacedByDivWithChildrenId+'"><div>inner div<span>inner span</span><a href="http://host/some/url">Link</a></div></div>';
		Wicket.DOM.replace(el, complexElMarkup);
		assert.equal( Wicket.DOM.serializeNode(Wicket.$(toBeReplacedByDivWithChildrenId)).toLowerCase(), complexElMarkup.toLowerCase(), "Wicket.DOM.replace should replace the span with a complex element." );
	});

	test("replace - test event notifications", assert => {

		Wicket.Event.subscribe('/dom/node/removing', function(jqEvent, elementToBeRemoved) {
			assert.equal(elementToBeRemoved.id, "testDomEventNotifications", "The removed element id match!");
		});

		Wicket.Event.subscribe('/dom/node/added', function(jqEvent, addedElement) {
			assert.equal(jQuery(addedElement).text(), "New One", "The added element text match!");
		});

		var toReplace = Wicket.$('testDomEventNotifications');
		var newElementMarkup = '<div id="testDomEventNotifications">New One</div>';
		Wicket.DOM.replace(toReplace, newElementMarkup);
		jQuery(document).off();
	});

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4650
	 */
	test("replace - do not publish '/dom/node/added' event notification when removing a component", assert => {

		assert.expect(1);

		Wicket.Event.subscribe('/dom/node/removing', function(jqEvent, elementToBeRemoved) {
			assert.equal(elementToBeRemoved.id, "testDomEventNotifications", "The removed element id match!");
		});

		Wicket.Event.subscribe('/dom/node/added', function(jqEvent, addedElement) {
			ok(false, "Event '/dom/node/added' should not be published when the new markup of the component is empty text!");
		});

		var toReplace = Wicket.$('testDomEventNotifications');
		var newElementMarkup = '';
		Wicket.DOM.replace(toReplace, newElementMarkup);
		jQuery(document).off();
	});

	test("text - read text from a node with single text type child", assert => {

		var node = jQuery("<div></div>")[0];
		var doc = node.ownerDocument;
		var textNode = doc.createTextNode("some text");
		node.appendChild(textNode);

		var text = Wicket.DOM.text(node);
		assert.equal(text, "some text", "Single text child text");
	});

	test("text - read text from a node with several text type children", assert => {

		var document = Wicket.Xml.parse("<root><![CDATA[text1]]>|<![CDATA[text2]]>|<![CDATA[text3]]></root>");
		var node = document.documentElement;

		var text = Wicket.DOM.text(node);
		assert.equal(text, "text1|text2|text3", "Several text children");
	});

	test("text - read text from a node with several children (text and elements)", assert => {

		var document = Wicket.Xml.parse("<root><![CDATA[text1|]]><child1>child1text|<![CDATA[text2|]]></child1><![CDATA[text3|]]><child2>child2Test</child2></root>");
		var node = document.documentElement;

		var text = Wicket.DOM.text(node);
		assert.equal(text, "text1|child1text|text2|text3|child2Test", "Several text and element children");
	});
});
