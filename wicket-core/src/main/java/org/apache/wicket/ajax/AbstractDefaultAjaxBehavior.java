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
package org.apache.wicket.ajax;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.json.JsonFunction;
import org.apache.wicket.ajax.json.JsonUtils;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 * The base class for Wicket's default AJAX implementation.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractDefaultAjaxBehavior extends AbstractAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/** reference to the default indicator gif file. */
	public static final ResourceReference INDICATOR = new PackageResourceReference(
		AbstractDefaultAjaxBehavior.class, "indicator.gif");

	private static final String DYNAMIC_PARAMETER_FUNCTION_TEMPLATE = "function(attrs){%s}";
	private static final String PRECONDITION_FUNCTION_TEMPLATE = "function(attrs){%s}";
	private static final String COMPLETE_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, textStatus){%s}";
	private static final String FAILURE_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, errorMessage, textStatus){%s}";
	private static final String SUCCESS_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, data, textStatus){%s}";
	private static final String AFTER_HANDLER_FUNCTION_TEMPLATE = "function(attrs){%s}";
	private static final String BEFORE_SEND_HANDLER_FUNCTION_TEMPLATE = "function(attrs, jqXHR, settings){%s}";
	private static final String BEFORE_HANDLER_FUNCTION_TEMPLATE = "function(attrs){%s}";

	/**
	 * Subclasses should call super.onBind()
	 * 
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	@Override
	protected void onBind()
	{
		getComponent().setOutputMarkupId(true);
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#renderHead(Component,
	 *      org.apache.wicket.markup.head.IHeaderResponse)
	 */
	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		super.renderHead(component, response);

		CoreLibrariesContributor.contributeAjax(component.getApplication(), response);

		RequestCycle requestCycle = component.getRequestCycle();
		Url baseUrl = requestCycle.getUrlRenderer().getBaseUrl();
		CharSequence ajaxBaseUrl = Strings.escapeMarkup(baseUrl.toString());
		response.render(JavaScriptHeaderItem.forScript("Wicket.Ajax.baseUrl=\"" + ajaxBaseUrl +
			"\";", "wicket-ajax-base-url"));

		renderExtraHeaderContributors(component, response);
	}

	/**
	 * Renders header contribution by IAjaxCallListener instances which additionally implement
	 * IComponentAwareHeaderContributor interface.
	 * 
	 * @param component
	 *            the component assigned to this behavior
	 * @param response
	 *            the current header response
	 */
	private void renderExtraHeaderContributors(final Component component,
		final IHeaderResponse response)
	{
		AjaxRequestAttributes attributes = getAttributes();

		List<IAjaxCallListener> ajaxCallListeners = attributes.getAjaxCallListeners();
		for (IAjaxCallListener ajaxCallListener : ajaxCallListeners)
		{
			if (ajaxCallListener instanceof IComponentAwareHeaderContributor)
			{
				IComponentAwareHeaderContributor contributor = (IComponentAwareHeaderContributor)ajaxCallListener;
				contributor.renderHead(component, response);
			}
		}
	}

	/**
	 * @return the Ajax settings for this behavior
	 * @since 6.0
	 */
	protected final AjaxRequestAttributes getAttributes()
	{
		AjaxRequestAttributes attributes = new AjaxRequestAttributes();
		updateAjaxAttributesBackwardCompatibility(attributes);
		updateAjaxAttributes(attributes);
		return attributes;
	}

	/**
	 * Gives a chance to the specializations to modify the attributes.
	 * 
	 * @param attributes
	 * @since 6.0
	 */
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * The code below handles backward compatibility.
	 * 
	 * @param attributes
	 */
	private void updateAjaxAttributesBackwardCompatibility(final AjaxRequestAttributes attributes)
	{
		AjaxCallListener backwardCompatibleAjaxCallListener = new AjaxCallListener();
		backwardCompatibleAjaxCallListener.onSuccess(getSuccessScript());
		backwardCompatibleAjaxCallListener.onFailure(getFailureScript());
		backwardCompatibleAjaxCallListener.onPrecondition(getPreconditionScript());
		attributes.getAjaxCallListeners().add(backwardCompatibleAjaxCallListener);

		AjaxChannel channel = getChannel();
		if (channel != null)
		{
			attributes.setChannel(channel);
		}
	}

	/**
	 * <pre>
	 * 				{
	 * 					u: 'editable-label?6-1.IBehaviorListener.0-text1-label',  // url
	 * 					m: 'POST',          // method name. Default: 'GET'
	 * 					c: 'label7',        // component id (String) or window for page
	 * 					e: 'click',         // event name
	 * 					sh: [],             // list of success handlers
	 * 					fh: [],             // list of failure handlers
	 * 					pre: [],            // list of preconditions. If empty set default : Wicket.$(settings{c}) !== null
	 * 					ep: {},             // extra parameters
	 * 					async: true|false,  // asynchronous XHR or not
	 * 					ch: 'someName|d',   // AjaxChannel
	 * 					i: 'indicatorId',   // indicator component id
	 * 					ad: true,           // allow default
	 * 				}
	 * </pre>
	 * 
	 * @param component
	 *            the component with that behavior
	 * @return the attributes as string in JSON format
	 */
	protected final CharSequence renderAjaxAttributes(final Component component)
	{
		AjaxRequestAttributes attributes = getAttributes();
		return renderAjaxAttributes(component, attributes);
	}

	/**
	 * 
	 * @param component
	 * @param attributes
	 * @return the attributes as string in JSON format
	 */
	protected final CharSequence renderAjaxAttributes(final Component component,
		AjaxRequestAttributes attributes)
	{
		JSONObject attributesJson = new JSONObject();

		try
		{
			attributesJson.put("u", getCallbackUrl());
			Method method = attributes.getMethod();
			if (Method.POST == method)
			{
				attributesJson.put("m", method);
			}

			if (component instanceof Page == false)
			{
				String componentId = component.getMarkupId();
				attributesJson.put("c", componentId);
			}

			String formId = attributes.getFormId();
			if (Strings.isEmpty(formId) == false)
			{
				attributesJson.put("f", formId);
			}

			if (attributes.isMultipart())
			{
				attributesJson.put("mp", true);
			}

			String submittingComponentId = attributes.getSubmittingComponentName();
			if (Strings.isEmpty(submittingComponentId) == false)
			{
				attributesJson.put("sc", submittingComponentId);
			}

			String indicatorId = findIndicatorId();
			if (Strings.isEmpty(indicatorId) == false)
			{
				attributesJson.put("i", indicatorId);
			}

			for (IAjaxCallListener ajaxCallListener : attributes.getAjaxCallListeners())
			{
				if (ajaxCallListener != null)
				{
					CharSequence beforeHandler = ajaxCallListener.getBeforeHandler(component);
					appendListenerHandler(beforeHandler, attributesJson, "bh",
						BEFORE_HANDLER_FUNCTION_TEMPLATE);

					CharSequence beforeSendHandler = ajaxCallListener.getBeforeSendHandler(component);
					appendListenerHandler(beforeSendHandler, attributesJson, "bsh",
						BEFORE_SEND_HANDLER_FUNCTION_TEMPLATE);

					CharSequence afterHandler = ajaxCallListener.getAfterHandler(component);
					appendListenerHandler(afterHandler, attributesJson, "ah",
						AFTER_HANDLER_FUNCTION_TEMPLATE);

					CharSequence successHandler = ajaxCallListener.getSuccessHandler(component);
					appendListenerHandler(successHandler, attributesJson, "sh",
						SUCCESS_HANDLER_FUNCTION_TEMPLATE);

					CharSequence failureHandler = ajaxCallListener.getFailureHandler(component);
					appendListenerHandler(failureHandler, attributesJson, "fh",
						FAILURE_HANDLER_FUNCTION_TEMPLATE);

					CharSequence completeHandler = ajaxCallListener.getCompleteHandler(component);
					appendListenerHandler(completeHandler, attributesJson, "coh",
						COMPLETE_HANDLER_FUNCTION_TEMPLATE);

					CharSequence precondition = ajaxCallListener.getPrecondition(component);
					appendListenerHandler(precondition, attributesJson, "pre",
						PRECONDITION_FUNCTION_TEMPLATE);
				}
			}

			JSONArray extraParameters = JsonUtils.asArray(attributes.getExtraParameters());

			if (extraParameters.length() > 0)
			{
				attributesJson.put("ep", extraParameters);
			}

			List<CharSequence> dynamicExtraParameters = attributes.getDynamicExtraParameters();
			if (dynamicExtraParameters != null)
			{
				for (CharSequence dynamicExtraParameter : dynamicExtraParameters)
				{
					String func = String.format(DYNAMIC_PARAMETER_FUNCTION_TEMPLATE,
						dynamicExtraParameter);
					JsonFunction function = new JsonFunction(func);
					attributesJson.append("dep", function);
				}
			}

			if (attributes.isAsynchronous() == false)
			{
				attributesJson.put("async", false);
			}

			String[] eventNames = attributes.getEventNames();
			if (eventNames.length == 1)
			{
				attributesJson.put("e", eventNames[0]);
			}
			else
			{
				for (String eventName : eventNames)
				{
					attributesJson.append("e", eventName);
				}
			}

			AjaxChannel channel = attributes.getChannel();
			if (channel != null)
			{
				attributesJson.put("ch", channel);
			}

			if (attributes.isAllowDefault())
			{
				attributesJson.put("ad", true);
			}

			Duration requestTimeout = attributes.getRequestTimeout();
			if (requestTimeout != null)
			{
				attributesJson.put("rt", requestTimeout.getMilliseconds());
			}

			boolean wicketAjaxResponse = attributes.isWicketAjaxResponse();
			if (wicketAjaxResponse == false)
			{
				attributesJson.put("wr", false);
			}

			String dataType = attributes.getDataType();
			if (AjaxRequestAttributes.XML_DATA_TYPE.equals(dataType) == false)
			{
				attributesJson.put("dt", dataType);
			}

			ThrottlingSettings throttlingSettings = attributes.getThrottlingSettings();
			if (throttlingSettings != null)
			{
				JSONObject throttlingSettingsJson = new JSONObject();
				throttlingSettingsJson.put("id", throttlingSettings.getId());
				throttlingSettingsJson.put("d", throttlingSettings.getDelay().getMilliseconds());
				if (throttlingSettings.getPostponeTimerOnUpdate())
				{
					throttlingSettingsJson.put("p", true);
				}
				attributesJson.put("tr", throttlingSettingsJson);
			}

			postprocessConfiguration(attributesJson, component);
		}
		catch (JSONException e)
		{
			throw new WicketRuntimeException(e);
		}

		String attributesAsJson = attributesJson.toString();

		return attributesAsJson;
	}

	private void appendListenerHandler(final CharSequence handler, final JSONObject attributesJson,
		final String propertyName, final String functionTemplate) throws JSONException
	{
		if (Strings.isEmpty(handler) == false)
		{
			final JsonFunction function;
			if (handler instanceof JsonFunction)
			{
				function = (JsonFunction)handler;
			}
			else
			{
				String func = String.format(functionTemplate, handler);
				function = new JsonFunction(func);
			}
			attributesJson.append(propertyName, function);
		}
	}

	/**
	 * Gives a chance to modify the JSON attributesJson that is going to be used as attributes for
	 * the Ajax call.
	 * 
	 * @param attributesJson
	 *            the JSON object created by #renderAjaxAttributes()
	 * @param component
	 *            the component with the attached Ajax behavior
	 * @throws JSONException
	 *             thrown if an error occurs while modifying {@literal attributesJson} argument
	 */
	protected void postprocessConfiguration(JSONObject attributesJson, Component component)
		throws JSONException
	{
	}

	/**
	 * @return javascript that will generate an ajax GET request to this behavior with its assigned
	 *         component
	 */
	public CharSequence getCallbackScript()
	{
		return getCallbackScript(getComponent());
	}

	/**
	 * @param component
	 *            the component to use when generating the attributes
	 * @return script that can be used to execute this Ajax behavior.
	 */
	// 'protected' because this method is intended to be called by other Behavior methods which
	// accept the component as parameter
	protected CharSequence getCallbackScript(final Component component)
	{
		CharSequence ajaxAttributes = renderAjaxAttributes(component);
		return "Wicket.Ajax.ajax(" + ajaxAttributes + ");";
	}

	/**
	 * Generates a javascript function that can take parameters and performs an AJAX call which
	 * includes these parameters. The generated code looks like this:
	 * 
	 * <pre>
	 * function(param1, param2) {
	 *    var attrs = attrsJson;
	 *    var params = {'param1': param1, 'param2': param2};
	 *    attrs.ep = jQuery.extend(attrs.ep, params);
	 *    Wicket.Ajax.ajax(attrs);
	 * }
	 * </pre>
	 * 
	 * @param extraParameters
	 * @return A function that can be used as a callback function in javascript
	 */
	public CharSequence getCallbackFunction(CallbackParameter... extraParameters)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("function (");
		boolean first = true;
		for (CallbackParameter curExtraParameter : extraParameters)
		{
			if (curExtraParameter.getFunctionParameterName() != null)
			{
				if (!first)
					sb.append(',');
				else
					first = false;
				sb.append(curExtraParameter.getFunctionParameterName());
			}
		}
		sb.append(") {\n");
		sb.append(getCallbackFunctionBody(extraParameters));
		sb.append("}\n");
		return sb;
	}

	/**
	 * Generates the body the {@linkplain #getCallbackFunction(CallbackParameter...) callback
	 * function}. To embed this code directly into a piece of javascript, make sure any context
	 * parameters are available as local variables, global variables or within the closure.
	 * 
	 * @param extraParameters
	 * @return The body of the {@linkplain #getCallbackFunction(CallbackParameter...) callback
	 *         function}.
	 */
	public CharSequence getCallbackFunctionBody(CallbackParameter... extraParameters)
	{
		AjaxRequestAttributes attributes = getAttributes();
		attributes.setEventNames();
		CharSequence attrsJson = renderAjaxAttributes(getComponent(), attributes);
		StringBuilder sb = new StringBuilder();
		sb.append("var attrs = ");
		sb.append(attrsJson);
		sb.append(";\n");
		sb.append("var params = {");
		boolean first = true;
		for (CallbackParameter curExtraParameter : extraParameters)
		{
			if (curExtraParameter.getAjaxParameterName() != null)
			{
				if (!first)
					sb.append(',');
				else
					first = false;
				sb.append('\'')
					.append(curExtraParameter.getAjaxParameterName())
					.append("': ")
					.append(curExtraParameter.getAjaxParameterCode());
			}
		}
		sb.append("};\n");
		if (attributes.getExtraParameters().isEmpty())
			sb.append("attrs.ep = params;\n");
		else
			sb.append("attrs.ep = Wicket.merge(attrs.ep, params);\n");
		sb.append("Wicket.Ajax.ajax(attrs);\n");
		return sb;
	}

	/**
	 * @return an optional javascript expression that determines whether the request will actually
	 *         execute (in form of return XXX;);
	 * @deprecated Use {@link org.apache.wicket.ajax.attributes.AjaxRequestAttributes}
	 */
	@Deprecated
	protected CharSequence getPreconditionScript()
	{
		return null;
	}

	/**
	 * @return javascript that will run when the ajax call finishes with an error status
	 */
	@Deprecated
	protected CharSequence getFailureScript()
	{
		return null;
	}

	/**
	 * @return javascript that will run when the ajax call finishes successfully
	 */
	@Deprecated
	protected CharSequence getSuccessScript()
	{
		return null;
	}

	/**
	 * Provides an AjaxChannel for this Behavior.
	 * 
	 * @return an AjaxChannel - Defaults to null.
	 * @deprecated Use {@link org.apache.wicket.ajax.attributes.AjaxRequestAttributes}
	 */
	@Deprecated
	protected AjaxChannel getChannel()
	{
		return null;
	}

	/**
	 * Finds the markup id of the indicator. The default search order is: component, behavior,
	 * component's parent hierarchy.
	 * 
	 * @return markup id or <code>null</code> if no indicator found
	 */
	protected String findIndicatorId()
	{
		if (getComponent() instanceof IAjaxIndicatorAware)
		{
			return ((IAjaxIndicatorAware)getComponent()).getAjaxIndicatorMarkupId();
		}

		if (this instanceof IAjaxIndicatorAware)
		{
			return ((IAjaxIndicatorAware)this).getAjaxIndicatorMarkupId();
		}

		Component parent = getComponent().getParent();
		while (parent != null)
		{
			if (parent instanceof IAjaxIndicatorAware)
			{
				return ((IAjaxIndicatorAware)parent).getAjaxIndicatorMarkupId();
			}
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.behavior.IBehaviorListener#onRequest()
	 */
	@Override
	public final void onRequest()
	{
		WebApplication app = (WebApplication)getComponent().getApplication();
		AjaxRequestTarget target = app.newAjaxRequestTarget(getComponent().getPage());

		RequestCycle requestCycle = RequestCycle.get();
		requestCycle.scheduleRequestHandlerAfterCurrent(target);

		respond(target);
	}

	/**
	 * @param target
	 *            The AJAX target
	 */
	protected abstract void respond(AjaxRequestTarget target);

}
