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
package org.apache.wicket.ng.request.handler.resource;

import java.util.Locale;

import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.resource.Resource;
import org.apache.wicket.util.lang.Checks;

/**
 * Request handler that renders a resource.
 * 
 * @author Matej Knopp
 */
public class ResourceRequestHandler implements RequestHandler
{
	private final Resource resource;
	private final Locale locale;
	private final String style;
	private final PageParameters pageParameters;

	/**
	 * Construct.
	 * 
	 * @param resource
	 * @param locale
	 * @param style
	 * @param pageParameters
	 */
	public ResourceRequestHandler(Resource resource, Locale locale, String style,
		PageParameters pageParameters)
	{
		Checks.argumentNotNull(resource, "resource");

		this.resource = resource;
		this.locale = locale;
		this.style = style;
		this.pageParameters = pageParameters != null ? pageParameters : new PageParameters();
	}

	/**
	 * @return locale
	 */
	public Locale getLocale()
	{
		return locale;
	}
	
	/**
	 * @return style
	 */
	public String getStyle()
	{
		return style;
	}
	
	/**
	 * @return page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}
	
	/**
	 * @return resource
	 */
	public Resource getResource()
	{
		return resource;
	}

	public void respond(RequestCycle requestCycle)
	{
		Resource.Attributes a = new Resource.Attributes(requestCycle.getRequest(),
			requestCycle.getResponse(), locale, style, pageParameters);
		resource.respond(a);
	}

	public void detach(RequestCycle requestCycle)
	{
	}
}
