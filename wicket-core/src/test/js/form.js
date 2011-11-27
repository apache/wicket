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

	var existingId = 'testElement';

	module("encode");

	test("Wicket.Form.encode ", function() {
		expect(2);

		var textInputValue = jQuery('#textInputId').val();
		var encodedASCII = Wicket.Form.encode(textInputValue);
		equal( encodedASCII, 'textValue', "Wicket.Form.encode() shouldn't change ASCII text'" );

		var textInputUTFValue = jQuery('#textInputUTFId').val();
		var encodedUTF = Wicket.Form.encode(textInputUTFValue);
		// the expected value is the encoded version of 'нещо на български' (translation of 'something in Bulgarian')
		equal( encodedUTF, '%D0%BD%D0%B5%D1%89%D0%BE%20%D0%BD%D0%B0%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8', "Wicket.Form.encode() should encode UTF text'" );
	});

	module('serializeSelect');

	test('Wicket.Form.serializeSelect', function() {
		expect(2);

		var select = Wicket.$('selectId');
		var serializedSelect = Wicket.Form.serializeSelect(select);
		equal(serializedSelect, 'select=0&', 'Wicket.Form.serializeSelect should be able to serialize non-multiple select!');

		var multipleSelect = Wicket.$('multipleSelectId');
		var serializedMultipleSelect = Wicket.Form.serializeSelect(multipleSelect);
		equal(serializedMultipleSelect, 'multipleSelect=0&multipleSelect=2&', 'Wicket.Form.serializeSelect should be able to serialize multiple select!');
	});

	module('serializeInput');

	test('Wicket.Form.serializeInput - input element', function() {
		expect(1);

		var queryString = '';
		jQuery('#testForm input').each(function() {
			queryString += Wicket.Form.serializeInput(this);
		});

		equal(queryString, 'textInput=textValue&textUTFInput=%D0%BD%D0%B5%D1%89%D0%BE+%D0%BD%D0%B0+%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8&checkBoxInput1=cbValue1&checkBoxInput3=cbValue3&radioInput=radioValue1&emailInput=m%40g.com&urlInput=http%3A%2F%2Fexample.com&searchInput=wicket&rangeInput=67&numberInput=16&colorInput=123456&');
	});


	test('Wicket.Form.serializeInput - textarea element', function() {
		expect(1);

		var queryString = '';
		jQuery('#testForm textarea').each(function() {
			queryString += Wicket.Form.serializeInput(this);
		});

		equal(queryString, 'textArea=some+text&');
	});

	module('Wicket.Form.serializeElement');

	test("Wicket.Form.serializeElement should not serialize elements in Wicket.Form.excludeFromAjaxSerialization", function() {
		Wicket.Form.excludeFromAjaxSerialization = {
			textInputUTFId: "true"
		};

		expect(1);

		var queryString = '';
		jQuery('input, textarea, select', jQuery('#testForm')).each(function() {
			queryString += Wicket.Form.serializeElement(this);
		});

		equal(queryString, 'textInput=textValue&checkBoxInput1=cbValue1&checkBoxInput3=cbValue3&radioInput=radioValue1&emailInput=m%40g.com&urlInput=http%3A%2F%2Fexample.com&searchInput=wicket&rangeInput=67&numberInput=16&colorInput=123456&multipleSelect=0&multipleSelect=2&select=0&textArea=some+text&');

		Wicket.Form.excludeFromAjaxSerialization = null;
	});

	module('Wicket.Form.serialize');

	test('Wicket.Form.serialize - form element WITHOUT searching for the parent form', function() {

		var dontTryToFindRootForm = true,
			queryString = '';

		jQuery('#urlInputId').each(function() {
			queryString = Wicket.Form.serialize(this, dontTryToFindRootForm);
		});

		equal(queryString, 'urlInput=http%3A%2F%2Fexample.com&', 'Wicket.Form.serialize should not serialize the whole form when an element is passed and the parent form should not be searched');
	});

	test('Wicket.Form.serialize - form element WITH searching for the parent form', function() {

		var dontTryToFindRootForm = false,
			queryString = '';

		jQuery('#urlInputId').each(function() {
			queryString = Wicket.Form.serialize(this, dontTryToFindRootForm);
		});

		equal(queryString, 'textInput=textValue&textUTFInput=%D0%BD%D0%B5%D1%89%D0%BE+%D0%BD%D0%B0+%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8&checkBoxInput1=cbValue1&checkBoxInput3=cbValue3&radioInput=radioValue1&emailInput=m%40g.com&urlInput=http%3A%2F%2Fexample.com&searchInput=wicket&rangeInput=67&numberInput=16&colorInput=123456&multipleSelect=0&multipleSelect=2&select=0&textArea=some+text&', 'Wicket.Form.serialize should serialize the whole form when an element is passed and the parent form should be searched');
	});


	test('Wicket.Form.serialize - form element WITH searching for the parent form', function() {

		var dontTryToFindRootForm = true,
			queryString = '';

		jQuery('#testForm').each(function() {
			queryString = Wicket.Form.serialize(this, dontTryToFindRootForm);
		});

		equal(queryString, 'textInput=textValue&textUTFInput=%D0%BD%D0%B5%D1%89%D0%BE+%D0%BD%D0%B0+%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8&checkBoxInput1=cbValue1&checkBoxInput3=cbValue3&radioInput=radioValue1&emailInput=m%40g.com&urlInput=http%3A%2F%2Fexample.com&searchInput=wicket&rangeInput=67&numberInput=16&colorInput=123456&multipleSelect=0&multipleSelect=2&select=0&textArea=some+text&', 'Wicket.Form.serialize should serialize the whole form when a the form itself is passed');
	});

});
