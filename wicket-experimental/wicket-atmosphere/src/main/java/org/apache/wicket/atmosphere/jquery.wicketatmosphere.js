;(function($) {
	'use strict';

	jQuery.fn.wicketAtmosphere = function(params) {
		var callbackAdded = false;
		var response;

		// jquery.atmosphere.response
		function callback(response) {

			if (response.transport !== 'polling' && response.state === 'messageReceived') {
				$.atmosphere.log('info', [ "response.responseBody: " + response.responseBody ]);
				if (response.status === 200) {
					(new Wicket.Ajax.Call()).loadedCallback($.parseXML(response.responseBody), {});
				}
			} else if (response.state === "opening") {
			}
		}

		var connectedEndpoint = $.atmosphere.subscribe(params.url,
				!callbackAdded ? callback : null, $.atmosphere.request = {
					logLevel : "debug",
					transport : "websocket",
					// transport : "streaming",
					// transport : "long-polling",
					maxRequests : 100000
				});
		callbackAdded = true;
		response = $.atmosphere.response;

		$(window).bind("beforeunload", function() {
			callbackAdded = false;
			$.atmosphere.unsubscribe();
		});
	};
}(jQuery));