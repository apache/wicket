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
package org.apache.wicket.ajaxng.request;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

/**
 * @author Matej Knopp
 */
public class AjaxUrlCodingStrategy implements IRequestTargetUrlCodingStrategy
{
	private final String mountPath;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 */
	public AjaxUrlCodingStrategy(String mountPath)
	{
		this.mountPath = mountPath;
	}

	private String getParameter(RequestParameters parameters, String key)
	{
		Object o = parameters.getParameters().get(key);
		if (o instanceof String[])
		{
			return ((String[])o)[0];
		}
		else
		{
			return null;
		}
	}

	private Page getPage(RequestParameters parameters)
	{
		String page = getParameter(parameters, PARAM_PAGE_ID);
		String elements[] = page.split(":");
		int pageId;
		String pageMapName = null;
		int version = 0;
		if (elements.length == 2)
		{
			pageId = Integer.valueOf(elements[0]);
			version = Integer.valueOf(elements[1]);
		}
		else if (elements.length == 3)
		{
			pageMapName = elements[0];
			pageId = Integer.valueOf(elements[1]);
			version = Integer.valueOf(elements[2]);
		}
		else
		{
			throw new IllegalStateException("Couldn't parse pageID '" + page + "'");
		}
		return Session.get().getPage(pageMapName, "" + pageId, version);
	}

	private Component getComponent(RequestParameters parameters)
	{
		Page page = getPage(parameters);

		if (page != null)
		{
			final String componentId = getParameter(parameters, PARAM_COMPONENT_ID);
			if (componentId == null)
			{
				return page;
			}
			else
			{
				return (Component)page.visitChildren(new IVisitor<Component>()
				{
					public Object component(Component component)
					{
						if (componentId.equals(component.getMarkupId(false)))
						{
							return component;
						}
						return CONTINUE_TRAVERSAL;
					}
				});
			}
		}
		else
		{
			throw new PageExpiredException("Page Expired");
		}
	}

	public IRequestTarget decode(RequestParameters requestParameters)
	{
		Component component = getComponent(requestParameters);
		if (component == null)
		{
			throw new IllegalStateException("Couldn't find component with id '" +
				getParameter(requestParameters, PARAM_COMPONENT_ID) + "'.");
		}

		int behaviorIndex = Integer.valueOf(getParameter(requestParameters, PARAM_BEHAVIOR_INDEX));

		int urlDepth = Integer.valueOf(getParameter(requestParameters, PARAM_URL_DEPTH));

		RequestCycle.get().getRequest().getRequestParameters().setUrlDepth(urlDepth);

		return new AjaxRequestTarget(component, behaviorIndex);
	}

	public CharSequence encode(IRequestTarget requestTarget)
	{
		// we need this as the prefix for the ajax configuration
		return getMountPath();
	}

	public String getMountPath()
	{
		return mountPath;
	}

	private static final String PARAM_PREFIX = "wicketNG:";
	
	/** Timestamp query parameter */
	public static final String PARAM_TIMESTAMP = PARAM_PREFIX + "timestamp";
	
	/** ComponentId query parameter */
	public static final String PARAM_COMPONENT_ID = PARAM_PREFIX + "componentId";
	
	/** PageId query parameter */
	public static final String PARAM_PAGE_ID = PARAM_PREFIX + "pageId";
	
	/** FormId query parameter */
	public static final String PARAM_FORM_ID = PARAM_PREFIX + "formId";
	
	/** Listener interface query parameter */
	public static final String PARAM_LISTENER_INTEFACE = PARAM_PREFIX + "listenerInterface";
	
	/** Behavior index query parameter */
	public static final String PARAM_BEHAVIOR_INDEX = PARAM_PREFIX + "behaviorIndex";
	
	/** URL Depth query parameter */
	public static final String PARAM_URL_DEPTH = PARAM_PREFIX + "urlDepth";

	public boolean matches(IRequestTarget requestTarget)
	{
		return requestTarget instanceof AjaxRequestTarget;
	}

	public boolean matches(String path)
	{
		return path.startsWith(getMountPath());
	}

}
