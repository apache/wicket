$q(document).ready(function() {
	"use strict";

	module('Ajax');

	asyncTest('successful ajax form submit', function () {
		expect(3);

		load('/ajax/form').then(function($) {

			// enter just the name field
			var $nameInput = $('input[name=name]');
			var name = 'Ajax form name';
			$nameInput.val(name);

			ajaxClick($('input[name=ajax-button]')).then(function($$) {

				// an error feedback message that email is mandatory is expected
				var $feedback = $$('li.feedbackPanelERROR > span');
				equal($feedback.length, 1, 'The error feedback message that email is missing is here');
				equal($feedback.text(), 'Email is required', 'The error feedback matches');

				// enter the email field too
				var $emailInput = $$('input[name=email]');
				var email = 'contact@example.com';
				$emailInput.val(email);

				ajaxClick($$('input[name=ajax-button]')).then(function($$$) {

					// the feedback panel must be empty now
					var $feedback = $$$('li.feedbackPanelERROR > span');
					equal($feedback.length, 0, 'The error feedback message should be gone');

					start();
				});
			});
		});
	});

});
