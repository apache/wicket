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
 * Wicket Ajax Support 
 *
 * @author Igor Vaynberg
 * @author Matej Knopp 
 */

if (Function.prototype.bind == null) {
	Function.prototype.bind = function(object) {
		var __method = this;
		return function() {
			return __method.apply(object, arguments);
		}
	}
}

// Wicket Namespace

if (typeof(Wicket) == "undefined")
	Wicket = { };

Wicket.emptyFunction = function() { };

Wicket.Class = {
	create: function() {
		return function() {
			this.initialize.apply(this, arguments);
		}
	}
}

/**
 * Browser types
 */

Wicket.Browser = { 
	isKHTML: function() {
		return /Konqueror|KHTML/.test(navigator.userAgent) && !/Apple/.test(navigator.userAgent);
	},
	
	isSafari: function() {
		return /KHTML/.test(navigator.userAgent) && /Apple/.test(navigator.userAgent);
	},
	
	isOpera: function() {
		return typeof(window.opera) != "undefined";
	},

	isIE: function() {
		return typeof(document.all) != "undefined" && typeof(window.opera) == "undefined";
	},
	
	isIEQuirks: function() {
		// is the browser internet explorer in quirks mode (we could use document.compatMode too)		
		return Wicket.Browser.isIE() && document.documentElement.clientHeight == 0;
	},		
	
	isIE7: function() {
		var index = navigator.userAgent.indexOf("MSIE");
		var version = parseFloat(navigator.userAgent.substring(index + 5));
		return Wicket.Browser.isIE() && version >= 7;
	},
	
	isGecko: function() {
		return /Gecko/.test(navigator.userAgent) && !Wicket.Browser.isSafari();
	}
};

/**
 * Add a check for old Safari. It should not be our responsibility to check the 
 * browser's version, but it's a minor version that makes a difference here,
 * so we try to be at least user friendly.  
 */
if (typeof DOMParser == "undefined" && Wicket.Browser.isSafari()) {
   DOMParser = function () {}

   DOMParser.prototype.parseFromString = function (str, contentType) {
   		alert('You are using an old version of Safari.\nTo be able to use this page you need at least version 2.0.1.');
   }
}


/**
 * Logging functionality. 
 */

Wicket.Log = { 

	enabled: function() {
		return wicketAjaxDebugEnabled();
	},
	
	info: function(msg) {
	    if (Wicket.Log.enabled())
			WicketAjaxDebug.logInfo(msg);
	},
	
	error: function(msg) {
		if (Wicket.Log.enabled())
			WicketAjaxDebug.logError(msg);
	},  

	log: function(msg) {
		if(Wicket.Log.enabled())
			WicketAjaxDebug.log(msg);
	}
},

/**
 * Functions executer takes array of functions and executes them. Each function gets
 * the notify object, which needs to be called for the next function to be executed.
 * This way the functions can be executed asynchronously. Each function has to call
 * the notify object at some point, otherwise the functions after it wont be executed.
 * After the FunctionExecuter is initiatialized, the start methods triggers the
 * first function.
 */
Wicket.FunctionsExecuter = Wicket.Class.create();

Wicket.FunctionsExecuter.prototype = {
	initialize: function(functions) {
		this.functions = functions;
		this.current = 0;
		this.depth = 0; // we need to limit call stack depth
	},
	
	processNext: function() {
		if (this.current < this.functions.length) {
			var f = this.functions[this.current];
			var run = function() {
				f(this.notify.bind(this));
			}.bind(this);
			this.current++;
						
			if (this.depth > 50 || Wicket.Browser.isKHTML() || Wicket.Browser.isSafari()) {
				// to prevent khtml bug that crashes entire browser
				// or to prevent stack overflow (safari has small call stack)
				this.depth = 0;
				window.setTimeout(run, 1);
			} else {
				this.depth ++;
				run();
			}				
		}
	},	
	
	start: function() {
		this.processNext();
	},
	
	notify: function() {
		this.processNext();
	}
}

/**
 * A cross-browser method that replaces the markup of an element. The behavior
 * is similiar to calling element.outerHtml=text in internet explorer. However
 * this method also takes care of executing javascripts within the markup on
 * browsers that don't do that automatically.
 */
