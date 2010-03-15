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
package org.apache.wicket.mock;

import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.pageStore.IPageManager;
import org.apache.wicket.pageStore.IPageManagerContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.IProvider;

public class MockApplication extends WebApplication
{

	public MockApplication()
	{
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return MockHomePage.class;
	}

	@Override
	public String getConfigurationType()
	{
		return DEVELOPMENT;
	}


	public Session getSession()
	{
		return getSessionStore().lookup(null);
	}

	@Override
	public final String getInitParameter(String key)
	{
		return null;
	}

	@Override
	protected void internalInit()
	{
		super.internalInit();
		setSessionStoreProvider(new MockSessionStoreProvider());
		setPageManagerProvider(new MockPageManagerProvider());
	}

	private static class MockSessionStoreProvider implements IProvider<ISessionStore>
	{

		public ISessionStore get()
		{
			return new MockSessionStore();
		}

	}
	private static class MockPageManagerProvider implements IPageManagerProvider
	{

		public IPageManager get(IPageManagerContext context)
		{
			return new MockPageManager(context);
		}


	}

}
