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
					if(visible==0)updateChoices();					
					if(selected<elementCount-1){
						selected++;
					}
					render();
					showAutoComplete();		
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
	    var transport = wicketAjaxGetTransport();
	    if (transport == null)return false
    	transport.onreadystatechange = function () {
			if (transport.readyState == 4) {
				if (transport.status == 200) {
					doUpdateChoices(transport.responseText);
		        } else {
        	    	if (wicketAjaxDebugEnabled()) {
	             	   var log=WicketAjaxDebug.logError;
    	            	log("received ajax response with code: "+transport.status);
   	            	}
        	    }
	        }            	
	    };	    
	    var value=wicketGet(elementId).value;
	    transport.open("GET",callbackUrl+"&q="+value,true);
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
		if(obj.offsetParent){
			do{
				topPosition+=obj.offsetTop;
				leftPosition+=obj.offsetLeft;
				obj=obj.offsetParent;
			} while(obj);
		} else {
			if(obj.y)topPosition=obj.y;
			if(obj.x)leftPosition=obj.x;
		}
		return [leftPosition,topPosition];
	}


	
	function doUpdateChoices(resp){
		var element=wicketGet(elementId+'-autocomplete');
		element.innerHTML=resp;
		selected=-1;
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
		var ret=element.firstChild.childNodes[selected].innerHTML;
		return ret;
	}
	
	function render(){	
		var element=wicketGet(elementId+'-autocomplete');
    	for(var i=0;i<elementCount;i++){
    		var node=element.firstChild.childNodes[i];
			if(selected==i){
	    		node.className='selected';
	    	} else {
	    		node.className='';
	    	}
    	}    		
	}
		
	initialize();
}
