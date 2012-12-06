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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.util.ClassProvider;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.reference.ClassReference;

/**
 * A mapper that is used when a request to the home page ("/") is made
 */
public class HomePageMapper extends MountedMapper
{

	/**
	 * Construct.
	 *
	 * @param pageClass
	 *            the class of the page which should handle requests to "/"
	 */
	public HomePageMapper(final Class<? extends IRequestablePage> pageClass)
	{
		super("/", pageClass);
	}

	/**
	 * Construct.
	 *
	 * @param pageClassProvider
	 *            the class of the page which should handle requests to "/"
	 */
	@Deprecated
	public HomePageMapper(ClassProvider<? extends IRequestablePage> pageClassProvider)
	{
		super("/", pageClassProvider);
	}

	/**
	 * Construct.
	 *
	 * @param pageClassProvider
	 *            the class of the page which should handle requests to "/"
	 */
	public HomePageMapper(IProvider<Class<? extends IRequestablePage>> pageClassProvider)
	{
		super("/", pageClassProvider);
	}

	/**
	 * Construct.
	 *
	 * @param pageClass
	 *            the class of the page which should handle requests to "/"
	 * @param pageParametersEncoder
	 *            the encoder that will be used to encode/decode the page parameters
	 */
	public HomePageMapper(Class<? extends IRequestablePage> pageClass,
		IPageParametersEncoder pageParametersEncoder)
	{
		super("/", pageClass, pageParametersEncoder);
	}

	/**
	 * Construct.
	 *
	 * @param pageClassProvider
	 *            the class of the page which should handle requests to "/"
	 * @param pageParametersEncoder
	 *            the encoder that will be used to encode/decode the page parameters
	 */
	@Deprecated
	public HomePageMapper(final ClassProvider<? extends IRequestablePage> pageClassProvider,
		IPageParametersEncoder pageParametersEncoder)
	{
		super("/", new ClassReference(pageClassProvider.get()), pageParametersEncoder);
	}

	/**
	 * Matches only when there are no segments/indexed parameters
	 *
	 * @see AbstractBookmarkableMapper#parseRequest(org.apache.wicket.request.Request)
	 */
	@Override
	protected UrlInfo parseRequest(Request request)
	{
		// get canonical url
		final Url url = request.getUrl().canonical();

		if (url.getSegments().size() > 0)
		{
			// home page cannot have segments/indexed parameters
			return null;
		}

		return super.parseRequest(request);
	}

	/**
	 * Use this mapper as a last option. Let all other mappers to try to handle the request
	 *
	 * @see MountedMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(Request request)
	{
		return Integer.MIN_VALUE + 1;
	}

}
