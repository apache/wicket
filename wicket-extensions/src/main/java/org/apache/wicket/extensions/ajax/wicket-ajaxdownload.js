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

	if (!window.Wicket) {
		window.Wicket = {};
	}

	if (Wicket.AjaxDownload) {
		return;
	}

	Wicket.AjaxDownload = {
		initiate : function(settings) {

			var notifyServer = function (result) {
                settings.attributes.ep = settings.attributes.ep || {};
                settings.attributes.ep.result = result;
                Wicket.Ajax.ajax(settings.attributes);
            };

			var checkComplete = function (frame) {
				var result;

				if (document.cookie.indexOf(settings.name + '=') > -1) {
					result = "success";
				} else if (frame) {
					var html = frame.contents().find('body').html();
					if (html && html.length) {
						result = "failed";
					}
				}

				if (result) {
					if (frame) {
						setTimeout(function () {
							frame.remove();
						}, 0);
					}

					notifyServer(result);
				} else {
					setTimeout(function() {
						checkComplete(frame);
					}, 100);
				}
			};

			if (settings.method === 'samewindow') {
				setTimeout(function () {
					window.location.assign(settings.downloadUrl);
					checkComplete();
				}, 10);
			} else if (settings.method === 'newwindow') {
				var wo = window.open(settings.downloadUrl, 'ajax-download');
				jQuery(wo).load(function(evt) {
					notifyServer(undefined);
				});
			} else {
				var frame = jQuery("<iframe></iframe>").hide().prop("src", settings.downloadUrl).appendTo("body");
				checkComplete(frame);
			}
		}
	}; 
	
})();
