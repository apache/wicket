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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajaxng.request.AjaxRequestTarget;
import org.apache.wicket.ajaxng.request.AjaxUrlCodingStrategy;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * @author Matej Knopp
 */
public class AjaxBehavior implements IBehavior
{

	private static final long serialVersionUID = 1L;

	private final List<Component> boundComponents = new ArrayList<Component>();

	/**
	 * Construct.
	 */
	public AjaxBehavior()
	{
	}

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
	private final static ResourceReference AJAX_NG = new JavascriptResourceReference(
		AjaxBehavior.class, "js/wicket-ajax-ng.js");

	/**
	 * Wicket javascript namespace.
	 */
	public final static String WICKET_NS = "WicketNG";

	public void renderHead(Component component, IHeaderResponse response)
	{
		response.renderJavascriptReference(YUI_BASE);
		response.renderJavascriptReference(YUI_OOP);
		response.renderJavascriptReference(YUI_EVENT);
		response.renderJavascriptReference(YUI_DOM);
		response.renderJavascriptReference(YUI_NODE);
		response.renderJavascriptReference(YUI_IO);
		response.renderJavascriptReference(AJAX_NG);

		CharSequence prefix = RequestCycle.get().urlFor(AjaxRequestTarget.DUMMY);

		StringBuilder config = new StringBuilder();
		config.append(WICKET_NS + ".ajax.globalSettings.urlPrefix='");
		config.append(prefix);
		config.append("'\n");

		config.append(WICKET_NS + ".ajax.globalSettings.urlParamComponentId='");
		config.append(AjaxUrlCodingStrategy.PARAM_COMPONENT_ID);
		config.append("'\n");

		config.append(WICKET_NS + ".ajax.globalSettings.urlParamTimestamp='");
		config.append(AjaxUrlCodingStrategy.PARAM_TIMESTAMP);
		config.append("'\n");

		config.append(WICKET_NS + ".ajax.globalSettings.urlParamPageId='");
		config.append(AjaxUrlCodingStrategy.PARAM_PAGE_ID);
		config.append("'\n");

		config.append(WICKET_NS + ".ajax.globalSettings.urlParamFormId='");
		config.append(AjaxUrlCodingStrategy.PARAM_FORM_ID);
		config.append("'\n");

		config.append(WICKET_NS + ".ajax.globalSettings.urlParamListenerInterface='");
		config.append(AjaxUrlCodingStrategy.PARAM_LISTENER_INTEFACE);
		config.append("'\n");

		config.append(WICKET_NS + ".ajax.globalSettings.urlParamBehaviorIndex='");
		config.append(AjaxUrlCodingStrategy.PARAM_BEHAVIOR_INDEX);
		config.append("'\n");

		response.renderJavascript(config, WICKET_NS + "-Config");
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
	
	public List<Component> getBoundComponents()
	{
		return Collections.unmodifiableList(boundComponents);
	}

	/**
	 * Renders the javascript object with Ajax request attributes. The object can be used
	 * as argument for <code>RequestQueueItem</code> constructor.
	 * 
	 * @param component
	 * @return attributes javascript object rendered as string.
	 */
	public String renderAttributes(Component component)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("p", escapeJavascriptString(getPageId(component.getPage())));

		if (component instanceof Page == false)
		{
			map.put("c", escapeJavascriptString(component.getMarkupId()));
		}

		int behaviorIndex = component.getBehaviors().indexOf(this);

		map.put("b", behaviorIndex);

		renderAttributes(component, getAttributes(), map);

		return renderMap(map);
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

	private String renderMap(Map<String, Object> map)
	{
		StringBuilder res = new StringBuilder();

		if (map == null)
		{
			return "{}";
		}
		
		res.append("{");
		boolean first = true;
		for (String s : map.keySet())
		{
			Object value = map.get(s);
			
			if (value == null)
			{
				continue;
			}
			
			if (!first)
			{
				res.append(",");
			}
			else
			{
				first = false;
			}
			
			res.append(s);
		
			res.append(":");
		
			res.append(value);

		}
		res.append("}");

		return res.toString();
	}

	private CharSequence escapeJavascriptString(CharSequence s)
	{		
		if (s == null)
		{
			return null;
		}
		StringBuilder res = new StringBuilder(s.length() + 2);

		res.append("'");
		
		for (int i = 0; i < s.length(); ++i)
		{
			char c = s.charAt(i);
			switch (c)
			{
				case '\'' :
					res.append("\\'");
					break;
				case '\"' :
					res.append("\\\"");
					break;
				case '\\':
					res.append("\\\\");
					break;
				case '\n' :
					res.append("\\n");					
					break;
				case '\r' :
					res.append("\\r");
					break;
				case '\t' :
					res.append("\\t");
					break;
				default:
					res.append(c);
			}
		}
		
		res.append("'");

		return res;
	}

	private CharSequence renderFunctionList(FunctionList list)
	{
		if (list == null || list.isEmpty())
		{
			return null;
		}
		else if (list.size() == 1)
		{
			return list.get(0);
		}
		else
		{
			StringBuilder res = new StringBuilder();
			boolean first = true;
			res.append("[");
			
			for (int i = 0; i < list.size(); ++i)				
			{
				String s = list.get(i);
				
				if (!first)
				{
					res.append(",");
				}
				else
				{
					first = false;
				}
				
				res.append(s);
			}
			
			res.append("]");
			return res;
		}		
	}
	
	private Map<String, Object> escapeMap(Map<String, Object> map)
	{
		if (map == null)
		{
			return null;
		}
		Map<String, Object> res = new HashMap<String, Object>();
		
		for (String s : map.keySet())
		{
			Object value = map.get(s);
			if (value instanceof Number == false)
			{
				value = escapeJavascriptString((value).toString());
			}
			res.put(escapeJavascriptString(s).toString(), value);
		}
		
		return res;
	}
	
	private void renderAttributes(Component component, AjaxRequestAttributes attributes,
		Map<String, Object> map)
	{
		if (attributes.getForm() != null)
		{
			map.put("f", escapeJavascriptString(attributes.getForm().getMarkupId()));
		}
		map.put("m", attributes.isMultipart());
		map.put("t", attributes.getRequesTimeout());
		map.put("pt", attributes.getProcessingTimeout());
		map.put("t", escapeJavascriptString(attributes.getToken()));
		map.put("r", attributes.isRemovePrevious());
		map.put("th", attributes.getThrottle());
		map.put("thp", attributes.isThrottlePostpone());
		map.put("pr", renderFunctionList(attributes.getPreconditions()));
		map.put("be", renderFunctionList(attributes.getBeforeHandlers()));
		map.put("s", renderFunctionList(attributes.getSuccessHandlers()));
		map.put("e", renderFunctionList(attributes.getErrorHandlers()));

		Map<String, Object> urlArguments = attributes.getUrlArguments();
		if (urlArguments != null && !urlArguments.isEmpty())
		{
			map.put("u", renderMap(escapeMap(urlArguments)));
		}
		
		map.put("ua", renderFunctionList(attributes.getUrlArgumentMethods()));
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
		return new AjaxRequestAttributesImpl();
	}
}
