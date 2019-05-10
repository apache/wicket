/*
 * 
 * FEATURES
 * - When modal is closed focus is restored to the element that had it before the modal was opened
 * - Focus is trapped inside the modal when using tab/shift-tab
 * - Focus is set on the first focusable element in the modal when it is opened
 * - On Escape or click outside the modal a button with class x-modal-close will be clicked
 * - Secondary close buttons can be added and marked with x-modal-close-secondary. Clicking these buttons forwards the
 *   click to the primary x-modal-close button
 * - Aria support
 *   - Various aria attributes added to the modal making it behave as a dialog to screen readers
 *   - aria-labelledby will be added if the modal content contains an element with x-modal-title class
 *   - adia-describedby will be added if the modal content contains an element with x-modal-description class
 * 
 * ENTRY POINTS
 * - window.wicket.modal.open: function(element, options)
 *   - element: string|dom|jquery - dom element that will be body of modal
 *   - options: object, see description below
 * - window.wicket.modal.close: function(element)
 *   - element: string|dom|jquery - dom element that was specified as body of modal
 *  
 * OPTIONS
 * validate: boolean
 *  - when modal is opened several checks will be performed
 *  - error when modal content does not contain an element with x-modal-close class
 *  - warning when modal content does not contain an element with modal-description class
 *  - error when modal does not contain any focusable elements
 * console: object
 *  - an object used for reporting validation errors
 *    - must have error(object...) method
 *    - must have warn(object...) method
 * 
 * ROADMAP
 * - Set max height of content as 80% of screen, also provide option later
 * - Open full screen on small screens - css fix only via media queries?
 * - Support for simultaneously opened modals - testing to make sure it works ok or do we need to implement stack tracking
 * 
 */