Wicket.replaceOuterHtml = function(element, text) {	
    if (element.outerHTML) { // internet explorer or opera support outerHtml
		var parent = element.parentNode;
       
		// find out the element's index and next element (if any). 
		// outerHtml can replace element with multiple elements and we need
		// to track the new elements in order to execute the script elements
		// they contain
		var i;
		var next = null; // next element will be stored here
		for (i = 0; i < parent.childNodes.length; ++i) {
			if (parent.childNodes[i] == element) {
				if (i != parent.childNodes.length - 1) {
       				next = parent.childNodes[i+1]
       			}
       			break;       			
       		}
		}
		
		// indicates whether we should manually invoke javascripts in the replaced content
		// (not necessary for opera when replacing elements inside table)
		var forceJavascriptExecution = true;
	   
		var tn = element.tagName;

		// elements inside tables have to be treated special
		if (tn != 'TBODY' && tn != 'TR' && tn != "TD" && tn != "THEAD") {
			// not inside table - regular replace
			element.outerHTML = text;						
		} else {	  		
			// this is a hack to get around the fact that internet explorer doesn't allow the
			// outerHtml attribute on table elements				
			var tempDiv = document.createElement("div");
			tempDiv.innerHTML = '<table style="display: none">' + text + '</table>';			
			element.parentNode.replaceChild(tempDiv.getElementsByTagName(tn).item(0), element);
						
			// this way opera already executes javascripts, so we don't want to execute javascripts later
			if (Wicket.Browser.isOpera())
				forceJavascriptExecution = false;				
		}
       
	    if (forceJavascriptExecution) {
			for (var j = i; j < parent.childNodes.length && parent.childNodes[j] != next; ++j) {	   		
				// execute the javascript contained by the newly created elements
				Wicket.Head.addJavascripts(parent.childNodes[j]);       
			}
		}

    } else {
    	// create range and fragment
        var range = element.ownerDocument.createRange();
        range.selectNode(element);
		var fragment = range.createContextualFragment(text);
		
		// get the elements to be added
		var elements = new Array();
		for (var i = 0; i < fragment.childNodes.length; ++i)
			elements.push(fragment.childNodes[i]);

        element.parentNode.replaceChild(fragment, element);        

		if (document.all != null) {
			for (var i in elements) {
				Wicket.Head.addJavascripts(elements[i]);
			}
		}
    }		
}	

/**
 * Decoding functionality
 *
 * Wicket sends rendered components and javascript as CDATA section of XML document. When the 
 * component body itself contains a CDATA section, Wicket needs to escape it properly. 
 */
Wicket.decode = function(encoding, text) {
    if (encoding == "wicket1") {
        return Wicket.decode1(text);
    }
}

Wicket.decode1 = function(text) {
    return Wicket.replaceAll(text, "]^", "]");
}

Wicket.replaceAll = function(str, from, to) {
    var idx = str.indexOf(from);
    while (idx > -1) {
        str = str.replace(from, to);
        idx = str.indexOf(from);
    }
    return str;
}

/**
 * Form serialization
 *
 * To post a form using Ajax Wicket first needs to serialize it, which means composing a string
 * from form elments names and values. The string will then be set as body of POST request.
 */

Wicket.Form = { }

Wicket.Form.encode = function(text) {
    if (encodeURIComponent) {
        return encodeURIComponent(text);
    } else {
        return escape(text);
    }
}

Wicket.Form.serializeSelect = function(select){
    var result = "";
    for (var i = 0; i < select.options.length; ++i) {
        var option = select.options[i];
        if (option.selected) {
            result += Wicket.Form.encode(select.name) + "=" + Wicket.Form.encode(option.value) + "&";
        }
    }
    return result;
}

// this function intentionally ignores image and submit inputs
Wicket.Form.serializeInput = function(input) {
    var type = input.type.toLowerCase();
    if ((type == "checkbox" || type == "radio") && input.checked) {
        return Wicket.Form.encode(input.name) + "=" + Wicket.Form.encode(input.value) + "&";
    } else if (type == "text" || type == "password" || type == "hidden" || type == "textarea") {
		return Wicket.Form.encode(input.name) + "=" + Wicket.Form.encode(input.value) + "&";
	} else {
		return "";
    }
}

// Returns url/post-body fragment representing element (e) 
Wicket.Form.serializeElement = function(e) {
    var tag = e.tagName.toLowerCase();
    if (tag == "select") {
        return Wicket.Form.serializeSelect(e);
    } else if (tag == "input" || tag == "textarea") {
        return Wicket.Form.serializeInput(e);
    } else {
    	return "";
    }
}

Wicket.Form.serialize = function(form) {
    var result = "";
    for (var i = 0; i < form.elements.length; ++i) {
        var e = form.elements[i];
        if (e.name && e.name != "" && !e.disabled) {
            result += Wicket.Form.serializeElement(e);
        }
    }
    return result;
}

/**
 * DOM nodes serialization functionality
 *
 * The purpose of these methods is to return a string representation
 * of the DOM tree.
 */
 
Wicket.DOM = { }

// Method for serializing DOM nodes to string
// original taken from Tacos (http://tacoscomponents.jot.com)
Wicket.DOM.serializeNodeChildren = function(node) {
	if (node == null) { 
		return "" 
	}
	var result = "";
	
	for (var i = 0; i < node.childNodes.length; i++) {
		var thisNode = node.childNodes[i];
		switch (thisNode.nodeType) {
			case 1: // ELEMENT_NODE
			case 5: // ENTITY_REFERENCE_NODE
				result += Wicket.DOM.serializeNode(thisNode);
				break;
			case 8: // COMMENT
				result += "<!--" + thisNode.nodeValue + "-->";
				break;
			case 4: // CDATA_SECTION_NODE
				result += "<![CDATA[" + thisNode.nodeValue + "]]>";
				break;				
			case 3: // TEXT_NODE
			case 2: // ATTRIBUTE_NODE
				result += thisNode.nodeValue;
				break;
			default:
				break;
		}
	}
	return result;	
}


