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
package org.apache.wicket.ajax.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 * Attributes of an Ajax Request.
 * 
 * @author Matej Knopp
 */
public final class AjaxRequestAttributes
{
	/**
	 * The method to be used when submitting a form
	 */
	public static enum Method
	{
		/** get */
		GET,

		/** post */
		POST;

		@Override
		public String toString()
		{
			return name();
		}
	}

	/**
	 * The JavaScript event propagation type
	 */
	public static enum EventPropagation
	{
		/**
		 * Stops the propagation of the JavaScript event to the parent of its target
		 */
		STOP,

		/**
		 * Stops the propagation of the JavaScript event to the parent of its target
		 * and all other event listeners registered on the same target
		 */
		STOP_IMMEDIATE,

		/**
		 * Do not stop the propagation of the JavaScript event
		 */
		BUBBLE
	}

	public static final String XML_DATA_TYPE = "xml";

	private boolean multipart = false;

	private Method method = Method.GET;

	private Duration requestTimeout;

	private boolean allowDefault = false;

	private EventPropagation eventPropagation = EventPropagation.STOP;

	/**
	 * The names of the events which will trigger the Ajax call
	 */
	private String[] eventNames = new String[0];

	/**
	 * The id of the for that should be submitted
	 */
	private String formId;

	/**
	 * The id of the button/link that submitted the form
	 */
	private String submittingComponentName;

	/**
	 * Indicates whether or not this AjaxBehavior will produce <ajax-response>. By default it will
	 * produce it but some behaviors may need to return their own response which shouldn't be
	 * processed by wicket-ajax.js
	 */
	private boolean wicketAjaxResponse = true;

	private String dataType = XML_DATA_TYPE;

	private List<IAjaxCallListener> ajaxCallListeners;

	private Map<String, Object> extraParameters;

	private List<CharSequence> dynamicExtraParameters;

	private AjaxChannel channel;

	/**
	 * Whether or not to use asynchronous XMLHttpRequest
	 */
	private boolean async = true;

	/**
	 * The settings to use if the Ajax call should be throttled. Throttled behaviors only execute
	 * once within the given delay even though they are triggered multiple times.
	 * <p>
	 * For example, this is useful when attaching a behavior to the keypress event. It is not
	 * desirable to have an ajax call made every time the user types so we throttle that call to a
	 * desirable delay, such as once per second. This gives us a near real time ability to provide
	 * feedback without overloading the server with ajax calls.
	 */
	private ThrottlingSettings throttlingSettings;

	/**
	 * Returns whether the form submit is multipart.
	 * <p>
	 * Note that for multipart AJAX requests a hidden IFRAME will be used and that can have negative
	 * impact on error detection.
	 * 
	 * @return <code>true</code> if the form submit should be multipart, <code>false</code>
	 *         otherwise
	 */
	public boolean isMultipart()
	{
		return multipart;
	}

	/**
	 * Determines whether the form submit is multipart.
	 * 
	 * <p>
	 * Note that for multipart AJAX requests a hidden IFRAME will be used and that can have negative
	 * impact on error detection.
	 * 
	 * @param multipart
	 * @return this object
	 */
	public AjaxRequestAttributes setMultipart(boolean multipart)
	{
		this.multipart = multipart;
		return this;
	}

	/**
	 * Returns the type of the Ajax request: <code>GET</code> or <code>POST</code>.
	 * <p>
	 * For a <code>POST</code>request all URL arguments are submitted as body. This can be useful if
	 * the URL parameters are longer than maximal URL length.
	 * 
	 * @return the type of the Ajax request. Default: {@linkplain Method#GET}
	 */
	public Method getMethod()
	{
		return method;
	}

