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
package org.apache.wicket.ajaxng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attributes of an Ajax Request.
 * 
 * <hr>
 * 
 * <p>
 * Note that some of these attributes represent javascript functions. Those functions get a
 * <code>RequestQueueItem</code> instance as first argument. The instance provides access to
 * following properties that the javascript functions can use:
 * <dl>
 * <dt>attributes</dt>
 * <dd> Object with request queue item attributes. The <code>attributes</code> Object contains
 * following properties:
 * <dl>
 * 
 * <dt>component</dt>
 * <dd>component DOM element or <code>null</code> if the behavior is attached to page</dd>
 * 
 * <dt>formId</dt>
 * <dd>id of the form DOM element or <code>null</code> if not specified</dd>
 * 
 * <dt>token</dt>
 * <dd>token string or <code>null</code> if not specified</dd>
 * 
 * <dt> </dd>
 * 
 * </dl>
 * </dd>
 * 
 * <dt>event</dt>
 * <dd>If the AJAX request was a result of javacript event (i.e. onclick) the <code>event</code>
 * property contains the actual event instance.
 * 
 * </dl>
 * 
 * @author Matej Knopp
 */
public final class AjaxRequestAttributes
{
	/**
	 * 
	 * Construct.
	 */
	public AjaxRequestAttributes()
	{

	}

	private boolean multipart = false;
	private boolean forcePost = false;
	private Integer requestTimeout;
	private Integer processingTimeout;
	private String token;
	private boolean removePrevious = false;
	private Integer throttle;
	private boolean throttlePostpone = false;
	private boolean allowDefault = false;

	private List<CharSequence> preconditions = null;
	private List<CharSequence> beforeHandlers = null;
	private List<CharSequence> successHandlers = null;
	private List<CharSequence> errorHandlers = null;
	private Map<String, Object> urlArguments = null;
	private List<CharSequence> urlArgumentMethods = null;
	private List<CharSequence> requestQueueItemCreationListeners = null;
	private List<ExpressionDecorator> expressionDecorators = null;

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
	 */
	public void setMultipart(boolean multipart)
	{
		this.multipart = multipart;
	}

	/**
	 * Returns whether the Ajax request should be a <code>POST</code> regardless of whether a form
	 * is being submitted.
	 * <p>
	 * For a <code>POST</code>request all URL arguments are submitted as body. This can be useful
	 * if the URL parameters are longer than maximal URL length.
	 * 
	 * @return <code>true</code> if the request should be post, <code>false</code> otherwise.
	 */
	public boolean isForcePost()
	{
		return forcePost;
	}

	/**
	 * Determines whether the Ajax request should be a <code>POST</code> regardless of whether a
	 * form is being submitted.
	 * <p>
	 * For a <code>POST</code>request all URL arguments are submitted as body. This can be useful
	 * if the URL parameters are longer than maximal URL length.
	 * 
	 * @param forcePost
	 */
	public void setForcePost(boolean forcePost)
	{
		this.forcePost = forcePost;
	}

	/**
	 * Returns the timeout in milliseconds for the AJAX request. This only involves the actual
	 * communication and not the processing afterwards. Can be <code>null</code> in which case the
	 * default request timeout will be used.
	 * 
	 * @return request timeout in milliseconds or <code>null<code> for default timeout
	 */
	public Integer getRequestTimeout()
	{
		return requestTimeout;
	}

	/**
	 * Sets the timeout in milliseconds for the AJAX request. This only involves the actual
	 * communication and not the processing afterwards. Can be <code>null</code> in which case the
	 * default request timeout will be used.
	 * 
	 * @param requestTimeout
	 */
	public void setRequestTimeout(Integer requestTimeout)
	{
		this.requestTimeout = requestTimeout;
	}

	/**
	 * Returns the timeout for the response processing. In case the response processing takes more
	 * than the timeout it won't block the request queue. Can be <code>null</code> in which case
	 * the default processing timeout will be used.
	 * 
	 * @return processing timeout in milliseconds or <code>null</code> for default timeout
	 */
	public Integer getProcessingTimeout()
	{
		return processingTimeout;
	}

