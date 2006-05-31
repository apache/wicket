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
		}

		obj.onkeypress=function(event){
			if(wicketKeyCode(getEvent(event))==KEY_ENTER){
				return killEvent(event);
			}
		}

		var choiceDiv = document.createElement("div");
		document.body.appendChild(choiceDiv);
		choiceDiv.id = elementId+"-autocomplete";
		choiceDiv.className = "wicket-aa";
		choiceDiv.style.display = "none";
		choiceDiv.style.position = "absolute";
		choiceDiv.style.zIndex = "900";
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
	    transport.open("GET",callbackUrl+"&random="+Math.random()+"&q="+value,true);
 		transport.onreadystatechange = function () {
			if (transport.readyState == 4) {
				if (transport.status == 200) {
				    if (wicketAjaxDebugEnabled()) {
				                var log=WicketAjaxDebug.logInfo;
        				        log("received ajax autocomplete response. "+transport.responseText.length+" characters.");
				                log("elementId="+elementId+"-autocomplete");
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

	function showAutoComplete(){
		var position=getPosition(wicketGet(elementId));		
		var obj=wicketGet(elementId+'-autocomplete');
		var input=wicketGet(elementId);	
		wicketShow(elementId+'-autocomplete');				
		obj.style.left=position[0]+'px'
		obj.style.top=(input.offsetHeight+position[1])+'px';
		obj.style.width=input.offsetWidth+'px';			
		visible=1;
	}

	function hideAutoComplete(){	
		visible=0;
		selected=-1;	
		wicketHide(elementId+'-autocomplete');
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
		var element=wicketGet(elementId+'-autocomplete');
		element.innerHTML=resp;
		showAutoComplete();	
	    if(element.firstChild && element.firstChild.childNodes) {
	    	elementCount=element.firstChild.childNodes.length;
    	} else {
    		elementCount=0;
    	}
    	render();
	}
	
	function getSelectedValue(){
		var element=wicketGet(elementId+'-autocomplete');
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
		var element=wicketGet(elementId+'-autocomplete');
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
		
	initialize();
}
