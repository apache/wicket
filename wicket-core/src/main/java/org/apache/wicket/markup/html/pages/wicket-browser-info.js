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
			 * Populates the form elements with the client info
			 *
			 * @param formMarkupId   The markup id of the special form
			 * @returns {HTMLFormElement} The special form
			 */
			populateFields: function(formMarkupId) {
				var postbackForm = document.getElementById(formMarkupId);
				postbackForm.navigatorAppName.value = window.navigator.appName;
				postbackForm.navigatorAppVersion.value = window.navigator.appVersion;
				postbackForm.navigatorAppCodeName.value = window.navigator.appCodeName;
				var cookieEnabled = (window.navigator.cookieEnabled);
				if (typeof(window.navigator.cookieEnabled) === "undefined" && !cookieEnabled) {
					document.cookie = "wickettestcookie";
					cookieEnabled = (document.cookie.indexOf("wickettestcookie") !== -1);
				}
				postbackForm.navigatorCookieEnabled.value = cookieEnabled;
				postbackForm.navigatorJavaEnabled.value =  window.navigator.javaEnabled();
				postbackForm.navigatorLanguage.value = window.navigator.language ? window.navigator.language : window.navigator.userLanguage;
				postbackForm.navigatorPlatform.value = window.navigator.platform;
				postbackForm.navigatorUserAgent.value = window.navigator.userAgent;
				if (window.screen) {
					postbackForm.screenWidth.value = window.screen.width;
					postbackForm.screenHeight.value = window.screen.height;
					postbackForm.screenColorDepth.value = window.screen.colorDepth;
				}
				postbackForm.utcOffset.value = (new Date(new Date().getFullYear(), 0, 1, 0, 0, 0, 0).getTimezoneOffset() / -60);
				postbackForm.utcDSTOffset.value = (new Date(new Date().getFullYear(), 6, 1, 0, 0, 0, 0).getTimezoneOffset() / -60);
				postbackForm.browserWidth.value =  window.innerWidth || document.body.offsetWidth;
				postbackForm.browserHeight.value =  window.innerHeight || document.body.offsetHeight;
				postbackForm.hostname.value =  window.location.hostname;

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
