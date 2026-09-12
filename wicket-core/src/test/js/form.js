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

	// mimics jQuery.param(params, true) (the 'traditional' serialization of an
	// array of {name, value} pairs) without depending on jQuery
	var toQueryString = function (params) {
		return params.map(function (p) {
			return encodeURIComponent(p.name) + '=' + encodeURIComponent(p.value);
		}).join('&');
	};

	module("encode");

	test("Wicket.Form.encode ", assert => {
		assert.expect(2);

		var textInputValue = document.getElementById('textInputId').value;
		var encodedASCII = Wicket.Form.encode(textInputValue);
		assert.equal( encodedASCII, 'textValue', "Wicket.Form.encode() shouldn't change ASCII text'" );

		var textInputUTFValue = document.getElementById('textInputUTFId').value;
		var encodedUTF = Wicket.Form.encode(textInputUTFValue);
		// the expected value is the encoded version of 'нещо на български' (translation of 'something in Bulgarian')
		assert.equal( encodedUTF, '%D0%BD%D0%B5%D1%89%D0%BE%20%D0%BD%D0%B0%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8', "Wicket.Form.encode() should encode UTF text'" );
	});

	test('Wicket.Form.serializeSelect', assert => {
		assert.expect(2);

		var select = Wicket.$('selectId');
		var serializedSelect = Wicket.Form.serializeSelect(select);
		serializedSelect = toQueryString(serializedSelect);
		assert.equal(serializedSelect, 'select=0', 'Wicket.Form.serializeSelect should be able to serialize non-multiple select!');

		var multipleSelect = Wicket.$('multipleSelectId');
		var serializedMultipleSelect = Wicket.Form.serializeSelect(multipleSelect);
		serializedMultipleSelect = toQueryString(serializedMultipleSelect);
		assert.equal(serializedMultipleSelect, 'multipleSelect=0&multipleSelect=2', 'Wicket.Form.serializeSelect should be able to serialize multiple select!');
	});

	test('Wicket.Form.serializeInput - input element', assert => {
		assert.expect(1);

		var actual = [];
		document.querySelectorAll('#testForm input').forEach(function(el) {
			var serialized = Wicket.Form.serializeInput(el);
			actual = actual.concat(serialized);
		});

		var expected = [
			{ name: "textInput",      value: "textValue"          },
			{ name: "textUTFInput",   value: "нещо на български"  },
			{ name: "checkBoxInput1", value: "cbValue1"           },
			{ name: "checkBoxInput3", value: "cbValue3"           },
			{ name: "radioInput",     value: "radioValue1"        },
			{ name: "emailInput",     value: "m@g.com"            },
			{ name: "urlInput",       value: "http://example.com" },
			{ name: "searchInput",    value: "wicket"             },
			{ name: "rangeInput",     value: "67"                 },
			{ name: "numberInput",    value: "16"                 },
			{ name: "colorInput",     value: "#123456"            }
		];
		assert.deepEqual(actual, expected);
	});


	test('Wicket.Form.serializeInput - textarea element', assert => {
		assert.expect(1);

		var actual = [];
		document.querySelectorAll('#testForm textarea').forEach(function(el) {
			var serialized = Wicket.Form.serializeInput(el);
			actual = actual.concat(serialized);
		});

		var expected = [
			{
				name: "textArea",
				value: "some text"
			}
		];
		assert.deepEqual(actual, expected);
	});

	test("Wicket.Form.serializeElement should not serialize elements in Wicket.Form.excludeFromAjaxSerialization", assert => {
		Wicket.Form.excludeFromAjaxSerialization = {
			textInputUTFId: "true"
		};

		assert.expect(1);

		var actual = [];
		document.querySelectorAll('#testForm input, #testForm textarea, #testForm select').forEach(function(el) {
			var serialized = Wicket.Form.serializeElement(el);
			actual = actual.concat(serialized);
		});

		var expected = [
			{ name: "textInput",      value: "textValue"          },
			{ name: "checkBoxInput1", value: "cbValue1"           },
			{ name: "checkBoxInput3", value: "cbValue3"           },
			{ name: "radioInput",     value: "radioValue1"        },
			{ name: "emailInput",     value: "m@g.com"            },
			{ name: "urlInput",       value: "http://example.com" },
			{ name: "searchInput",    value: "wicket"             },
			{ name: "rangeInput",     value: "67"                 },
			{ name: "numberInput",    value: "16"                 },
			{ name: "colorInput",     value: "#123456"            },
			{ name: "multipleSelect", value: "0"                  },
			{ name: "multipleSelect", value: "2"                  },
			{ name: "select",         value: "0"                  },
			{ name: "textArea",       value: "some text"          }
		];
		assert.deepEqual(actual, expected);

		Wicket.Form.excludeFromAjaxSerialization = null;
	});

	test("Wicket.Form.serializeElement should serialize the HTMLFormElement's which a children of a non-HTMLFormElement", assert => {
		assert.expect(1);

		var actual = Wicket.Form.serializeElement('nonHtmlFormElement', true);

		var expected = [
			{ name: "textInput",      value: "textValue"          },
			{ name: "textUTFInput",      value: "нещо на български"          },
			{ name: "checkBoxInput1", value: "cbValue1"           },
			{ name: "checkBoxInput3", value: "cbValue3"           },
			{ name: "radioInput",     value: "radioValue1"        },
			{ name: "emailInput",     value: "m@g.com"            },
			{ name: "urlInput",       value: "http://example.com" },
			{ name: "searchInput",    value: "wicket"             },
			{ name: "rangeInput",     value: "67"                 },
			{ name: "numberInput",    value: "16"                 },
			{ name: "colorInput",     value: "#123456"            },
			{ name: "multipleSelect", value: "0"                  },
			{ name: "multipleSelect", value: "2"                  },
			{ name: "select",         value: "0"                  },
			{ name: "textArea",       value: "some text"          }
		];
		assert.deepEqual(actual, expected);
	});

	test("Wicket.Form.serializeElement should serialize the HTMLFormElement's which a children of a non-HTMLFormElement", assert => {
		assert.expect(1);

		var actual = Wicket.Form.serializeElement('nonHtmlFormElement', false);

		var expected = [];
		assert.deepEqual(actual, expected);
	});

	test('Wicket.Form.serialize - form element WITHOUT searching for the parent form', assert => {

		var dontTryToFindRootForm = true;

		var queryString = Wicket.Form.serialize(document.getElementById('urlInputId'), dontTryToFindRootForm);

		queryString = toQueryString(queryString);
		assert.equal(queryString, 'urlInput=http%3A%2F%2Fexample.com', 'Wicket.Form.serialize should not serialize the whole form when an element is passed and the parent form should not be searched');
	});

	test('Wicket.Form.serialize - form element WITH searching for the parent form', assert => {

		var dontTryToFindRootForm = false;

		var queryString = Wicket.Form.serialize(document.getElementById('urlInputId'), dontTryToFindRootForm);

		queryString = toQueryString(queryString);
		assert.equal(queryString, 'textInput=textValue&textUTFInput=%D0%BD%D0%B5%D1%89%D0%BE%20%D0%BD%D0%B0%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8&checkBoxInput1=cbValue1&checkBoxInput3=cbValue3&radioInput=radioValue1&emailInput=m%40g.com&urlInput=http%3A%2F%2Fexample.com&searchInput=wicket&rangeInput=67&numberInput=16&colorInput=%23123456&multipleSelect=0&multipleSelect=2&select=0&textArea=some%20text', 'Wicket.Form.serialize should serialize the whole form when an element is passed and the parent form should be searched');
	});


	test('Wicket.Form.serialize - form element WITH searching for the parent form', assert => {

		var dontTryToFindRootForm = true;

		var queryString = Wicket.Form.serialize(document.getElementById('testForm'), dontTryToFindRootForm);

		queryString = toQueryString(queryString);
		assert.equal(queryString, 'textInput=textValue&textUTFInput=%D0%BD%D0%B5%D1%89%D0%BE%20%D0%BD%D0%B0%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8&checkBoxInput1=cbValue1&checkBoxInput3=cbValue3&radioInput=radioValue1&emailInput=m%40g.com&urlInput=http%3A%2F%2Fexample.com&searchInput=wicket&rangeInput=67&numberInput=16&colorInput=%23123456&multipleSelect=0&multipleSelect=2&select=0&textArea=some%20text', 'Wicket.Form.serialize should serialize the whole form when a the form itself is passed');
	});

	test('Wicket.Form.serializeForm - serialize nested form (div element)', assert => {

		assert.expect(1);

		var template = document.createElement('template');
		template.innerHTML =
			"<form>" +
				"<input type='text' name='textInput' value='textInputValue'/>" +
				"<div id='nestedForm'>" +
					"<input type='checkbox' name='checkboxInput' value='checkboxInputValue' checked/>" +
					"<input type='checkbox' name='checkboxInput' value='checkboxInputValue' checked/>" + // second time
					"<input type='radio' name='radioInput' value='radioInputValue' checked/>" +
					"<textarea name='textareaInput'>textareaValue</textarea>" +
					"<select name='selectInput'>" +
						"<option value='selectInputValue1'>Value 1</option>" +
						"<option value='selectInputValue2' selected>Value 2</option>" +
					"</select>" +
				"</div>" +
			"</form>";

		document.getElementById("qunit-fixture").appendChild(template.content);
		var nestedFormDiv = Wicket.$('nestedForm');
		var actual = Wicket.Form.serializeForm(nestedFormDiv);

		var expected = [
			{
				"name": "textInput",
				"value": "textInputValue"
			},
			{
				"name": "checkboxInput",
				"value": "checkboxInputValue"
			},
			{
				"name": "checkboxInput",
				"value": "checkboxInputValue"
			},
			{
				"name": "radioInput",
				"value": "radioInputValue"
			},
			{
				"name": "selectInput",
				"value": "selectInputValue2"
			},
			{
				"name": "textareaInput",
				"value": "textareaValue"
			}
		];
		assert.deepEqual(actual, expected, "Nested form successfully serialized");
	});
});
