/*  AutoAssist JavaScript Widget, Version 0.6.5
 *  Written by Cheng Guangnan <chenggn@capxous.com>, support provided by the Support Team <support@capxous.com>
 *  For details, see the AutoAssist web site: http://capxous.com/autoassist
/*--------------------------------------------------------------------------*/

Object.extend(Event, {
  KEY_PAGE_UP:		33,
  KEY_PAGE_DOWN:	34,
  KEY_END:			35, 
  KEY_HOME:			36, 
  KEY_INSERT:		45,
  KEY_SHIFT:		16,
  KEY_CTRL:			17,
  KEY_ALT:			18
});

var AutoAssist = Class.create();

AutoAssist.prototype = {

	version: "0.6.5",	

	requestNo: 0,
	responseNo: 0,
	visible: false,

	cssSelector: {
		main: "aa",
		highlight: "aa_highlight",
		wait: "aa_wait"
	},

	defaultOptions: {
		eventName: "onSelect",
		typing_timeout: 618
	},
	
	initialize: function(txtBox, getURL, options) {
		this.txtBox = $(txtBox);
		this.txtBox.onkeydown = this.onkeydown.bindAsEventListener(this);
		this.txtBox.autoassist = this;
		this.timeoutId = 0;

		this.getURL = getURL;
	
		this.floatDiv = document.createElement("div");
		this.floatDiv.style.position = "absolute";
		Element.addClassName(this.floatDiv, this.cssSelector.main);
		Element.hide(this.floatDiv);
		document.body.appendChild(this.floatDiv);
		
		this.bufferDiv = document.createElement("div");
		Element.addClassName(this.bufferDiv, this.cssSelector.main);
		
		this.currentNodeIndex = -1;
		this.size = -1;
		this.setOptions(options);
		
		Event.observe(document, "click", this.hide.bindAsEventListener(this));
		Event.observe(this.txtBox, "dblclick", this.doRequest.bindAsEventListener(this));
	},
	
	setOptions: function(options) {
	    this.options = this.defaultOptions;
    	Object.extend(this.options, options || {});
	},

	onkeydown: function(evt) {
		switch (evt.keyCode) {
			case Event.KEY_TAB:
			case Event.KEY_LEFT:
			case Event.KEY_RIGHT:
			case Event.KEY_PAGE_UP:
			case Event.KEY_PAGE_DOWN:
			case Event.KEY_END:
			case Event.KEY_HOME:
			case Event.KEY_INSERT:
			case Event.KEY_SHIFT:
			case Event.KEY_CTRL:
			case Event.KEY_ALT:
				return;			
			case Event.KEY_ESC:
				this.hide();
				return;
			case Event.KEY_UP:
				this.up();
				this.show();
				return;
			case Event.KEY_DOWN:
				this.down();
				this.show();
				return;	
			case Event.KEY_RETURN:
				if (this.visible) {
					this.select();
					return;
				}
			default:
				this.log("evt.keyCode:" + evt.keyCode);
				if (this.timeoutId != 0) {
					clearTimeout(this.timeoutId);
				}
				this.timeoutId = setTimeout(this.doRequest.bind(this), this.options.typing_timeout);
				this.hide();
		}		
	},
	
	select: function() {
		if (this.currentNode()) {
			var stat = this.currentNode().getAttribute(this.options.eventName);
			try { eval(stat); } catch (e) {};
			this.hide();
		}
	},
	
	currentNode: function() {
		if (this.children) {
			return this.children[this.currentNodeIndex];
		} else {
			return undefined;
		}
	},
	
	highlight: function(h) {
		if (this.currentNode()) {
			Element.removeClassName(this.currentNode(), this.cssSelector.highlight);
		}
		this.currentNodeIndex = h;
		if (this.currentNode()) {
			Element.addClassName(this.currentNode(), this.cssSelector.highlight);
		}
	},
	
	up: function() {		
		if (this.currentNodeIndex > -1) this.highlight(this.currentNodeIndex - 1);
	},

	down: function() {		
		if (this.currentNodeIndex < this.size - 1) this.highlight(this.currentNodeIndex + 1);
	},
	
	cleanup: function() {
		this.size = 0;
		this.currentNodeIndex = -1;
		this.floatDiv.innerHTML = "";
		this.bufferDiv.innerHTML = "";		
	},
		
	isValidNode:function(n) {
		return (n.nodeType == 1) && (n.getAttribute(this.options.eventName) != undefined);
	},
	
	preRequest: function() {
		if (this.txtBox.value.length == 0) return false;
		return true;
	},
	
	getURL: function() {
	},
	
	doRequest: function() {
		if (this.preRequest()) {
			this.onLoading();
			var updaterOptions = {
				method: "get",
				onComplete: this.onComplete.bindAsEventListener(this),
				onFailure: this.onFailure.bindAsEventListener(this)
			}
			var URL = this.getURL();			
			this.currentRequest = new Ajax.Updater(this.bufferDiv, URL, updaterOptions);
			this.requestNo++;
			this.log(URL);
		}
	},
	
	onException: function() {
		this.log("onException");
	},
	
	onFailure: function() {
		this.log("onFailure");	
	},
	
	onLoading: function() {
		this.cleanup();
		this.hide();
		Element.addClassName(this.txtBox, this.cssSelector.wait);
	},
	
	onComplete: function() {
		setTimeout(this.updateContent.bind(this, arguments[0]), 10);
	},
	
	updateContent: function() {
		this.responseNo++;		
		this.log(this.requestNo + "/" + this.responseNo);
		
		var tx = ((this.currentRequest == null) || (this.currentRequest.transport == arguments[0]));
		this.log(tx);
		if (tx) {	
			this.floatDiv.innerHTML = this.bufferDiv.innerHTML;
			this.size = 0;
			this.currentNodeIndex = -1;
	
			var children = this.floatDiv.childNodes;
			this.children = new Array();
			
			for (var i = 0; i < children.length; i++) {
				var item = children[i];
				if (this.isValidNode(item)) {
					var f = new Function("this.highlight(" + this.size + "); ");
					Event.observe(item, "mouseover", f.bindAsEventListener(this));		
					Event.observe(item, "click", this.select.bindAsEventListener(this));
					this.children.push(item);
					this.size++;					
				}
			}		
			this.down();
			this.show();
			
			Element.removeClassName(this.txtBox, this.cssSelector.wait);
		}
	},
	
	show: function() {
		this.floatDiv.style.width = this.txtBox.offsetWidth - 8 + "px";
		var p = Position.cumulativeOffset(this.txtBox);
		this.floatDiv.style.top = p[1] + this.txtBox.offsetHeight + "px";
		this.floatDiv.style.left = p[0]+ "px";

		this.visible = true;
		Element.show(this.floatDiv);
	},
	
	hide: function() {
		this.visible = false;
		Element.hide(this.floatDiv);
	},
	
	log: function(msg) {
	try {
		$("log").value = msg + "<br/>\n" + $("log").value;
	} catch (e) {}
	}
}