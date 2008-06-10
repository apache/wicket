(function() {
		
	var oldWicket = window.Wicket;	
	
	Wicket = { };

	Wicket.$ = function(arg) 
	{
		if (arg == null || typeof(arg) == "undefined") 
		{
			return null;
		}
		if (arguments.length > 1) 
		{
			var e=[];
			for (var i=0; i<arguments.length; i++) 
			{
				e.push(Wicket.$(arguments[i]));
			}
			return e;
		} 
		else if (typeof arg == 'string') 
		{
			return document.getElementById(arg);
		} 
		else 
		{
			return arg;
		}
	}

	var bind = function(method, object) {		
		return function() {
			return method.apply(object, arguments);
		}
	}
	
	/**
	 * Returns true if the argument element belongs to the current document.
	 * If the argument is a string, returns whether the document contains element with given id.
	 * If the argument is neither element nor a string function returns true.
	 */
	Wicket.$$ = function(element) 
	{	
		if (typeof(element) == "string") 
		{		
			element = Wicket.$(element);
		}	
		if (element == null || typeof(element) == "undefined" ||
		    element.tagName == null || typeof(element.tagName) == "undefined") 
		{
		    return true;
		}
		
		var id = element.getAttribute('id');
		if (typeof(id) == "undefined" || id == null || id == "")
			return element.ownerDocument == document;
		else
			return document.getElementById(id) == element;
	}
	
	var ua = YAHOO.env.ua;
	
	var DummyLogger = 
	{
		trace: function() { },
		debug: function() { },
		info: function() { 	},
		error: function() { }
	}
	
	var prepend = function(element, array)
	{
		var res = new Array();
		res.push(element);
		for (var i = 0; i < array.length; ++i)
		{
			res.push(array[i]);
		}
		return res;
	}
	
	var FirebugLogger =
	{
		trace: function()
		{			
			console.debug.apply(this, prepend("TRACE:", arguments));
		},
		debug: function() 
		{
			console.debug.apply(this, prepend("DEBUG:", arguments));
		},
		info: function() 
		{
			console.info.apply(this, prepend("INFO:", arguments));
		},
		error: function() 
		{
			console.error.apply(this, prepend("ERROR:", arguments));
		}
	}
	
	var logger = DummyLogger;
	
	if (ua.gecko && typeof(console) !== "undefined" && logger === DummyLogger)
	{
		logger = FirebugLogger;
	}	
	
	var logConfig = { trace: true, debug: true, info: true, error: true };
	
	var l = 
	{
		trace: function()
		{
			if (logConfig.trace)
				logger.trace.apply(this, arguments);
		},
		debug: function()
		{
			if (logConfig.debug)
				logger.debug.apply(this, arguments);
		},
		info: function()
		{
			if (logConfig.info)
				logger.info.apply(this, arguments);
		},
		error: function()
		{
			if (logConfig.error)
				logger.error.apply(this, arguments);
		}
	};
		
	Wicket.Log = l;
	
	/**
	 * YAHOO event cleanups the listeners only on page unload. However, if the page lives long
	 * enough the elements removed from document that have listener attached cause IE GC not free the
	 * memory. So we manually register each element with listener and then periodically check 
	 * whether the element is still in document. If it's not the element's listeners are removed.
	 */
	var elementsWithListeners = new Array();

	var purgeInactiveListeners = function() 
	{
		var c = 0;
		var a = elementsWithListeners;
		for (var i = 0; i < a.length; ++i) 
		{
			var e = a[i];
			if (e != null && Wicket.$$(e) == false) 
			{		
				l.trace("Events: Purging listeners from element ", e);
				E.purgeElement(e);
				a[i] = null;
				++c;
			}
		}
		if (c > 0)
		{
			l.debug("Purged listeners from " + c + " element(s) removed from the document.");
		}		
	};

	window.setInterval(purgeInactiveListeners, 60000);	
	
	var E = YAHOO.util.Event;
	
	var oldAddListener = E.addListener;
	
	/**
	 * Intercept the YAHOO.util.Event.addListener method and append the element
	 * to elementsWithListeners array so that we can purge it once it get removed from DOM;
	 */
	E.addListener = function(el)
	{		
		l.trace("Events: Adding event listeners", arguments);
		oldAddListener.apply(this, arguments);
		if (el !== window && el !== document)
		{
			var a = elementsWithListeners;
			var i = a.length;
			a[i] = Wicket.$(el);
		}
	};
	
	E.addListener(window, "unload", function() { elementsWithListeners = null; } );
	
	var ThrottlerEntry = function(func)
	{
		this.func = func;
	};
	
	/**
	 * Throttler is responsible for throttle down function execution to make sure that is not
	 * executed more often that the specified interval. Throttler can be used to reduce number
	 * of AJAX requests. To match function with previously executed functions a (string) token
	 * is used.
	 * 
	 * @param postponeTimerOnUpdate is an optional parameter. If it is set to true, then the timer is
     *    reset each time the throttle function gets called. Use this behaviour if you want something
     *    to happen at X milliseconds after the *last* call to throttle.
     *    If the parameter is not set, or set to false, then the timer is not reset.
	 */
	var Throttler = function(postponeTimerOnUpdate)
	{
		this.entries = { };
		this.executionTimes = { };
		this.postponeTimerOnUpdate = postponeTimerOnUpdate || false;
	};
	
	Throttler.prototype = 
	{
		/**
		 * Either execute the specified function immediately, or postpone the execution if function
		 * with same token has been invoked in time that is less than "millis" milliseconds from now.
		 * 
		 * @param token - string token to match previously executed function with the one specified now
		 * @param millis - how much to postpone the function after the last invocation
		 * @param func - function
		 */
		throttle: function(token, millis, func)
		{
			// check if throttling is necessary. thottling is always necessary when postponeTimerOnUpdate 
		    // is true
			if (!this.postponeTimerOnUpdate && this.checkLastExecutionTime(token, millis, func))
			{
				return;
			}
			var entry = this.entries[token];
			if (entry == null)
			{
				entry = new ThrottlerEntry(func);
				entry.timeout = window.setTimeout(bind(function() { this.execute(token) }, this), millis);
				this.entries[token] = entry;
				l.trace("Throttler: Setting throttle, token:", token, ", millis:", millis, ", func:", func);
			}
			else
			{
				entry.func = func;
				if (this.postponeTimerOnUpdate)
				{
					window.clearTimeout(entry.timeout);
					entry.timeout = window.setTimeout(bind(this.execute, this), millis);
					l.trace("Throttler: Postponing throttle, token:", token, ", millis:", millis, ", func:", func);
				}
				else
				{
					l.trace("Throttler: Replacing throttle, token:", token, ", millis:", millis, ", func:", func);
				}
			}
		},
		
		// Checks if the function needs to be postponed. If not, executes it immediately and returns true.
		// Otherwise returns false.
		checkLastExecutionTime:function(token, millis, func)
		{
			var e = this.executionTimes[token];
			var now = new Date().getTime();
			if (e == null || (e + millis) < now)
			{
				l.trace("Throttler: Executing function immediately, token:", token, ", millis:", millis, ", func:", func);
				this.executionTimes[token] = now;
				this.entries[token] = null;
				func();
				return true;
			}
			else
			{
				return false;
			}
		},
		
		execute: function(token)
		{			
			var entry = this.entries[token];
			if (entry != null)
			{				
				var f = entry.func;
				l.trace("Invoking throttled function, token:", token, "func:", f);
				this.entries[token] = null;
				this.executionTimes[token] = new Date().getTime();
				f();				
			}
		}
	};
	
	Wicket.Throttler = Throttler;
	
	/**
	 * An item in a RequestQueue. Item constructor get a map of configuration attribute. Each attribute 
	 * can be either specified by a full name or by a shortcut.   
	 * 
	 * Possible attributes:
	 * 
	 *   SHORTCUT, NAME            TYPE       DESCRIPTION
	 *   
	 *   c, component            - Element    Component responsible for the AJAX request or null           
	 *                                        when the component is a page. If there is no DOM element 
	 *                                        for the component the attribute can contain a string 
	 *                                        (Component#getMarkupId()). The string is then used to find
	 *                                        the matching component on the server side.
	 *                                         
	 *   f, formId               - String     Form ID if the AJAX request should submit a form or null
	 *                                        if the request doesn't involve form submission
	 *                                        
	 *   m, multipart	         - Boolean    (only when formId is not null) True if the form submit should
	 *                                        be multipart, false otherwise. Note that for multipart AJAX 
	 *                                        requests a hidden IFRAME will be used and that can have 
	 *                                        negative impact on error detection.
	 *                                         
	 *   t, requestTimeout       - Integer    Timeout in milliseconds for the AJAX request. This only 
	 *                                        involves the actual communication and not the processing 
	 *                                        afterwards. Can be null in which case the default request
	 *                                        timeout will be used.
	 *                                        
	 *   pt, processingTimeout   - Integer    Timeout for the response processing. In case the response
	 *                                        processing takes more than the timeout it won't block the
	 *                                        request queue.
	 *          
	 *   p, pageId               - String     Used to identify the originating page. String in form of
	 *                                        <pageId>:<pageVersion>
	 *          
	 *   l, listenerInterface    - String     Listener interface name that will be used when building
	 *                                        the URL. Can be null if a behavior should be invoked. 
	 *                                        
	 *   b, behaviorIndex        - Integer    Index of behavior that should be invoked on the component
	 *                                        in case the listenerInterface is null
	 *                                        
	 *   t, token                - String     Optional string identifying related items in request queue. 
	 *                                        Used to identify previous items (items with same token) that 
	 *                                        will be removed when this item is added and removePrevious 
	 *                                        is true. Also required when throttle attribute is used.
	 *                                        Token is also used to invoke global precondition/success/error
	 *                                        handlers that are registered for certain token(s).
	 *        
	 *   r, removePrevious       - Boolean    Optional. If there are previous items with same token in the 
	 *                                        queue they will be removed if removePrevisious is true. This 
	 *                                        can be useful when the items are added in queue faster
	 *                                        than they are processed and only the latest request matters.
	 *                                        An example of this could be periodically updated component.
	 *                                        There is no point of having multiple refreshing requests
	 *                                        stored in the queue for such component because only the
	 *                                        last request is relevant. Alternative to this is the
	 *                                        throttle attribute.  
	 *   
	 *   th, throttle            - Integer    Optional. Limits adding items with same token to at most one 
	 *                                        item per n milliseconds where n is the value of throttle 
	 *                                        attribute. Useful to limit the number of AJAX requests that 
	 *                                        are triggered by a user action such as typing into a text 
	 *                                        field.    
	 *   
	 *   pr, precondition        - Method(s)  Optional. Method or array of methods that is/are invoked 
	 *                                        before the request executes. The method(s) will get this 
	 *                                        RequestItem passed as fist argument and have to return 
	 *                                        a boolean value. If any of these methods return false the 
	 *                                        request is cancelled.
	 *                                        
	 *   s, successHandlers      - Method(s)  Optional. Method or array of methods that is/are invoked after 
	 *                                        the request is successfully processed. The method(s) will get 
	 *                                        this RequestQueueItem passed as fist argument.  
	 *                                        
	 *   e, errorHandlers        - Method(s)  Optional. Method or array of methods that is/are invoked when 
	 *                                        an error happens during the AJAX request or the processing 
	 *                                        afterwards, or when some of the timeouts is exceeded. The 
	 *                                        method(s) will get this RequestQueueItem passed as fist 
	 *                                        argument. 
	 *                                        
	 *   u, urlPostProcessors    - Method(s)  Optional. Method or array of methods that can postprocess 
	 *                                        the URL before it hits the server. Each of the methods 
	 *                                        will get the URL as first argument and this RequestQueueItem 
	 *                                        as second argument and must return postprocessed URL.
	 *                                        
     *   ua, urlArguments        - Object     Optional. Map that contains additional URL arguments. These 
     *                                        will be appended to the URL before postprocessing it. This is 
     *                                        simpler alternative to urlPostProcessor or urlArgumentMethods.
     *                                        
     *   uam, urlArgumentMethods - Method(s)  Optional. Method or array of methods that produce additional URL
     *                                        arguments. Each of the methods will get this RequestQueueItem
     *                                        passed and must return a Map<String, String> (Object).
	 */
	var RequestQueueItem = function(attributes)
	{
		this.attributes = attributes;
	}
	
	var RequestQueue = function()
	{
	};
	
	RequestQueue.prototype = 
	{
		
	};
	
	
	
	new f();
	// ===================== REVERT THE OLD WICKET OBJECT ===================== 
	
	WicketNG = Wicket;	
	Wicket = oldWicket;
	
})();