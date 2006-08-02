var WicketAjaxDebug = {

	showResponseText : false,

	debugWindowId : "wicketAjaxDebugWindow",
	
	debugWindowDragHandleId : "wicketAjaxDebugWindowHandle",
	
	debugWindowLogId : "wicketAjaxDebugWindowLogId",
	
	getDebugWindow : function()
	{
	    wicketGet(WicketAjaxDebug.debugWindowId);
	},
	
	showDebugWindow : function() {
	    wicketShow(WicketAjaxDebug.debugWindowId);
	},
	
	hideDebugWindow : function() {
	    wicketHide(WicketAjaxDebug.debugWindowId);
	},

    log : function(msg, prefix) {
        var d = wicketGet(WicketAjaxDebug.debugWindowLogId);
        var c = document.createElement("div");
        msg = msg.replace(/</g, "&lt;");
        msg = msg.replace(/>/g, "&gt;");
        msg = msg.replace(/\n/g, "<br/>");
        msg = msg.replace(/ /g, "&nbsp;");  
        msg = msg.replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;");          
        if (prefix != null)
        	msg = "<b>" + prefix + "</b> " + msg;
        c.innerHTML = msg;
        c.setAttribute("style","font-size: 82%; margin: 0px; padding:0px");        
        d.appendChild(c);
        d.scrollTop = d.scrollHeight;
    },
    
    logError : function(msg) {
        WicketAjaxDebug.log(msg, "ERROR: ");
    },

    logInfo : function(msg) {
        WicketAjaxDebug.log(msg, "INFO: ");
    },

    clearLog : function() {
        var d = wicketGet(WicketAjaxDebug.debugWindowLogId);
		d.innerHTML = "";
    },

	init : function() {

        if ( wicketAjaxDebugEnabled()) {
        	var wad=WicketAjaxDebug;
        	var dwid=wad.debugWindowId;
        	var dwdhid=wad.debugWindowDragHandleId;

			var firstTime = document.getElementById(dwid) == null;

			if (firstTime)
	            document.write(	        	
					"<div style='width: 50em; display: none; position: absolute; left: 200px; top: 300px; z-index: 1000;' id='"+dwid+"'>"+
					"	<div style='border: 1px solid black; padding: 1px; background-color: #eee'>"+
					"		<div style='overflow: auto; width: 100%'>"+
					"			<div style='float: right; padding: 0.2em; padding-right: 1em;'>"+
					"				<a href='javascript:WicketAjaxDebug.clearLog()' style='color:blue'>clear</a> | "+
					"				<a href='javascript:WicketAjaxDebug.hideDebugWindow()' style='color:blue'>close</a>"+
					"			</div>"+
					"			<div id='"+dwdhid+"' style='padding: 0.2em; background-color: gray; color: white; padding-left: 1em; margin-right: 8em; cursor: move;'>"+
					"				Wicket Ajax Debug Window (drag me here)"+
					"			</div>"+
					"			<div id='"+WicketAjaxDebug.debugWindowLogId+"' style='width: 100%; height: 30em; background-color: white; overflow: auto; white-space: nowrap'>"+
					"			</div>"+
					"		</div>"+					
					"	</div>" +
					"</div>" 
	            );
            WicketDrag.init(wicketGet(dwdhid), wicketGet(dwid));

			if (firstTime)            
	            document.write(
	            
	             "<div style='position:fixed; _position: absolute; left:10px; bottom: 10px; z-index:100;'>"
	            +"    <a href='javascript:WicketAjaxDebug.showDebugWindow()'>WICKET AJAX DEBUG</a>"
	            +"</div>");
        }
	}

};

WicketAjaxDebug.init();


