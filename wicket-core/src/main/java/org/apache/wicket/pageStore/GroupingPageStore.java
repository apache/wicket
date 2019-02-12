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
package org.apache.wicket.pageStore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.wicket.MetaDataEntry;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.string.Strings;

/**
 * An {@link IPageStore} that groups pages.
 * <p>
 * By default all pages are stored in a single group, you'll have to override {@link #getGroup(IManageablePage)} to provide the actual group
 * for a stored page, e.g. using a single group for all pages inside a single browser tab.
 */
public abstract class GroupingPageStore extends DelegatingPageStore
{

	private static final String DEFAULT_GROUP = "default";

	private static final MetaDataKey<SessionData> KEY = new MetaDataKey<SessionData>()
	{
		private static final long serialVersionUID = 1L;
	};

	private int maxGroups;
	
	/**
	 * Is a group of a page stable.
	 */
	private boolean stableGroups = false;

	/**
	 * @param delegate
	 *            store to delegate to
	 * @param maxGroups
	 *            maximum groups to keep
	 */
	public GroupingPageStore(IPageStore delegate, int maxGroups)
	{
		super(delegate);

		this.maxGroups = maxGroups;
	}

	/**
	 * Indicate that groups are stable, i.e. the group of a page never changes.
	 */
	public GroupingPageStore withStableGroups()
	{
		stableGroups = true;

		return this;
	}

	/**
	 * Get the group of a page, default is <code>"default"</code>
	 * 
	 * @return group of page, must not be empty
	 */
	protected String getGroup(IManageablePage page)
	{
		return DEFAULT_GROUP;
	}

	private String getGroupInternal(IManageablePage page)
	{
		String group = getGroup(page);

		if (Strings.isEmpty(group))
		{
			throw new WicketRuntimeException("group must not be empy");
		}

		return group;
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		SessionData sessionData = getSessionData(context);

		sessionData.addPage(context, page, getGroupInternal(page), maxGroups, stableGroups, getDelegate());
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		SessionData sessionData = getSessionData(context);
		
		sessionData.removePage(context, page, getDelegate());
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		SessionData sessionData = getSessionData(context);

		sessionData.removeAllPages(context, getDelegate());
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		SessionData sessionData = getSessionData(context);
		
		return sessionData.getPage(context, id, getDelegate());
	}

	private SessionData getSessionData(IPageContext context)
	{
		SessionData data = context.getSessionData(KEY);
		if (data == null)
		{
			data = context.setSessionData(KEY, new SessionData());
		}

		return data;
	}

	/**
	 * Data kept in the {@link Session}.
	 */
	static class SessionData implements Serializable
	{
		private LinkedList<String> groups = new LinkedList<>();

		private Map<String, MetaDataEntry<?>[]> metaData = new HashMap<>();

		public synchronized <T> void setMetaData(String group, MetaDataKey<T> key, T value)
		{
			metaData.put(group, key.set(metaData.get(group), value));
		}

		public synchronized <T> T getMetaData(String group, MetaDataKey<T> key)
		{
			return key.get(metaData.get(group));
		}
		
		public synchronized void addPage(IPageContext context, IManageablePage page, String group, int maxGroups, boolean stableGroups, IPageStore delegate)
		{
			if (stableGroups == false)
			{
				// group might have changed, so remove page first from all groups
				for (String other : groups)
				{
					delegate.removePage(new GroupContext(context, this, other), page);
				}
			}

			// add as last
			groups.remove(group);
			groups.addLast(group);
			
			// delegate
			delegate.addPage(new GroupContext(context, this, group), page);

			while (groups.size() > maxGroups)
			{
				String first = groups.removeFirst();
				
				delegate.removeAllPages(new GroupContext(context, this, first));
			}
		}
		
		public IManageablePage getPage(IPageContext context, int id, IPageStore delegate)
		{
			for (String group : groups)
			{
				IManageablePage page = delegate.getPage(new GroupContext(context, this, group), id);
				if (page != null)
				{
					return page;
				}
			}
			return null;
		}

		public synchronized void removePage(IPageContext context, IManageablePage page, IPageStore delegate)
		{
			for (String group : groups)
			{
				delegate.removePage(new GroupContext(context, this, group), page);
			}
		}

		public synchronized void removeAllPages(IPageContext context, IPageStore delegate)
		{
			for (String group : groups)
			{
				delegate.removeAllPages(new GroupContext(context, this, group));
			}
		}
	}

	/**
	 * Context passed to the delegate store to group data and attributes.
	 */
	static class GroupContext implements IPageContext
	{

		private final IPageContext context;

		private final SessionData sessionData;

		private final String group;

		public GroupContext(IPageContext context, SessionData sessionData, String group)
		{
			this.context = context;
			this.sessionData = sessionData;
			this.group = group;
		}

		@Override
		public String getSessionId()
		{
			return context.getSessionId() + "_" + group;
		}

		@Override
		public <T extends Serializable> T setSessionData(MetaDataKey<T> key, T value)
		{
			sessionData.setMetaData(group, key, value);
			
			return value;
		}

		@Override
		public <T extends Serializable> T getSessionData(MetaDataKey<T> key)
		{
			return sessionData.getMetaData(group, key);
		}

		@Override
		public <T extends Serializable> void setSessionAttribute(String key, T value)
		{
			context.setSessionAttribute(key + "_" + group, value);
		}

		@Override
		public <T extends Serializable> T getSessionAttribute(String key)
		{
			return context.getSessionAttribute(key + "_" + group);
		}

		@Override
		public <T> void setRequestData(MetaDataKey<T> key, T data)
		{
			throw new WicketRuntimeException("no request available for group");
		}

		@Override
		public <T> T getRequestData(MetaDataKey<T> key)
		{
			throw new WicketRuntimeException("no request available for group");
		}
	}
}