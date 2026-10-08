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
(function(window, document, undefined) {
	'use strict';

	if (window.Wicket && window.Wicket.trapFocus) {
		return;
	}

	const FOCUSABLE_SELECTOR = 'a[href], area[href], input:not([disabled]), select:not([disabled]), ' +
		'textarea:not([disabled]), button:not([disabled]), iframe, object, embed, *[tabindex], *[contenteditable]';

	function isVisible(element) {
		return !!(element.offsetWidth || element.offsetHeight || element.getClientRects().length);
	}

	/** find all elements inside container that can receive focus */
	function findFocusable(container) {
		const candidates = Array.prototype.slice.call(container.querySelectorAll(FOCUSABLE_SELECTOR));
		return candidates.filter(function (element) {
			return isVisible(element) && element.getAttribute('tabindex') !== '-1';
		});
	}

	// one global active 'focusin' handler for all traps
	let focusin = function () {};

	// setup a focus trap for an element
	window.Wicket.trapFocus = function(elementId, styleClass) {

		const element = document.getElementById(elementId);
		if (!element) {
			Wicket.Log.error("trap-focus: no element with id '%s' found", elementId);
			return;
		}

		// keep old active element
		const oldActive = document.activeElement;
		Wicket.Log.debug("trap-focus: focus was on element", oldActive);

		// allow focus on element itself
		element.setAttribute('tabindex', '0');

		// handles focus navigation via tab key
		const keydownHandler = function(e) {
			if (Wicket.Event.keyCode(e) === 9) { // tab
				const focusable = findFocusable(element);
				if (focusable.length > 0) {
					const firstFocusable = focusable[0];
					const lastFocusable  = focusable[focusable.length - 1];

					if (e.shiftKey) {
						if (e.target === firstFocusable || e.target === element) {
							e.preventDefault();
							lastFocusable.focus();
						}
					} else {
						if (e.target === lastFocusable || e.target === element) {
							e.preventDefault();
							firstFocusable.focus();
						}
					}
				}
			}
		};
		element.addEventListener('keydown', keydownHandler);

		// mark current trap
		const oldTraps = Array.prototype.slice.call(document.querySelectorAll('.' + styleClass));
		oldTraps.forEach(function (trap) {
			trap.classList.remove(styleClass);
		});
		element.classList.add(styleClass);

		// turn off previous 'focusin' handler
		const previousFocusin = focusin;
		document.removeEventListener("focusin", previousFocusin);

		// ... pull in focus
		const initialFocusable = findFocusable(element)[0];
		if (initialFocusable) {
			initialFocusable.focus();
		}

		// ... and install new handler
		focusin = function() {
			if (!element.contains(document.activeElement) && element !== document.activeElement) {
				// focus is outside of element, so pull in focus
				const toFocus = findFocusable(element)[0];
				if (toFocus) {
					toFocus.focus();
				}
			}
		};
		document.addEventListener("focusin", focusin);

		// listen for removal of the trapped element from the document, using Wicket's
		// generic DOM removal notification (published by Wicket.DOM.replace()/remove())
		// so that this works regardless of the Ajax/DOM engine in use.
		const onRemoving = function (event, removedElement) {
			if (removedElement === element || (removedElement.contains && removedElement.contains(element))) {
				Wicket.Event.unsubscribe(Wicket.Event.Topic.DOM_NODE_REMOVING, onRemoving);

				// turn off 'focusin' handler
				document.removeEventListener("focusin", focusin);
				element.removeEventListener('keydown', keydownHandler);

				// ... restore old focus
				if (oldActive) {
					try {
						oldActive.focus();
						Wicket.Log.debug("trap-focus: restored focus to element ", oldActive);
					} catch (error) {
						Wicket.Log.error("trap-focus: error restoring focus. Attempted to set focus to element, but got an exception", oldActive, error);
					}
				}

				// ... re-install previous 'focusin' handler
				focusin = previousFocusin;
				document.addEventListener("focusin", focusin);

				// ... and restore trap mark
				oldTraps.forEach(function (trap) {
					trap.classList.add(styleClass);
				});
				element.classList.remove(styleClass);
			}
		};
		Wicket.Event.subscribe(Wicket.Event.Topic.DOM_NODE_REMOVING, onRemoving);
	};

}(window, document));
