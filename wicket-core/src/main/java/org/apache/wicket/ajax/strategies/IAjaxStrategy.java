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
package org.apache.wicket.ajax.strategies;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;

/**
 *
 */
public interface IAjaxStrategy
{
	final String DYNAMIC_PARAMETER_FUNCTION_TEMPLATE = "function(attrs){%s}";
	final String PRECONDITION_FUNCTION_TEMPLATE = "function(attrs){%s}";
	final String COMPLETE_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, textStatus){%s}";
	final String FAILURE_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, errorMessage, textStatus){%s}";
	final String SUCCESS_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, data, textStatus){%s}";
	final String AFTER_HANDLER_FUNCTION_TEMPLATE = "function(attrs){%s}";
	final String BEFORE_SEND_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, settings){%s}";
	final String BEFORE_HANDLER_FUNCTION_TEMPLATE = "function(attrs){%s}";

	
	void renderHead(final AjaxEventBehavior behavior, final IHeaderResponse response);

	void onComponentTag(final AjaxEventBehavior behavior, final ComponentTag tag);

	JSONObject getAjaxAttributes(AbstractDefaultAjaxBehavior behavior, Component component, AjaxRequestAttributes attributes);

	void postprocessConfiguration(AbstractDefaultAjaxBehavior behavior, JSONObject attributesJson,
	                              Component component) throws JSONException;

	CharSequence getCallbackScript(final Component component, JSONObject ajaxAttributes);

	CharSequence getCallbackFunction(AjaxRequestAttributes attributes, CallbackParameter... extraParameters);

	CharSequence getCallbackFunctionBody(AjaxRequestAttributes attributes, CallbackParameter... extraParameters);
}
