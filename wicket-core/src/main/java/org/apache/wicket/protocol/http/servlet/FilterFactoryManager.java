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
package org.apache.wicket.protocol.http.servlet;

import java.util.Iterator;
import java.util.List;

import javax.servlet.FilterConfig;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Generics;

/**
 * A very simple manager for web filter (web filter factories to be exact).
 * 
 * @author Juergen Donnerstag
 */
public class FilterFactoryManager implements Iterable<AbstractRequestWrapperFactory>
{
	private List<AbstractRequestWrapperFactory> filters;

	/**
	 * Construct.
	 */
	public FilterFactoryManager()
	{
	}

	/**
	 * Add a filter factory
	 * 
	 * @param wrapperFactory
	 * @return this
	 */
	public final FilterFactoryManager add(final AbstractRequestWrapperFactory wrapperFactory)
	{
		if (wrapperFactory != null)
		{
			if (filters == null)
			{
				filters = Generics.newArrayList(2);
			}

			filters.add(wrapperFactory);
		}

		return this;
	}

	/**
	 * Add a X-Forwarded web filter factory
	 * 
	 * @param config
	 *            If null, <code>WebApplication.get().getWicketFilter().getFilterConfig()</code>
	 *            will be called to retrieve the config.
	 * @return this
	 */
	public final FilterFactoryManager addXForwardedRequestWrapperFactory(FilterConfig config)
	{
		if (config == null)
		{
			config = WebApplication.get().getWicketFilter().getFilterConfig();
		}

		XForwardedRequestWrapperFactory factory = new XForwardedRequestWrapperFactory();
		factory.init(config);

		return add(factory);
	}

	/**
	 * Add a Secure remote address web filter factory
	 * 
	 * @param config
	 *            If null, <code>WebApplication.get().getWicketFilter().getFilterConfig()</code>
	 *            will be called to retrieve the config.
	 * @return this
	 */
	public final FilterFactoryManager addSecuredRemoteAddressRequestWrapperFactory(
		FilterConfig config)
	{
		if (config == null)
		{
			config = WebApplication.get().getWicketFilter().getFilterConfig();
		}

		SecuredRemoteAddressRequestWrapperFactory factory = new SecuredRemoteAddressRequestWrapperFactory();
		factory.init(config);

		return add(factory);
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<AbstractRequestWrapperFactory> iterator()
	{
		return filters.iterator();
	}
}
