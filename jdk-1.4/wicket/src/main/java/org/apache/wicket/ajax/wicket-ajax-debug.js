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
var WicketAjaxDebug = {

	showResponseText : false,
	
	scrollLock : false,

	debugWindowId : "wicketAjaxDebugWindow",
	
	debugWindowScrollLockLinkId : "wicketAjaxDebugScrollLock",
	
	debugWindowDragHandleId : "wicketAjaxDebugWindowDragHandle",
	
	debugWindowResizeHandleId : "wicketAjaxDebugWindowResizeHandle",
	
	debugWindowLogId : "wicketAjaxDebugWindowLogId",
	
	getDebugWindow : function() {
		WicketAjaxDebug.init();
	    wicketGet(WicketAjaxDebug.debugWindowId);
	},
	
	showDebugWindow : function() {
		WicketAjaxDebug.init();
	    wicketShow(WicketAjaxDebug.debugWindowId);
	},
	
	hideDebugWindow : function() {
		WicketAjaxDebug.init();
	    wicketHide(WicketAjaxDebug.debugWindowId);
	},

    log : function(msg, label) {
		WicketAjaxDebug.init();
        var d = wicketGet(WicketAjaxDebug.debugWindowLogId);
        var c = document.createElement("div");

		msg = "" + msg;		
        msg = msg.replace(/</g, "&lt;");
        msg = msg.replace(/>/g, "&gt;");
        msg = msg.replace(/\n/g, "<br/>");
        msg = msg.replace(/ /g, "&nbsp;");  
        msg = msg.replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;");
		
		if (typeof(label) != "undefined")
			msg = "<b>" + label + "</b>" + msg;
		          
        c.innerHTML = msg;
        c.setAttribute("style","font-size: 82%; margin: 0px; padding:0px");        
        d.appendChild(c);
        
        if (WicketAjaxDebug.scrollLock == false) {
        	d.scrollTop = d.scrollHeight;
        }
    },
    
    logError : function(msg) {
		WicketAjaxDebug.init();
        WicketAjaxDebug.log(msg, "ERROR: ");
    },

    logInfo : function(msg) {
		WicketAjaxDebug.init();
        WicketAjaxDebug.log(msg, "INFO: ");
    },

    clearLog : function() {
		WicketAjaxDebug.init();
        var d = wicketGet(WicketAjaxDebug.debugWindowLogId);
		d.innerHTML = "";
    },

	init : function() {		

        if ( wicketAjaxDebugEnabled()) {
        	var wad=WicketAjaxDebug;
        	var dwid=wad.debugWindowId;
        	var dwdhid=wad.debugWindowDragHandleId;
        	var dwrhid=wad.debugWindowResizeHandleId;

			var firstTime = document.getElementById(dwid) == null;

			if (firstTime) {
				
	            var html = 	        	
					"<div style='width: 450px; display: none; position: absolute; left: 200px; top: 300px; z-index: 1000;' id='"+dwid+"'>"+
					"	<div style='border: 1px solid black; padding: 1px; background-color: #eee'>"+
					"		<div style='overflow: auto; width: 100%'>"+
					"			<div style='float: right; padding: 0.2em; padding-right: 1em;'>"+
					"               <a href='javascript:WicketAjaxDebug.switchScrollLock()' id='"+WicketAjaxDebug.debugWindowScrollLockLinkId+"' style='color:blue' onfocus='this.blur();'>scroll lock</a> |"+
					"				<a href='javascript:WicketAjaxDebug.clearLog()' style='color:blue'>clear</a> | "+
					"				<a href='javascript:WicketAjaxDebug.hideDebugWindow()' style='color:blue'>close</a>"+
					"			</div>"+
					"			<div id='"+dwdhid+"' style='padding: 0.2em; background-color: gray; color: white; padding-left: 1em; margin-right: 14em; cursor: move;'>"+
					"				Wicket Ajax Debug Window (drag me here)"+
					"			</div>"+
					"			<div id='"+WicketAjaxDebug.debugWindowLogId+"' style='width: 100%; height: 200px; background-color: white; overflow: auto; white-space: nowrap'>"+
					"			</div>"+
					"           <div style='height: 10px; margin:0px; padding:0px;overflow:hidden;'>"+
					"              <div style='height: 10px; width: 10px; background-color: gray; margin:0px; padding: 0px;overflow:hidden; float:right; cursor: nw-resize' id='" + WicketAjaxDebug.debugWindowResizeHandleId + "'>"+
					"              </div>"+
					"           </div>"+
					"		</div>"+					
					"	</div>" +
					"</div>"+
					"<div id='wicketDebugLink' style='position:fixed; left: 10px; bottom: 10px; z-index:100; _position: absolute; " +
					"                                  _left: expression(eval(document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft) + 10);"+
					"                                  _top: expression(-10 - wicketDebugLink.offsetHeight + eval(document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop) + ( document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight ));'>"+
					"    <a href='javascript:WicketAjaxDebug.showDebugWindow()'>WICKET AJAX DEBUG</a>"+
					"</div>";
												
				
				WicketAjaxDebug.addElement(html);
				Wicket.$(WicketAjaxDebug.debugWindowScrollLockLinkId).focusSet = true;
            	Wicket.Drag.init(wicketGet(dwdhid), function() {} , function() { }, WicketAjaxDebug.onDrag);
            	Wicket.Drag.init(wicketGet(dwrhid), function() {} , function() { }, WicketAjaxDebug.onResize);
			}

        }
	},
	
	switchScrollLock: function() {
		WicketAjaxDebug.scrollLock = !WicketAjaxDebug.scrollLock;
		var link = Wicket.$(WicketAjaxDebug.debugWindowScrollLockLinkId);
		if (WicketAjaxDebug.scrollLock) {
			link.style.color = "red";
		} else {
			link.style.color = "blue";
		}
	},
	
	onResize: function(element, deltaX, deltaY) {
		var window = wicketGet(WicketAjaxDebug.debugWindowId);
		var log = wicketGet(WicketAjaxDebug.debugWindowLogId);
		
		var width = parseInt(window.style.width, 10) + deltaX;
		var height = parseInt(log.style.height, 10) + deltaY;
		
		var res = [0, 0];
		
		if (width < 300) {
			res[0] = 300 - width;
			width = 300;
		}
		
		if (height < 100) {
			res[1] = 100 - height;
			height = 100;	
		}						
			
		window.style.width = width + "px";
		log.style.height = height + "px";		
		
		return res;
	},
	
	onDrag: function(element, deltaX, deltaY) {
		var w = wicketGet(WicketAjaxDebug.debugWindowId);
		
		var x = parseInt(w.style.left, 10) + deltaX;
		var y = parseInt(w.style.top, 10) + deltaY;

		var res = [0, 0];
		
		if (x < 0) {
			res[0] = -deltaX;
			x = 0;
		}
		if (y < 0) {
			res[1] = -deltaY;
			y = 0;	
		}						
			
		w.style.left = x + "px";
		w.style.top = y + "px";		
		
		return res;
	},
	
	addElement : function(html) {
		var element = document.createElement("div");				
		element.innerHTML = html;
		document.body.appendChild(element);
	},

	addEvent: function(obj, evType, fn) { 
		if (obj.addEventListener) { 
			obj.addEventListener(evType, fn, false); 
			return true; 
		} else if (obj.attachEvent) { 
			var r = obj.attachEvent("on"+evType, fn); 
   			return r; 
		} else { 
   			return false; 
		} 
	}
};

WicketAjaxDebug.addEvent(window, "load", WicketAjaxDebug.init);