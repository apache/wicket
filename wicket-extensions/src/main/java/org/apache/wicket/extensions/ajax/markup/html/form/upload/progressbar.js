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

	if (typeof(Wicket) === "undefined") {
		window.Wicket = {};
	}

	Wicket.WUPB = Wicket.Class.create();
	Wicket.WUPB.prototype = {

		initialize : function(formid, statusid, barid, url, fileid, initialStatus, onProgressUpdated) {
			this.statusid = statusid;
			this.barid = barid;
			this.url = url;
			this.fileid = fileid;
			this.initialStatus = initialStatus;
			this.onProgressUpdated = onProgressUpdated;

			if (formid) {
				const formElement = Wicket.$(formid);
				this.originalCallback = formElement.onsubmit;
				formElement.onsubmit = Wicket.bind(this.submitCallback, this);
			}
		},

		submitCallback : function() {
			if (this.originalCallback && !this.originalCallback()) {
				return false;
			} else {
				this.start();
				return true;
			}
		},

		start : function(){
			let displayprogress = true;
			if (this.fileid) {
				const fileupload = Wicket.$(this.fileid);
				displayprogress = fileupload && fileupload.value;
			}
			if (displayprogress) {
				this.setPercent(0);
				this.setStatus(this.initialStatus);
				const $statusId = Wicket.$(this.statusid);
				if ($statusId != null) {
					Wicket.DOM.show($statusId);
				}
				const $barid = Wicket.$(this.barid);
				if ($barid != null) {
					Wicket.DOM.show($barid);
				}
				this.scheduleUpdate();
			}
		},

		setStatus : function(status) {
			const label = document.createElement("label");
			label.innerHTML = status;
			const $statusId = Wicket.$(this.statusid);
			if ($statusId != null) {
				const oldLabel = $statusId.firstChild;
				if (oldLabel != null){
					$statusId.removeChild(oldLabel);
				}
				$statusId.appendChild(label);
			}
		},

		setPercent : function(progressPercent) {
			const barId = Wicket.$(this.barid);
			if (barId != null && barId.firstChild != null && barId.firstChild.firstChild != null) {
				barId.firstChild.firstChild.style.width = progressPercent + '%';
			}
			if (this.onProgressUpdated) {
				this.onProgressUpdated(progressPercent);
			}
		},

		scheduleUpdate : function(){
			window.setTimeout(Wicket.bind(this.load, this), 1000);
		},

		_createIFrame : function (iframeName) {
			const iframe = document.createElement("iframe");
			iframe.name = iframeName;
			iframe.id = iframeName;
			iframe.src = "about:blank";
			iframe.setAttribute("hidden", "");
			return iframe;
		},

		load : function() {
			const URL = this.url;

	        this.iframe = this._createIFrame(""+Math.random());

	        document.body.appendChild(this.iframe);

			Wicket.Event.add(this.iframe, "load", Wicket.bind(this.update, this));
			this.iframe.src = URL;
		},

		update : function() {
			const responseAsText = this.iframe.contentDocument.body.innerHTML;

			const update = responseAsText.split('|');

			const progressPercent = update[1];
			const status = update[2];

			this.setPercent(progressPercent);
			this.setStatus( status );

			this.iframe.parentNode.removeChild(this.iframe);
			this.iframe = null;

			if (progressPercent === '100') {
				const $statusId = Wicket.$(this.statusid);
				if ($statusId != null) {
					Wicket.DOM.hide($statusId);
				}
				const $barid = Wicket.$(this.barid);
				if ($barid != null) {
					Wicket.DOM.hide($barid);
				}
			} else {
				this.scheduleUpdate();
			}
		}
	};
})();
