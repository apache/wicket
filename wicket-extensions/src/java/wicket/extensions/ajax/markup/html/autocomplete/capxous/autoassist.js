/*  AutoAssist JavaScript Widget, Version 0.6
 *  Written by Cheng Guangnan <chenggn@capxous.com>
 *  For details, see the AutoAssist web site: http://capxous.com/autoassist
/*--------------------------------------------------------------------------*/

var AutoAssist = Class.create();

AutoAssist.prototype = {

	version: "0.6",	
	status: "none",

	cssSelector: {
		main: "aa",
		highlight: "aa_highlight",
		wait: "aa_wait"
	},

	defaultOptions: {
		eventName: "onSelect"
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
		
		this.currentNodeIndex = -1;
		this.size = -1;
		this.setOptions(options);
		
		Event.observe(window, "click", this.hide.bindAsEventListener(this));
		Event.observe(this.txtBox, "dblclick", this.doRequest.bindAsEventListener(this));
	},
	
	setOptions: function(options) {
	    this.options = this.defaultOptions;
    	Object.extend(this.options, options || {});
	},

	onkeydown: function(evt) {
		switch (evt.keyCode) {
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
				if (this.status != "none") {
					this.select();
					return;
				} else {
				}
			default:
				if (this.timeoutId != 0) {
					clearTimeout(this.timeoutId);
				}
				var stat = "$('" + this.txtBox.id + "').autoassist.doRequest();";				
				this.timeoutId = setTimeout(stat, 500);
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
			
			var defaultOptions = {
				method: "get",
				onComplete: this.onComplete.bindAsEventListener(this),
				onFailure: this.onFailure.bindAsEventListener(this)
			}

			this.currentRequest = new Ajax.Updater(this.floatDiv, this.getURL(), defaultOptions);
		}
	},
	
	onFailure: function() {
	},
	
	onLoading: function() {
		this.cleanup();
		this.hide();
		Element.addClassName(this.txtBox, this.cssSelector.wait);
	},
	
	onComplete: function() {
		if (this.currentRequest.transport == arguments[0]) {
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

		this.status = "show";
		Element.show(this.floatDiv);
	},

	hide: function() {
		this.status = "none";
		Element.hide(this.floatDiv);
	}
}