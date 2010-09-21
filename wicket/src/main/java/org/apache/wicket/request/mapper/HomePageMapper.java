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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.lang.Args;

/**
 * Mapper for rendering home page.
 * 
 * @author Matej Knopp
 */
public class HomePageMapper extends AbstractComponentMapper
{
	private final IPageParametersEncoder pageParametersEncoder;

	/**
	 * Construct.
	 */
	public HomePageMapper()
	{
		this(new PageParametersEncoder());
	}

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 */
	public HomePageMapper(IPageParametersEncoder pageParametersEncoder)
	{
		Args.notNull(pageParametersEncoder, "pageParametersEncoder");

		this.pageParametersEncoder = pageParametersEncoder;
	}


	public int getCompatibilityScore(Request request)
	{
		return 0;
	}

	public Url mapHandler(IRequestHandler requestHandler)
	{
		return null;
	}

	public IRequestHandler mapRequest(Request request)
	{
		final Url url = request.getUrl();

		if (url.getSegments().size() == 0)
		{
			final Class<? extends IRequestablePage> homePageClass = getContext().getHomePageClass();

			final PageProvider pageProvider;

			if (url.getQueryParameters().size() > 0)
			{
				PageParameters pageParameters = extractPageParameters(request, 0,
					pageParametersEncoder);
				pageProvider = new PageProvider(homePageClass, pageParameters);
			}
			else
			{
				pageProvider = new PageProvider(homePageClass);
			}

			return new RenderPageRequestHandler(pageProvider);
		}
		else
		{
			return null;
		}
	}
}
