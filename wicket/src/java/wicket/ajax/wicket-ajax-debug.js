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


	init : function() {
        if ( wicketAjaxDebugEnabled() ) {
        	var wad=WicketAjaxDebug;
        	var dwid=wad.debugWindowId;
        	var dwdhid=wad.debugWindowDragHandleId;
        	
            document.write(
        	
        	 "<div id='"+dwid+"' "
        	+"    style='display:none; position: absolute; left:300px; top:300px; width: 600px; "
        	+"    padding: 5px; background-color: white; border: 2px solid #333;' "
        	+"    >"
        	
        	+"    <div id='"+dwdhid+"' "
        	+"        style='padding: 2px 10px 2px 10px; cursor:move; float:left;"
        	+"        color: white; background-color: navy;'"
        	+"        >"
        	
        	+"        Wicket Ajax Debug Window (drag me here)"
        	+"    </div>"
        	+"    <div style='float:right;'>"
        	+"        <a href='javascript:WicketAjaxDebug.hideDebugWindow()'>close</a>"
        	+"     </div> <div style='float:reset'></div>"
        	    
        	+"    <form>"
        	    
        	+"        <textarea cols='60' rows='20' wrap='off' "
        	+"            name='"+WicketAjaxDebug.debugWindowLogId+"' id='"+WicketAjaxDebug.debugWindowLogId+"' "
        	+"            style='width:100%;' "
        	+"        ></textarea>"
        	    
        	+"    </form>"
        	+"</div>");
            
            WicketDrag.init(wicketGet(dwdhid), wicketGet(dwid));
            
            document.write(
            
             "<div style='position:absolute; left:10px; top:95%;'>"
            +"    <a href='javascript:WicketAjaxDebug.showDebugWindow()'>WICKET AJAX DEBUG</a>"
            +"</div>");
        }
	}

};

WicketAjaxDebug.init();


