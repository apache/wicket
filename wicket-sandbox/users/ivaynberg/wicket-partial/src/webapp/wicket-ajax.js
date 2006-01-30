// MISC FUNCTIONS
function wicketShow(id) {
    document.getElementById(id).style.display="";
}

function wicketHide(id) {
    document.getElementById(id).style.display="none";
}


 
// AJAX FUNCTIONS
function wicketAjaxGet(url, successHandler) {

    var transport=null; 

    if (window.XMLHttpRequest) {
        transport=new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        transport=new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    transport.onreadystatechange = function() { wicketAjaxOnStateChange(transport, successHandler) };
    transport.open("GET", url+"&random="+Math.random(), true);
    transport.send(null);
    
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