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
package org.apache.wicket.ng.page.common;


import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.ng.page.PageManager;
import org.apache.wicket.ng.page.PageManagerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for {@link PageManager} implementations. Subclass should extend
 * {@link RequestAdapter} and override {@link #newRequestAdater()} method to return it's
 * {@link RequestAdapter} implementation.
 * 
 * 
 * @author Matej Knopp
 * 
 */
public abstract class AbstractPageManager implements PageManager
{
	private PageManagerContext context;

	public AbstractPageManager()
	{
	}

	protected abstract RequestAdapter newRequestAdapter(PageManagerContext context);

	public abstract boolean supportsVersioning();

	public abstract void sessionExpired(String sessionId);

	public void setContext(PageManagerContext context)
	{
		this.context = context;
	}

	public PageManagerContext getContext()
	{
		return context;
	};

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

	public void commitRequest()
	{
		RequestAdapter adapter = getRequestAdapter();
		adapter.commitRequest();
	}

	public ManageablePage getPage(int id)
	{
		RequestAdapter adapter = getRequestAdapter();
		ManageablePage page = adapter.getPage(id);
		if (page != null)
		{
			touchPage(page);
		}
		return page;
	}

	public void newSessionCreated()
	{
		RequestAdapter adapter = getRequestAdapter();
		adapter.newSessionCreated();
	}

	public void touchPage(ManageablePage page)
	{
		if (!page.isPageStateless())
		{
			getContext().bind();
		}
		RequestAdapter adapter = getRequestAdapter();
		adapter.touch(page);
	}

	static Logger logger = LoggerFactory.getLogger(AbstractPageManager.class);
}