	/**
	 * Sets the type of the Ajax request: <code>GET</code> or <code>POST</code>.
	 * <p>
	 * For a <code>POST</code>request all URL arguments are submitted as body. This can be useful if
	 * the URL parameters are longer than maximal URL length.
	 * 
	 * @param method
	 *            the type of the Ajax request
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setMethod(final Method method)
	{
		this.method = Args.notNull(method, "method");
		return this;
	}

	/**
	 * Returns the timeout in milliseconds for the AJAX request. This only involves the actual
	 * communication and not the processing afterwards. Can be <code>null</code> in which case the
	 * default request timeout will be used.
	 * 
	 * @return request timeout or <code>null<code> for default timeout. Default: no timeout.
	 */
	public Duration getRequestTimeout()
	{
		return requestTimeout;
	}

	/**
	 * Sets the timeout in milliseconds for the AJAX request. This only involves the actual
	 * communication and not the processing afterwards. Can be <code>null</code> in which case the
	 * default request timeout will be used.
	 * 
	 * @param requestTimeout
	 * @return this object
	 */
	public AjaxRequestAttributes setRequestTimeout(final Duration requestTimeout)
	{
		this.requestTimeout = requestTimeout;
		return this;
	}

	/**
	 * @return a list of {@link IAjaxCallListener}s which will be notified during the the execution
	 *         of the Ajax call.
	 */
	public List<IAjaxCallListener> getAjaxCallListeners()
	{
		if (ajaxCallListeners == null)
		{
			ajaxCallListeners = new ArrayList<IAjaxCallListener>();
		}
		return ajaxCallListeners;
	}

	/**
	 * Map that contains additional (static) URL parameters. These will be appended to the request
	 * URL. If you need more than one value for a key then use a java.util.List or an Object[] as a
	 * value of that key.
	 * 
	 * @return a map with additional URL arguments
	 * @see #getDynamicExtraParameters()
	 */
	public Map<String, Object> getExtraParameters()
	{
		if (extraParameters == null)
		{
			extraParameters = new HashMap<String, Object>();
		}
		return extraParameters;
	}

	/**
	 * Array of JavaScript functions that produce additional URL arguments.
	 * 
	 * <p>
	 * If there are no multivalued parameters then the function can return a simple JavaScript
	 * object. Example:
	 * 
	 * <pre>
	 *  return {
	 *      'param1': document.body.tagName,
	 *      'param2': calculateParam2()
	 *  }
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * If there are multivalued parameters then an array of objects may be used. Example:
	 * 
	 * <pre>
	 *  return [
	 *      { name: 'param1', value: document.body.tagName },
	 *      { name: 'param1', value: calculateSecondValueForParam1() },
	 *      { name: 'param2', value: calculateParam2() }
	 *  ]
	 * 
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return a list of functions that produce additional URL arguments.
	 * @see #getExtraParameters()
	 */
	public List<CharSequence> getDynamicExtraParameters()
	{
		if (dynamicExtraParameters == null)
		{
			dynamicExtraParameters = new ArrayList<CharSequence>();
		}
		return dynamicExtraParameters;
	}

	/**
	 * Only applies for event behaviors. Returns whether the behavior should allow the default event
	 * handler to be invoked. For example if the behavior is attached to a link and
	 * {@link #isAllowDefault()} returns <code>false</code> (which is default value), the link's URL
	 * will not be followed. If the Ajax behavior is attached to a checkbox or a radio button then
	 * the default behavior should be allowed to actually check the box or radio button, i.e. this
	 * method should return <code>true</code>.
	 * 
	 * @return {@code true} if the default event handler should be invoked, {@code false} otherwise.
	 */
	public boolean isAllowDefault()
	{
		return allowDefault;
	}

	/**
	 * Only applies for event behaviors. Returns whether the behavior should allow the JavaScript event
	 * to propagate to the parent of its target.
	 */
	public EventPropagation getEventPropagation()
	{
		return eventPropagation;
	}

	/**
	 * Only applies for event behaviors. Determines whether the behavior should allow the default
	 * event handler to be invoked.
	 * 
	 * @see #isAllowDefault()
	 * 
	 * @param allowDefault
	 * @return {@code this} object for chaining
	 * @see #isAllowDefault()
	 */
	public AjaxRequestAttributes setAllowDefault(boolean allowDefault)
	{
		this.allowDefault = allowDefault;
		return this;
	}

