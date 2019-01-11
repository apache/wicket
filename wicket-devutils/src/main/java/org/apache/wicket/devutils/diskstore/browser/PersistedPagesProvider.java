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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.pageStore.IPersistedPage;
import org.apache.wicket.pageStore.IPersistentPageStore;
import org.apache.wicket.util.lang.Objects;

/**
 * An {@link IDataProvider} that extracts the information about {@link IPersistedPage}s.
 */
class PersistedPagesProvider extends SortableDataProvider<IPersistedPage, String>
{
	/**
	 * The model that brings the currently selected session id
	 */
	private final IModel<String> sessionId;

	private final IModel<IPersistentPageStore> store;

	private List<IPersistedPage> pages;

	PersistedPagesProvider(final IModel<String> sessionId, IModel<IPersistentPageStore> store)
	{
		this.sessionId = sessionId;
		this.store = store;
	}

	@Override
	public Iterator<? extends IPersistedPage> iterator(long first, long count)
	{
		List<IPersistedPage> pages = getPages();

		if (getSort() != null) {
			Collections.sort(pages, new SortComparator());
		}

		return pages.subList((int)first,  (int)(first + count)).iterator();
	}

	private List<IPersistedPage> getPages()
	{
		if (pages == null)
		{
			pages = new ArrayList<>();

			if (sessionId.getObject() != null)
			{
				String sessId = sessionId.getObject();

				IPersistentPageStore persistentPagesStore = store.getObject();

				if (persistentPagesStore != null)
				{
					pages.addAll(persistentPagesStore.getPersistentPages(sessId));
				}
			}
		}

		return pages;
	}

	@Override
	public long size()
	{
		return getPages().size();
	}

	/**
	 * @param description
	 * 
	 *            {@inheritDoc}
	 */
	@Override
	public IModel<IPersistedPage> model(IPersistedPage description)
	{
		return new Model<>(description);
	}

	@Override
	public void detach()
	{
		sessionId.detach();
		store.detach();

		pages = null;
	}

	private class SortComparator implements Comparator<IPersistedPage>
	{

		@Override
		public int compare(IPersistedPage page0, IPersistedPage page1)
		{
			Object value0 = PropertyResolver.getValue(getSort().getProperty(), page0);
			Object value1 = PropertyResolver.getValue(getSort().getProperty(), page1);

			int c = Objects.compareWithConversion(value0, value1);

			if (getSort().isAscending() == false)
			{
				c = c * -1;
			}

			return c;
		}
	}
}