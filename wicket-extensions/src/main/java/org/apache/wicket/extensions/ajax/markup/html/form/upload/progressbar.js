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
if (typeof(Wicket) == "undefined") Wicket = { };

Wicket.WUPB = Wicket.Class.create();

Wicket.WUPB.prototype = {
		
	initialize : function(def) {
		this.def = def;
	},

	bind : function(formid) {
		formElement = Wicket.$(formid);
		this.originalCallback = formElement.onsubmit;
		formElement.onsubmit = this.submitCallback.bind(this);
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
		this.displayprogress = true;
		if (this.def.fileid) {
			var fileupload = Wicket.$(this.def.fileid);
			this.displayprogress = fileupload && fileupload.value && fileupload.value != '';
		}
		if(this.displayprogress) {
			this.setStatus('Upload starting...');
			Wicket.$(this.def.barid).firstChild.firstChild.style.width='0%';

			Wicket.$(this.def.statusid).style.display='block';
			Wicket.$(this.def.barid).style.display='block';
			this.scheduleUpdate();
		}
	},
	
	setStatus : function(status){
		var label = document.createElement("label");
		label.innerHTML = status;
		var oldLabel = Wicket.$(this.def.statusid).firstChild;
		if( oldLabel != null){
			Wicket.$(this.def.statusid).removeChild(oldLabel);
		}
		Wicket.$(this.def.statusid).appendChild(label);		
	},
	
	scheduleUpdate : function(){
		window.setTimeout(this.ajax.bind(this), 1000);
	},

	ajax : function() {
		var URL = this.def.url + '?anticache=' + Math.random();
		
    	this.iframe = Wicket._createIFrame(""+Math.random());
		
    	document.body.appendChild(this.iframe);
		
		Wicket.Event.add(this.iframe, "load", this.update.bind(this));
		this.iframe.src = URL; 
		
	},

	update : function() {
		
		if(this.iframe.contentDocument){
			var responseAsText = this.iframe.contentDocument.body.innerHTML;
		}else{
			// for IE 5.5, 6 and 7:
			var responseAsText = this.iframe.contentWindow.document.body.innerHTML
		}
		
		var update = responseAsText.split('|');

		var completed_upload_size = update[2];
		var total_upload_size = update[3];
		var progressPercent = update[1];
		var transferRate = update[4];
		var timeRemaining = update[5];

		if ((timeRemaining != "") && (completed_upload_size != 0)) {

			Wicket.$(this.def.barid).firstChild.firstChild.style.width = progressPercent + '%';
			this.setStatus( progressPercent
				+ '% finished, ' + completed_upload_size + ' of '
				+ total_upload_size + ' at ' + transferRate + "; "
				+ timeRemaining );

		}
		
		this.iframe.parentNode.removeChild(this.iframe);
		this.iframe = null;
		
		if (progressPercent == 100 || timeRemaining == 0) {
			if (progressPercent == 100) {
				Wicket.$(this.def.barid).firstChild.firstChild.style.width = '100%';
			}
			wicketHide(this.def.statusid);
			wicketHide(this.def.barid);
		} else {
			this.scheduleUpdate();
		}
	}
};

Wicket.WUPB.Def = Wicket.Class.create();

Wicket.WUPB.Def.prototype = {
	initialize : function(formid, statusid, barid, url, fileid) {
		this.formid = formid;
		this.statusid = statusid;
		this.barid = barid;
		this.url = url;
		this.fileid = fileid;
	}
};