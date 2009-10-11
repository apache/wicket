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
package org.apache.wicket.ng.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.ng.page.PageManagerContext;
import org.apache.wicket.ng.page.common.AbstractPageManager;
import org.apache.wicket.ng.page.common.RequestAdapter;

public class MockPageManager extends AbstractPageManager
{

	public MockPageManager()
	{
	}

	private final Map<String, Map<Integer, ManageablePage>> sessionMap = new ConcurrentHashMap<String, Map<Integer, ManageablePage>>();

	private class MockRequestAdapter extends RequestAdapter
	{
		public MockRequestAdapter(PageManagerContext context)
		{
			super(context);
		}

		@Override
		protected ManageablePage getPage(int id)
		{
			Map<Integer, ManageablePage> pages = sessionMap.get(getSessionId());
			return pages != null ? pages.get(id) : null;
		}

		@Override
		protected void newSessionCreated()
		{

		}

		@Override
		protected void storeTouchedPages(List<ManageablePage> touchedPages)
		{
			bind();
			Map<Integer, ManageablePage> pages = sessionMap.get(getSessionId());
			if (pages == null)
			{
				pages = new HashMap<Integer, ManageablePage>();
				sessionMap.put(getSessionId(), pages);
			}
			for (ManageablePage page : touchedPages)
			{
				pages.put(page.getPageId(), page);
			}
		}
	};

	@Override
	protected RequestAdapter newRequestAdapter(PageManagerContext context)
	{
		return new MockRequestAdapter(context);
	}

	@Override
	public void sessionExpired(String sessionId)
	{
		sessionMap.remove(sessionId);
	}

	@Override
	public boolean supportsVersioning()
	{
		return false;
	}

	public void destroy()
	{
		sessionMap.clear();
	}
}
