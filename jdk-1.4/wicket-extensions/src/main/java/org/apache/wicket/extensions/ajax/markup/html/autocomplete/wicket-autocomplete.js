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
/*
 * Wicket Ajax Autocomplete
 *
 * @author Janne Hietam&auml;ki
 */

if (typeof(Wicket) == "undefined")
	Wicket = { };

Wicket.AutoCompleteSettings =  {
	enterHidesWithNoSelection : false
};

Wicket.AutoComplete=function(elementId, callbackUrl, preselect){
    var KEY_TAB=9;
    var KEY_ENTER=13;
    var KEY_ESC=27;
    var KEY_LEFT=37;
    var KEY_UP=38;
    var KEY_RIGHT=39;
    var KEY_DOWN=40;
    var KEY_SHIFT=16;
    var KEY_CTRL=17;
    var KEY_ALT=18;

    var selected=-1; 	// index of the currently selected item
    var elementCount=0; // number of items on the auto complete list
    var visible=0;		// is the list visible
    var mouseactive=0;	// is mouse selection active
	var	hidingAutocomplete=0;		// are we hiding the autocomplete list

	// pointers of the browser events
   	var objonkeydown;
	var objonblur;
	var objonkeyup;
	var objonkeypress;
	var objonchange;
	var objonchangeoriginal;

    function initialize(){    	
        var obj=wicketGet(elementId);

        objonkeydown=obj.onkeydown;
        objonblur=obj.onblur;
        objonkeyup=obj.onkeyup;
        objonkeypress=obj.onkeypress;
        
        // WICKET-1280
        objonchangeoriginal=obj.onchange; 
        obj.onchange=function(event){
      		if(mouseactive==1)return false;
      		if(typeof objonchangeoriginal=="function")objonchangeoriginal();
      	}
        objonchange=obj.onchange;
                
      	obj.onblur=function(event){      		
    		if(mouseactive==1){
    			Wicket.$(elementId).focus();
    			return false;
    		}
          	hideAutoComplete();
        }
      	
        obj.onkeydown=function(event){
            switch(wicketKeyCode(Wicket.fixEvent(event))){
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
          		        hidingAutocomplete=1;
					} else if (Wicket.AutoCompleteSettings.enterHidesWithNoSelection==true) {
 			            hideAutoComplete();
          		        hidingAutocomplete=1;
					}
	                mouseactive=0;
		            if(typeof objonkeydown=="function")objonkeydown();
    				if(typeof objonchange=="function")objonchange();

	                if(selected>-1){
	                	//return killEvent(event);
            	    }
            	    return true;
                break;
                default:
            }
        }

        obj.onkeyup=function(event){
            switch(wicketKeyCode(Wicket.fixEvent(event))){
                case KEY_ENTER:
	                return killEvent(event);
                case KEY_UP:
                case KEY_DOWN:
                case KEY_ESC:
                case KEY_TAB:
                case KEY_RIGHT:
                case KEY_LEFT:
                case KEY_SHIFT:
                case KEY_ALT:
                case KEY_CTRL:
                break;
                default:
    	            updateChoices();
            }
			if(typeof objonkeyup=="function")objonkeyup();
            return null;
        }

        obj.onkeypress=function(event){
            if(wicketKeyCode(Wicket.fixEvent(event))==KEY_ENTER){
                if(selected>-1 || hidingAutocomplete==1){
			        hidingAutocomplete=0;
	                return killEvent(event);
                }
            }
			if(typeof objonkeypress=="function")objonkeypress();
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
            
            // WICKET-1350/WICKET-1351
            choiceDiv.onmouseout=function() {mouseactive=0;};
            choiceDiv.onmouseover=function() {mouseactive=1;};
        }

        choiceDiv.show = function() { wicketShow(this.id) }
        choiceDiv.hide = function() { wicketHide(this.id) }

        return choiceDiv;
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
        if(preselect==true){
        	selected = 0;
        }
        else{
        	selected=-1;
        }
        var value = wicketGet(elementId).value;
       	var request = new Wicket.Ajax.Request(callbackUrl+"&q="+processValue(value), doUpdateChoices, false, true, false, "wicket-autocomplete|d");
       	request.get();
    }

    function processValue(param) {
        return (encodeURIComponent)?encodeURIComponent(param):escape(param);
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
        hidingAutocomplete=1;
        visible=0;
        selected=-1;
        if ( document.getElementById(getMenuId()) )
        {
	        getAutocompleteMenu().hide();
    	    hideShowCovered();
        }
    }

    function getPosition(obj) {
        var leftPosition=0;
        var topPosition=0;
        do {
            topPosition += obj.offsetTop || 0;
            leftPosition += obj.offsetLeft || 0;
            obj = obj.offsetParent;
        } while (obj);
        return [leftPosition,topPosition];
    }

    function doUpdateChoices(resp){
    
    	// check if the input hasn't been cleared in the meanwhile
    	var input=wicketGet(elementId);
   		if (input.value==null || input.value=="") {
   			hideAutoComplete();
   			return;
   		}
    
        var element = getAutocompleteMenu();
        element.innerHTML=resp;
        if(element.firstChild && element.firstChild.childNodes) {
            elementCount=element.firstChild.childNodes.length;

            for(var i=0;i<elementCount;i++){
	            var node=element.firstChild.childNodes[i];

				node.onclick = function(event){
					mouseactive=0;
					wicketGet(elementId).value=getSelectedValue();
					if(typeof objonchange=="function")objonchange();
					hideAutoComplete();
       			}

				node.onmouseover = function(event){					
					selected = getElementIndex(this);
					render();
				 	showAutoComplete();
				}
       		}
        } else {
            elementCount=0;
        }

        if(elementCount>0){
            showAutoComplete();
        } else {
            hideAutoComplete();
        }
        render();
        
        scheduleEmptyCheck();
    }
    
    function scheduleEmptyCheck() {
    	window.setTimeout(function() {
    		var input=wicketGet(elementId);
    		if (input.value==null || input.value=="") {
    			hideAutoComplete();
    		}
    	}, 100);
    }

    function getSelectedValue(){
        var element=getAutocompleteMenu();
        var attr=element.firstChild.childNodes[selected].attributes['textvalue'];
        var value;
        if (attr==undefined) {
            value=element.firstChild.childNodes[selected].innerHTML;
            } else {
            value=attr.value;
        }
        return stripHTML(value);
    }

    function getElementIndex(element) {
		for(var i=0;i<element.parentNode.childNodes.length;i++){
	        var node=element.parentNode.childNodes[i];
			if(node==element)return i;
		}
		return -1;
    }

    function stripHTML(str) {
        return str.replace(/<[^>]+>/g,"");
    }

    function render(){
        var element= getAutocompleteMenu();
        for(var i=0;i<elementCount;i++){
            var node=element.firstChild.childNodes[i];

            var classNames=node.className.split(" ");
            for (var j=0; j<classNames.length; j++) {
                if (classNames[j]=='selected') {
                    classNames[j]='';
                }
            }

            if(selected==i){
                classNames.push('selected');
                node.scrollIntoView(true);
            }

            node.className=classNames.join(" ");
        }
    }


    function isVisible(obj) {
		var value = obj.style.visibility;
		if (!value) {
			if (document.defaultView && typeof(document.defaultView.getComputedStyle)=="function") {
				value=document.defaultView.getComputedStyle(obj,"").getPropertyValue("visibility");
			} else if (obj.currentStyle) {
				value = obj.currentStyle.visibility;
			} else {
				value='';
			}
		}
		return value;
	}

    function hideShowCovered(){
        if (!/msie/i.test(navigator.userAgent) && !/opera/i.test(navigator.userAgent)) {
            return;
        }
        // IE7 fix, if this doesn't go in a timeout then the complete page could become invisible.
        // when closing the popup.
		setTimeout(hideShowCoveredTimeout,1);
    }
    
    function hideShowCoveredTimeout(){
		var el=getAutocompleteMenu();
        var p=getPosition(el);

        var acLeftX=p[0];
        var acRightX=el.offsetWidth+acLeftX;
        var acTopY=p[1];
        var acBottomY=el.offsetHeight+acTopY;

        var hideTags=new Array("select","iframe","applet");

        for (var j=0;j<hideTags.length;j++) {
            var tagsFound=document.getElementsByTagName(hideTags[j]);
            for (var i=0; i<tagsFound.length; i++){
                var tag=tagsFound[i];
                p=getPosition(tag);
                var leftX=p[0];
                var rightX=leftX+tag.offsetWidth;
                var topY=p[1];
                var bottomY=topY+tag.offsetHeight;

                if (this.hidden || (leftX>acRightX) || (rightX<acLeftX) || (topY>acBottomY) || (bottomY<acTopY)) {
                    if(!tag.wicket_element_visibility) {
                        tag.wicket_element_visibility=isVisible(tag);
                    }
                    tag.style.visibility=tag.wicket_element_visibility;
				} else {
					if (!tag.wicket_element_visibility) {
						tag.wicket_element_visibility=isVisible(tag);
					}
                    tag.style.visibility = "hidden";
                }
            }
        }        
    }

    initialize();
}