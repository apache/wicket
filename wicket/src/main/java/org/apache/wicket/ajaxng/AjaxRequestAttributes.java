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

import java.util.Map;

import org.apache.wicket.markup.html.form.Form;

/**
 * Attributes for an Ajax Request. 
 * 
 * <hr>
 *  
 * <p>
 * Note that some of these attributes represent javascript functions. Those functions get a
 * <code>RequestQueueItem</code> instance as first argument. The instance provides access to
 * following properties that the javascript functions can use:
 * <dl>
 * <dt>attributes</dt>
 * <dd> Object with request queue item attributes. The <code>attributes</code> Object contains following
 * properties:
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
 * 
 * @author Matej Knopp
 */
public interface AjaxRequestAttributes
{
	/**
	 * Form instance if the AJAX request should submit a form or <code>null</code> if the request
	 * doesn't involve form submission.
	 * 
	 * @return form instance or <code>null</code>
	 */
	Form<?> getForm();

	/**
	 * Returns whether the form submit is multipart.
	 * <p>
	 * Note that for multipart AJAX requests a hidden IFRAME will be used and that can have negative
	 * impact on error detection.
	 * 
	 * @return <code>true</code> if the form submit should be multipart, false otherwise
	 */
	Boolean isMultipart();

	/**
	 * Timeout in milliseconds for the AJAX request. This only involves the actual communication and
	 * not the processing afterwards. Can be <code>null</code> in which case the default request
	 * timeout will be used.
	 * 
	 * @return request timeout in milliseconds or <code>null<code> for default timeout
	 */
	Integer getRequesTimeout();

	/**
	 * Timeout for the response processing. In case the response processing takes more than the
	 * timeout it won't block the request queue. Can be <code>null</code> in which case the
	 * default processing timeout will be used.
	 * 
	 * @return processing timeout in milliseconds or <code>null</code> for default timeout
	 */
	Integer getProcessingTimeout();

	/**
	 * Optional string identifying related items in request queue. Used to identify previous items
	 * (items with same token) that will be removed when this item is added and
	 * {@link #isRemovePrevious()} returns <code>true</code>. Also required when throttling is
	 * enabled.
	 * 
	 * @see #getThrottle()
	 * @see #isRemovePrevious()
	 * 
	 * @return token string or <code>null</code>
	 */
	String getToken();

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
	Boolean isRemovePrevious();

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
	Integer getThrottle();

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
	Boolean isThrottlePostpone();

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
	 * @return FunctionList or <code>null</code>
	 */
	FunctionList getPreconditions();

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
	 * @return FunctionList or <code>null</code>
	 */
	FunctionList getBeforeHandlers();

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
	 * @return FunctionList or <code>null</code>
	 */
	FunctionList getSuccessHandlers();

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
	 * @return FunctionList or <code>null</code>
	 */
	FunctionList getErrorHandlers();

	/**
	 * Map that contains additional URL arguments. These will be appended to the request URL. This
	 * is simpler alternative to {@link #getUrlArgumentMethods()}
	 * 
	 * @return Map with additional URL arguments or <code>null</code>
	 */
	Map<String, Object> getUrlArguments();

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
	 * @return FunctionList or <code>null</code>
	 */
	FunctionList getUrlArgumentMethods();
}
