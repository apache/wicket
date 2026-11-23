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
 * Used by ModalDialog: moves the focus into a dialog when it opens and back to the element that
 * had it when the dialog closes, keeps Tab and Shift+Tab inside the dialog and closes it on Escape.
 *
 * The open dialogs are kept in the order they were opened. Keys go to the innermost open dialog
 * that contains the focused element, or to the dialog opened last when no element has the focus.
 * A dialog that is rendered again while open, by an Ajax update, keeps the element to give the
 * focus back to.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket.ModalDialog) === 'object') {
		return;
	}

	const FOCUSABLE = 'a[href], area[href], button, input, select, textarea, iframe, object, ' +
		'embed, summary, audio[controls], video[controls], [contenteditable], [tabindex]';

	const dialogs = [];

	let focused = document.activeElement;
	let previouslyFocused = null;

	function isVisible(element) {
		return !!(element.offsetWidth || element.offsetHeight || element.getClientRects().length) &&
			window.getComputedStyle(element).visibility !== 'hidden';
	}

	function isFocusable(element) {
		return element.tabIndex >= 0 && !element.matches(':disabled') && !element.closest('[inert]') &&
			isVisible(element);
	}

	function isUncheckedRadioOfCheckedGroup(element, container) {
		if (element.type !== 'radio' || !element.name || element.checked) {
			return false;
		}
		return Array.prototype.some.call(container.querySelectorAll('input[type="radio"]'),
			function (radio) {
				return radio.checked && radio.name === element.name && radio.form === element.form;
			});
	}

	function tabbables(container) {
		return Array.prototype.filter.call(container.querySelectorAll(FOCUSABLE), function (element) {
			return isFocusable(element) && !isUncheckedRadioOfCheckedGroup(element, container);
		});
	}

	function focus(element) {
		if (element.tabIndex < 0 && !element.hasAttribute('tabindex')) {
			element.setAttribute('tabindex', '-1');
		}
		element.focus();
	}

	function initialFocus(dialogElement) {
		const autofocus = Array.prototype.find.call(dialogElement.querySelectorAll('[autofocus]'),
			isFocusable);
		return autofocus || tabbables(dialogElement)[0] || dialogElement;
	}

	function isNothing(element) {
		return !element || element === document.body || element === document.documentElement;
	}

	function find(overlayId) {
		return dialogs.find(function (dialog) {
			return dialog.overlayId === overlayId;
		});
	}

	function isShown(element) {
		return !!element && !element.hasAttribute('data-wicket-placeholder');
	}

	function dialogFor(target) {
		for (let i = dialogs.length - 1; i >= 0; i--) {
			const overlay = document.getElementById(dialogs[i].overlayId);
			if (!dialogs[i].removed && overlay && overlay.contains(target)) {
				return dialogs[i];
			}
		}
		if (isNothing(target)) {
			for (let i = dialogs.length - 1; i >= 0; i--) {
				if (!dialogs[i].removed && isShown(document.getElementById(dialogs[i].overlayId))) {
					return dialogs[i];
				}
			}
		}
		return null;
	}

	function triggerOf(overlay) {
		const active = document.activeElement;
		if (overlay.contains(active)) {
			return overlay.contains(previouslyFocused) ? null : previouslyFocused;
		}
		return isNothing(active) ? null : active;
	}

	function giveFocusBack(dialog) {
		let trigger = dialog.trigger;
		if (trigger && !trigger.isConnected) {
			trigger = trigger.id ? document.getElementById(trigger.id) : null;
		}
		if (!trigger) {
			return;
		}

		const below = dialogFor(document.body);
		if (below && !document.getElementById(below.overlayId).contains(trigger)) {
			return;
		}

		try {
			trigger.focus();
		} catch (error) {
			Wicket.Log.error("modal-dialog: could not give the focus back to", trigger, error);
		}
	}

	function settle(dialog) {
		const index = dialogs.indexOf(dialog);
		if (index < 0 || !dialog.removed || isShown(document.getElementById(dialog.overlayId))) {
			return;
		}
		dialogs.splice(index, 1);
		giveFocusBack(dialog);
	}

	function onTab(event) {
		const dialog = dialogFor(event.target);
		if (!dialog || !dialog.trapFocus) {
			return;
		}

		const dialogElement = document.getElementById(dialog.dialogId);
		const elements = tabbables(dialogElement);
		const target = event.target;
		const inside = target !== dialogElement && dialogElement.contains(target);

		let next = null;
		if (elements.length === 0) {
			next = dialogElement;
		} else if (event.shiftKey) {
			const hasPrevious = inside && elements.some(function (element) {
				return target.compareDocumentPosition(element) & Node.DOCUMENT_POSITION_PRECEDING;
			});
			next = hasPrevious ? null : elements[elements.length - 1];
		} else {
			const hasNext = inside && elements.some(function (element) {
				return target.compareDocumentPosition(element) & Node.DOCUMENT_POSITION_FOLLOWING;
			});
			next = hasNext ? null : elements[0];
		}

		if (next) {
			event.preventDefault();
			focus(next);
		}
	}

	function onEscape(event) {
		const dialog = dialogFor(event.target);
		if (dialog && dialog.escape) {
			event.preventDefault();

			const attrs = Object.assign({}, dialog.escape);
			attrs.event = event;
			Wicket.Ajax.ajax(attrs);
		}
	}

	document.addEventListener('focusin', function (event) {
		previouslyFocused = focused;
		focused = event.target;
	});

	document.addEventListener('keydown', function (event) {
		if (event.defaultPrevented || event.isComposing) {
			return;
		}
		if (event.key === 'Escape' || event.keyCode === 27) {
			onEscape(event);
		} else if ((event.key === 'Tab' || event.keyCode === 9) && !event.altKey &&
			!event.ctrlKey && !event.metaKey) {
			onTab(event);
		}
	});

	Wicket.Event.subscribe(Wicket.Event.Topic.DOM_NODE_REMOVING, function (jqEvent, removed) {
		dialogs.forEach(function (dialog) {
			const overlay = document.getElementById(dialog.overlayId);
			if (overlay && removed && (removed === overlay ||
				(typeof removed.contains === 'function' && removed.contains(overlay)))) {
				dialog.removed = true;
				Promise.resolve().then(function () {
					settle(dialog);
				});
			}
		});
	});

	Wicket.ModalDialog = {

		/**
		 * Registers a dialog that is shown, or shown again after an Ajax update.
		 *
		 * @param {Object} settings - overlay: the markup id of the overlay, dialog: the markup id
		 *   of the dialog, trapFocus: whether Tab and Shift+Tab cycle through the dialog, escape:
		 *   the Ajax attributes of the request that closes the dialog, or null to keep it open on
		 *   Escape
		 */
		open: function (settings) {
			const overlay = document.getElementById(settings.overlay);
			const dialogElement = document.getElementById(settings.dialog);
			if (!overlay || !dialogElement) {
				Wicket.Log.error("modal-dialog: no element with id '%s' or '%s' found",
					settings.overlay, settings.dialog);
				return;
			}

			let dialog = find(settings.overlay);
			const shownAgain = !!dialog;
			if (!shownAgain) {
				dialog = { overlayId: settings.overlay, trigger: triggerOf(overlay) };
				dialogs.push(dialog);
			}
			dialog.dialogId = settings.dialog;
			dialog.trapFocus = settings.trapFocus !== false;
			dialog.escape = settings.escape || null;
			dialog.removed = false;

			const active = document.activeElement;
			if (overlay.contains(active)) {
				return;
			}
			if (shownAgain && (!isNothing(active) ||
				Wicket.Focus.refocusLastFocusedComponentAfterResponse)) {
				return;
			}
			focus(initialFocus(dialogElement));
		}
	};
})();
