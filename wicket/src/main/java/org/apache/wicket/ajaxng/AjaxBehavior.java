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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajaxng.json.JSONArray;
import org.apache.wicket.ajaxng.json.JSONFunction;
import org.apache.wicket.ajaxng.json.JSONObject;
import org.apache.wicket.ajaxng.request.AjaxRequestTarget;
import org.apache.wicket.ajaxng.request.AjaxUrlCodingStrategy;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;

/**
 * @author Matej Knopp
 */
public abstract class AjaxBehavior implements IBehavior
{

	private static final long serialVersionUID = 1L;

	private final List<Component> boundComponents = new ArrayList<Component>(1);

	/**
	 * Construct.
	 */
	public AjaxBehavior()
	{
	}

	/**
	 * The respond method is invoked during an Ajax request for this behavior.
	 * 
	 * @param target
	 */
	public abstract void respond(AjaxRequestTarget target);

	private final static ResourceReference YUI_BASE = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/yui-base/yui-base.js");
	private final static ResourceReference YUI_OOP = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/oop/oop.js");
	private final static ResourceReference YUI_EVENT = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/event/event.js");
	private final static ResourceReference YUI_DOM = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/dom/dom.js");
	private final static ResourceReference YUI_NODE = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/node/node.js");
	private final static ResourceReference YUI_IO = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/io/io.js");
	private final static ResourceReference YUI_GET = new JavascriptResourceReference(
		AjaxBehavior.class, "js/yui3/get/get.js");
	private final static ResourceReference AJAX_NG = new JavascriptResourceReference(
		AjaxBehavior.class, "js/wicket-ajax-ng.js");

	/**
	 * Wicket javascript namespace.
	 */
	public final static String WICKET_NS = "W";

	public void renderHead(Component component, IHeaderResponse response)
	{
		response.renderJavascriptReference(YUI_BASE);
		response.renderJavascriptReference(YUI_OOP);
		response.renderJavascriptReference(YUI_EVENT);
		response.renderJavascriptReference(YUI_DOM);
		response.renderJavascriptReference(YUI_NODE);
		response.renderJavascriptReference(YUI_IO);
		response.renderJavascriptReference(YUI_GET);
		response.renderJavascriptReference(AJAX_NG);

		CharSequence prefix = RequestCycle.get().urlFor(AjaxRequestTarget.DUMMY);

		StringBuilder config = new StringBuilder();
		
		config.append("(function() {\n");
		
		config.append("var gs = " + WICKET_NS + ".ajax.globalSettings;\n");
		
		config.append("gs.urlPrefix='");
		config.append(prefix);
		config.append("';\n");

		config.append("gs.defaultPageId='");
		config.append(getPageId(component.getPage()));
		config.append("';\n");
		
		config.append("gs.urlParamComponentId='");
		config.append(AjaxUrlCodingStrategy.PARAM_COMPONENT_ID);
		config.append("';\n");

		config.append("gs.urlParamTimestamp='");
		config.append(AjaxUrlCodingStrategy.PARAM_TIMESTAMP);
		config.append("';\n");

		config.append("gs.urlParamPageId='");
		config.append(AjaxUrlCodingStrategy.PARAM_PAGE_ID);
		config.append("';\n");

		config.append("gs.urlParamFormId='");
		config.append(AjaxUrlCodingStrategy.PARAM_FORM_ID);
		config.append("';\n");

		config.append("gs.urlParamListenerInterface='");
		config.append(AjaxUrlCodingStrategy.PARAM_LISTENER_INTEFACE);
		config.append("';\n");

		config.append("gs.urlParamBehaviorIndex='");
		config.append(AjaxUrlCodingStrategy.PARAM_BEHAVIOR_INDEX);
		config.append("';\n");
		
		config.append("gs.urlParamUrlDepth='");
		config.append(AjaxUrlCodingStrategy.PARAM_URL_DEPTH);
		config.append("';\n");
		
		config.append("gs.urlDepthValue=");
		config.append(getUrlDepth());
		config.append(";\n");
		
		config.append("})();");

		response.renderJavascript(config, WICKET_NS + "-Config");
	}

