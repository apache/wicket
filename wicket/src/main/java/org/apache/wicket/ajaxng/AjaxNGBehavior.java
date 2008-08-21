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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajaxng.request.AjaxNGRequestTarget;
import org.apache.wicket.ajaxng.request.AjaxNGUrlCodingStrategy;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * @author Matej Knopp
 */
public class AjaxNGBehavior implements IBehavior, IHeaderContributor
{

	private static final long serialVersionUID = 1L;

	private Component component;
	
	/**
	 * Construct.
	 */
	public AjaxNGBehavior()
	{
	}

		
	private final static ResourceReference YUI_BASE = new JavascriptResourceReference(AjaxNGBehavior.class, "js/yui3/yui-base/yui-base.js");
	private final static ResourceReference YUI_OOP = new JavascriptResourceReference(AjaxNGBehavior.class, "js/yui3/oop/oop.js");
	private final static ResourceReference YUI_EVENT = new JavascriptResourceReference(AjaxNGBehavior.class, "js/yui3/event/event.js");
	private final static ResourceReference YUI_DOM = new JavascriptResourceReference(AjaxNGBehavior.class, "js/yui3/dom/dom.js");
	private final static ResourceReference YUI_NODE = new JavascriptResourceReference(AjaxNGBehavior.class, "js/yui3/node/node.js");
	private final static ResourceReference YUI_IO = new JavascriptResourceReference(AjaxNGBehavior.class, "js/yui3/io/io.js");	
	private final static ResourceReference AJAX_NG = new JavascriptResourceReference(AjaxNGBehavior.class, "js/wicket-ajax-ng.js");

	public final static String JS_PREFIX = "WicketNG";
	
	public void renderHead(IHeaderResponse response)
	{
		response.renderJavascriptReference(YUI_BASE);
		response.renderJavascriptReference(YUI_OOP);
		response.renderJavascriptReference(YUI_EVENT);
		response.renderJavascriptReference(YUI_DOM);
		response.renderJavascriptReference(YUI_NODE);
		response.renderJavascriptReference(YUI_IO);
		response.renderJavascriptReference(AJAX_NG);
		
		CharSequence prefix = RequestCycle.get().urlFor(AjaxNGRequestTarget.DUMMY);
		
		StringBuilder config = new StringBuilder();
		config.append(JS_PREFIX +".ajax.globalSettings.urlPrefix='");
		config.append(prefix);
		config.append("'\n");
		
		config.append(JS_PREFIX +".ajax.globalSettings.urlParamComponentId='");
		config.append(AjaxNGUrlCodingStrategy.PARAM_COMPONENT_ID);
		config.append("'\n");
		
		config.append(JS_PREFIX +".ajax.globalSettings.urlParamTimestamp='");
		config.append(AjaxNGUrlCodingStrategy.PARAM_TIMESTAMP);
		config.append("'\n");
		
		config.append(JS_PREFIX +".ajax.globalSettings.urlParamPageId='");
		config.append(AjaxNGUrlCodingStrategy.PARAM_PAGE_ID);
		config.append("'\n");
				
		config.append(JS_PREFIX +".ajax.globalSettings.urlParamFormId='");
		config.append(AjaxNGUrlCodingStrategy.PARAM_FORM_ID);
		config.append("'\n");
		
		config.append(JS_PREFIX +".ajax.globalSettings.urlParamListenerInterface='");
		config.append(AjaxNGUrlCodingStrategy.PARAM_LISTENER_INTEFACE);
		config.append("'\n");
		
		config.append(JS_PREFIX +".ajax.globalSettings.urlParamBehaviorIndex='");
		config.append(AjaxNGUrlCodingStrategy.PARAM_BEHAVIOR_INDEX);
		config.append("'\n");
		
		response.renderJavascript(config, JS_PREFIX + "-Config");
	}

	public void afterRender(Component component)
	{
	}

	public void beforeRender(Component component)
	{
	}

	public void bind(Component component)
	{
		if (this.component != null && this.component != component)
		{
			throw new IllegalStateException("The behavior can be only bound to one component.");			
		}
		this.component = component;
		component.setOutputMarkupId(true);
	}

	protected String getAttributes()
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

}
