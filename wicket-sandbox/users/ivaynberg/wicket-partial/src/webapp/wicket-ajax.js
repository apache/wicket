function wicketAjaxGet(url) {

    var transport=null; 

    if (window.XMLHttpRequest) {
        transport=new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        transport=new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    transport.onreadystatechange = function() { wicketAjaxOnStateChange(transport) };
    transport.open("GET", url+"&random="+Math.random(), true);
    transport.send(null);
    
}

function wicketAjaxOnStateChange(transport) {
    if (transport.readyState==4) {
        if (transport.status==200) {
            wicketAjaxProcess(transport.responseXML);
        }
    }
}

function wicketAjaxProcess(envelope) {
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