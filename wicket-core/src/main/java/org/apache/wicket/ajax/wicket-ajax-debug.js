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
	
	wicketDebugLink: "wicketDebugLink",
	
	getDebugWindow : function() {
		WicketAjaxDebug.init();
	    wicketGet(WicketAjaxDebug.debugWindowId);
	},
	
	showDebugWindow : function() {
		WicketAjaxDebug.init();
	    wicketShow(WicketAjaxDebug.debugWindowId);

        var link = wicketGet(WicketAjaxDebug.wicketDebugLink);
        link.style.backgroundColor = "white";
        link.style.color = "blue";
        
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
		msg = msg.replace(/&/g, "&amp;");
        msg = msg.replace(/</g, "&lt;");
        msg = msg.replace(/>/g, "&gt;");
        msg = msg.replace(/\n/g, "<br/>");
        msg = msg.replace(/ /g, "&nbsp;");  
        msg = msg.replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;");
		
		if (typeof(label) != "undefined")
			msg = "<b>" + label + "</b>" + msg;
		          
        WicketAjaxDebug.doInnerHTML(c, msg);
        c.setAttribute("style","font-size: 82%; margin: 0px; padding:0px");        
        d.appendChild(c);
        
        if (WicketAjaxDebug.scrollLock == false) {
        	d.scrollTop = d.scrollHeight;
        }
    },
    
    logError : function(msg) {
		WicketAjaxDebug.init();
        WicketAjaxDebug.log(msg, "<span style='color: red'>ERROR</span>: ");
        
        if (wicketGet(WicketAjaxDebug.debugWindowId).style.display === "none") {
            var link = wicketGet(WicketAjaxDebug.wicketDebugLink);
            link.style.backgroundColor = "crimson";
            link.style.color = "aliceBlue";
        }

        if (typeof(console) != "undefined"&&typeof(console.error) == 'function') {
        	console.error('Wicket.Ajax: ' + msg);
        }
    },

    logInfo : function(msg) {
		WicketAjaxDebug.init();
        WicketAjaxDebug.log(msg, "<span style='color: blue'>INFO</span>: ");
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
					"<div style='width: 450px; display: none; position: absolute; left: 200px; top: 300px; z-index: 1000000;' id='"+dwid+"'>"+
					"	<div style='border: 1px solid black; padding: 1px; background-color: #eee'>"+
					"		<div style='overflow: auto; width: 100%'>"+
					"			<div style='float: right; padding: 0.2em; padding-right: 1em; color: black;'>"+
					"               <a href='javascript:WicketAjaxDebug.switchScrollLock()' id='"+WicketAjaxDebug.debugWindowScrollLockLinkId+"' style='color:blue' onfocus='this.blur();'>scroll lock</a> |"+
					"				<a href='javascript:WicketAjaxDebug.clearLog()' style='color:blue'>clear</a> | "+
					"				<a href='javascript:WicketAjaxDebug.hideDebugWindow()' style='color:blue'>close</a>"+
					"			</div>"+
					"			<div id='"+dwdhid+"' style='padding: 0.2em; background-color: gray; color: white; padding-left: 1em; margin-right: 14em; cursor: move;'>"+
					"				Wicket Ajax Debug Window (drag me here)"+
					"			</div>"+
					"			<div id='"+WicketAjaxDebug.debugWindowLogId+"' style='width: 100%; height: 200px; background-color: white; color: black; overflow: auto; white-space: nowrap; text-align:left;'>"+
					"			</div>"+
					"           <div style='height: 10px; margin:0px; padding:0px;overflow:hidden;'>"+
					"              <div style='height: 10px; width: 10px; background-color: gray; margin:0px; padding: 0px;overflow:hidden; float:right; cursor: nw-resize' id='" + WicketAjaxDebug.debugWindowResizeHandleId + "'>"+
					"              </div>"+
					"           </div>"+
					"		</div>"+					
					"	</div>" +
					"</div>";
				// Special style for Internet 6 and 7 in quirks mode
				if (Wicket.Browser.isIE() && (Wicket.Browser.isIEQuirks() || !Wicket.Browser.isIE7())) {
					html +=	
						"<a id='"+WicketAjaxDebug.wicketDebugLink+"' style='position:absolute; right: 10px; bottom: 10px; z-index:1000000; padding-top: 0.3em; padding-bottom: 0.3em; line-height: normal ; _padding-top: 0em; width: 12em; border: 1px solid black; background-color: white; text-align: center; opacity: 0.7; filter: alpha(opacity=70); color: blue; " +
						"                                  left: expression(-10 - wicketDebugLink.offsetWidth + eval(document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft) +(document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth));"+
						"                                  top: expression(-10 - wicketDebugLink.offsetHeight + eval(document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop) + (document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight));'";
				} else {
					html += 
						"<a id='"+WicketAjaxDebug.wicketDebugLink+"' style='position:fixed; right: 10px; bottom: 10px; z-index:1000000; padding-top: 0.3em; padding-bottom: 0.3em; line-height: normal ; _padding-top: 0em; width: 12em; border: 1px solid black; background-color: white; text-align: center; opacity: 0.7;  color: blue;'";
				}
				
				html += "  href='javascript:WicketAjaxDebug.showDebugWindow()'>WICKET AJAX DEBUG</a>";
					
												
				
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
	},
	
	/**
	 * A XHTML Strict safe function to set HTMLElement's innerHTML.
	 * WICKET-3023
	 */
	doInnerHTML: function(elem, html) {
 
        try {
            elem.innerHTML = html;
            return true;
        } catch (e) {
            try {
                var children = elem.childNodes;
     
                for (var i = 0; i < children.length; i++) {
                    elem.removeChild(children[i]);
                }
     
                var nodes = new DOMParser().parseFromString(html, 'text/xml');
                var range = document.createRange();
                range.selectNodeContents(elem);
                range.deleteContents();
     
                for (var i = 0; i < nodes.childNodes.length; i++) {
                    elem.appendChild(nodes.childNodes[i]);
                }
     
                return true;
            }
            catch(ee) {
                return false;
            }
        }
    }
};

WicketAjaxDebug.addEvent(window, "load", WicketAjaxDebug.init);