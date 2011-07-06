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
package org.apache.wicket.devutils.diskstore.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.devutils.diskstore.DebugDiskDataStore;
import org.apache.wicket.devutils.diskstore.DebugPageManagerProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.pageStore.PageWindowManager.PageWindow;

/**
 * An {@link IDataProvider} that extracts the information about the stored pages
 */
public class PageWindowProvider implements ISortableDataProvider<PageWindowDescription>
{
	private static final int MAX_PAGES_TO_READ = 1000;

	public Iterator<? extends PageWindowDescription> iterator(int first, int count)
	{
		List<PageWindow> lastPageWindows = getPageWindows();
		List<PageWindow> subList = lastPageWindows.subList(first, first + count);
		List<PageWindowDescription> pageDescriptions = new ArrayList<PageWindowDescription>();
		for (PageWindow pw : subList)
		{
			pageDescriptions.add(new PageWindowDescription(pw));
		}

		return pageDescriptions.iterator();
	}

	private List<PageWindow> getPageWindows()
	{
		List<PageWindow> lastPageWindows = new ArrayList<PageWindow>();
		if (Session.exists() && Session.get().isTemporary() == false)
		{
			String sessionId = Session.get().getId();
			DebugPageManagerProvider pageManagerProvider = (DebugPageManagerProvider)Application.get()
				.getPageManagerProvider();
			DebugDiskDataStore dataStore = pageManagerProvider.getDataStore();
			lastPageWindows.addAll(dataStore.getLastPageWindows(sessionId, MAX_PAGES_TO_READ));
		}
		return lastPageWindows;
	}

	public int size()
	{
		return getPageWindows().size();
	}

	/**
	 * @param description
	 * 
	 *            {@inheritDoc}
	 */
	public IModel<PageWindowDescription> model(PageWindowDescription description)
	{
		return new Model<PageWindowDescription>(description);
	}

	public void detach()
	{
	}

	/*
	 * No sort state for now. The provider is ISortableDataProvider just because we use
	 * DefaultDataTable
	 */
	public ISortState getSortState()
	{
		return null;
	}

}
