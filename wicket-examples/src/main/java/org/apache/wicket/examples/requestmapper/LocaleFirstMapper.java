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
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.core.request.mapper.AbstractComponentMapper;
import org.apache.wicket.util.string.Strings;

/**
 * A {@link IRequestMapper} that reads the session locale from the first url segment
 * 
 * @author ivaynberg
 * @author matej.knopp
 */
public class LocaleFirstMapper extends AbstractComponentMapper
{

	private final IRequestMapper chain;

	/**
	 * Construct.
	 * 
	 * @param chain
	 */
	public LocaleFirstMapper(final IRequestMapper chain)
	{
		this.chain = chain;
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	public int getCompatibilityScore(Request request)
	{
		if (getLocaleFromUrl(request) != null)
		{
			request = stripLocaleSegment(request);
		}

		// since we match all urls the score is simply delegated to the chain
		return chain.getCompatibilityScore(request);
	}

	private Request stripLocaleSegment(Request request)
	{
		Url url = request.getUrl();
		url.getSegments().remove(0);
		return request.cloneWithUrl(url);
	}

	private Locale getLocaleFromUrl(Request request)
	{
		// locale is the first segment in the url
		List<String> segments = request.getUrl().getSegments();
		if (segments != null && segments.size() > 1)
		{
			String localeAsString = segments.get(0);
			if (!Strings.isEmpty(localeAsString))
			{
				return LocaleHelper.parseLocale(localeAsString);
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	public IRequestHandler mapRequest(Request request)
	{
		Locale locale = getLocaleFromUrl(request);
		if (locale != null)
		{
			Session.get().setLocale(locale);

			// now that we have proccessed the first segment we need to strip from the url
			request = stripLocaleSegment(request);
		}

		// chain url processing
		return chain.mapRequest(request);
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler handler)
	{

		// let the chain create the url
		Url url = chain.mapHandler(handler);

		if (url != null)
		{
			Locale locale = Session.get().getLocale();
			if (locale == null)
			{
				locale = Locale.US;
			}
			url.getSegments().add(0, locale.toString());
		}

		return url;
	}


}
