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

	var existingId = 'testElement',
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
		equal( Wicket.$$(nonExistingId), true, "Wicket.$$ should return true for non existing elements." );
	});

	test("Wicket.$$ looks for element in iframe", function() {
		var iframeEl = Wicket.$(iframeId); 
		var iframeDocument = (iframeEl.contentWindow || iframeEl.contentDocument);
		if (iframeDocument.document) iframeDocument = iframeDocument.document;
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
		Wicket.DOM.show(el);
		equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
	});

	test("show() an element by id ", function() {
		Wicket.DOM.hide(existingId);
		Wicket.DOM.show(existingId);
		var el = Wicket.$(existingId);
		equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
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
		equal( el.style.display, '', "Wicket.DOM.show should set .style.display to ''." );
	});

	test("(show|hide)Incrementally() an element by id ", function() {
		Wicket.DOM.hideIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'none', "Wicket.DOM.hideIncrementally should set .style.display to 'none'." );
		Wicket.DOM.hideIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'none', ".style.display should be 'none'." );
		Wicket.DOM.showIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, 'none', ".style.display should still be 'none'." );
		Wicket.DOM.showIncrementally(existingId);
		equal( Wicket.$(existingId).style.display, '', "Wicket.DOM.show should set .style.display to ''." );
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
			start();
			equal(elementToBeRemoved.id, "testDomEventNotifications", "The removed element id match!");
		});

		Wicket.Event.subscribe('/dom/node/added', function(jqEvent, addedElement) {
			start();
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
			start();
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
});
