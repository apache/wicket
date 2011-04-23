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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;

/**
 * Simple {@link IPageManager} used for testing.
 * 
 * @author Matej Knopp
 */
public class MockPageManager implements IPageManager
{
	private final Map<Integer, IManageablePage> pages = new HashMap<Integer, IManageablePage>();

	/**
	 * Construct.
	 * 
	 */
	public MockPageManager()
	{
	}

	public void commitRequest()
	{
	}

	public void destroy()
	{
		pages.clear();
	}

	public IManageablePage getPage(int id)
	{
		return pages.get(id);
	}

	public void newSessionCreated()
	{
		pages.clear();
	}

	public void sessionExpired(String sessionId)
	{
		pages.clear();
	}

	/**
	 * @param context
	 */
	public void setContext(IPageManagerContext context)
	{
	}

	public boolean supportsVersioning()
	{
		return true;
	}

	public void touchPage(IManageablePage page)
	{
		pages.put(page.getPageId(), page);
	}

	/**
	 * @see org.apache.wicket.page.IPageManager#getContext()
	 */
	public IPageManagerContext getContext()
	{
		return null;
	}
}
