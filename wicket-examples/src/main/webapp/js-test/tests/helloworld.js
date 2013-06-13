$q(document).ready(function() {
	"use strict";

	module('Hello World');

	asyncTest('hello world', function () {
		expect(2);

		load('/helloworld').then(function($) {

			var $message = $('#message');
			equal($message.length, 1, "The greeting is there");
			equal($message.text(), 'Hello World!', "The greeting is correct");

			start();
		});
	});

});
