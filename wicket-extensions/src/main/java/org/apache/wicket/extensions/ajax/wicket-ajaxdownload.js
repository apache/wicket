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
			document.cookie = settings.name +
				'=;path=/;Max-Age=0;expires=Thu, 01 Jan 1970 00:00:01 GMT' + settings.sameSite;
			const notifyServer = function(result) {
				settings.attributes.ep = settings.attributes.ep || {};
				settings.attributes.ep.result = result;
				Wicket.Ajax.ajax(settings.attributes);
			};

			const checkComplete = function(watcher) {
				let result;

				if (document.cookie.indexOf(settings.name + '=') > -1) {
					result = "success";
				} else {
					const html = watcher.html();
					if (html && html.length) {
						result = "failed";
					}
				}

				if (result) {
					watcher.dismiss(result);
					
					notifyServer(result);
				} else {
					setTimeout(function() {
						checkComplete(watcher);
					}, 100);
				}
			};

			if (settings.method === 'samewindow') {
				setTimeout(function () {
					window.location.assign(settings.downloadUrl);
					checkComplete({
						html: function() {
							return "";
						},

						dismiss: function(result) {
						}
					});
				}, 10);
			} else if (settings.method === 'newwindow') {
				const wo = window.open(settings.downloadUrl);
				checkComplete({
					html: function() {
						const body = wo && wo.document && wo.document.body;
						return body ? body.innerHTML : "";
					},

					dismiss: function(result) {
						if (result === "failed") {
							wo.close();
						}
					}
				});
			} else if (settings.method === 'iframe') {
				const frame = document.createElement("iframe");
				frame.setAttribute("hidden", "");
				frame.style.display = "none";
				frame.src = settings.downloadUrl;
				document.body.appendChild(frame);
				checkComplete({
					html: function() {
						const body = frame.contentDocument && frame.contentDocument.body;
						return body ? body.innerHTML : "";
					},

					dismiss: function() {
						// don't remove iframe immediately
						setTimeout(function () {
							frame.remove();
						}, 0);
					}
				});
			} else {
				// jquery does not support binary download
				const xhr = new XMLHttpRequest();

				xhr.open("GET", settings.downloadUrl);
				xhr.responseType = "blob";
				xhr.onload = function() {
					if (this.status === 200) {
						let filename = "";
						const disposition = xhr.getResponseHeader("Content-Disposition");
						if (disposition) {
							const matches = /filename[^;=\n]*=(([""]).*?\2|[^;\n]*)/.exec(disposition);
							if (matches !== null && matches[1]) {
								filename = matches[1].replace(/[""]/g, "");
								filename = decodeURIComponent(filename);
							}
						}

						const type = xhr.getResponseHeader("Content-Type");
						const blob = new Blob([xhr.response], {type: type});

						const blobUrl = URL.createObjectURL(blob);

						const anchor = document.createElement("a");
						anchor.href = blobUrl;
						anchor.download = filename;
						anchor.setAttribute("hidden", "");
						anchor.style.display = "none";
						document.body.appendChild(anchor);

						anchor.click();

						setTimeout(function () {
							URL.revokeObjectURL(blobUrl);
							anchor.remove();
						}, 100);
						notifyServer("success");
					} else {
						notifyServer("failed");
					}
				};
				xhr.onerror = function() {
					notifyServer("failed");
				};
				xhr.send();
			}
		}
	}; 
})();
