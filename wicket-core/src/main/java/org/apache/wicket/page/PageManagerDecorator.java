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
 * Decorator for {@link IPageManager}
 * 
 * @author igor
 */
public class PageManagerDecorator implements IPageManager
{
	private final IPageManager delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate
	 */
	public PageManagerDecorator(IPageManager delegate)
	{
		Args.notNull(delegate, "delegate");
		this.delegate = delegate;
	}

	@Override
	public IPageManagerContext getContext()
	{
		return delegate.getContext();
	}

	@Override
	public IManageablePage getPage(int id)
	{
		return delegate.getPage(id);
	}

	@Override
	public void touchPage(IManageablePage page)
	{
		delegate.touchPage(page);
	}

	@Override
	public void untouchPage(IManageablePage page)
	{
		delegate.untouchPage(page);
	}

	@Override
	public boolean supportsVersioning()
	{
		return delegate.supportsVersioning();
	}

	@Override
	public void commitRequest()
	{
		delegate.commitRequest();
	}

	@Override
	public void newSessionCreated()
	{
		delegate.newSessionCreated();
	}

	@Override
	public void clear()
	{
		delegate.clear();
	}

	@Override
	public void destroy()
	{
		delegate.destroy();
	}

}
