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
package org.apache._wicket.request.handler.resource;

import java.util.Locale;

import org.apache._wicket.PageParameters;
import org.apache._wicket.RequestCycle;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.resource.Resource;

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
		if (resource == null)
		{
			throw new IllegalArgumentException("Argument 'resource' may not be null.");
		}
		this.resource = resource;
		this.locale = locale;
		this.style = style;
		this.pageParameters = pageParameters != null ? pageParameters : new PageParameters();
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
