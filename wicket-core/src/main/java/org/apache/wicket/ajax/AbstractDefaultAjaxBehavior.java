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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.JavaScriptAfterHandler;
import org.apache.wicket.ajax.attributes.JavaScriptBeforeHandler;
import org.apache.wicket.ajax.attributes.JavaScriptFailureHandler;
import org.apache.wicket.ajax.attributes.JavaScriptPrecondition;
import org.apache.wicket.ajax.attributes.JavaScriptSuccessHandler;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
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
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#renderHead(Component, org.apache.wicket.markup.head.IHeaderResponse)
	 */
	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		CoreLibrariesContributor.contributeAjax(component.getApplication(), response);

		Url baseUrl = RequestCycle.get().getUrlRenderer().getBaseUrl();
		CharSequence ajaxBaseUrl = Strings.escapeMarkup(baseUrl.toString());
		response.render(JavaScriptHeaderItem.forScript("Wicket.Ajax.baseUrl=\"" + ajaxBaseUrl +
				"\";", "wicket-ajax-base-url"));

		renderExtraHeaderContributors(component, response);
	}

	/**
	 * Renders header contribution by JavaScriptFunctionBody instances which additionally implement
	 * IComponentAwareHeaderContributor interface.
	 *
	 * @param component
	 *      the component assigned to this behavior
	 * @param response
	 *      the current header response
	 */
	private void renderExtraHeaderContributors(Component component, IHeaderResponse response)
	{
		final IAjaxCallDecorator ajaxCallDecorator = getAjaxCallDecorator();
		if (ajaxCallDecorator instanceof IComponentAwareHeaderContributor)
		{
			IComponentAwareHeaderContributor contributor = (IComponentAwareHeaderContributor)ajaxCallDecorator;
			contributor.renderHead(component, response);
		}

		AjaxRequestAttributes attributes = getAttributes();

		List<JavaScriptBeforeHandler> beforeHandlers = attributes.getBeforeHandlers();
		for (JavaScriptBeforeHandler beforeHandler : beforeHandlers) {
			beforeHandler.renderHead(component, response);
		}

		List<JavaScriptAfterHandler> afterHandlers = attributes.getAfterHandlers();
		for (JavaScriptAfterHandler afterHandler : afterHandlers) {
			afterHandler.renderHead(component, response);
		}

		List<JavaScriptFailureHandler> failureHandlers = attributes.getFailureHandlers();
		for (JavaScriptFailureHandler failureHandler : failureHandlers) {
			failureHandler.renderHead(component, response);
		}

		List<JavaScriptSuccessHandler> successHandlers = attributes.getSuccessHandlers();
		for (JavaScriptSuccessHandler successHandler : successHandlers) {
			successHandler.renderHead(component, response);
		}

		List<JavaScriptPrecondition> preconditions = attributes.getPreconditions();
		for (JavaScriptPrecondition precondition : preconditions) {
			precondition.renderHead(component, response);
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
	private void updateAjaxAttributesBackwardCompatibility(AjaxRequestAttributes attributes)
	{
		IAjaxCallDecorator callDecorator = getAjaxCallDecorator();
		if (callDecorator != null)
		{
			CharSequence onSuccessScript = callDecorator.decorateOnSuccessScript(getComponent(),
				getSuccessScript());

			if (onSuccessScript != null)
			{
				JavaScriptSuccessHandler successHandler = new JavaScriptSuccessHandler(onSuccessScript);
				attributes.getSuccessHandlers().add(successHandler);
			}

			CharSequence onFailureScript = callDecorator.decorateOnFailureScript(getComponent(),
				getFailureScript());

			if (onFailureScript != null)
			{
				JavaScriptFailureHandler failureHandler = new JavaScriptFailureHandler(onFailureScript);
				attributes.getFailureHandlers().add(failureHandler);
			}
		}

		CharSequence preconditionScript = getPreconditionScript();
		if (preconditionScript != null)
		{
			JavaScriptPrecondition precondition = new JavaScriptPrecondition(preconditionScript);
			attributes.getPreconditions().add(precondition);
		}

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
	 * 					m: 'POST',		// method name. Default: 'GET'
	 * 					c: 'label7',	// component id (String) or window for page
	 * 					e: 'click',		// event name
	 * 					sh: [],			// list of success handlers
	 * 					fh: [],			// list of failure handlers
	 * 					pre: [],		// list of preconditions. If empty set default : Wicket.$(settings{c}) !== null
	 * 					ep: {},			// extra parameters
	 * 					async: true|false,	// asynchronous XHR or not
	 * 					ch: 'someName|d',	// AjaxChannel
	 * 					i: 'indicatorId',	// indicator component id
	 * 					ad: true,			// allow default
	 * 					
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

			for (JavaScriptBeforeHandler bh : attributes.getBeforeHandlers())
			{
				attributesJson.append("bh", bh);
			}

			for (JavaScriptAfterHandler ah : attributes.getAfterHandlers())
			{
				attributesJson.append("ah", ah);
			}

			for (JavaScriptSuccessHandler sh : attributes.getSuccessHandlers())
			{
				attributesJson.append("sh", sh);
			}

			for (JavaScriptFailureHandler fh : attributes.getFailureHandlers())
			{
				attributesJson.append("fh", fh);
			}

			for (JavaScriptPrecondition pre : attributes.getPreconditions())
			{
				attributesJson.append("pre", pre);
			}

			JSONObject extraParameters = new JSONObject();
			Iterator<Entry<String, Object>> itor = attributes.getExtraParameters()
				.entrySet()
				.iterator();
			while (itor.hasNext())
			{
				Entry<String, Object> entry = itor.next();
				String name = entry.getKey();
				Object value = entry.getValue();
				extraParameters.accumulate(name, value);
			}
			if (extraParameters.length() > 0)
			{
				attributesJson.put("ep", extraParameters);
			}

			List<CharSequence> urlArgumentMethods = attributes.getDynamicExtraParameters();
			if (urlArgumentMethods != null)
			{
				for (CharSequence urlArgument : urlArgumentMethods)
				{
					attributesJson.append("dep", urlArgument);
				}
			}

			if (attributes.isAsynchronous() == false)
			{
				attributesJson.put("async", false);
			}

			String eventName = attributes.getEventName();
			if (Strings.isEmpty(eventName) == false)
			{
				attributesJson.put("e", eventName);
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

			Integer requestTimeout = attributes.getRequestTimeout();
			if (requestTimeout != null)
			{
				attributesJson.put("rt", requestTimeout);
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

			postprocessConfiguration(attributesJson, component);
		}
		catch (JSONException e)
		{
			throw new RuntimeException(e);
		}

		String attributesAsJson = attributesJson.toString();

		return attributesAsJson;
	}

	protected void postprocessConfiguration(JSONObject object, Component component)
		throws JSONException
	{
	}

	/**
	 * @return ajax call decorator used to decorate the call generated by this behavior or null for
	 *         none
	 * @deprecated Use {@link org.apache.wicket.ajax.attributes.AjaxRequestAttributes} instead
	 */
	@Deprecated
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	/**
	 * @return javascript that will generate an ajax GET request to this behavior
	 */
	protected CharSequence getCallbackScript()
	{
		CharSequence attrsJson = renderAjaxAttributes(getComponent());
		return "Wicket.Ajax.get(" + attrsJson + ")";
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
	 * Returns javascript that performs an ajax callback to this behavior. The script is decorated
	 * by the ajax callback decorator from
	 * {@link AbstractDefaultAjaxBehavior#getAjaxCallDecorator()}.
	 * 
	 * @param partialCall
	 *            JavaScript of a partial call to the function performing the actual ajax callback.
	 *            Must be in format <code>function(params,</code> with signature
	 *            <code>function(params, onSuccessHandler, onFailureHandler</code>. Example:
	 *            <code>wicketAjaxGet('callbackurl'</code>
	 * 
	 * @return script that performs ajax callback to this behavior
	 */
	@Deprecated
	protected CharSequence generateCallbackScript(final CharSequence partialCall)
	{
		final CharSequence onSuccessScript = getSuccessScript();
		final CharSequence onFailureScript = getFailureScript();
		final CharSequence precondition = getPreconditionScript();

		final IAjaxCallDecorator decorator = getAjaxCallDecorator();

		String indicatorId = findIndicatorId();

		CharSequence success = (onSuccessScript == null) ? "" : onSuccessScript;
		CharSequence failure = (onFailureScript == null) ? "" : onFailureScript;

		if (decorator != null)
		{
			success = decorator.decorateOnSuccessScript(getComponent(), success);
		}

		if (!Strings.isEmpty(indicatorId))
		{
			String hide = "; Wicket.DOM.hideIncrementally('" + indicatorId + "');";
			success = success + hide;
			failure = failure + hide;
		}

		if (decorator != null)
		{
			failure = decorator.decorateOnFailureScript(getComponent(), failure);
		}

		AppendingStringBuffer buff = new AppendingStringBuffer(256);
		buff.append("var ").append(IAjaxCallDecorator.WICKET_CALL_RESULT_VAR).append("=");
		buff.append(partialCall);

		buff.append(", Wicket.bind(function() { ").append(success).append("}, this)");
		buff.append(", Wicket.bind(function() { ").append(failure).append("}, this)");

		if (precondition != null)
		{
			buff.append(", Wicket.bind(function() {");
			buff.append(precondition);
			buff.append("}, this)");
		}

		AjaxChannel channel = getChannel();
		if (channel != null)
		{
			if (precondition == null)
			{
				buff.append(", null");
			}
			buff.append(", '");
			buff.append(channel.getChannelName());
			buff.append("'");
		}

		buff.append(");");

		CharSequence call = buff;

		if (!Strings.isEmpty(indicatorId))
		{
			final AppendingStringBuffer indicatorWithPrecondition = new AppendingStringBuffer(
				"if (");
			if (precondition != null)
			{
				indicatorWithPrecondition.append("Wicket.bind(function(){")
					.append(precondition)
					.append("}, this)()");
			}
			else
			{
				indicatorWithPrecondition.append("true");
			}
			indicatorWithPrecondition.append(") { Wicket.DOM.showIncrementally('")
				.append(indicatorId)
				.append("');}")
				.append(call);

			call = indicatorWithPrecondition;
		}

		if (decorator != null)
		{
			call = decorator.decorateScript(getComponent(), call);
		}

		return call;
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
	// TODO rename this to onEvent or something? respond is mostly the same as
	// onRender
	// this is not the case this is still the event handling period. respond is
	// called
	// in the RequestCycle on the AjaxRequestTarget..
	protected abstract void respond(AjaxRequestTarget target);

	/**
	 * Wraps the provided javascript with a throttled block. Throttled behaviors only execute once
	 * within the given delay even though they are triggered multiple times.
	 * <p>
	 * For example, this is useful when attaching an event behavior to the onkeypress event. It is
	 * not desirable to have an ajax call made every time the user types so we throttle that call to
	 * a desirable delay, such as once per second. This gives us a near real time ability to provide
	 * feedback without overloading the server with ajax calls.
	 * 
	 * @param script
	 *            javascript to be throttled
	 * @param throttleId
	 *            the id of the throttle to be used. Usually this should remain constant for the
	 *            same javascript block.
	 * @param throttleDelay
	 *            time span within which the javascript block will only execute once
	 * @return wrapped javascript
	 */
	public static CharSequence throttleScript(CharSequence script, String throttleId,
		Duration throttleDelay)
	{
		Args.notEmpty(script, "script");
		Args.notEmpty(throttleId, "throttleId");
		Args.notNull(throttleDelay, "throttleDelay");

		return new AppendingStringBuffer("Wicket.throttler.throttle( '").append(throttleId)
			.append("', ")
			.append(throttleDelay.getMilliseconds())
			.append(", Wicket.bind(function() { ")
			.append(script)
			.append("}, this));");
	}
}
