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
package org.apache.wicket.examples.requestmapper;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.AbstractComponentMapper;
import org.apache.wicket.core.request.mapper.HomePageMapper;

/**
 * An {@link IRequestMapper} that handles requests to the home page ('/') and appends the string
 * representation of the current session locale in the URL
 * 
 * <p>
 * I.e. a request to http://example.com/app will end up in http://example.com/app/en_US
 * 
 * @author mgrigorov
 */
public class CustomHomeMapper extends AbstractComponentMapper
{
	/**
	 * If there is just one url segment (the locale?!) then return a bigger compatibility score than
	 * {@link HomePageMapper#getCompatibilityScore(Request)}
	 * 
	 * @see org.apache.wicket.core.request.mapper.HomePageMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	public int getCompatibilityScore(Request request)
	{
		return request.getUrl().getSegments().size() == 1 ? 1 : 0;
	}

	/**
	 * @see org.apache.wicket.core.request.mapper.HomePageMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url homeUrl = null;

		if (requestHandler instanceof IPageRequestHandler)
		{
			IPageRequestHandler pageRequestHandler = (IPageRequestHandler)requestHandler;

			if (pageRequestHandler.getPageClass().equals(Application.get().getHomePage()))
			{
				String locale = Session.get().getLocale().toString();
				homeUrl = new Url();
				homeUrl.getSegments().add(0, locale);
			}
		}

		return homeUrl;
	}

	/**
	 * @see org.apache.wicket.core.request.mapper.HomePageMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	public IRequestHandler mapRequest(Request request)
	{
		if (isHomeUrl(request))
		{
			return new RenderPageRequestHandler(new PageProvider(getContext().getHomePageClass()));
		}
		else
		{
			return null;
		}
	}

	/**
	 * A home URL is considered a URL without any segments or with one segment and its value is
	 * valid locale
	 * 
	 * @param request
	 * @return <code>true</code> if the request is to the home page ("/")
	 */
	private boolean isHomeUrl(Request request)
	{
		boolean isHomeUrl = false;

		List<String> segments = request.getUrl().getSegments();
		if (segments.isEmpty())
		{
			isHomeUrl = true;
		}
		else if (segments.size() == 1)
		{
			String localeCandidate = segments.get(0);
			isHomeUrl = LocaleHelper.isLocale(localeCandidate);
			// on success the Session's locale can be changed here
		}

		return isHomeUrl;
	}
}
