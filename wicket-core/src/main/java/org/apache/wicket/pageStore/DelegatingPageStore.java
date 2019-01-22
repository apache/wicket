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
import org.apache.wicket.util.lang.Args;

/**
 * An {@link IPageStore} that delegates to another storage.
 */
public abstract class DelegatingPageStore implements IPageStore
{
	private final IPageStore delegate;
	
	protected DelegatingPageStore(IPageStore delegate)
	{
		this.delegate = Args.notNull(delegate, "delegate");
	}

	public IPageStore getDelegate()
	{
		return delegate;
	}
	
	/**
	 * Versioning is supported depending on the delegate.
	 */
	@Override
	public boolean supportsVersioning()
	{
		return delegate.supportsVersioning();
	}
	
	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		delegate.addPage(context, page);
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		delegate.removePage(context, page);
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		delegate.removeAllPages(context);
	}
	
	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		return delegate.getPage(context, id);
	}

	@Override
	public void detach(IPageContext context)
	{
		delegate.detach(context);
	}

	@Override
	public void destroy()
	{
		delegate.destroy();
	}
}
