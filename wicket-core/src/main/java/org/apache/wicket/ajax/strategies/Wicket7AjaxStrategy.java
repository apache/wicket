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
import org.apache.wicket.ajax.attributes.AjaxAttributeName;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;

/**
 *
 */
public class Wicket7AjaxStrategy implements IAjaxStrategy
{
	@Override
	public void renderHead(AjaxEventBehavior behavior, IHeaderResponse response)
	{
	}

	@Override
	public void onComponentTag(AjaxEventBehavior behavior, ComponentTag tag)
	{
		CharSequence ajaxAttributes = behavior.renderAjaxAttributes();
		tag.put("data-w-" + behavior.getEvent(), ajaxAttributes);
	}

	@Override
	public JSONObject getAjaxAttributes(AbstractDefaultAjaxBehavior behavior, Component component, AjaxRequestAttributes attributes)
	{
		return null;
	}

	@Override
	public void postprocessConfiguration(AbstractDefaultAjaxBehavior behavior, JSONObject attributesJson, Component component) throws JSONException
	{
		attributesJson.remove(AjaxAttributeName.BEFORE_HANDLER.jsonName());
		attributesJson.remove(AjaxAttributeName.PRECONDITION.jsonName());
		attributesJson.remove(AjaxAttributeName.BEFORE_SEND_HANDLER.jsonName());
		attributesJson.remove(AjaxAttributeName.SUCCESS_HANDLER.jsonName());
		attributesJson.remove(AjaxAttributeName.FAILURE_HANDLER.jsonName());
		attributesJson.remove(AjaxAttributeName.COMPLETE_HANDLER.jsonName());
		attributesJson.remove(AjaxAttributeName.AFTER_HANDLER.jsonName());

		attributesJson.remove(AjaxAttributeName.MARKUP_ID.jsonName());
	}

	@Override
	public CharSequence getCallbackScript(Component component, JSONObject ajaxAttributes)
	{
		return null;
	}

	@Override
	public CharSequence getCallbackFunction(AjaxRequestAttributes attributes, CallbackParameter... extraParameters)
	{
		return null;
	}

	@Override
	public CharSequence getCallbackFunctionBody(AjaxRequestAttributes attributes, CallbackParameter... extraParameters)
	{
		return null;
	}

}
