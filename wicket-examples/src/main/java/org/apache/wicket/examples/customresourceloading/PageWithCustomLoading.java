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
package org.apache.wicket.examples.customresourceloading;

import java.net.URL;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.core.util.resource.UrlResourceStream;


/**
 * The markup for this page is loaded by the Page component itself.
 * 
 * @see IMarkupResourceStreamProvider
 * @see IMarkupCacheKeyProvider
 * 
 * @author Eelco Hillenius
 */
public class PageWithCustomLoading extends WicketExamplePage
	implements
		IMarkupResourceStreamProvider,
		IMarkupCacheKeyProvider
{
	/**
	 * Constructor
	 */
	public PageWithCustomLoading()
	{
	}

	/**
	 * This implementation loads from a custom name/ location. While not advisable as the default
	 * way of loading resources, overriding this method can provide a component specific break out
	 * so that you e.g. can load a template from a database without any other component or the
	 * application having to know about it.
	 * 
	 * @param container
	 *            The MarkupContainer which requests to load the Markup resource stream
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @return A IResourceStream if the resource was found
	 */
	public IResourceStream getMarkupResourceStream(final MarkupContainer container,
		final Class<?> containerClass)
	{
		// load a template with a totally different name from this package using
		// this component's class loader
		final URL url = PageWithCustomLoading.class.getResource("CustomLoadedTemplate.html");
		if (url != null)
		{
			return new UrlResourceStream(url);
		}

		// no resource was not found
		return null;
	}

	/**
	 * Prevent the markup from ever be cached. This is optionally - components that don't implement
	 * {@link IMarkupCacheKeyProvider} will just have their markup cached - but is useful when
	 * markup varies. If you don't need such dynamic loading, it is advisible to not implement
	 * {@link IMarkupCacheKeyProvider}.
	 * 
	 * @see org.apache.wicket.markup.IMarkupCacheKeyProvider#getCacheKey(org.apache.wicket.MarkupContainer,
	 *      java.lang.Class)
	 */
	public String getCacheKey(MarkupContainer container, Class<?> containerClass)
	{
		return null;
	}
}