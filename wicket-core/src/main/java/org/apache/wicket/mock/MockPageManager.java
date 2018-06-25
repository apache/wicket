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
import org.apache.wicket.pageStore.IPageStore;

/**
 * Simple {@link IPageManager} used for testing.
 * 
 * @author Matej Knopp
 */
public class MockPageManager implements IPageManager
{
	private final Map<Integer, IManageablePage> pages = new HashMap<>();

	@Override
	public boolean supportsVersioning()
	{
		return false;
	}
	
	@Override
	public void destroy()
	{
		pages.clear();
	}

	@Override
	public IManageablePage getPage(int id)
	{
		return pages.get(id);
	}

	@Override
	public void removePage(final IManageablePage page) {
		pages.remove(page.getPageId());
	}

	@Override
	public void addPage(IManageablePage page)
	{
		pages.put(page.getPageId(), page);
	}

	@Override
	public void removeAllPages()
	{
		pages.clear();
	}

	@Override
	public void detach()
	{
	}

	@Override
	public IPageStore getPageStore()
	{
		throw new UnsupportedOperationException();
	}
}