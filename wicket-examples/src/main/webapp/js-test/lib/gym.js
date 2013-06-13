
var load = function(url) {
    var deferred = $q.Deferred();
    onPageLoad(function(iframe, $) {
        deferred.resolve(iframe, $);
    });
    getIframe().attr('src', url);

    return deferred;
}

var click = function($btn) {
    var deferred = $q.Deferred();
    onPageLoad(function(iframe, $$) {
        deferred.resolve(iframe, $$);
    });

	$btn.click();

    return deferred;
}

// private
var getIframe = function() {
	return $q('#applicationFrame');
}

// private
var onPageLoad = function(toExecute) {

	getIframe()
		.off('load')
		.on('load', function() {
			$q(this).off('load');

			var newIframe, $;

			newIframe = window.frames[0];
			$ = newIframe.jQuery || jQueryWithContext;

			//debug(newIframe);

			toExecute.call(newIframe, $);
		});
};

/**
 * Non-Ajax pages do not have jQuery so we use
 * $q with context to simulate it
 */
// private
var jQueryWithContext = function(selector) {
	return $q(selector, $q(getIframe()).contents());
};

var ajaxClick = function($btn) {
	var deferred = $q.Deferred();
	var iframeWindow = getIframe()[0].contentWindow;

	onAjaxComplete(iframeWindow, function($$) {
		deferred.resolve($$);
	});

	$btn.click();

	return deferred;
}

/**
 * Registers a callback when Wicket Ajax call is completed
 */
// private
var onAjaxComplete = function(iframe, toExecute) {

	// unregister any leaked subscriber
	iframe.jQuery(iframe.document).off('/ajax/call/complete');
	
	// register the requested subscriber
	iframe.Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, textStatus) {
		// immediately unregister this subscriber
		iframe.jQuery(iframe.document).off('/ajax/call/complete');
		
		// call back
		var $$ = iframe.jQuery || jQueryWithContext;
		toExecute($$);
	});
};

// unused
var followHref = function(iframe, $, $link) {
	var loc = iframe.document.location;
//	console.log('Current url', loc.href);
//	console.log('Link', $link.selector);

	if ($link.length) {
		var newUrl = $link.attr('href');
//		console.log('Following href: ', newUrl);
		loc.replace(newUrl);
	}
}

var debug = function(iframe) {
	"use strict";

	console.log('Current url: ', iframe.window.location.href);
}
