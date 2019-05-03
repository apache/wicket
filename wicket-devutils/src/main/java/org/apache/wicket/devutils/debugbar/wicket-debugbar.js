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
;(function (jQuery, undefined) {

	'use strict';

	if (typeof(Wicket) === 'undefined') {
		window.Wicket = {};
	}

	if (typeof(Wicket.debugBar) === 'object') {
		return;
	}

	Wicket.debugBar = function() {
		
		function setExpandedCookie(value) {
			document.cookie =  "wicketDebugBarState=" + window.escape(value);
		}
		
		function getExpandedCookie() {
			var name = 'wicketDebugBarState';
			if (document.cookie.length > 0) {
				var start = document.cookie.indexOf (name + "=");
				if (start !== -1) {
					start = start + name.length + 1;
					var end = document.cookie.indexOf(";", start);
					if (end === -1) {
						end = document.cookie.length;
					}
					return window.unescape(document.cookie.substring(start,end));
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		jQuery('#wicketDebugBarCollapse').on("click", function() {
			var content = jQuery('#wicketDebugBarContents');
			setExpandedCookie(!content.is(':visible'));
			content.toggle(400);
		});

		jQuery('#wicketDebugBarRemove').on("click", function() {
			var bar = jQuery('#wicketDebugBar');
			setExpandedCookie(!bar.is(':visible'));
			bar.hide();
		});

	    // determine state and set it
		if (getExpandedCookie() === 'false') {
			jQuery('#wicketDebugBarContents').hide();
		}
		
		var original = Wicket.Log.error;
		Wicket.Log.error = function(msg) {
			original(msg);
			
			jQuery('#wicketDebugBar')
				.addClass('wicketDebugBarError')
				.one('animationend', function() {
					jQuery(this).removeClass('wicketDebugBarError');
				});
		};
	};
})(jQuery);
