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
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.core.request.mapper.HomePageMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;

/**
 * An {@link IRequestMapper} that handles requests to the home page ('/') and appends the string
 * representation of the current session locale in the URL
 * 
 * <p>
 * I.e. a request to http://example.com/app will end up in http://example.com/app/en_US
 */
public class CustomHomeMapper extends HomePageMapper
{
	/**
	 * Constructor.
	 *
	 * @param pageClass
	 *      the class of the home page
	 */
	public CustomHomeMapper(final Class<? extends IRequestablePage> pageClass)
	{
		super(pageClass);
	}

	/**
	 * Sets the current session Locale as first segment in the Url.
	 *
	 * @see org.apache.wicket.core.request.mapper.HomePageMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url homeUrl = super.mapHandler(requestHandler);

		if (homeUrl != null)
		{
			String locale = Session.get().getLocale().toString();
			homeUrl.getSegments().add(0, locale);
		}

		return homeUrl;
	}

	/**
	 * Removes the leading segment if it a valid Locale
	 *
	 * @see org.apache.wicket.core.request.mapper.HomePageMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	@Override
	public IRequestHandler mapRequest(Request request)
	{
		IRequestHandler requestHandler = null;
		Url url = request.getUrl();
		List<String> segments = url.getSegments();

		if (segments.size() == 1)
		{
			String localeAsString = segments.get(0);
			Locale locale = LocaleHelper.parseLocale(localeAsString);
			if (locale != null)
			{
				Session.get().setLocale(locale);
				segments.remove(0);

				Request requestWithoutLocale = request.cloneWithUrl(url);
				requestHandler = super.mapRequest(requestWithoutLocale);
			}
		}

		return requestHandler;
	}

	/**
	 * If there is just one url segment (the locale?!) then return a bigger compatibility score than
	 * {@link HomePageMapper#getCompatibilityScore(Request)}
	 *
	 * @see org.apache.wicket.core.request.mapper.HomePageMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(Request request)
	{
		return request.getUrl().getSegments().size() == 1 ? 1 : 0;
	}
}
