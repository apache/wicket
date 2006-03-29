// DEBUG FUNCTIONS
function wicketAjaxDebugEnabled() {
    if (typeof(wicketAjaxDebugEnable)=="undefined") {
        return false;
    } else {
        return wicketAjaxDebugEnable==true;
    }
}

// MISC FUNCTIONS
function wicketKeyCode(event) {
    if (typeof(event.keyCode)=="undefined") {
        return event.which;
    } else {
        return event.keyCode;
    }
}

function wicketGet(id) {
    return document.getElementById(id);
}

function wicketShow(id) {
    wicketGet(id).style.display = "inline";
}
function wicketHide(id) {
    wicketGet(id).style.display = "none";
}



// AJAX FUNCTIONS
function wicketAjaxGetTransport() {
    var transport = null;
    if (window.XMLHttpRequest) {
        transport = new XMLHttpRequest();
    } else {
        if (window.ActiveXObject) {
            transport = new ActiveXObject("Microsoft.XMLHTTP");
        }
    }
    
    if (transport==null&&wicketAjaxDebugEnabled()) {
        var log=WicketAjaxDebug.logError;
        log("Could not locate ajax transport. Your browser does not support the required XMLHttpRequest object or wicket could not gain access to it.");
    }    
    return transport;
}

function wicketAjaxGet(url, successHandler, failureHandler) {
    if (wicketAjaxDebugEnabled()) {
        var log=WicketAjaxDebug.logInfo;
        log("");
        log("initiating ajax GET request with...");
        log("url: "+url);
        log("successHandler:"+successHandler);
        log("failureHandler:"+failureHandler);
    }
   
    var transport = wicketAjaxGetTransport();
    if (transport == null) {
        return false;
    }
    transport.onreadystatechange = function () {
        wicketAjaxOnStateChange(transport, successHandler, failureHandler);
    };
    transport.open("GET", url + "&random=" + Math.random(), true);
    transport.send(null);
    return true;
}
function wicketAjaxPost(url, body, successHandler, failureHandler) {
    if (wicketAjaxDebugEnabled()) {
        var log=WicketAjaxDebug.logInfo;
        log("");
        log("initiating ajax POST request with...");
        log("url: "+url);
        log("body: "+body);
        log("successHandler:"+successHandler);
        log("failureHandler:"+failureHandler);
    }
   
    var transport = wicketAjaxGetTransport();
    if (transport == null) {
        return false;
    }
    transport.onreadystatechange = function () {
        wicketAjaxOnStateChange(transport, successHandler, failureHandler);
    };
    transport.open("POST", url + "&random=" + Math.random(), true);
    transport.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    transport.send(body);
    return true;
}
function wicketSubmitForm(form, url, submitButton, successHandler, failureHandler) {
    var body = wicketSerializeForm(form);
    if (submitButton != null) {
        body += wicketEncode(submitButton) + "=1";
    }
    return wicketAjaxPost(url, body, successHandler, failureHandler);
}
function wicketSubmitFormById(formId, url, submitButton, successHandler, failureHandler) {
    var form = document.getElementById(formId);
    return wicketSubmitForm(form, url, submitButton, successHandler, failureHandler);
}
function wicketAjaxOnStateChange(transport, successHandler, failureHandler) {
    if (transport.readyState == 4) {
        if (transport.status == 200) {
            if (wicketAjaxDebugEnabled()) {
                var log=WicketAjaxDebug.logInfo;
                log("received ajax response. "+transport.responseText.length+" characters, envelope following...");
                log("");
                log(transport.responseText);
            }
            wicketAjaxProcess(transport.responseXML, successHandler, failureHandler);
        } else {
            if (wicketAjaxDebugEnabled()) {
                var log=WicketAjaxDebug.logError;
                log("received ajax response with code: "+transport.status);
            }
			wicketAjaxCallFailureHandler(failureHandler);
		}        
    }
}
function wicketAjaxProcess(envelope, successHandler, failureHandler) {
	try {
	    var root = envelope.getElementsByTagName("ajax-response");
	    root = root[0];
	    if (root == null || root.tagName != "ajax-response") {
	        if (wicketAjaxDebugEnabled()) {
	            var log=WicketAjaxDebug.logError;
	            log("malformed response envelope: could not find root <ajax-response> element");
	        }
	       	wicketAjaxCallFailureHandler(failureHandler);
	    }
	    for (var i = 0; i < root.childNodes.length; i++) {
	        var node = root.childNodes[i];
	        if (node.tagName == "component") {
	            wicketAjaxProcessComponent(node);
	        } else if (node.tagName == "evaluate") {
	                wicketAjaxProcessEvaluation(node);
	        }
	    }
	    
	    if (wicketAjaxDebugEnabled()) {
	        var log=WicketAjaxDebug.logInfo;
	        log("response envelope successfully processed");
	    }
	    
	    
	    if (successHandler!=undefined && successHandler != null) {
	        if (wicketAjaxDebugEnabled()) {
	            var log=WicketAjaxDebug.logInfo;
	            log("invoking success handler...");
	        }
	        successHandler();
	    } 
	
	    if (wicketAjaxDebugEnabled()) {
	        var log=WicketAjaxDebug.logInfo;
	        log("request successfully processed");
	    }
	} catch (e) {
		if (wicketAjaxDebugEnabled()) {
			var log=WicketAjaxDebug.logError;
			log("error while processing response: "+e);
		}
		wicketAjaxCallFailureHandler(failureHandler);
	}
	    
}

