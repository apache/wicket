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
 * Wicket BrowserInfo Support
 *
 * Populates a special form with the client/browser info
 * and submits it to the server
 */

;(function (undefined) {

	'use strict';

	if (typeof(Wicket) === 'undefined') {
		window.Wicket = {};
	}

	if (typeof(Wicket.BrowserInfo) === 'undefined') {
		window.Wicket.BrowserInfo = {

			/**
			 * Collects the client information
			 *
			 * @returns {Object}
			 */
			collect: function() {
				var info = {};
				info.navigatorAppName = window.navigator.appName;
				info.navigatorAppVersion = window.navigator.appVersion;
				info.navigatorAppCodeName = window.navigator.appCodeName;
				var cookieEnabled = (window.navigator.cookieEnabled);
				if (typeof(window.navigator.cookieEnabled) === "undefined" && !cookieEnabled) {
					document.cookie = "wickettestcookie";
					cookieEnabled = (document.cookie.indexOf("wickettestcookie") !== -1);
				}
				info.navigatorCookieEnabled = cookieEnabled;
				info.navigatorJavaEnabled =  window.navigator.javaEnabled();
				info.navigatorLanguage = window.navigator.language ? window.navigator.language : window.navigator.userLanguage;
				info.navigatorPlatform = window.navigator.platform;
				info.navigatorUserAgent = window.navigator.userAgent;
				if (window.screen) {
					info.screenWidth = window.screen.width;
					info.screenHeight = window.screen.height;
					info.screenColorDepth = window.screen.colorDepth;
				}
				info.utcOffset = (new Date(new Date().getFullYear(), 0, 1, 0, 0, 0, 0).getTimezoneOffset() / -60);
				info.utcDSTOffset = (new Date(new Date().getFullYear(), 6, 1, 0, 0, 0, 0).getTimezoneOffset() / -60);
				info.browserWidth =  window.innerWidth || document.body.offsetWidth;
				info.browserHeight =  window.innerHeight || document.body.offsetHeight;
				info.hostname =  window.location.hostname;
				return info;
			},

			/**
			 * Populates the form elements with the client info
			 *
			 * @param formMarkupId   The markup id of the special form
			 * @returns {HTMLFormElement} The special form
			 */
			populateFields: function(formMarkupId) {
				var postbackForm = document.getElementById(formMarkupId);
				var info = Wicket.BrowserInfo.collect();
				var i;
				for (i in info) {
					postbackForm[i].value = info[i];
				}

				return postbackForm;
			},

			/**
			 * Populates and submits the special form.
			 *
			 * @param formMarkupId  The markup id of the special form
			 */
			submitForm: function(formMarkupId) {
				var postbackForm = Wicket.BrowserInfo.populateFields(formMarkupId);
				postbackForm.submit();
			}
		};
	}

})();