	/**
	 * Only applies to event behaviors. Determines whether the behavior should allow the
	 * JavaScript event to propagate to the parent of its target.
	 *
	 * @param eventPropagation
	 *      the type of the stop
	 * @return {@code this} object, for chaining
	 */
	public AjaxRequestAttributes setEventPropagation(EventPropagation eventPropagation)
	{
		this.eventPropagation = Args.notNull(eventPropagation, "eventPropagation");
		return this;
	}

	/**
	 * @param async
	 *            a flag whether to do asynchronous Ajax call or not
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setAsynchronous(final boolean async)
	{
		this.async = async;
		return this;
	}

	/**
	 * @return whether to do asynchronous Ajax call
	 */
	public boolean isAsynchronous()
	{
		return async;
	}

	/**
	 * @return the channel to use
	 */
	public AjaxChannel getChannel()
	{
		return channel;
	}

	/**
	 * @param channel
	 *            the Ajax channel to use. Pass {@code null} to use the default channel with name
	 *            <em>0</em> and queueing type.
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setChannel(final AjaxChannel channel)
	{
		this.channel = channel;
		return this;
	}

	/**
	 * @return the name(s) of the event(s) which will trigger the Ajax call
	 */
	public String[] getEventNames()
	{
		return eventNames;
	}

	/**
	 * @param eventNames
	 *            the names of the events which will trigger the Ajax call
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setEventNames(String... eventNames)
	{
		Args.notNull(eventNames, "eventNames");
		this.eventNames = eventNames;
		return this;
	}

	/**
	 * @return the id of the for that should be submitted
	 */
	public String getFormId()
	{
		return formId;
	}

	/**
	 * @param formId
	 *            the id of the for that should be submitted
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setFormId(final String formId)
	{
		this.formId = formId;
		return this;
	}

	/**
	 * @return the input name of the button/link that submits the form
	 */
	public String getSubmittingComponentName()
	{
		return submittingComponentName;
	}

	/**
	 * @param submittingComponentName
	 *            the input name of the button/link that submits the form
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setSubmittingComponentName(String submittingComponentName)
	{
		this.submittingComponentName = submittingComponentName;
		return this;
	}

	/**
	 * @return a flag indicating whether the Ajax response should be processed by Wicket (i.e. to
	 *         replace components, execute scripts, etc.). Default: {@code true}.
	 */
	public boolean isWicketAjaxResponse()
	{
		return wicketAjaxResponse;
	}

	/**
	 * @param wicketAjaxResponse
	 *            a flag indicating whether the Ajax response should be processed by Wicket (i.e. to
	 *            replace components, execute scripts, etc.).
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setWicketAjaxResponse(final boolean wicketAjaxResponse)
	{
		this.wicketAjaxResponse = wicketAjaxResponse;
		return this;
	}

	/**
	 * Returns the type of the data in the Ajax response. For example: 'xml', 'json', 'html', etc.
	 * See the documentation of jQuery.ajax() method for more information.
	 * 
	 * @return the type of the data in the Ajax response.
	 */
	public String getDataType()
	{
		return dataType;
	}

	/**
	 * @param dataType
	 *            the type of the data in the Ajax response.
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setDataType(final String dataType)
	{
		this.dataType = Args.notEmpty(dataType, "dataType");
		return this;
	}

	/**
	 * @return the settings to use when throttling is needed.
	 */
	public ThrottlingSettings getThrottlingSettings()
	{
		return throttlingSettings;
	}

	/**
	 * @param throttlingSettings
	 *            the settings to use when throttling is needed. Pass {@code null} to disable
	 *            throttling.
	 * @return {@code this} object for chaining
	 */
	public AjaxRequestAttributes setThrottlingSettings(ThrottlingSettings throttlingSettings)
	{
		this.throttlingSettings = throttlingSettings;
		return this;
	}

}