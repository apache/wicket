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

import org.apache.wicket.Component;

/**
 * Interface used to listen at the most important points when Wicket performs an Ajax callback.
 * <br/>
 *  Ajax call listeners are potential contributors to the page header by implementing
 * {@link org.apache.wicket.markup.html.IComponentAwareHeaderContributor}. E.g. the JavaScript used
 * by the listener may depend on some JavaScript library, by implementing
 * {@link org.apache.wicket.markup.html.IComponentAwareHeaderContributor} interface
 * they can assure it will be loaded.
 *
 * @since 6.0
 */
public interface IAjaxCallListener
{
	/**
	 * The JavaScript that will be executed after successful return of the Ajax call.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>data - the Ajax response. Its type depends on {@link AjaxRequestAttributes#dataType}</li>
	 *     <li>textStatus - the status as text</li>
	 *     <li>jqXHR - the jQuery XMLHttpRequest object</li>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 * </ol>
	 *
	 * @param component
	 *      the Component with the Ajax behavior
	 * @return the JavaScript that will be executed after a successful
	 *      return of the Ajax call.
	 */
	CharSequence getSuccessHandler(Component component);

	/**
	 * The JavaScript that will be executed after unsuccessful return of the Ajax call.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 * </ol>
	 *
	 * @param component
	 *      the Component with the Ajax behavior
	 * @return the JavaScript that will be executed after a unsuccessful
	 *      return of the Ajax call.
	 */
	CharSequence getFailureHandler(Component component);

	/**
	 * The JavaScript that will be executed before the Ajax call, as early as possible. Even before the preconditions.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 * </ol>
	 *
	 * @param component
	 *      the Component with the Ajax behavior
	 * @return the JavaScript that will be executed before the Ajax call.
	 */
	CharSequence getBeforeHandler(Component component);


	/**
	 * The JavaScript that will be executed right before make of the the Ajax call,
	 * only if all preconditions pass.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 *     <li>jqXHR - the jQuery XMLHttpRequest object</li>
	 *     <li>settings - the settings used for the jQuery.ajax() call</li>
	 * </ol>
	 *
	 * @param component
	 *      the Component with the Ajax behavior
	 * @return the JavaScript that will be executed before the Ajax call.
	 */
	CharSequence getBeforeSendHandler(Component component);

	/**
	 * The JavaScript that will be executed after the Ajax call.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 * </ol>
	 *
	 * @param component
	 *      the Component with the Ajax behavior
	 * @return the JavaScript that will be executed after the start of the Ajax call
	 *      but before its response is returned. <strong>Note</strong>: if the Ajax call
	 *      is synchronous (see {@link AjaxRequestAttributes#setAsynchronous(boolean)})
	 *      then this JavaScript will be executed after the
	 *      {@linkplain #getCompleteHandler(org.apache.wicket.Component) complete handler}.
	 */
	CharSequence getAfterHandler(Component component);

	/**
	 * The JavaScript that will be executed after both successful and unsuccessful return of the Ajax call.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>jqXHR - the jQuery XMLHttpRequest object</li>
	 *     <li>textStatus - the status as text</li>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 * </ol>
	 * @param component
	 *      the Component with the Ajax behavior
	 * @return the JavaScript that will be executed after both successful
	 *      and unsuccessful return of the Ajax call.
	 */
	CharSequence getCompleteHandler(Component component);

	/**
	 * A JavaScript function that is invoked before the request executes.
	 * If it returns {@code false} then the execution of the Ajax call will
	 * be cancelled.
	 * The script will be executed in a function that receives the following parameters:
	 * <ol>
	 *     <li>attrs - the AjaxRequestAttributes as JSON</li>
	 * </ol>
	 *
	 * @return the JavaScript that should be used to decide whether the Ajax
	 *      call should be made at all.
	 */
	CharSequence getPrecondition(Component component);
}
