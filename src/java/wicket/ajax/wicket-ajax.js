// MISC FUNCTIONS
function wicketShow(id) {
    document.getElementById(id).style.display="";
}

function wicketHide(id) {
    document.getElementById(id).style.display="none";
}
 
function wicketGetValue(comp) {
    if (comp.type=="checkbox") {
        return comp.checked;
    } else {
        return comp.value;
    }
}

 
// AJAX FUNCTIONS
function wicketAjaxGet(url, successHandler) {

    var transport=null; 

    if (window.XMLHttpRequest) {
        transport=new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        transport=new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    if (transport==null) {
        return false;
    }
    
    transport.onreadystatechange = function() { wicketAjaxOnStateChange(transport, successHandler) };
    transport.open("GET", url+"&random="+Math.random(), true);
    transport.send(null);
    
    return true;
}

function wicketAjaxOnStateChange(transport, successHandler) {
    if (transport.readyState==4) {
        if (transport.status==200) {
            wicketAjaxProcess(transport.responseXML, successHandler);
        }
    }
}

function wicketAjaxProcess(envelope, successHandler) {
    var root=envelope.getElementsByTagName("ajax-response");
    root=root[0];
    
    if (root==null||root.tagName!="ajax-response") {
        //TODO handle error properly
        alert('error, ajas-response element not found');
    }
    
    for (var i=0;i<root.childNodes.length;i++) {
        var node=root.childNodes[i];
        if (node.tagName=="component") {
            wicketAjaxProcessComponent(node);
        } else if (node.tagName=="evaluate") {
            wicketAjaxProcessEvaluation(node);
        }
    }
    
    if (successHandler!=null) {
        successHandler();
    }
}

function wicketAjaxProcessComponent(node) {
    var compId=node.getAttribute("id");
    var text=node.firstChild.nodeValue;
    document.getElementById(compId).innerHTML=text;
}

function wicketAjaxProcessEvaluation(node) {
    var text=node.firstChild.nodeValue;
    eval(text);
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
	if ((type == "checkbox" || type == "radio") && input.checked)
		return wicketEncode(input.name) + "=" + wicketEncode(input.value) + "&";
	else if (type == "text" || type == "password" || type=="hidden" || type=="textarea")
		return wicketEncode(input.name) + "=" + wicketEncode(input.value) + "&" ;
	else return "";
}

// returns url/post-body fragment representing element (e) 
function wicketSerialize(e) {
	var tag = e.tagName.toLowerCase();
	if (tag == "select") {
		return wicketSerializeSelect(e);
	} else if (tag == "input" || tag == "textarea") {
		return wicketSerializeInput(e);
	}
}

function wicketSerializeForm(form) {
	var result = "";
	for (var i = 0; i < form.elements.length; ++i) {
		var e = form.elements[i];
		if (!e.disabled) {
            result+=wicketSerialize(e);
		}
	}
	return result;
}