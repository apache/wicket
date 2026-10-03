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
 * Copies text to the clipboard and shows a check mark where the copy was made.
 *
 * Wicket.Clipboard.copy(text, iconTarget) copies the text and briefly shows a check icon over the
 * end of iconTarget, inside its box, so a cell clipping its content does not hide it. Every element carrying a data-wicket-copy attribute copies its value when it
 * is clicked, or double clicked if data-wicket-copy-on="dblclick", showing the icon in itself.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === "undefined") {
		window.Wicket = { };
	}

	if (typeof(Wicket.Clipboard) !== "undefined") {
		return;
	}

	const SVG = 'http://www.w3.org/2000/svg';

	const ICON_CLASS = 'wicket-clipboard-copied';

	const HOST_CLASS = 'wicket-clipboard-host';

	const writeText = function (text) {
		if (navigator.clipboard && navigator.clipboard.writeText) {
			return navigator.clipboard.writeText(text);
		}
		return new Promise(function (resolve, reject) {
			const area = document.createElement('textarea');
			area.value = text;
			area.setAttribute('readonly', '');
			area.className = 'wicket-clipboard-buffer';
			document.body.appendChild(area);
			area.select();
			const copied = document.execCommand && document.execCommand('copy');
			area.remove();
			if (copied) {
				resolve();
			} else {
				reject(new Error('copying is not supported'));
			}
		});
	};

	const announce = function (message) {
		let region = document.getElementById('wicket-clipboard-status');
		if (!region) {
			region = document.createElement('span');
			region.id = 'wicket-clipboard-status';
			region.className = 'wicket-clipboard-status';
			region.setAttribute('role', 'status');
			document.body.appendChild(region);
		}
		region.textContent = message;
	};

	const createIcon = function () {
		const icon = document.createElement('span');
		icon.className = ICON_CLASS;
		icon.setAttribute('aria-hidden', 'true');
		const svg = document.createElementNS(SVG, 'svg');
		svg.setAttribute('viewBox', '0 0 24 24');
		const path = document.createElementNS(SVG, 'path');
		path.setAttribute('d', 'M4 12l5 5L20 6');
		path.setAttribute('fill', 'none');
		path.setAttribute('stroke', 'currentColor');
		path.setAttribute('stroke-width', '3');
		path.setAttribute('stroke-linecap', 'round');
		path.setAttribute('stroke-linejoin', 'round');
		svg.appendChild(path);
		icon.appendChild(svg);
		return icon;
	};

	const showIcon = function (target) {
		if (!target) {
			return;
		}
		const previous = target.querySelector(':scope > .' + ICON_CLASS);
		if (previous) {
			previous.remove();
		}
		const icon = createIcon();
		target.classList.add(HOST_CLASS);
		target.appendChild(icon);
		window.setTimeout(function () {
			icon.remove();
			if (!target.querySelector(':scope > .' + ICON_CLASS)) {
				target.classList.remove(HOST_CLASS);
			}
		}, Wicket.Clipboard.iconMillis);
	};

	Wicket.Clipboard = {

		/**
		 * How long the check icon stays, in milliseconds.
		 */
		iconMillis: 1500,

		/**
		 * The text read to screen readers after a copy.
		 */
		copiedMessage: 'Copied',

		/**
		 * Copies text to the clipboard and shows the check icon at the end of iconTarget.
		 *
		 * @param text {String} - the text to copy
		 * @param iconTarget {Element} - where to show the icon, optional
		 * @return {Promise} resolved once the text is copied
		 */
		copy: function (text, iconTarget) {
			return writeText(String(text)).then(function () {
				showIcon(iconTarget);
				announce(Wicket.Clipboard.copiedMessage);
			});
		},

		/**
		 * Copies the value of the data-wicket-copy attribute of the element an event happened in,
		 * if the event is the one the element copies on.
		 */
		onEvent: function (event) {
			const element = event.target && event.target.closest &&
				event.target.closest('[data-wicket-copy]');
			if (!element) {
				return;
			}
			const on = element.getAttribute('data-wicket-copy-on') || 'click';
			if (on !== event.type) {
				return;
			}
			if (event.type === 'dblclick' && window.getSelection) {
				window.getSelection().removeAllRanges();
			}
			Wicket.Clipboard.copy(element.getAttribute('data-wicket-copy'), element)
				.catch(function (error) {
					if (Wicket.Log) {
						Wicket.Log.error('Wicket.Clipboard: copying failed', error);
					}
				});
		}
	};

	document.addEventListener('click', Wicket.Clipboard.onEvent);
	document.addEventListener('dblclick', Wicket.Clipboard.onEvent);
})();