;
(function($, window, document, console, undefined) {
	'use strict';

	if (window.wicket && window.wicket.modal) {
		return;
	}

	var DATA_KEY = "modal-dialog-data";
	var OVERLAY_SELECTOR = ".modal-dialog-overlay";
	var CONTAINER_SELECTOR = ".modal-dialog";
	var SCROLL_SELECTOR=".modal-dialog-scroll-area";
	var CONTENT_SELECTOR = ".modal-dialog-content";
	var CLOSE_SELECTOR = ".x-modal-close";
	var SECONDARY_CLOSE_SELECTOR = ".x-modal-close-secondary";
	
	//
	// UTILITY METHODS
	//

	/** Retreives id of the element, creates one if none */
	var getOrCreateIdCounter = 0;
	function getOrCreateId(element) {
		if (!element.attr("id")) {
			element.attr("id", "modal-autoid-" + (getOrCreateIdCounter++));
		}
		return element.attr("id");
	}

	/**
	 * Resolves a value to a dom node, useful when parsing arguments passed to
	 * functions
	 */
	function resolveDomNode(element) {
		if ((typeof element) === "string") {
			return $(document.getElementById(element));
		} else if (element.tagName) {
			return $(element);
		} else if (element instanceof $) {
			return element;
		}
		throw new Error("Cannot resolve value: " + element + " to dom node");
	}

	/** Finds all elements inside container that can receive focus */
	function findFocusable(container) {
		var focusables = 'a[href], area[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, *[tabindex], *[contenteditable]';
		return container.find(focusables).filter(":visible");
	}

	/**
	 * Finds all elements inside the container that can receive focus via the
	 * tab key
	 */
	function findTabbable(container) {
		return findFocusable(container).not("*[tabindex=-1]");
	}

	/** Focuses the first element inside the modal */
	function focusDefaultFocusable(container) {
		var matches = findFocusable(container);
		var first = matches.not(".modal-dialog-close").first();
		if (first.length > 0) {
			first.focus();
		} else {
			matches.first().focus();
		}
	}

	/**
	 * Finds and clicks the close button inside the modal. Returns true if
	 * button was found.
	 */
	function findAndClickCloseButton(container) {
		var matches = container.find(CLOSE_SELECTOR).filter(":visible");
		if (matches.length > 0) {
			matches.first().click();
			return true;
		} else {
			return false;
		}
	}

	//
	// BEHAVIORS
	//
	// Behaviors are event listeners that get called from open and close
	// methods, they allow various aspects of code such as focus management and
	// aria attribute management to be decoupled from each other making the
	// overall code cleaner and easier to maintain.
	//
	// The structure of a behavior is an object with the following properties:
	// initialize: function()
	// - called before first modal is opened
	// destroy: function()
	// - called after last modal is closed
	// prepare: function(overlayElement, contentElement, data)
	// - called after overlay dom is constructed, but before it is inserted
	// - into main dom
	// open: function(overlayElement, contentElement, data)
	// - called after overlayElement is inserted into main dom
	// close: function(overlayElement, contentElement, data)
	// - called after overlayElement is removed from main dom

	//Scroll settings to remember for ios scroll to top issue. Currently, ios allows body 
	//scrolling unless body is set to position: fixed, which causes the window to scroll to top.
    var scrollTop;

	/** Behavior that appends a css class to body as long as any modal is open */
	var appendBodyClassBehavior = {

		initialize : function() {
			var body = $("body");
			body.addClass("modal-dialog-open modal-dialog-no-scroll");

			scrollTop = $(window).scrollTop();

            if (!!navigator.platform && /iPad|iPhone|iPod/.test(navigator.platform)) {
                body.addClass("modal-dialog-open-ios");
            }
		},
		terminate : function() {
			$("body").removeClass("modal-dialog-open modal-dialog-no-scroll modal-dialog-open-ios");
            if (!!navigator.platform && /iPad|iPhone|iPod/.test(navigator.platform)) {
            	$(window).scrollTop(scrollTop);
            }
		}
	};

	/**
	 * Behavior that memorizes the focussed element when dialog is opened, and
	 * returns focus to it when dialog is closed
	 */
	var returnFocusOnCloseBehavior = {
		open : function(overlay, element, data) {
			data.opener = document.activeElement;
			if (data.options.validate) {
				if (!data.opener || $(data.opener).is("body")) {
					data.options.console.error("Error saving focused element when opening the modal, it is either none or body: ",
							data.opener);
				}
			}
		},
		close : function(overlay, element, data) {
			if (data.opener) {
				try {
					data.opener.focus();
				} catch (error) {
					if (data.options.validate) {
						data.options.console.error(
								"Error restoring focus after modal is closed. Attempted to set focus to element, but got an exception",
								data.opener, error);
					}
					throw error;
				}
			}
		}
	}

	/** Takes care of adding any necessary aria-related attributes to the dialog */
	var addAriaAttributesBehavior = {
		prepare : function(overlay, element, data) {
			var content = overlay.find(CONTENT_SELECTOR);
			var attrs = {
				"role" : "dialog",
				"aria-modal" : "true"
			};

			var title = element.find(".x-modal-title").first();
			if (title.length > 0) {
				attrs["aria-labelledby"] = getOrCreateId(title);
			} else if (data.options.validate) {
				data.options.console.error("No .x-modal-title element present in modal content: ", element.get(0));
			}

			var description = element.find(".x-modal-description").first();
			if (description.length > 0) {
				attrs["aria-describedby"] = getOrCreateId(description);
			} else if (data.options.validate) {
				data.options.console.warn("No .x-modal-description element present in modal content: ", element.get(0));
			}

			content.attr(attrs);
		}
	}

	/** Closes the modal if the overlay is clicked or an escape key is pressed */
	var closeOnOverlayClickOrEscapeBehavior = {
		prepare : function(overlay, element, data) {
			if (data.options.closeOnClickOutside) {
				overlay.on("click.modal-dialog", function (event) {
					if ($(event.target).closest(CONTENT_SELECTOR).length === 0) {
						// clicked outside modal's content
						findAndClickCloseButton(element);
					}
				});
			}
			if (data.options.closeOnEscape) {
				overlay.on("keydown", function (event) {
					if (event.which == 27) {
						event.preventDefault();
						event.stopPropagation();
						findAndClickCloseButton(element);
					}
				});
			}
		},
		open : function(overlay, element, data) {
			if (data.options.validate && (data.options.closeOnClickOutside || data.options.closeOnEscape)) {
				if (element.find(CLOSE_SELECTOR).filter(":visible").length === 0) {
					data.options.console.error("Modal Dialog content does not contain a clickable element with class .x-modal-close."
							+ " Clicking outside the modal or pressing ESC will have no effect");
				}
			}
		}
	};

	/** Detects clicks on secondary close buttons (SENODARY_CLOSE_SELECTOR) and forwards the click to the primary close button */
	var secondaryCloseButtonBehavior = {
		prepare: function(overlay, element, data) {
			overlay.on("click", SECONDARY_CLOSE_SELECTOR, function(event) {
				event.preventDefault();
				event.stopPropagation();
				findAndClickCloseButton(element);
			});
		}
	}

	/** Traps focus inside the modal window. */
	var trapFocusInsideModalBehavior = {
		prepare : function(overlay, element, data) {
			overlay.on("keydown", function(e) {
				if (e.which === 9) { // tab
					var container = $(e.target).closest(CONTENT_SELECTOR);
					var focusables = findTabbable(container);
					var firstFocusable = focusables.get(0);
					var lastFocusable = focusables.get(focusables.length - 1);

					if (!e.shiftKey && e.target === lastFocusable) {
						e.preventDefault();
						firstFocusable.focus();
					}
					if (e.shiftKey && e.target === firstFocusable) {
						e.preventDefault();
						lastFocusable.focus();
					}
				}
			});

			overlay.on("DOMNodeRemoved.modal-dialog", function(e) {
				// handles focus transitions when nodes are removed, for example
				// a node that has focus is removed via an ajax update
				window.setTimeout(function() {
					// needs to run in timeout because the event will get called
					// with the node that is being removed as active
					var active = $(document.activeElement);
					if (active.closest(CONTENT_SELECTOR).length === 0) {
						// focus has been moved to something outside the modal,
						// refocus
						focusDefaultFocusable(element);
					}
				}, 0);
			});
		},
		close : function(overlay, element, data) {
			overlay.off("DOMNodeRemoved.modal-dialog");
		}

	};

	var focusDefaultOnOpeningBehavior = {
		open : function(overlay, element, data) {
			focusDefaultFocusable(element);
		}
	};

	var sizingBehavior = {
		prepare : function(overlay, element, data) {
			if (data.options.maxWidth) {
				overlay.find(CONTAINER_SELECTOR).css({
					maxWidth : data.options.maxWidth
				});
			}
			if(data.options.maxHeight) {
				overlay.find(SCROLL_SELECTOR).css({
					maxHeight: data.options.maxHeight
				});
			}
		}
	};

	var defaultOptions = {
		validate : false,
		console : window.console,
		maxWidth : null,
		maxHeight: null, //"80vh"
		closeOnClickOutside: false,
		closeOnEscape: true
	};

	var behaviors = [ appendBodyClassBehavior, returnFocusOnCloseBehavior, closeOnOverlayClickOrEscapeBehavior, addAriaAttributesBehavior,
			trapFocusInsideModalBehavior, focusDefaultOnOpeningBehavior, sizingBehavior, secondaryCloseButtonBehavior ];

	//
	// Entry Methods
	//

	window.wicket = window.wicket || {};
	var ns = window.wicket.modal = {};

	ns.open = function(element, options) {
		options = $.extend({}, defaultOptions, options);
		element = resolveDomNode(element);

		var data = {
			element : element,
			options : options,
		};

		element.data(DATA_KEY, data);

		var firstDialogOpened = $(document).find(OVERLAY_SELECTOR).length === 0;

		if (firstDialogOpened) {
			for (var i = 0; i < behaviors.length; i++) {
				if (behaviors[i].initialize) {
					behaviors[i].initialize();
				}
			}
		}

		data.contentParent = element.parent();

		data.overlay = $(""//
				+ "<div class='modal-dialog-overlay'>" //
				+ "  <div class='modal-dialog'>" //
				+ "    <div class='modal-dialog-scroll-area'>" //
				+ "      <div class='modal-dialog-content' tabindex='0'>" //
				+ "      </div>" //
				+ "    </div>" //
				+ "  </div>" //
				+ "</div>");

		for (var i = 0; i < behaviors.length; i++) {
			if (behaviors[i].prepare) {
				behaviors[i].prepare(data.overlay, element, data);
			}
		}

		$("body").append(data.overlay);

		element.appendTo(data.overlay.find(CONTENT_SELECTOR));

		for (var i = 0; i < behaviors.length; i++) {
			if (behaviors[i].open) {
				behaviors[i].open(data.overlay, element, data);
			}
		}

	}

	ns.close = function(element) {
		element = resolveDomNode(element);
		var data = element.data(DATA_KEY);

		for (var i = 0; i < behaviors.length; i++) {
			if (behaviors[i].close) {
				behaviors[i].close(data.overlay, element, data);
			}
		}

		element.removeData(DATA_KEY);
		element.appendTo(data.contentParent);
		data.overlay.remove();

		var lastDialogClosed = $(document).find("modal-dialog-overlay").length === 0;
		if (lastDialogClosed) {
			for (var i = 0; i < behaviors.length; i++) {
				if (behaviors[i].terminate) {
					behaviors[i].terminate(element);
				}
			}
		}
	}

}(jQuery, window, document, console, undefined));
