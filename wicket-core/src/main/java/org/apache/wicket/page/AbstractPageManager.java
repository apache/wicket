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
package org.apache.wicket.page;

import org.apache.wicket.util.lang.Args;

/**
 * Convenience class for {@link IPageManager} implementations. Subclass should extend
 * {@link RequestAdapter} and override {@link #newRequestAdapter(IPageManagerContext)} method to return it's
 * {@link RequestAdapter} implementation.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractPageManager implements IPageManager
{
	private final IPageManagerContext context;

	/**
	 * Construct.
	 * 
	 * @param context
	 */
	public AbstractPageManager(IPageManagerContext context)
	{
		this.context = Args.notNull(context, "context");
	}

	/**
	 * 
	 * @param context
	 * @return a new request adapter
	 */
	protected abstract RequestAdapter newRequestAdapter(IPageManagerContext context);

	/**
	 * @return The page manager context
	 */
	@Override
	public IPageManagerContext getContext()
	{
		return context;
	}

	/**
	 * @see #newRequestAdapter(IPageManagerContext)
	 * @return the request adapter
	 */
	protected RequestAdapter getRequestAdapter()
	{
		RequestAdapter adapter = (RequestAdapter)getContext().getRequestData();
		if (adapter == null)
		{
			adapter = newRequestAdapter(getContext());
			getContext().setRequestData(adapter);
		}
		return adapter;
	}

	@Override
	public void commitRequest()
	{
		getRequestAdapter().commitRequest();
	}

	@Override
	public IManageablePage getPage(int id)
	{
		IManageablePage page = getRequestAdapter().getPage(id);
		if (page != null)
		{
			touchPage(page);
		}
		return page;
	}

	@Override
	public void newSessionCreated()
	{
		getRequestAdapter().newSessionCreated();
	}

	@Override
	public void touchPage(IManageablePage page)
	{
		getRequestAdapter().touch(page);
	}

	@Override
	public void untouchPage(IManageablePage page)
	{
		getRequestAdapter().touch(page);
	}
}
