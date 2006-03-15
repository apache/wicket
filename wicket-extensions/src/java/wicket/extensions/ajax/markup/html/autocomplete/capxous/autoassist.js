/*  AutoAssist JavaScript Widget, version 0.5.3
 *  (c) Cheng Guangnan <chenggn@capxous.com>

 *  For details, see the AutoAssist web site: http://capxous.com/autoassist
/*--------------------------------------------------------------------------*/

var AutoAssist = Class.create();

AutoAssist.prototype = {
	Version: "0.5.3",	
	CSS_AutoAssist: "AutoAssist",
	CSS_Highlight: "Highlight",
	CSS_Loading: "Loading",
	
	status: "none",
	
	initialize: function(txtBox, options) {
		this.txtBox = $(txtBox);
		this.txtBox.onkeydown = this.onkeydown.bindAsEventListener(this);
		this.txtBox.autoassist = this;
		this.timeoutId = 0;
		this.check_javascript = "$('" + this.txtBox.id + "').autoassist.request()";
	
		this.floatDiv = document.createElement("div");
		this.floatDiv.style.position = "absolute";
		Element.addClassName(this.floatDiv, this.CSS_AutoAssist);
		Element.hide(this.floatDiv);
		document.body.appendChild(this.floatDiv);
		
		this.currentNodeIndex = -1;
		this.size = -1;
		this.setOptions(options);
		
		Event.observe(window, "click", this.hide.bindAsEventListener(this));
		Event.observe(this.txtBox, "dblclick", this.request.bindAsEventListener(this));
	},
	
	setOptions: function(options) {
	    this.options = {
		    eventName: "onSelect",
		    setRequestOptions: Prototype.emptyFunction
	    };
    	Object.extend(this.options, options || {});
	},
	
	request: function() {
		this.busy();		
		this.clean();
		
		var defaultOptions = {
			url : "",
			method: "get",
			onComplete: this.onComplete.bindAsEventListener(this)
		}
				
		Object.extend(defaultOptions, (this.options.setRequestOptions.bindAsEventListener(this)()) || {});
        var myAjax = new Ajax.Updater(this.floatDiv, defaultOptions.url, defaultOptions); 	
	},
	
	busy: function() {
		Element.addClassName(this.txtBox, this.CSS_Loading);
	},

	ready: function() {
		Element.removeClassName(this.txtBox, this.CSS_Loading);
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
			case Event.KEY_LEFT:
			case Event.KEY_RIGHT:
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
				this.timeoutId = setTimeout(this.check_javascript, 500);
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
			Element.removeClassName(this.currentNode(), this.CSS_Highlight);
		}
		this.currentNodeIndex = h;
		if (this.currentNode()) {
			Element.addClassName(this.currentNode(), this.CSS_Highlight);
		}
	},
	
	up: function() {		
		if (this.currentNodeIndex > -1) this.highlight(this.currentNodeIndex - 1);
	},

	down: function() {		
		if (this.currentNodeIndex < this.size - 1) this.highlight(this.currentNodeIndex + 1);
	},
	
	clean: function() {
		this.size = 0;
		this.currentNodeIndex = -1;
		this.floatDiv.innerHTML = "";
	},
		
	isValidNode:function(n) {
		return (n.nodeType == 1);
	},
	
	onComplete: function() {		
		this.size = 0;
		this.currentNodeIndex = -1;

		var children = this.floatDiv.childNodes;
		this.children = new Array();
		
		for (var i = 0; i < children.length; i++) {
			var n = children[i];
			if (this.isValidNode(n)) {
				var f = new Function("this.highlight(" + this.size + "); ");
				Event.observe(n, "mouseover", f.bindAsEventListener(this));		
				Event.observe(n, "click", this.select.bindAsEventListener(this));
				this.size++;
				this.children.push(n);
			}
		}		
		this.down();
		this.show();
		this.ready();
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