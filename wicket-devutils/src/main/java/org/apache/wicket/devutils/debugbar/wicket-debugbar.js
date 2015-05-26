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
function wicketDebugBarCollapse() {
	wicketDebugBarToggleVisibility('wicketDebugBarContents');
}

function wicketDebugBarRemove() {
	wicketDebugBarToggleVisibility('wicketDebugBar');
}

function wicketDebugBarToggleVisibility(elemID) {
	var elem = document.getElementById(elemID);
	var vis  = elem.style.display != 'none';
	elem.style.display = (vis ? 'none' : '');
    // alter the state cookie so we can initialize it properly on domReady
	wicketDebugBarSetExpandedCookie(vis ? 'collapsed' : 'expanded')
}

function wicketDebugBarSetExpandedCookie(value) {
	document.cookie =  "wicketDebugBarState=" + window.escape(value);
}

function wicketDebugBarGetExpandedCookie() {
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

function wicketDebugBarCheckState() {
	var state = wicketDebugBarGetExpandedCookie();
    // state cookie has not been set. determine state and set it
	if (state === null) {
		var isVisible = $('#wicketDebugBarContents').is(':visible');
		wicketDebugBarSetExpandedCookie(isVisible ? 'expanded' : 'collapsed');
    // set state of debug bar according to cookie
	} else {
		if (state === 'expanded') {
			$('#wicketDebugBarContents').css('display', 'inherit');
		} else {
			$('#wicketDebugBarContents').css('display', 'none');
		}
	}
}