	/**
	 * Sets the timeout for the response processing. In case the response processing takes more than
	 * the timeout it won't block the request queue. Can be <code>null</code> in which case the
	 * default processing timeout will be used.
	 * 
	 * @param processingTimeout
	 */
	public void setProcessingTimeout(Integer processingTimeout)
	{
		this.processingTimeout = processingTimeout;
	}

	/**
	 * Returns optional string identifying related items in request queue. Used to identify previous
	 * items (items with same token) that will be removed when this item is added and
	 * {@link #isRemovePrevious()} returns <code>true</code>. Also required when throttling is
	 * enabled.
	 * 
	 * @see #getThrottle()
	 * @see #isRemovePrevious()
	 * 
	 * @return token string or <code>null</code>
	 */
	public String getToken()
	{
		return token;
	}

	/**
	 * Sets optional string identifying related items in request queue. Used to identify previous
	 * items (items with same token) that will be removed when this item is added and
	 * {@link #isRemovePrevious()} returns <code>true</code>. Also required when throttling is
	 * enabled.
	 * 
	 * @see #getThrottle()
	 * @see #isRemovePrevious()
	 * 
	 * @param token
	 */
	public void setToken(String token)
	{
		this.token = token;
	}

	/**
	 * If there are previous items with same token in the queue they will be removed if
	 * {@link #isRemovePrevious()} returns <code>true</code>. This can be useful when the items
	 * are added in queue faster than they are processed and only the latest request matters.
	 * <p>
	 * An example of this could be periodically updated component. There is no point of having
	 * multiple refreshing requests stored in the queue for such component because only the last
	 * request is relevant. Alternative to this is throttling.
	 * 
	 * @see #getToken()
	 * @see #getThrottle()
	 * 
	 * @return boolean value or <code>null</code>
	 */
	public Boolean isRemovePrevious()
	{
		return removePrevious;
	}

	/**
	 * If there are previous items with same token in the queue they will be removed if
	 * {@link #setRemovePrevious(boolean)} is set to <code>true</code>. This can be useful when
	 * the items are added in queue faster than they are processed and only the latest request
	 * matters.
	 * <p>
	 * An example of this could be periodically updated component. There is no point of having
	 * multiple refreshing requests stored in the queue for such component because only the last
	 * request is relevant. Alternative to this is throttling.
	 * 
	 * @param removePrevious
	 */
	public void setRemovePrevious(boolean removePrevious)
	{
		this.removePrevious = removePrevious;
	}

	/**
	 * Limits adding items with same token to at most one item per n milliseconds where n is the
	 * return value.
	 * <p>
	 * Useful to limit the number of AJAX requests that are triggered by a user action such as
	 * typing into a text field. Throttle attribute only applies when token is specified.
	 * 
	 * @see #getToken()
	 * 
	 * @return throttling timeout in milliseconds or <code>null</code>
	 */
	public Integer getThrottle()
	{
		return throttle;
	}

	/**
	 * Limits adding items with same token to at most one item per n milliseconds where n is the
	 * return value.
	 * <p>
	 * Useful to limit the number of AJAX requests that are triggered by a user action such as
	 * typing into a text field. Throttle attribute only applies when token is specified.
	 * 
	 * @see #getToken()
	 * 
	 * @param throttle
	 */
	public void setThrottle(Integer throttle)
	{
		this.throttle = throttle;
	}

	/**
	 * Only applicable when throttling is enabled. Defaults to <code>false</code>. Causes the
	 * throttle timer reset each time item with same token is being added to queue.
	 * <p>
	 * Example: Event is fired by user typing in a TextField. Throttle value is 2000 (ms), throttle
	 * postpone is <code>true</code>. The event will be fired 2000ms after user typed the last
	 * character. If throttle postpone is <code>false</code>, The event is fired immediately
	 * after user starts typing and then every two seconds as long as user keeps typing.
	 * 
	 * @return boolean value or <code>null</code>
	 */
	public boolean isThrottlePostpone()
	{
		return throttlePostpone;
	}

