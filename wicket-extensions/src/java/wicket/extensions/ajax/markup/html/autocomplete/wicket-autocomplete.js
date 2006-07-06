function WicketAutoComplete(elementId,callbackUrl){
    var KEY_BACKSPACE=8;
    var KEY_TAB=9;
    var KEY_ENTER=13;
    var KEY_ESC=27;
    var KEY_LEFT=37;
    var KEY_UP=38;
    var KEY_RIGHT=39;
    var KEY_DOWN=40;
    
    var selected=-1;
    var elementCount=0;
    var visible=0;
    
    function initialize(){
        var obj=wicketGet(elementId);
        obj.onblur=function(event){
            hideAutoComplete();
        }
        
        obj.onkeydown=function(event){
            switch(wicketKeyCode(getEvent(event))){
                case KEY_UP:
        	        if(selected>-1)selected--;
            	    if(selected==-1){
    	           	    hideAutoComplete();
                   	} else {
	                    render();
        	        }
            	    if(navigator.appVersion.indexOf('AppleWebKit')>0)return killEvent(event);
                	break;
                case KEY_DOWN:
               		if(selected<elementCount-1){
                	    selected++;
	                }
    	            if(visible==0){
        	            updateChoices();
            	    } else {
                	    render();
                    	showAutoComplete();
	                }
    	            if(navigator.appVersion.indexOf('AppleWebKit')>0)return killEvent(event);
        	        break;
                case KEY_ESC:
            	    hideAutoComplete();
                	return killEvent(event);
                break;
                case KEY_ENTER:
	                if(selected>-1){
    	                obj.value=getSelectedValue();
        	            hideAutoComplete();
            	    }
                	return killEvent(event);
                break;
                default:
            }
        }
                
        obj.onkeyup=function(event){
            switch(wicketKeyCode(getEvent(event))){
                case KEY_ENTER:
	                return killEvent(event);
                case KEY_UP:
                case KEY_DOWN:
                case KEY_ESC:
                case KEY_TAB:
                case KEY_RIGHT:
                case KEY_LEFT:
                break;
                default:
    	            updateChoices();
            }
            return null;
        }
                
        obj.onkeypress=function(event){
            if(wicketKeyCode(getEvent(event))==KEY_ENTER){
                return killEvent(event);
            }
        }
    }
    
    function getMenuId() {
        return elementId+"-autocomplete";
    }
    
    function getAutocompleteMenu() {
        var choiceDiv = document.getElementById(getMenuId());
        if (choiceDiv == null) {
            choiceDiv = document.createElement("div");
            document.body.appendChild(choiceDiv);
            choiceDiv.id = getMenuId();
            choiceDiv.className = "wicket-aa";
            choiceDiv.style.display = "none";
            choiceDiv.style.position = "absolute";
            choiceDiv.style.zIndex = "10000";
        }
        
        choiceDiv.show = function() { wicketShow(this.id) }
        choiceDiv.hide = function() { wicketHide(this.id) }
        
        return choiceDiv;
    }
    
    function getEvent(event){
        if(!event)return window.event;
        return event;
    }
    
    function killEvent(event){
        if(!event)event=window.event;
        if(!event)return false;
        if(event.cancelBubble!=null){
            event.cancelBubble=true;
        }
        if(event.returnValue){
            event.returnValue=false;
        }
        if(event.stopPropagation){
            event.stopPropagation();
        }
        if(event.preventDefault){
            event.preventDefault();
        }
        return false;
    }
    
    function updateChoices(){
        selected=-1;
        var transport = wicketAjaxGetTransport();
        if (transport == null){
            if (wicketAjaxDebugEnabled()) {
                var log=WicketAjaxDebug.logError;
                log("Ajax-transport not available!");
            }
            return false;
        }
        
        var value=wicketGet(elementId).value;
        transport.open("GET",callbackUrl+"&random="+Math.random()+"&q="+processValue(value),true);
        transport.onreadystatechange = function () {
            if (transport.readyState == 4) {
                if (transport.status == 200) {
                    if (wicketAjaxDebugEnabled()) {
                        var log=WicketAjaxDebug.logInfo;
                        log("received ajax autocomplete response. "+transport.responseText.length+" characters.");
                        log("elementId="+getMenuId());
                        log(transport.responseText);
                    }
                    doUpdateChoices(transport.responseText);
                    } else {
                    if (wicketAjaxDebugEnabled()) {
                        var log=WicketAjaxDebug.logError;
                        log("received ajax response with code: "+transport.status);
                    }
                }
            }
        };
        transport.send(null);
    }

    function processValue(param) {
        var browserName = navigator.appName;
        if (browserName != "Microsoft Internet Explorer"){
            return param;
        }
        return encodeURIComponent(param);
    }
    
    function showAutoComplete(){
        var position=getPosition(wicketGet(elementId));
        var menu = getAutocompleteMenu();
        var input=wicketGet(elementId);
        menu.show();
        menu.style.left=position[0]+'px'
        menu.style.top=(input.offsetHeight+position[1])+'px';
        menu.style.width=input.offsetWidth+'px';
        visible=1;
        hideShowCovered();
    }
    
    function hideAutoComplete(){
        visible=0;
        selected=-1;
        getAutocompleteMenu().hide();
        hideShowCovered();
    }
    
    function getPosition(obj) {
        var leftPosition=0;
        var topPosition=0;
        do {
            topPosition += obj.offsetTop  || 0;
            leftPosition += obj.offsetLeft || 0;
            obj = obj.offsetParent;
        } while (obj);
        return [leftPosition,topPosition];
    }
    
    function doUpdateChoices(resp){
        var element = getAutocompleteMenu();
        element.innerHTML=resp;
        if(element.firstChild && element.firstChild.childNodes) {
            elementCount=element.firstChild.childNodes.length;
            } else {
            elementCount=0;
        }
        if(elementCount>0){
            showAutoComplete();
            } else {
            hideAutoComplete();
        }
        render();
    }
    
    function getSelectedValue(){
        var element = getAutocompleteMenu();
        var attr=element.firstChild.childNodes[selected].attributes['textvalue'];
        var value;
        if (attr==undefined) {
            value = element.firstChild.childNodes[selected].innerHTML;
            } else {
            value = attr.value;
        }
        return stripHTML(value);
    }
    
    function stripHTML(str) {
        return str.replace(/<[^>]+>/g,"");
    }
    
    function render(){
        var element= getAutocompleteMenu();
        for(var i=0;i<elementCount;i++){
            var node=element.firstChild.childNodes[i];
            var classNames = node.className.split(" ");
            for (var j=0; j<classNames.length; j++) {
                if (classNames[j] == 'selected') {
                    classNames[j] = '';
                }
            }
            
            if(selected==i){
                classNames.push('selected');
            }
            
            node.className = classNames.join(" ");
        }
    }
    
    // The following is borrowed and modified from calendar.js
    function hideShowCovered() {
        if (!/msie/i.test(navigator.userAgent) && !/opera/i.test(navigator.userAgent)) {
            return;
        }
        function getVisib(obj) {
            var value = obj.style.visibility;
            if (!value) {
                if (document.defaultView && typeof (document.defaultView.getComputedStyle) == "function") { // Gecko, W3C
                    value = document.defaultView.getComputedStyle(obj, "").getPropertyValue("visibility");
                    } else if (obj.currentStyle) { // IE
                    value = obj.currentStyle.visibility;
                    } else {
                    value = '';
                }
            }
            return value;
        }
        
        var tags = new Array("applet", "iframe", "select");
        var el = getAutocompleteMenu();
        
        var p = getPosition(el);
        var EX1 = p[0];
        var EX2 = el.offsetWidth + EX1;
        var EY1 = p[1];
        var EY2 = el.offsetHeight + EY1;
        
        for (var k = tags.length; k > 0; ) {
            var ar = document.getElementsByTagName(tags[--k]);
            var cc = null;
            
            for (var i = ar.length; i > 0;) {
                cc = ar[--i];
                
                p = getPosition(cc);
                var CX1 = p[0];
                var CX2 = cc.offsetWidth + CX1;
                var CY1 = p[1];
                var CY2 = cc.offsetHeight + CY1;
                
                if (this.hidden || (CX1 > EX2) || (CX2 < EX1) || (CY1 > EY2) || (CY2 < EY1)) {
                    if (!cc.__msh_save_visibility) {
                        cc.__msh_save_visibility = getVisib(cc);
                    }
                    cc.style.visibility = cc.__msh_save_visibility;
                    } else {
                    if (!cc.__msh_save_visibility) {
                        cc.__msh_save_visibility = getVisib(cc);
                    }
                    cc.style.visibility = "hidden";
                }
            }
        }
    }
    initialize();
} 