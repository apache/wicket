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
package org.apache.wicket;

import java.util.function.Supplier;

import org.apache.wicket.core.request.mapper.BookmarkableMapper;
import org.apache.wicket.core.request.mapper.BufferedResponseMapper;
import org.apache.wicket.core.request.mapper.HomePageMapper;
import org.apache.wicket.core.request.mapper.PageInstanceMapper;
import org.apache.wicket.core.request.mapper.ResourceReferenceMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;

/**
 * Mapper that encapsulates mappers that are necessary for Wicket to function.
 * 
 * @author igor.vaynberg
 */
public class SystemMapper extends CompoundRequestMapper
{
	private final Application application;

	/**
	 * Constructor
	 * 
	 * @param application
	 */
	public SystemMapper(final Application application)
	{
		this.application = application;

		add(newPageInstanceMapper());
		add(newBookmarkableMapper());
		add(newHomePageMapper(new HomePageProvider(application)));
		add(newResourceReferenceMapper(new PageParametersEncoder(),
			new ParentFolderPlaceholderProvider(application), getResourceCachingStrategy()));
		add(newUrlResourceReferenceMapper());
		add(RestartResponseAtInterceptPageException.MAPPER);
		add(newBufferedResponseMapper());
	}

	protected IRequestMapper newBufferedResponseMapper()
	{
		return new BufferedResponseMapper();
	}

	protected IRequestMapper newUrlResourceReferenceMapper()
	{
		return new UrlResourceReferenceMapper();
	}

	private IRequestMapper newResourceReferenceMapper(PageParametersEncoder pageParametersEncoder,
	                                                  ParentFolderPlaceholderProvider parentFolderPlaceholderProvider,
	                                                  Supplier<IResourceCachingStrategy> resourceCachingStrategy)
	{
		return new ResourceReferenceMapper(pageParametersEncoder, parentFolderPlaceholderProvider,resourceCachingStrategy);
	}

	protected IRequestMapper newBookmarkableMapper()
	{
		return new BookmarkableMapper();
	}

	protected IRequestMapper newPageInstanceMapper()
	{
		return new PageInstanceMapper();
	}

	protected IRequestMapper newHomePageMapper(Supplier<Class<? extends IRequestablePage>> homePageProvider)
	{
		return new HomePageMapper(homePageProvider);
	}

	protected Supplier<IResourceCachingStrategy> getResourceCachingStrategy()
	{
		return () -> application.getResourceSettings().getCachingStrategy();
	}

	protected static class ParentFolderPlaceholderProvider implements Supplier<String>
	{
		private final Application application;

		protected ParentFolderPlaceholderProvider(Application application)
		{
			this.application = application;
		}

		@Override
		public String get()
		{
			return application.getResourceSettings().getParentFolderPlaceholder();
		}
	}

	protected static class HomePageProvider<C extends Page> implements Supplier<Class<C>>
	{
		private final Application application;

		protected HomePageProvider(final Application application)
		{
			this.application = application;
		}

		@Override
		public Class<C> get()
		{
			return (Class<C>) application.getHomePage();
		}
	}
}