	/**
	 * When set to true causes the throttle timer reset each time item with same token is being
	 * added to queue.
	 * 
	 * @see #isThrottlePostpone()
	 * 
	 * @param throttlePostpone
	 */
	public void setThrottlePostpone(boolean throttlePostpone)
	{
		this.throttlePostpone = throttlePostpone;
	}

	/**
	 * Array of javascript functions that are invoked before the request executes. The functions
	 * will get a <code>RequestQueueItem</code> instance passed as fist argument and have to
	 * return a boolean value. If any of these functions returns <code>false</code> the request is
	 * canceled.
	 * <p>
	 * Example of single function:
	 * 
	 * <pre>
	 *    function(requestQueueItem) 
	 *    { 
	 *      if (someCondition()) 
	 *      {
	 *         return true; 
	 *      } 
	 *      else 
	 *      {
	 *         return false;
	 *      }
	 *    }
	 * </pre>
	 * 
	 * Preconditions can also be asynchronous (with the rest of the queue waiting until precondition
	 * finishes). An example of asynchronous precondition:
	 * 
	 * <pre>
	 *    function(requestQueueItem, makeAsync, asyncReturn) 
	 *    { 
	 *      makeAsync(); // let the queue know that this precondition is asynchronous
	 *      var f = function()
	 *      {
	 *        if (someCondition())
	 *        {
	 *          asyncReturn(true); // return the precondition value
	 *        }
	 *        else
	 *        {
	 *          asyncReturn(false); // return the precondition value
	 *        }
	 *      };
	 *      window.setTimeout(f, 1000); // postpone the actual check 1000 millisecond. The queue will wait.
	 *    }
	 * </pre>
	 * 
	 * @return List<CharSequence> or <code>null</code>
	 */
	public List<CharSequence> getPreconditions()
	{
		if (preconditions == null)
		{
			preconditions = new ArrayList<CharSequence>();
		}
		return preconditions;
	}

	/**
	 * Array of javascript functions that are invoked before the actual AJAX request. This
	 * invocation only happens when all precondition functions return true. Each of the function
	 * will get a <code>RequestQueueItem</code> instance passed as first argument.
	 * <p>
	 * Example of single function:
	 * 
	 * <pre>
	 *    function(requestQueueItem) 
	 *    { 
	 *      doSomethingWhenAjaxRequestBegins();
	 *    }
	 * </pre>
	 * 
	 * @see #getPreconditions()
	 * 
	 * @return List<CharSequence> or <code>null</code>
	 */
	public List<CharSequence> getBeforeHandlers()
	{
		if (beforeHandlers == null)
		{
			beforeHandlers = new ArrayList<CharSequence>();
		}
		return beforeHandlers;
	}

	/**
	 * Array of javascript functions that are invoked after the request is successfully processed.
	 * Each function will get a <code>RequestQueueItem</code> instance passed as fist argument.
	 * <p>
	 * Example of single function:
	 * 
	 * <pre>
	 *    function(requestQueueItem) 
	 *    { 
	 *      doSomethingWhenRequestIsSuccessfullyProcessed();
	 *    }
	 * </pre>
	 * 
	 * @return List<CharSequence> or <code>null</code>
	 */
	public List<CharSequence> getSuccessHandlers()
	{
		if (successHandlers == null)
		{
			successHandlers = new ArrayList<CharSequence>();
		}
		return successHandlers;
	}

