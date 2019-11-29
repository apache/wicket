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

/*
 * Used by TrapFocusBehavior to trap focus inside a component's markup.
 *
 * @author Igor Vaynberg
 * @author svenmeier
 */
;
(function($, window, document, undefined) {
	'use strict';

	if (window.Wicket && window.Wicket.trapFocus) {
		return;
	}
	
	/** find all elements inside container that can receive focus */
	function findFocusable(container) {
		var focusables = 'a[href], area[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, *[tabindex], *[contenteditable]';
		return container.find(focusables).filter(":visible, *:not([tabindex=-1])");
	}

	// special handler notified by jQuery on removal of a 'trapfocusremove' handler - this
	// happens whenever an element with a focus trap is removed from the DOM, see below
	$.event.special.trapfocusremove = {
		remove: function(handleObj) {
			// forward removal notification, this allows the focus trap to be cleaned up  
			handleObj.handler();
		}
	};

	// one global active 'focusin' handler for all traps  
	var focusin = $.noop;

	// setup a focus trap for an element
	window.Wicket.trapFocus = function(element) {
		
		// keep old active element
		var oldActive = document.activeElement;
		Wicket.Log.debug("trap-focus: focus was on element", oldActive);

		var $element = $('#' + element);

		// allow focus on element itself
		$element.attr('tabindex', 0);
		
		// handles focus navigation via tab key
		$element.on("keydown", function(e) {
			if (Wicket.Event.keyCode(e) === 9) { // tab
				var $focusable = findFocusable($element);
				if ($focusable.length > 0) {
					var firstFocusable = $focusable.get(0);
					var lastFocusable  = $focusable.get($focusable.length - 1);

					if (e.shiftKey) {
						if (e.target === firstFocusable || $element.is(e.target)) {
							e.preventDefault();
							lastFocusable.focus();
						}
					} else {
						if (e.target === lastFocusable || $element.is(e.target)) {
							e.preventDefault();
							firstFocusable.focus();
						}
					}
				}
			}
		});
		
		// turn off previous 'focusin' handler
		var previousfocusin = focusin;
		$(document).off("focusin", focusin);

		// ... pull in focus
		findFocusable($element).first().focus();
		
		// ... and install new handler
		focusin = function() {
			if (!$.contains($element[0], document.activeElement) && $element[0] !== document.activeElement) {
				// focus is outside of element, so pull in focus
				findFocusable($element).first().focus();
			}
		};
		$(document).on("focusin", focusin);
		
		// listen for removal
		$element.on("trapfocusremove", function() {
			// turn off 'focusin' handler
			$(document).off("focusin", focusin);
			
			// ... restore old focus
			if (oldActive) {
				try {
					oldActive.focus();
					Wicket.Log.debug("trap-focus: restored focus to element ", oldActive);
				} catch (error) {
					Wicket.Log.error("trap-focus: error restoring focus. Attempted to set focus to element, but got an exception", oldActive, error);
				}
			}
			
			// ... and re-install previous 'focusin' handler
			focusin = previousfocusin;
			$(document).on("focusin", focusin);
		});
	};

}(jQuery, window, document, undefined));