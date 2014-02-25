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

/*jshint maxlen: 430 */
/*global console: true */


/*
 * Wicket Ajax Debug Window
 */

;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'undefined') {
		window.Wicket = {};
	}

	if (typeof(Wicket.Ajax) === 'undefined') {
		Wicket.Ajax = {};
	}

	if (typeof(Wicket.Ajax.DebugWindow) === 'object') {
		return;
	}

	Wicket.Ajax.DebugWindow = {

		/**
		 * A flag indicating whether the ajax debug window is enabled. Switched on by AbstractDefaultAjaxBehavior.java
		 */
		enabled: false,

		scrollLock : false,

		debugWindowId : 'wicketAjaxDebugWindow',

		debugWindowScrollLockLinkId : 'wicketAjaxDebugScrollLock',

		debugWindowDragHandleId : 'wicketAjaxDebugWindowDragHandle',

		debugWindowResizeHandleId : 'wicketAjaxDebugWindowResizeHandle',

		debugWindowLogId : 'wicketAjaxDebugWindowLogId',

		wicketDebugLink: 'wicketDebugLink',

		throttler: new Wicket.Throttler(true),

		showDebugWindow : function() {
			var self = Wicket.Ajax.DebugWindow;
			self.init();

			jQuery('#'+self.wicketDebugLink)
				.css('backgroundColor', 'white')
				.css('color', 'blue');

			jQuery('#'+self.debugWindowId).slideToggle("fast", "swing");
		},

		hideDebugWindow : function() {
			var self = Wicket.Ajax.DebugWindow;
			self.init();
			jQuery('#'+self.debugWindowId).slideToggle("fast", "swing");
		},

		log : function(msg, label) {
			var self = Wicket.Ajax.DebugWindow;
			self.init();
			var $log = jQuery('#'+self.debugWindowLogId);
			var $child = jQuery("<div>");

			msg = "" + msg;
			msg = msg.replace(/&/g, "&amp;");
			msg = msg.replace(/</g, "&lt;");
			msg = msg.replace(/>/g, "&gt;");
			msg = msg.replace(/\n/g, "<br/>");
			msg = msg.replace(/ /g, "&nbsp;");
			msg = msg.replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;");

			if (typeof(label) !== "undefined") {
				msg = "<b>" + label + "</b>" + msg;
			}

			$child.html(msg);
			$child
				.css('fontSize','82%')
				.css('margin', '0px')
				.css('padding', '0px');
			$log.append($child);

			if (self.scrollLock === false) {
				$log.scrollTop($log[0].scrollHeight);
			}
		},

		logError : function (msg) {
			var self = Wicket.Ajax.DebugWindow;
			self.init();
			self.log(msg, "<span style='color: red'>ERROR</span>: ");

			if (jQuery('#'+self.debugWindowId).css('display') === 'none') {
				jQuery('#'+self.wicketDebugLink)
					.css('backgroundColor', 'crimson')
					.css('color', 'aliceBlue');

				self.throttler.throttle("Wicket.Ajax.DebugWindow", 300, function() {
					jQuery('#'+self.wicketDebugLink)
						// poor man's animation to get developer's attention
						.hide(150).show(150).hide(150).show(150);
				});
			}

			if (typeof(console) !== "undefined" && typeof(console.error) === 'function') {
				console.error('Wicket.Ajax: ' + msg);
			}
		},

		logInfo : function(msg) {
			var self = Wicket.Ajax.DebugWindow;
			self.init();
			self.log(msg, "<span style='color: blue'>INFO</span>: ");
		},

		clearLog : function() {
			var self = Wicket.Ajax.DebugWindow;
			self.init();
			jQuery('#'+self.debugWindowLogId).empty();
		},

		init : function() {

			if ( Wicket.Ajax.DebugWindow.enabled) {
				var self = Wicket.Ajax.DebugWindow;
				var $window = jQuery('#'+self.debugWindowId);
				var dwdhid = self.debugWindowDragHandleId;
				var dwrhid = self.debugWindowResizeHandleId;

				if ($window.length === 0) {

					var html =
						"<div style='width: 450px; display: none; position: absolute; left: 200px; top: 300px; z-index: 1000000;' id='"+self.debugWindowId+"'>"+
						"	<div style='border: 1px solid black; padding: 1px; background-color: #eee'>"+
						"		<div style='overflow: auto; width: 100%'>"+
						"			<div style='float: right; padding: 0.2em; padding-right: 1em; color: black;'>"+
						"               <a href='javascript:Wicket.Ajax.DebugWindow.switchScrollLock()' id='"+self.debugWindowScrollLockLinkId+
											"' style='color:blue' onfocus='this.blur();'>scroll lock</a> |"+
						"				<a href='javascript:Wicket.Ajax.DebugWindow.clearLog()' style='color:blue'>clear</a> | "+
						"				<a href='javascript:Wicket.Ajax.DebugWindow.hideDebugWindow()' style='color:blue'>close</a>"+
						"			</div>"+
						"			<div id='"+dwdhid+
										"' style='padding: 0.2em; background-color: gray; color: white; padding-left: 1em; margin-right: 14em; cursor: move;'>"+
						"				Wicket Ajax Debug Window (drag me here)"+
						"			</div>"+
						"			<div id='"+self.debugWindowLogId+"' style='width: 100%; height: 200px; background-color: white; color: black; overflow: auto; white-space: nowrap; text-align:left;'>"+
						"			</div>"+
						"           <div style='height: 10px; margin:0px; padding:0px;overflow:hidden;'>"+
						"              <div style='height: 10px; width: 10px; background-color: gray; margin:0px; padding: 0px;overflow:hidden; float:right; cursor: nw-resize' id='" + self.debugWindowResizeHandleId + "'>"+
						"              </div>"+
						"           </div>"+
						"		</div>"+
						"	</div>" +
						"</div>";


					// Special style for Internet 6 and 7 in quirks mode
					if (Wicket.Browser.isIELessThan11() && (Wicket.Browser.isIEQuirks() || !Wicket.Browser.isIE7())) {
						html +=
							"<a id='"+self.wicketDebugLink+"' style='position:absolute; right: 10px; bottom: 10px; z-index:1000000; padding-top: 0.3em; padding-bottom: 0.3em; line-height: normal ; _padding-top: 0em; width: 12em; border: 1px solid black; background-color: white; text-align: center; opacity: 0.7; filter: alpha(opacity=70); color: blue; " +
							"                                  left: expression(-10 - wicketDebugLink.offsetWidth + eval(document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft) +(document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth));"+
							"                                  top: expression(-10 - wicketDebugLink.offsetHeight + eval(document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop) + (document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight));'";
					} else {
						html +=
							"<a id='"+self.wicketDebugLink+"' style='position:fixed; right: 10px; bottom: 10px; z-index:1000000; padding-top: 0.3em; padding-bottom: 0.3em; line-height: normal ; _padding-top: 0em; width: 12em; border: 1px solid black; background-color: white; text-align: center; opacity: 0.7;  color: blue;'";
					}

					html += "  href='javascript:Wicket.Ajax.DebugWindow.showDebugWindow()'>WICKET AJAX DEBUG</a>";

					jQuery(html).appendTo(document.body);
					Wicket.$(self.debugWindowScrollLockLinkId).focusSet = true;
					Wicket.Drag.init(Wicket.$(dwdhid), jQuery.noop, jQuery.noop, self.onDrag);
					Wicket.Drag.init(Wicket.$(dwrhid), jQuery.noop, jQuery.noop, self.onResize);
				}
			}
		},

		switchScrollLock: function() {
			var self = Wicket.Ajax.DebugWindow;
			self.scrollLock = !self.scrollLock;
			var $link = jQuery('#'+self.debugWindowScrollLockLinkId),
				color;
			if (self.scrollLock) {
				color = 'red';
			} else {
				color = 'blue';
			}
			$link.css('color', color);
		},

		onResize: function(element, deltaX, deltaY) {
			var self = Wicket.Ajax.DebugWindow;

			var $window = jQuery('#'+self.debugWindowId),
				$log = jQuery('#'+self.debugWindowLogId);

			var width = parseInt($window.css('width'), 10) + deltaX;
			var height = parseInt($log.css('height'), 10) + deltaY;

			var res = [0, 0];

			if (width < 300) {
				res[0] = 300 - width;
				width = 300;
			}

			if (height < 100) {
				res[1] = 100 - height;
				height = 100;
			}

			$window.css('width', width + "px");
			$log.css('height',  height + "px");

			return res;
		},

		onDrag: function(element, deltaX, deltaY) {
			var self = Wicket.Ajax.DebugWindow;
			var $window = jQuery('#'+self.debugWindowId);

			var x = parseInt($window.css('left'), 10) + deltaX;
			var y = parseInt($window.css('top'), 10) + deltaY;

			var res = [0, 0];

			if (x < 0) {
				res[0] = -deltaX;
				x = 0;
			}
			if (y < 0) {
				res[1] = -deltaY;
				y = 0;
			}

			$window.css('left', x + "px");
			$window.css('top', y + "px");

			return res;
		}
	};

	jQuery(window).on('load', Wicket.Ajax.DebugWindow.init);

})();