Wicket.DOM.serializeNode = function(node){
	if (node == null) { 
		return "" 
	}
	var result = "";
	result += '<' + node.nodeName;
	
	if (node.attributes && node.attributes.length > 0) {
				
		for (var i = 0; i < node.attributes.length; i++) {
			result += " " + node.attributes[i].name 
				+ "=\"" + node.attributes[i].value + "\"";	
		}
	}
	
	result += '>';
	result += Wicket.DOM.serializeNodeChildren(node);
	result += '</' + node.nodeName + '>';
	return result;
}

// Utility function that determines whether given element is part of the current document
Wicket.DOM.containsElement = function(element) {
	var id = element.getAttribute("id");
	if (id != null)
		return document.getElementById(id) != null;
	else
		return false;
}

/**
 * Channel management
 *
 * Wicket Ajaax requests are organized in channels. A channel maintain the order of 
 * requests and determines, what should happen when a request is fired while another 
 * one is being processed. The default behavior (stack) puts the all subsequent requests 
 * in a queue, while the drop behavior limits queue size to one, so only the most
 * recent of subsequent requests is executed.
 * The name of channel determines the policy. E.g. chanel with name foochannel|s is 
 * a stack channel, while barchannel|d is a drop channel.
 *
 * The Channel class is supposed to be used through the ChannelManager.
 */
Wicket.Channel = Wicket.Class.create();
Wicket.Channel.prototype = {
	initialize: function(name) {
		var res = name.match(/^([^|]+)\|(d|s)$/)
		if (res == null)
			this.type ='s'; // default to stack 
		else
			this.type = res[2];
		this.callbacks = new Array();
		this.busy = false;
	},	
	
	schedule: function(callback) {
		if (this.busy == false) {
			this.busy = true;			
			return callback();
		} else {
			Wicket.Log.info("Chanel busy - postponing...");
			if (this.type == 's') // stack 
				this.callbacks.push(callback);
			else /* drop */
				this.callbacks[0] = callback;
			return null;				
		}
	},
	
	done: function() {
		var c = null;
		
		if (this.callbacks.length > 0) {
			c = this.callbacks.shift();
		}
			
		if (c != null && typeof(c) != "undefined") {
			Wicket.Log.info("Calling posponed function...");
			// we can't call the callback from this call-stack
			// therefore we set it on timer event
			window.setTimeout(c, 1);			
		} else {
			this.busy = false;
		}
	}
};

/**
 * Channel manager maintains a map of channels. 
 */
Wicket.ChannelManager = Wicket.Class.create();
Wicket.ChannelManager.prototype = {
	initialize: function() {
		this.channels = new Array();
	},
  
    // Schedules the callback to channel with given name.
	schedule: function(channel, callback) {
		var c = this.channels[channel];
		if (c == null) {
			c = new Wicket.Channel(channel);
			this.channels[channel] = c;
		}
		return c.schedule(callback);
	},
	
	// Tells the ChannelManager that the current callback in channel with given name 
	// has finished processing and another scheduled callback can be executed (if any).
	done: function(channel) {
		var c = this.channels[channel];
		if (c != null)
			c.done();
	}
};

// Default channel manager instance
Wicket.channelManager = new Wicket.ChannelManager();

/**
 * The Ajax class handles low level details of creating and pooling XmlHttpRequest objects,
 * as well as registering and execution of pre-call, post-call and failure handlers.
 */
 Wicket.Ajax = { 
 	// Creates a new instance of a XmlHttpRequest
	createTransport: function() {
	    var transport = null;
	    if (window.ActiveXObject) {
	        transport = new ActiveXObject("Microsoft.XMLHTTP");
	    } else if (window.XMLHttpRequest) {
	        transport = new XMLHttpRequest();
	    } 
	    
	    if (transport == null) {
	        Wicket.Log.error("Could not locate ajax transport. Your browser does not support the required XMLHttpRequest object or wicket could not gain access to it.");
	    }    
	    return transport;
	},
	
	transports: [],
	
	// Returns a transport from pool if any of them is not being used, or creates new instance
	getTransport: function() {
		var t = Wicket.Ajax.transports;
		for (var i = 0; i < t.length; ++i) {
			if (t[i].readyState == 0 || t[i].readyState == 4) {
				return t[i];
			}
		}
		t.push(Wicket.Ajax.createTransport());
		return t[t.length-1];		
	},
	
	preCallHandlers: [],
	postCallHandlers: [],	
	failureHandlers: [],
	
	registerPreCallHandler: function(handler) {
		var h = Wicket.Ajax.preCallHandlers;
		h.push(handler);
	},
	
	registerPostCallHandler: function(handler) {
		var h = Wicket.Ajax.postCallHandlers;
		h.push(handler);
	},
	
	registerFailureHandler: function(handler) {
		var h = Wicket.Ajax.failureHandlers;
		h.push(handler);
	},
	
	invokePreCallHandlers: function() {
		var h = Wicket.Ajax.preCallHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking pre-call handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	},
	
	invokePostCallHandlers: function() {
		var h = Wicket.Ajax.postCallHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking post-call handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	},

	invokeFailureHandlers: function() {
		var h = Wicket.Ajax.failureHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking failure handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	}
}