function wicketAjaxCallFailureHandler(failureHandler) {
	if (failureHandler!=undefined && failureHandler!=null) {
		if (wicketAjaxDebugEnabled()) {
			var log=WicketAjaxDebug.logInfo;
			log("invoking failure handler...");
		}
		failureHandler();
	}
	
	if (wicketGlobalAjaxErrorHandler!=undefined&&wicketGlobalAjaxErrorHandler!=null) {
		if (wicketAjaxDebugEnabled()) {
			var log=WicketAjaxDebug.logInfo;
			log("invoking wicketGlobalAjaxErrorHandler failure handler...");
		}
		wicketGlobalAjaxErrorHandler();
	}
}

function wicketAjaxProcessComponent(node) {
    var compId = node.getAttribute("id");
    
    var text="";
    if (node.hasChildNodes()) {
       text = node.firstChild.nodeValue;
    }
  
    var encoding = node.getAttribute("encoding");
    if (encoding != null&&encoding!="") {
        text = wicketDecode(encoding, text);
    }
    var element=document.getElementById(compId);
    if (element.outerHTML) {
        element.outerHTML = text;
    } else {
        var range = element.ownerDocument.createRange();
        range.selectNode(element);
        element.parentNode.replaceChild(
        range.createContextualFragment(text), element);
    }
    
}
function wicketAjaxProcessEvaluation(node) {
    var text = node.firstChild.nodeValue;
    var encoding = node.getAttribute("encoding");
    if (encoding != null) {
        text = wicketDecode(encoding, text);
    }
    eval(text);
}
function wicketDecode(encoding, text) {
    if (encoding == "wicket1") {
        return wicketDecode1(text);
    }
}
function wicketDecode1(text) {
    return wicketReplaceAll(text, "]^", "]");
}
function wicketReplaceAll(str, from, to) {
    var idx = str.indexOf(from);
    while (idx > -1) {
        str = str.replace(from, to);
        idx = str.indexOf(from);
    }
    return str;
}


//FORM SERIALIZATION FUNCTIONS
function wicketEncode(text) {
    if (encodeURIComponent) {
        return encodeURIComponent(text);
    } else {
        return escape(text);
    }
}
function wicketSerializeSelect(select) {
    var result = "";
    for (var i = 0; i < select.options.length; ++i) {
        var option = select.options[i];
        if (option.selected) {
            result += wicketEncode(select.name) + "=" + wicketEncode(option.value) + "&";
        }
    }
    return result;
}

// this function intentionally ignores image and submit inputs
function wicketSerializeInput(input) {
    var type = input.type.toLowerCase();
    if ((type == "checkbox" || type == "radio") && input.checked) {
        return wicketEncode(input.name) + "=" + wicketEncode(input.value) + "&";
    } else {
        if (type == "text" || type == "password" || type == "hidden" || type == "textarea") {
            return wicketEncode(input.name) + "=" + wicketEncode(input.value) + "&";
        } else {
            return "";
        }
    }
}

// returns url/post-body fragment representing element (e) 
function wicketSerialize(e) {
    var tag = e.tagName.toLowerCase();
    if (tag == "select") {
        return wicketSerializeSelect(e);
    } else {
        if (tag == "input" || tag == "textarea") {
            return wicketSerializeInput(e);
        }
    }
}
function wicketSerializeForm(form) {
    var result = "";
    for (var i = 0; i < form.elements.length; ++i) {
        var e = form.elements[i];
        if (!e.disabled) {
            result += wicketSerialize(e);
        }
    }
    return result;
}

// THROTTLE FUNCTIONS
function WicketThrottlerEntry(func) {
	this.func=func;
	this.timestamp=new Date().getTime();
}

WicketThrottlerEntry.prototype.getTimestamp=function() {
	return this.timestamp;
}

WicketThrottlerEntry.prototype.getFunc=function() {
	return this.func;
}

WicketThrottlerEntry.prototype.setFunc=function(func) {
	this.func=func;
}


function WicketThrottler() {
	this.entries=new Array();
}

WicketThrottler.prototype.throttle=function(id, millis, func) {
	var entry=this.entries[id];
	var me=this;
	if (entry==undefined) {
		entry=new WicketThrottlerEntry(func);
		this.entries[id]=entry;
		window.setTimeout(function() { me.execute(id); }, millis);
	} else {
		entry.setFunc(func);
	}
}

WicketThrottler.prototype.execute=function(id) {
	var entry=this.entries[id];
	if (entry!=undefined) {
		var func=entry.getFunc();
		var tmp=func();
	}
	
	this.entries[id]=undefined;
}

var wicketThrottler=new WicketThrottler();
