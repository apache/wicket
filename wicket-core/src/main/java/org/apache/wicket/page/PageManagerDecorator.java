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

	/** {@inheritDoc} */
	@Override
	public IPageManagerContext getContext()
	{
		return delegate.getContext();
	}

	/** {@inheritDoc} */
	@Override
	public IManageablePage getPage(int id)
	{
		return delegate.getPage(id);
	}

	/** {@inheritDoc} */
	@Override
	public void touchPage(IManageablePage page)
	{
		delegate.touchPage(page);
	}

  public void untouchPage(IManageablePage page)
  {
    if (delegate instanceof AbstractPageManager) {
      AbstractPageManager apm = (AbstractPageManager) delegate;
      apm.untouchPage(page);
    }
  }

	@Override
	public boolean supportsVersioning()
	{
		return delegate.supportsVersioning();
	}

	/** {@inheritDoc} */
	@Override
	public void commitRequest()
	{
		delegate.commitRequest();
	}

	/** {@inheritDoc} */
	@Override
	public void newSessionCreated()
	{
		delegate.newSessionCreated();
	}

	/** {@inheritDoc} */
	@Override
	public void sessionExpired(String sessionId)
	{
		delegate.sessionExpired(sessionId);
	}

	/** {@inheritDoc} */
	@Override
	public void destroy()
	{
		delegate.destroy();
	}

}
