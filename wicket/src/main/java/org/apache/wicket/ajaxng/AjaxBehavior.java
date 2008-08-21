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
import java.util.List;

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

		
	private final static ResourceReference YUI_BASE = new JavascriptResourceReference(AjaxBehavior.class, "js/yui3/yui-base/yui-base.js");
	private final static ResourceReference YUI_OOP = new JavascriptResourceReference(AjaxBehavior.class, "js/yui3/oop/oop.js");
	private final static ResourceReference YUI_EVENT = new JavascriptResourceReference(AjaxBehavior.class, "js/yui3/event/event.js");
	private final static ResourceReference YUI_DOM = new JavascriptResourceReference(AjaxBehavior.class, "js/yui3/dom/dom.js");
	private final static ResourceReference YUI_NODE = new JavascriptResourceReference(AjaxBehavior.class, "js/yui3/node/node.js");
	private final static ResourceReference YUI_IO = new JavascriptResourceReference(AjaxBehavior.class, "js/yui3/io/io.js");	
	private final static ResourceReference AJAX_NG = new JavascriptResourceReference(AjaxBehavior.class, "js/wicket-ajax-ng.js");

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
		config.append(WICKET_NS +".ajax.globalSettings.urlPrefix='");
		config.append(prefix);
		config.append("'\n");
		
		config.append(WICKET_NS +".ajax.globalSettings.urlParamComponentId='");
		config.append(AjaxUrlCodingStrategy.PARAM_COMPONENT_ID);
		config.append("'\n");
		
		config.append(WICKET_NS +".ajax.globalSettings.urlParamTimestamp='");
		config.append(AjaxUrlCodingStrategy.PARAM_TIMESTAMP);
		config.append("'\n");
		
		config.append(WICKET_NS +".ajax.globalSettings.urlParamPageId='");
		config.append(AjaxUrlCodingStrategy.PARAM_PAGE_ID);
		config.append("'\n");
				
		config.append(WICKET_NS +".ajax.globalSettings.urlParamFormId='");
		config.append(AjaxUrlCodingStrategy.PARAM_FORM_ID);
		config.append("'\n");
		
		config.append(WICKET_NS +".ajax.globalSettings.urlParamListenerInterface='");
		config.append(AjaxUrlCodingStrategy.PARAM_LISTENER_INTEFACE);
		config.append("'\n");
		
		config.append(WICKET_NS +".ajax.globalSettings.urlParamBehaviorIndex='");
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

	protected String getAttributes(Component component)
	{
		StringBuilder res = new StringBuilder();
		
		res.append("{");
		
		res.append("p:'");		
		Page page = component.getPage();
		if (page.getPageMapName() != null)
		{
			res.append(page.getPageMapName());
			res.append(":");
		}
		res.append(page.getNumericId());
		res.append(":");
		res.append(page.getCurrentVersionNumber());
		
		res.append("'");
		
		
		if (component instanceof Page == false)
		{
			res.append(",c:'");
			res.append(component.getMarkupId());
			res.append("'");
		}
		
		int behaviorIndex = component.getBehaviors().indexOf(this);
		
		res.append(",b:");
		res.append(behaviorIndex);
				
		res.append("}");
		
		return res.toString();
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
