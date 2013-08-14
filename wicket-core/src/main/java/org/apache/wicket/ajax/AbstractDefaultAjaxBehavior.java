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
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.strategies.IAjaxStrategy;
import org.apache.wicket.ajax.strategies.Wicket6AjaxStrategy;
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
		response.render(JavaScriptHeaderItem.forScript("Wicket.Ajax.baseUrl=\"" + ajaxBaseUrl
			+ "\";", "wicket-ajax-base-url"));

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
		WebApplication application = (WebApplication)getComponent().getApplication();
		AjaxRequestTargetListenerCollection ajaxRequestTargetListeners = application
			.getAjaxRequestTargetListeners();
		for (AjaxRequestTarget.IListener listener : ajaxRequestTargetListeners)
		{
			listener.updateAjaxAttributes(attributes);
		}
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

	public final CharSequence renderAjaxAttributes()
	{
		AjaxRequestAttributes attributes = getAttributes();
		return renderAjaxAttributes(getComponent(), attributes);
	}

	/**
	 * 
	 * @param component
	 * @param attributes
	 * @return the attributes as string in JSON format
	 */
	protected final JSONObject getAjaxAttributes(final Component component, AjaxRequestAttributes attributes)
	{
		return getAjaxStrategy().getAjaxAttributes(this, component, attributes);
	}

	/**
	 *
	 * @param component
	 * @param attributes
	 * @return the attributes as string in JSON format
	 */
	protected final CharSequence renderAjaxAttributes(final Component component, AjaxRequestAttributes attributes)
	{
		return getAjaxAttributes(component, attributes).toString();
	}

	/**
	 * Gives a chance to modify the JSON attributesJson that is going to be used as attributes for
	 * the Ajax call.
	 * 
	 * @param attributesJson
	 *            the JSON object created by #getAjaxAttributes()
	 * @param component
	 *            the component with the attached Ajax behavior
	 * @throws JSONException
	 *             thrown if an error occurs while modifying {@literal attributesJson} argument
	 */
	public void postprocessConfiguration(JSONObject attributesJson, Component component)
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
		AjaxRequestAttributes attributes = getAttributes();
		JSONObject ajaxAttributes = getAjaxAttributes(component, attributes);
		return getAjaxStrategy().getCallbackScript(component, ajaxAttributes);
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
		return getAjaxStrategy().getCallbackFunction(getAttributes(), extraParameters);
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
		return getAjaxStrategy().getCallbackFunctionBody(getAttributes(), extraParameters);
	}

	/**
	 * Finds the markup id of the indicator. The default search order is: component, behavior,
	 * component's parent hierarchy.
	 * 
	 * @return markup id or <code>null</code> if no indicator found
	 */
	// TODO: REVIEW: changed to public to be able to access it from IAjaxStrategy
	// an option is to pass it as parameter to org.apache.wicket.ajax.strategies.IAjaxStrategy.getAjaxAttributes()
	public String findIndicatorId()
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

	protected IAjaxStrategy getAjaxStrategy()
	{
		IAjaxStrategy ajaxStrategy;
		if (WebApplication.exists())
		{
			ajaxStrategy = WebApplication.get().getAjaxSettings().getAjaxStrategy();
		}
		else
		{
			ajaxStrategy = new Wicket6AjaxStrategy();
		}
		return ajaxStrategy;
	}

	/**
	 * @param target
	 *            The AJAX target
	 */
	protected abstract void respond(AjaxRequestTarget target);

}
