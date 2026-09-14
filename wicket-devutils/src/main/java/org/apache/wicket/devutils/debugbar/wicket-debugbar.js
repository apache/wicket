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
;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'undefined') {
		window.Wicket = {};
	}

	if (typeof(Wicket.debugBar) === 'object') {
		return;
	}

	function isVisible(element) {
		return !!(element.offsetWidth || element.offsetHeight || element.getClientRects().length);
	}

	Wicket.debugBar = function() {

		function setExpandedCookie(value) {
			document.cookie =  "wicketDebugBarState=" + window.encodeURIComponent(value);
		}

		function getExpandedCookie() {
			const name = 'wicketDebugBarState';
			if (document.cookie.length > 0) {
				let start = document.cookie.indexOf (name + "=");
				if (start !== -1) {
					start = start + name.length + 1;
					let end = document.cookie.indexOf(";", start);
					if (end === -1) {
						end = document.cookie.length;
					}
					return window.decodeURIComponent(document.cookie.substring(start,end));
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		const collapse = document.getElementById('wicketDebugBarCollapse');
		if (collapse) {
			collapse.addEventListener("click", function() {
				const content = document.getElementById('wicketDebugBarContents');
				const wasVisible = isVisible(content);
				setExpandedCookie(!wasVisible);
				if (wasVisible) {
					Wicket.DOM.hide(content);
				} else {
					Wicket.DOM.show(content);
				}
			});
		}

		const remove = document.getElementById('wicketDebugBarRemove');
		if (remove) {
			remove.addEventListener("click", function() {
				const bar = document.getElementById('wicketDebugBar');
				setExpandedCookie(!isVisible(bar));
				Wicket.DOM.hide(bar);
			});
		}

	    // determine state and set it
		if (getExpandedCookie() === 'false') {
			Wicket.DOM.hide(document.getElementById('wicketDebugBarContents'));
		}

		const original = Wicket.Log.error;
		Wicket.Log.error = function() {
			original.apply(Wicket.Log, arguments);

			const bar = document.getElementById('wicketDebugBar');
			if (bar) {
				bar.classList.add('wicketDebugBarError');
				bar.addEventListener('animationend', function onAnimationEnd() {
					bar.classList.remove('wicketDebugBarError');
					bar.removeEventListener('animationend', onAnimationEnd);
				}, { once: true });
			}
		};
	};
})();