	/**
	 * Array of javascript functions that are invoked when an unexpected error happens during the
	 * AJAX request or the processing afterwards, or when some of the timeouts is exceeded. The
	 * functions will get a <code>RequestQueueItem</code> passed as fist argument. If possible
	 * error message will be second argument passed to the handlers.
	 * <p>
	 * Example of single function:
	 * 
	 * <pre>
	 *    function(requestQueueItem, error) 
	 *    { 
	 *      if (typeof(error) == &quot;string&quot;) 
	 *      {
	 *      	alert('Error processing request ' + error);
	 *      }
	 *    }
	 * </pre>
	 * 
	 * @return List<CharSequence> or <code>null</code>
	 */
	public List<CharSequence> getErrorHandlers()
	{
		if (errorHandlers == null)
		{
			errorHandlers = new ArrayList<CharSequence>();
		}
		return errorHandlers;
	}

	/**
	 * Map that contains additional URL arguments. These will be appended to the request URL. This
	 * is simpler alternative to {@link #getUrlArgumentMethods()}
	 * 
	 * @return Map with additional URL arguments or <code>null</code>
	 */
	public Map<String, Object> getUrlArguments()
	{
		if (urlArgumentMethods == null)
		{
			urlArguments = new HashMap<String, Object>();
		}
		return urlArguments;
	}

	/**
	 * Array of javascript functions that produce additional URL arguments. Each of the functions
	 * will get an <code>RequestQueueItem</code> passed as first argument and must return a
	 * <code>Map<String,
	 * String></code> (Object).
	 * <p>
	 * Example of single function:
	 * 
	 * <pre>
	 *    function(requestQueueItem)
	 *    {
	 *    	return { param1: &quot;value1&quot;, param2: 15 }
	 *    } 
	 * </pre>
	 * 
	 * @return List<CharSequence> or <code>null</code>
	 */
	public List<CharSequence> getUrlArgumentMethods()
	{
		if (urlArgumentMethods == null)
		{
			urlArgumentMethods = new ArrayList<CharSequence>();
		}
		return urlArgumentMethods;
	}

	/**
	 * Array of javascript functions invoked when a <code>RequestQueueItem</code> instance is
	 * created. The <code>RequestQueueItem</code> instance will be passed as first argument.
	 * 
	 * @return List<CharSequence> or <code>null</code>
	 */
	public List<CharSequence> getRequestQueueItemCreationListeners()
	{
		if (requestQueueItemCreationListeners == null)
		{
			requestQueueItemCreationListeners = new ArrayList<CharSequence>();
		}
		return requestQueueItemCreationListeners;
	}

	/**
	 * Certain behaviors support decorating the javascript expression they generate with
	 * {@link ExpressionDecorator}s.
	 * <p>
	 * This usually decorates the javascript expression that is responsible for creating
	 * <code>RequestQueueItem</code> and adding it to the queue. The decorator may be useful to
	 * intercept the <code>RequestQueueItem</code> creation on in the earliest possible time.
	 * <p>
	 * Note that not all Ajax behaviors are required to support decorating the expression.
	 * 
	 * @return list of {@link ExpressionDecorator} .
	 */
	public List<ExpressionDecorator> getExpressionDecorators()
	{
		if (expressionDecorators == null)
		{
			expressionDecorators = new ArrayList<ExpressionDecorator>();
		}
		return expressionDecorators;
	}

	/**
	 * Only applies for event behaviors. Returns whether the behavior should allow the default event
	 * handler to be invoked. For example if the behavior is attached to a link and
	 * {@link #isAllowDefault()} returns <code>false</code> (which is default value), the link's
	 * URL will not be followed. If {@link #isAllowDefault()} returns <code>true</code>, the link
	 * URL will be loaded (and the onclick handler fired if there is any).
	 * 
	 * @return <code>true</code> if the default event handler should be invoked,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAllowDefault()
	{
		return allowDefault;
	}

	/**
	 * Only applies for event behaviors. Determines whether the behavior should allow the default
	 * event handler to be invoked.
	 * 
	 * @see #isAllowDefault()
	 * 
	 * @param allowDefault
	 */
	public void setAllowDefault(boolean allowDefault)
	{
		this.allowDefault = allowDefault;
	}
}
