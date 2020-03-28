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
package org.apache.wicket.pageStore;

import org.apache.wicket.page.IManageablePage;

/**
 * A store of pages that uses an {@link IPageStore} as a cache in front of another store to delegate to.
 */
public class CachingPageStore extends DelegatingPageStore
{

	/**
	 * The cache.
	 */
	private final IPageStore cache;

	/**
	 * Constructor.
	 * @param delegate store to delegate to
	 * @param cache store to use as cache
	 */
	public CachingPageStore(IPageStore delegate, IPageStore cache)
	{
		super(delegate);
		
		this.cache = cache;
	}

	/**
	 * Get the store used a cache.
	 * 
	 * @return store
	 */
	public IPageStore getCache()
	{
		return cache;
	}

	/**
	 * Get the page from cache first.
	 */
	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		IManageablePage page = cache.getPage(context, id);
		if (page != null) {
			return page;
		}
		
		return getDelegate().getPage(context, id);
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		cache.addPage(context, page);

		getDelegate().addPage(context, page);
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		cache.removePage(context, page);
		
		getDelegate().removePage(context, page);
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		cache.removeAllPages(context);

		getDelegate().removeAllPages(context);
	}
	
	@Override
	public void revertPage(IPageContext context, IManageablePage page)
	{
		cache.revertPage(context, page);
		
		getDelegate().revertPage(context, page);
	}
	
	@Override
	public void detach(IPageContext context)
	{
		cache.detach(context);
		
		getDelegate().detach(context);
	}

	@Override
	public void destroy()
	{
		cache.destroy();
		
		getDelegate().destroy();
	}
}