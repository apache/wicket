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

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPageStore;

public class MockPageStore implements IPageStore
{
	private final LinkedList<IManageablePage> pages = new LinkedList<>();

	@Override
	public boolean supportsVersioning()
	{
		// pretend versioning support, although pages are not actually serialized
		return true;
	}
	
	@Override
	public void destroy()
	{
		pages.clear();
	}

	public List<IManageablePage> getPages()
	{
		return pages;
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		for (IManageablePage page : pages) {
			if (page.getPageId() == id) {
				return page;
			}
		}
		return null;
	}

	@Override
	public void removePage(IPageContext context, final IManageablePage page)
	{
		for (IManageablePage candidate : pages) {
			if (candidate.getPageId() == page.getPageId()) {
				pages.remove(candidate);
				return;
			}
		}
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		pages.clear();
	}

	@Override
	public boolean canBeAsynchronous(IPageContext context)
	{
		return true;
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		removePage(context, page);
		
		pages.addLast(page);
	}
}