	private int getUrlDepth()
	{
		Request request = RequestCycle.get().getRequest();
		if (request instanceof ServletWebRequest)
		{
			ServletWebRequest swr = (ServletWebRequest)request;
			// If we're coming in with an existing depth, use it. Otherwise,
			// compute from the URL. This provides correct behavior for repeated
			// AJAX requests: If we need to generate a URL within an AJAX
			// request for another one, it needs to be at the same depth as the
			// original AJAX request.
			int urlDepth = swr.getRequestParameters().getUrlDepth();
			return urlDepth > -1 ? urlDepth : swr.getDepthRelativeToWicketHandler();
		}
		else
		{
			return -1;
		}
	}
	
	public void afterRender(Component component)
	{
	}

	public void beforeRender(Component component)
	{
	}

	public void bind(Component component)
	{
		if (boundComponents.contains(component) == false)
		{
			boundComponents.add(component);
			component.setOutputMarkupId(true);
		}
	}

	/**
	 * Returns a list of components this behavior is bound to.
	 * 
	 * @return list of components
	 */
	public List<Component> getBoundComponents()
	{
		return Collections.unmodifiableList(boundComponents);
	}

	/**
	 * Renders the javascript object with Ajax request attributes. The object can be used as
	 * argument for <code>RequestQueueItem</code> constructor.
	 * 
	 * @param component
	 * @return attributes javascript object rendered as string.
	 */
	public String renderAttributes(Component component)
	{
		JSONObject o = new JSONObject();

		if (component instanceof Page == false)
		{
			o.put("c", component.getMarkupId());
		}

		int behaviorIndex = component.getBehaviors().indexOf(this);

		o.put("b", behaviorIndex);

		renderAttributes(component, getAttributes(), o);

		return o.toString();
	}

	private CharSequence getPageId(Page page)
	{
		StringBuilder res = new StringBuilder(5);

		if (page.getPageMapName() != null)
		{
			res.append(page.getPageMapName());
			res.append(":");
		}
		res.append(page.getNumericId());
		res.append(":");
		res.append(page.getCurrentVersionNumber());

		return res;
	}


	private void renderFunctionList(JSONObject target, String attributeName, FunctionList list)
	{
		if (list != null && !list.isEmpty())
		{
			if (list.size() == 1)
			{
				target.put(attributeName, new JSONFunction(list.get(0)));
			}
			else
			{
				JSONArray a = new JSONArray();
				for (String s : list)
				{
					a.put(new JSONFunction(s));
				}
				target.put(attributeName, a);
			}
		}
	}

	private void renderAttributes(Component component, AjaxRequestAttributes attributes,
		JSONObject o)
	{
		if (attributes.getForm() != null)
		{
			o.put("f", attributes.getForm().getMarkupId());
		}
		o.put("m", attributes.isMultipart());
		o.put("rt", attributes.getRequesTimeout());
		o.put("pt", attributes.getProcessingTimeout());
		o.put("t", attributes.getToken());
		o.put("r", attributes.isRemovePrevious());
		o.put("th", attributes.getThrottle());
		o.put("thp", attributes.isThrottlePostpone());

		renderFunctionList(o, "pr", attributes.getPreconditions());
		renderFunctionList(o, "be", attributes.getBeforeHandlers());
		renderFunctionList(o, "s", attributes.getSuccessHandlers());
		renderFunctionList(o, "e", attributes.getErrorHandlers());
		renderFunctionList(o, "rqi", attributes.getRequestQueueItemCreationListeners());

		Map<String, Object> urlArguments = attributes.getUrlArguments();
		if (urlArguments != null && !urlArguments.isEmpty())
		{
			JSONObject args = new JSONObject();
			for (String s : urlArguments.keySet())
			{
				args.put(s, urlArguments.get(s));
			}
		}

		renderFunctionList(o, "ua", attributes.getUrlArgumentMethods());
	}

	public void detach(Component component)
	{
	}

	public void exception(Component component, RuntimeException exception)
	{
	}

	public boolean getStatelessHint(Component component)
	{
		return false;
	}

	public boolean isEnabled(Component component)
	{
		return true;
	}

	public boolean isTemporary()
	{
		return false;
	}

	public void onComponentTag(Component component, ComponentTag tag)
	{
	}

	/**
	 * Returns attributes for Ajax Request.
	 * 
	 * @return {@link AjaxRequestAttributes} instance
	 */
	public AjaxRequestAttributes getAttributes()
	{
		return new AjaxRequestAttributes();
	}
}