/**
 * The Ajax.Request class encapsulates a XmlHttpRequest. 
 */
Wicket.Ajax.Request = Wicket.Class.create();

Wicket.Ajax.Request.prototype = {
    // Creates a new request object.
	initialize: function(url, loadedCallback, parseResponse, randomURL, failureHandler, channel) {
		this.url = url;
		this.loadedCallback = loadedCallback;
		// whether we should give the loadedCallback parsed response (DOM tree) or the raw string
		this.parseResponse = parseResponse != null ? parseResponse : true; 
		this.randomURL = randomURL != null ? randomURL : true;
		this.failureHandler = failureHandler != null ? failureHandler : function() { };
		this.async = true;
		this.channel = channel;

		// when suppressDone is set, the loadedCallback is responsible for calling
		// Ajax.Request.done() to process possibly pendings requests in the channel.
		this.suppressDone = false;
		this.instance = Math.random();
		this.debugContent = true;
	},
	
	done: function() {
		Wicket.channelManager.done(this.channel);
	},
	
	createUrl: function() {
		if (this.randomURL == false)
			return this.url;
		else
			return this.url + "&random=" + Math.random();
	},
	
	log: function(method, url) {
		var log = Wicket.Log.info;
		log("");
		log("Initiating Ajax "+method+" request on " + url);
	},
	
	failure: function() {
		this.failureHandler();
   		Wicket.Ajax.invokePostCallHandlers();
   		Wicket.Ajax.invokeFailureHandlers();
	},
	
	// Executes a get request
	get: function() {
		if (this.channel != null) {
			var res = Wicket.channelManager.schedule(this.channel, this.doGet.bind(this));
			return res != null ? res : true;
		} else {
			return this.doGet();
		}
	},
	
	// The actual get request implementation
	doGet: function() {
		this.transport = Wicket.Ajax.getTransport();
	
		var url = this.createUrl();	
		this.log("GET", url);
		
		Wicket.Ajax.invokePreCallHandlers();
		
		var t = this.transport;
		if (t != null) {
			t.open("GET", url, this.async);
			t.onreadystatechange = this.stateChangeCallback.bind(this);
			// set a special flag to allow server distinguish between ajax and non-ajax requests
			t.setRequestHeader("Wicket-Ajax", "true");
			t.send(null);
			return true;
		} else {
			this.failure();
       		return false;
		}
	},
	
	// Posts the given string
	post: function(body) {
		if (this.channel != null) {
			var res = Wicket.channelManager.schedule(this.channel, function() { this.doPost(body); }.bind(this));
			return res != null ? res: true;
		} else {
			return doPost(this);
		}
	},
	
	// The actual post implementation
	doPost: function(body) {
		this.transport = Wicket.Ajax.getTransport();	
	
		var url = this.createUrl();	
		this.log("POST", url);
		
		Wicket.Ajax.invokePreCallHandlers();
		
		var t = this.transport;
		if (t != null) {
			t.open("POST", url, this.async);
			t.onreadystatechange = this.stateChangeCallback.bind(this);
			t.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			// set a special flag to allow server distinguish between ajax and non-ajax requests
			t.setRequestHeader("Wicket-Ajax", "true");
			t.send(body);
			return true;
		} else {
       		this.failure();
       		return false;
		}
	},
	
	// Method that processes the request states
	stateChangeCallback: function() {	
		var t = this.transport;

		if (t != null && t.readyState == 4) {
			if (t.status == 200) {		
				// response came without error
				var responseAsText = t.responseText;
				
				// first try to get the redirect header
				var redirectUrl;
				try {
					redirectUrl = t.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}
				
				// the redirect header was set, go to new url
				if (typeof(redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "") {
					t.onreadystatechange = Wicket.emptyFunction;
					window.location = redirectUrl;
				}
				else {
					// no redirect, just regular response
					var log = Wicket.Log.info;				
					log("Received ajax response (" + responseAsText.length + " characters)");
					if (this.debugContent != false) {
						log("\n" + responseAsText);
					}
	        		
	        		// parse the response if the callback needs a DOM tree
	        		if (this.parseResponse == true) {
						var xmldoc;					
						if (typeof(window.XMLHttpRequest) != "undefined" && typeof(DOMParser) != "undefined") {						
							var parser = new DOMParser();
							xmldoc = parser.parseFromString(responseAsText, "text/xml");						
						} else if (window.ActiveXObject) {
							xmldoc = t.responseXML;
						}
						// invoke the loaded callback with an xml document
						this.loadedCallback(xmldoc); 
					} else {
						// invoke the loaded callback with raw string
						this.loadedCallback(responseAsText);
					}        		
					if (this.suppressDone == false)
						this.done();
				}
        	} else {
        		// when an error happened
        		var log = Wicket.Log.error;
        		log("Received Ajax response with code: " + t.status);
		   		this.done();        		
        		this.failure();
        	}    	
        	t.onreadystatechange = Wicket.emptyFunction;
        	this.transport = null;       
        }        
	}
};


/**
 * Ajax call fires a Wicket Ajax request and processes the response. 
 * The response can contain 
 *   - javascript that should be invoked
 *   - body of components being replaced
 *   - header contributions of components
 */
Wicket.Ajax.Call = Wicket.Class.create();

Wicket.Ajax.Call.prototype = {
	// Initializes the Call
	initialize: function(url, successHandler, failureHandler, channel) {
		this.successHandler = successHandler != null ? successHandler : function() { };
		this.failureHandler = failureHandler != null ? failureHandler : function() { };

		var c = channel != null ? channel : "0|s"; // set the default channel if not specified
		// initialize the internal Ajax request
		this.request = new Wicket.Ajax.Request(url, this.loadedCallback.bind(this), true, true, failureHandler, c);
		this.request.suppressDone = true;
	},
	
	// On ajax request failure
	failure: function(message) {
		if (message != null)
			Wicket.Log.error("Error while parsing response: " + message);
		this.request.done();
		this.failureHandler();
   		Wicket.Ajax.invokePostCallHandlers();
   		Wicket.Ajax.invokeFailureHandlers();
	},	
	
	// Fires a get request
	call: function() {	
		return this.request.get();
	},
	
	// Fires a post request
	post: function(body) {
		return this.request.post(body);
	},

	// Submits a form using ajax.
	// This method serializes a form and sends it as POST body.
	submitForm: function(form, submitButton) {
	    var body = Wicket.Form.serialize(form);
	    if (submitButton != null) {
	        body += Wicket.Form.encode(submitButton) + "=1";
	    }
	    return this.request.post(body);
	},
	
	// Submits a form using ajax
	submitFormById: function(formId, submitButton) {
		var form = document.getElementById(formId);
		if (form == null || typeof (form) == "undefined")
			Wicket.Log.error("Trying to submit form with id '"+formId+"' that is not in document.");
		return this.submitForm(form, submitButton);
	},
	
	// Processes the response
	loadedCallback: function(envelope) {
		// To process the response, we go through the xml document and add a function for every action (step).
		// After this is done, a FunctionExecuter object asynchronously executes these functions.
		// The asynchronous execution is necessary, because some steps might involve loading external javascript,
		// which must be asynchronous, so that it doesn't block the browser, but we also have to maintain
		// the order in which scripts are loaded and we have to delay the next steps until the script is
		// loaded.
		try {			
			var root = envelope.getElementsByTagName("ajax-response")[0];
					
			// the root element must be <ajax-response	
		    if (root == null || root.tagName != "ajax-response") {
		    	this.failure("Could not find root <ajax-response> element");
		    	return;
		    }
						
			// iinitialize the array for steps (closures that execute each action)
		    var steps = new Array();

		    if (Wicket.Browser.isKHTML()) {
		    	// there's a nasty bug in KHTML that makes the browser crash
		    	// when the methods are delayed. Therefore we have to fire it
		    	// ASAP. The javascripts that would cause dependency problems are
		    	// loaded synchronously in konqueror.
			    steps.push = function(method) {
			    	method(function() { });
			    }
			}
			
			// go through the ajax response and for every action (component, js evaluation, header contribution)
			// ad the proper closure to steps
		    for (var i = 0; i < root.childNodes.length; ++i) {
		    	var node = root.childNodes[i];				

		        if (node.tagName == "component") {
		           this.processComponent(steps, node);
		        } else if (node.tagName == "evaluate") {
		           this.processEvaluation(steps, node);
		        } else if (node.tagName == "header-contribution") {
		           this.processHeaderContribution(steps, node);
		        }
		        
		    }

			// add the last step, which should trigger the success call the done method on request
			this.success(steps);
		    
		    if (Wicket.Browser.isKHTML() == false) {
			    Wicket.Log.info("Response parsed. Now invoking steps...");		    		   		    
			    var executer = new Wicket.FunctionsExecuter(steps);
			    executer.start();		    
		    }		    
		} catch (e) {
			this.failure(e.message);
		}
	},
	
	// Adds a closure to steps that should be invoked after all other steps have been successfully executed
	success: function(steps) {
		steps.push(function(notify) {
			Wicket.Log.info("Response processed successfully.");			
			Wicket.Ajax.invokePostCallHandlers();
			this.request.done();
			this.successHandler();	
			// continue to next step (which should make the processing stop, as success should be the final step)		
			notify();			
		}.bind(this));
	},

	// Adds a closure that replaces a component	
	processComponent: function(steps, node) {
		steps.push(function(notify) {
			// get the component id
			var compId = node.getAttribute("id");
			var text="";

			// get the new component body
			if (node.hasChildNodes()) {
				text = node.firstChild.nodeValue;
			}

			// if the text was escaped, unascape it
			// (escaping is done when the component body contains a CDATA section)
			var encoding = node.getAttribute("encoding");
			if (encoding != null && encoding!="") {
				text = Wicket.decode(encoding, text);
			}
			
			// get existing component
			var element = document.getElementById(compId);

			if (element == null || typeof(element) == "undefined") {			
				Wicket.Log.error("Component with id [["+compId+"]] a was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.");
			} else {
				// replace the component
				Wicket.replaceOuterHtml(element, text);
			}
			// continue to next step
			notify();
		});
	},
	
	// Adds a closure that evaluates javascript code
	processEvaluation: function(steps, node) {
		steps.push(function(notify) {
			// get the javascript body
		    var text = node.firstChild.nodeValue;
		    
		    // unescape it if necessary
		    var encoding = node.getAttribute("encoding");
		    if (encoding != null) {
		        text = Wicket.decode(encoding, text);
		    }
		    try {
		   		// do the evaluation
		    	eval(text);
		    } catch (exception) {
		    	Wicket.Log.error("Exception evaluating javascript: " + exception);
		    }
		    // continue to next step
			notify();
		});
	},
	
	// Adds a closure that processes a header contribution
	processHeaderContribution: function(steps, node) {
		var c = new Wicket.Head.Contributor();
		c.processContribution(steps, node);
	}
};

/**
 * Header contribution allows component to include custom javascript and stylesheet. 
 *
 * Header contributor takes the code component would render to page head and
 * interprets it just as browser would when loading a page. 
 * That means loading external javascripts and stylesheets, executing inline 
 * javascript and aplying inline styles. 
 *
 * Header contributor also filters duplicate entries, so that it doesn't load/process
 * resources that have been loaded.
 * For inline styles and javascript, element id is used to filter out duplicate entries.
 * For stylesheet and javascript references, url is used for filtering.
 */
Wicket.Head = { };

Wicket.Head.Contributor = Wicket.Class.create();

Wicket.Head.Contributor.prototype = {
	initialize: function() {
	},
	
	// Parses the header contribution element (returns a DOM tree with the contribution)
	parse: function(headerNode) {
		// the header contribution is stored as CDATA section in the header-contribution element.
		// even though we need to parse it (and we have aleady parsed the response), header
		// contribution needs to be treated separately. The reason for this is that
		// Konqueror crashes when it there is a <script element in the parsed string. So we
		// need to replace that first
		
		// get the header contribution text and unescape it if necessary
		var text = headerNode.firstChild.nodeValue;	
	    var encoding = headerNode.getAttribute("encoding");
	    
	    if (encoding != null && encoding != "") {
	        text = Wicket.decode(encoding, text);        
	    }       
	    
	    if (Wicket.Browser.isKHTML()) {
			// konqueror crashes if there is a <script element in the xml, but <SCRIPT is fine. 
			text = text.replace(/<script/g,"<SCRIPT");
			text = text.replace(/<\/script>/g,"</SCRIPT>");	
		}
				
		// build a DOM tree of the contribution
		var xmldoc;
		if (window.ActiveXObject) {
	        xmldoc = new ActiveXObject("Microsoft.XMLDOM");
			xmldoc.loadXML(text);
		} else {
		    var parser = new DOMParser();    
		    xmldoc = parser.parseFromString(text, "text/xml");	
		}	
		
		return xmldoc;	
	},
	
	// Processes the parsed header contribution
	processContribution: function(steps, headerNode) {
		var xmldoc = this.parse(headerNode);
		var rootNode = xmldoc.documentElement;

		// go through the individual elements and process them according to their type
		for (var i = 0; i < rootNode.childNodes.length; i++) {
			var node = rootNode.childNodes[i];			
			if (node.tagName != null) {
				var name = node.tagName.toLowerCase();
				
				// it is possible that a reference is surrounded by a <wicket:link
				// in that case, we need to find the inner element
				if (name == "wicket:link") {					
					for (var j = 0; j < node.childNodes.length; ++j) {
						var childNode = node.childNodes[j];
						// try to find a regular node inside wicket:link
						if (childNode.nodeType == 1) {
							node = childNode;
							name = node.tagName.toLowerCase();
							break;
						}					
					}					
				}
						
				// process the element
			    if (name == "link") {
					this.processLink(steps, node);
				} else if (name == "script") {
					this.processScript(steps, node);
				} else if (name == "style") {
					this.processStyle(steps, node);
				}
			}
		}	
	},
	
	// Process an external stylesheet element
	processLink: function(steps, node) {		
		steps.push(function(notify) {
			// if the element is already in head, skip it
			if (Wicket.Head.containsElement(node, "href")) {
				notify();
				return;
			}
			// create link element
			var css = Wicket.Head.createElement("link");

			// copy required attributes
			css.id = node.getAttribute("id");
			css.rel = node.getAttribute("rel");
			css.href = node.getAttribute("href");
			css.type = node.getAttribute("type");
			
			// add element to head
			Wicket.Head.addElement(css);
			
			// continue to next step
			notify();
		});
	},
	
	// Process an inline style element
	processStyle: function(steps, node) {
		steps.push(function(notify) {
			// if element with same id is already in document, skip it
			if (Wicket.DOM.containsElement(node)) {
				notify();
				return;
			}	
			// serialize the style to string
			var content = Wicket.DOM.serializeNodeChildren(node);
			
			// create style element
			var style = Wicket.Head.createElement("style");
			
			// copy id attribute
			style.id = node.getAttribute("id");										
				
			// create stylesheet
			if (Wicket.Browser.isIE()) { 			
				document.createStyleSheet().cssText = content;
			} else {			
				var textNode = document.createTextNode(content);
				style.appendChild(textNode);
			} 		
			Wicket.Head.addElement(style);
			
			// continue to next step
			notify();
		});
	},
	
	// Process a script element (both inline and external)
	processScript: function(steps, node) {
		steps.push(function(notify) {		
			// if element in same id is already in document, 
			// or element with same src attribute is in document, skip it
			if (Wicket.DOM.containsElement(node) ||
				Wicket.Head.containsElement(node, "src")) {
				notify(); 
				return;
			}
			
			// determine whether it is external javascript (has src attribute set)
			var src = node.getAttribute("src");
			if (src != null && src != "") {
				// load the external javascript using Wicket.Ajax.Request
				
				// callback when script is loaded
				var onLoad = function(content) {
					Wicket.Head.addJavascript(content, null, src);
					Wicket.Ajax.invokePostCallHandlers();

					// continue to next step
					notify();
				}
				// we need to schedule the request as timeout
				// calling xml http request from another request call stack doesn't work
				window.setTimeout(function() {
					var req = new Wicket.Ajax.Request(src, onLoad, false, false);
					req.debugContent = false;
					if (Wicket.Browser.isKHTML())
						// konqueror can't process the ajax response asynchronously, threfore the 
						// javascript loading must be also synchronous
						req.async = false;
					// get the javascript
					req.get();					
				},1);
			} else {
				// serialize the element content to string
				var text = Wicket.DOM.serializeNodeChildren(node);
				
				// add javascript to document head
				Wicket.Head.addJavascript(text, node.getAttribute("id"));
				
				// continue to next step
				notify();
			}
		});					
	}	
};

/**
 * Head manipulation
 */

// Creates an element in document
Wicket.Head.createElement = function(name) {
	return document.createElement(name);
}

// Adds the element to page head
Wicket.Head.addElement = function(element) {
	var head = document.getElementsByTagName("head");

	if (head[0]) {
		head[0].appendChild(element);
	}
}

// Returns true, if the page head contains element that has attribute with
// name mandatoryAttribute same as the given element and their names match.
//
// e.g. Wicket.Head.containsElement(myElement, "src") return true, if there
// is an element in head that is of same type as myElement, and whose src
// attribute is same as myElement.src.
Wicket.Head.containsElement = function(element, mandatoryAttribute) {
	var attr = element.getAttribute(mandatoryAttribute);
	if (attr == null || attr == "" || typeof(attr) == "undefined")
		return false;

	var head = document.getElementsByTagName("head")[0];
	var nodes = head.getElementsByTagName(element.tagName);
	for (var i = 0; i < nodes.length; ++i) {
		var node = nodes[i];		
		// check node names and mandatory attribute values
		// we also have to check for attribute name that is suffixed by "_".
		// this is necessary for filtering script references
		if (node.tagName.toLowerCase() == element.tagName.toLowerCase() &&
			(node.getAttribute(mandatoryAttribute) == attr ||
		     node.getAttribute(mandatoryAttribute+"_") == attr)) {
		    return true;
		}
	}
	return false;
}

// Adds a javascript element to page header. 
// The fakeSrc attribute is used to filter out duplicate javascript references.
// External javascripts are loaded using xmlhttprequest. Then a javascript element is created and the
// javascript body is used as text for the element. Fori javascript references, wicket uses the src 
// attribute to filter out duplicates. However, since we set the body of the element, we can't assign
// also a src value. Therefore we put the url to the src_ (notice the underscore)  attribute.
// Wicket.Head.containsElement is aware of that and takes also the underscored attributes into account.
Wicket.Head.addJavascript = function(content, id, fakeSrc) {
	var script = Wicket.Head.createElement("script");
	script.id = id;
	script.setAttribute("src_", fakeSrc);
	
	// set the javascript as element content
	if (null == script.canHaveChildren || script.canHaveChildren) {
		var textNode = document.createTextNode(content);			
		script.appendChild(textNode);
	} else {
		script.text = content;
	} 		
	Wicket.Head.addElement(script);	
}

// Goes through all script elements contained by the element and add them to head
Wicket.Head.addJavascripts = function(element) {	
	function add(element) {
		var content = Wicket.DOM.serializeNodeChildren(element);
		if (content == null || content == "")
			content = element.text;
		Wicket.Head.addJavascript(content);		
	}
	if (typeof(element) != "undefined" &&
	    typeof(element.tagName) != "undefined" &&
	    element.tagName.toLowerCase() == "script") {
		add(element);
	} else {
		// we need to check if there are any children, becase Safari
		// aborts when the element is a text node			
		if (element.childNodes.length > 0) {			
			var scripts = element.getElementsByTagName("script");
			for (var i = 0; i < scripts.length; ++i) {
				add(scripts[i]);
			}
		}
	}
}

/**
 * Throttler's purpose is to make sure that ajax requests wont be fired too often.
 */

Wicket.ThrottlerEntry = Wicket.Class.create();
Wicket.ThrottlerEntry.prototype = {
	initialize: function(func) {
		this.func = func;
		this.timestamp = new Date().getTime();
	},
	
	getTimestamp: function() {
		return this.timestamp;
	},
	
	getFunc: function() {
		return this.func;
	},
	
	setFunc: function(func) {
		this.func = func;
	}
};

Wicket.Throttler = Wicket.Class.create();
Wicket.Throttler.prototype = {
	initialize: function() {
		this.entries = new Array();
	},
	
	throttle: function(id, millis, func) {
		var entry = this.entries[id];
		var me = this;
		if (entry == undefined) {
			entry = new Wicket.ThrottlerEntry(func);
			this.entries[id] = entry;
			window.setTimeout(function() { me.execute(id); }, millis);
		} else {
			entry.setFunc(func);
		}	
	},
	
	execute: function(id) {
		var entry = this.entries[id];
		if (entry != undefined) {
			var func = entry.getFunc();
			var tmp = func();
		}
		
		this.entries[id] = undefined;	
	}
};

Wicket.throttler = new Wicket.Throttler();

/**
 * Events related code
 * Based on code from Mootools (http://mootools.net)
 */

Wicket.Event = {
	// adds an event of specified type to the element
	// also supports the domready event on window
	// domready is event fired when the DOM is complete, but before loading external resources (images, ...)
	add: function(element, type, fn) {
		// is the event domready?
		if (element == window && type == "domready") {
			Wicket.Event.addDomReadyEvent(fn);
		} else {
			if (element.addEventListener){
				element.addEventListener((type == 'mousewheel' && window.gecko) ? 'DOMMouseScroll' : type, fn, false);
			} else {
				fn = fn.bind(element);
				element.attachEvent('on'+type, fn);
			}
		}
		return element;
	},
	
	// handlers that will be fired on dom ready event
	domReadyHandlers : new Array(),
	
	// fires the dom ready event and cleanup the handlers
	fireDomReadyHandlers : function() {
		var h = Wicket.Event.domReadyHandlers;
		while (h.length > 0) {
			var c = h.shift();
			c();
		}
		Wicket.Event.domReadyHandlers = null;
	},
	
	// adds the dom ready event 
	addDomReadyEvent : function(fn) {
		// is the window already loaded?
		if (window.loaded)  {
			fn();
		} else if (!window.events || !window.events.domready) {
			// register the handler
			Wicket.Event.domReadyHandlers.push(fn);
		
			// callback
			var domReady = function() {
				if (window.loaded) 
					return;
				window.loaded = true;
				
				// if there was a timer, clean it (khtml, safari)
				if (Wicket.Event.domReadyTimer) {
					clearTimeout(Wicket.Event.domReadyTimer);
					Wicket.Event.domReadyTimer = null;
				}
				
				// invoke the handlers
				Wicket.Event.fireDomReadyHandlers();
			}.bind(this);
			
			if (document.readyState && (Wicket.Browser.isKHTML() || Wicket.Browser.isSafari())) { 
			   //safari and konqueror don't support the event - simulate it through a timeou
				Wicket.Event.domReadyTimer = window.setTimeout(function() {
					if (document.readyState == "loaded" ||
					    document.readyState == "complete") {
					    domReady();
					}
				}, 1);
			} else if (document.readyState && Wicket.Browser.isIE()) { 
				// internet explorer - use script with defer attribute
				document.write("<script id=ie_ready defer src=javascript:void(0)><\/script>");
				document.getElementById('ie_ready').onreadystatechange = function() {
					if (this.readyState == 'complete') domReady();
				};
			} else { 
				// other browsers
				Wicket.Event.add(document, "DOMContentLoaded", domReady);
			}
		} else {
			window.addEventListener("domready", fn, false);
		}
	}
}

/*
 * Compatibility layer to maintain the original wicket-ajax API.
 */

var wicketThrottler = Wicket.throttler;

function wicketAjaxGet(url, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.call();
}

function wicketAjaxPost(url, body, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.post(body);
}

function wicketSubmitForm(form, url, submitButton, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.submitForm(form, submitButton);
}

function wicketSubmitFormById(formId, url, submitButton, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.submitFormById(formId, submitButton);
}

wicketSerialize = Wicket.Form.serializeElement;

wicketSerializeForm = Wicket.Form.serialize;

wicketEncode = Wicket.Form.encode;

wicketDecode = Wicket.decode;

wicketAjaxGetTransport = Wicket.Ajax.getTransport;

// Global handlers stubs

Wicket.Ajax.registerPreCallHandler(function() {
	if (typeof(window.wicketGlobalPreCallHandler) != "undefined") {
	    var global=wicketGlobalPreCallHandler;
	    if (global!=null) {
	    	global();
	    }
	}    
});

Wicket.Ajax.registerPostCallHandler(function() {
	if (typeof(window.wicketGlobalPostCallHandler) != "undefined") {
	    var global=wicketGlobalPostCallHandler;
	    if (global!=null) {
	    	global();
	    }
	}    
});

Wicket.Ajax.registerFailureHandler(function() {
	if (typeof(window.wicketGlobalFailureHandler) != "undefined") {
	    var global=wicketGlobalFailureHandler;
	    if (global!=null) {
	    	global();
	    }
	}    
});

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
    var e=wicketGet(id);
    e.style.display = "";
}

function wicketHide(id) {
    var e=wicketGet(id);
    e.style.display = "none";
}

