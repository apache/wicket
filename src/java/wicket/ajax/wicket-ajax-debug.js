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

    log : function(msg) {
        var ta=wicketGet(WicketAjaxDebug.debugWindowLogId);
        ta.value=ta.value+"\n"+msg;
        ta.scrollTop = ta.scrollHeight;
    },
    
    logError : function(msg) {
        WicketAjaxDebug.log("ERROR: "+msg);
    },

    logInfo : function(msg) {
        WicketAjaxDebug.log(" INFO: "+msg);
    },

    clearLog : function() {
        var ta=wicketGet(WicketAjaxDebug.debugWindowLogId);
        ta.value=null;
    },

	init : function() {

        if ( wicketAjaxDebugEnabled()) {
        	var wad=WicketAjaxDebug;
        	var dwid=wad.debugWindowId;
        	var dwdhid=wad.debugWindowDragHandleId;

			var firstTime = document.getElementById(dwid) == null;

			if (firstTime)
	            document.write(
	        	
	        	 "<table id='"+dwid+"' cellspacing='0' cellpadding='2'"
	        	+"    style='display:none; position: absolute; left:300px; top:300px; width: 600px; z-index:100;"
	        	+"    background-color: white; border: 2px solid #333; padding: 5px;' "
	        	+"    ><tr>"
	        	
	        	+"    <td id='"+dwdhid+"' "
	        	+"        style='cursor:move;"
	        	+"        color: white; background-color: navy;'"
	        	+"        >"
	        	
	        	+"        Wicket Ajax Debug Window (drag me here)"
	        	+"    </td>"
	        	+"    <td style='text-align: right;'>"
	        	+"        <a href='javascript:WicketAjaxDebug.clearLog()'>clear</a> | "
	        	+"        <a href='javascript:WicketAjaxDebug.hideDebugWindow()'>close</a>"
	        	+"     </td> </tr><td colspan='2'>"
	        	    
	        	+"    <form>"
	        	    
	        	+"        <textarea cols='60' rows='20' wrap='off' "
	        	+"            name='"+WicketAjaxDebug.debugWindowLogId+"' id='"+WicketAjaxDebug.debugWindowLogId+"' "
	        	+"            style='width:100%;' "
	        	+"        ></textarea>"
	        	    
	        	+"    </form>"
	        	+"</td></tr></table>");
            
            WicketDrag.init(wicketGet(dwdhid), wicketGet(dwid));

			if (firstTime)            
	            document.write(
	            
	             "<div style='position:absolute; left:10px; top:95%; z-index:100;'>"
	            +"    <a href='javascript:WicketAjaxDebug.showDebugWindow()'>WICKET AJAX DEBUG</a>"
	            +"</div>");
        }
	}

};

WicketAjaxDebug.init();


