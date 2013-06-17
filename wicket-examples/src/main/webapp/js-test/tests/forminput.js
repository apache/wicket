$q(document).ready(function() {
	"use strict";

	module('Form Input');

	asyncTest('Change StringProperty', function () {
		expect(2);

		gym.load('/forminput').then(function($) {

			var $stringPropertyInput = $('#stringProperty');
			var text = 'qunit test value';
			$stringPropertyInput.val(text);

			gym.click($('input[value=save]')).then(function($$) {

				var $feedback = $$('li.feedbackPanelINFO > span');
				equal($feedback.length, 1, 'The feedback is here');
				equal($feedback.text().indexOf("stringProperty = '"+text+"'"), 29, 'The entered text is here');
				
				start();
			});
		});
	});

	asyncTest('Change the locale', function () {
		expect(2);

		gym.load('/forminput').then(function($) {

			var $select = $('select[name=localeSelect]');
			var locale = '2'; // German

			$select.val(locale);

			gym.click($('input[value=save]')).then(function($$) {

				var $integerInRangeProperty = $$('label[for=integerInRangeProperty]');
				equal($integerInRangeProperty.length, 1, 'The label for integerInRangeProperty is here');
				equal($integerInRangeProperty.text(), 'Nur Werte zwischen 0 und 100 sind erlaubt', 'The german version is correct');

				start();
			});
		});
	});

});
