$q(document).ready(function() {
	"use strict";

	module('Echo');

	asyncTest('echo', function () {
		expect(2);

		load('/echo').then(function($) {

			var $messageInput = $('input[name=msgInput]');
			var message = 'Hello Functional QUnit';
			$messageInput.val(message);
		
			click($('input[type=submit]')).then(function($$) {

				var $msg = $$('#msg');
				equal($msg.length, 1, 'The entered message is here');
				equal($msg.text(), message, 'The entered message is here');

				start();
			});
		});
	});

});
