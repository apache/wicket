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
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.lang.WicketObjects;

/**
 * Wicket's default page store
 * 
 */
public class DefaultPageStore implements IPageStore
{
	private final String applicationName;

	private final SerializedPagesCache serializedPagesCache;

	private final IDataStore pageDataStore;

	/**
	 * Construct.
	 * 
	 * @param applicationName
	 * @param dataStore
	 * @param cacheSize
	 */
	public DefaultPageStore(final String applicationName, final IDataStore dataStore,
		final int cacheSize)
	{
		Args.notNull(applicationName, "applicationName");
		Args.notNull(dataStore, "DataStore");

		this.applicationName = applicationName;
		pageDataStore = dataStore;
		serializedPagesCache = new SerializedPagesCache(cacheSize);
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		pageDataStore.destroy();
	}

	/**
	 * 
	 * @param sessionId
	 * @param pageId
	 * @return page data
	 */
	protected byte[] getPageData(final String sessionId, final int pageId)
	{
		return pageDataStore.getData(sessionId, pageId);
	}

	/**
	 * 
	 * @param sessionId
	 * @param pageId
	 */
	protected void removePageData(final String sessionId, final int pageId)
	{
		pageDataStore.removeData(sessionId, pageId);
	}

	/**
	 * 
	 * @param sessionId
	 */
	protected void removePageData(final String sessionId)
	{
		pageDataStore.removeData(sessionId);
	}

	/**
	 * 
	 * @param sessionId
	 * @param pageId
	 * @param data
	 */
	protected void storePageData(final String sessionId, final int pageId, final byte[] data)
	{
		pageDataStore.storeData(sessionId, pageId, data);
	}

	/**
	 * 
	 * @return application name
	 */
	public String getApplicationName()
	{
		return applicationName;
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#getPage(java.lang.String, int)
	 */
	public IManageablePage getPage(final String sessionId, final int id)
	{
		SerializedPage fromCache = serializedPagesCache.getPage(sessionId, id);
		if (fromCache != null)
		{
			return deserializePage(fromCache.data);
		}

		byte[] data = getPageData(sessionId, id);
		if (data != null)
		{
			return deserializePage(data);
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#removePage(java.lang.String, int)
	 */
	public void removePage(final String sessionId, final int id)
	{
		serializedPagesCache.removePage(sessionId, id);
		removePageData(sessionId, id);
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#storePage(java.lang.String,
	 *      org.apache.wicket.page.IManageablePage)
	 */
	public void storePage(final String sessionId, final IManageablePage page)
	{
		SerializedPage serialized = serializePage(sessionId, page);
		serializedPagesCache.storePage(serialized);
		storePageData(sessionId, serialized.getPageId(), serialized.getData());
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(final String sessionId)
	{
		removePageData(sessionId);
		serializedPagesCache.removePages(sessionId);
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#convertToPage(java.lang.Object)
	 */
	public IManageablePage convertToPage(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		else if (object instanceof IManageablePage)
		{
			return (IManageablePage)object;
		}
		else if (object instanceof SerializedPage)
		{
			SerializedPage page = (SerializedPage)object;
			byte data[] = page.getData();
			if (data == null)
			{
				data = getPageData(page.getSessionId(), page.getPageId());
			}
			if (data != null)
			{
				return deserializePage(data);
			}
			return null;
		}

		String type = object.getClass().getName();
		throw new IllegalArgumentException("Unknown object type " + type);
	}

	/**
	 * 
	 * @param serializedPage
	 * @return
	 */
	private SerializedPage restoreStrippedSerializedPage(final SerializedPage serializedPage)
	{
		SerializedPage result = serializedPagesCache.getPage(serializedPage.getSessionId(),
			serializedPage.getPageId());
		if (result != null)
		{
			return result;
		}

		byte data[] = getPageData(serializedPage.getSessionId(), serializedPage.getPageId());
		return new SerializedPage(serializedPage.getSessionId(), serializedPage.getPageId(), data);
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#prepareForSerialization(java.lang.String,
	 *      java.lang.Object)
	 */
	public Serializable prepareForSerialization(final String sessionId, final Object object)
	{
		if (pageDataStore.isReplicated())
		{
			return null;
		}

		SerializedPage result = null;

		if (object instanceof IManageablePage)
		{
			IManageablePage page = (IManageablePage)object;
			result = serializedPagesCache.getPage(sessionId, page.getPageId());
			if (result == null)
			{
				result = serializePage(sessionId, page);
				serializedPagesCache.storePage(result);
			}
		}
		else if (object instanceof SerializedPage)
		{
			SerializedPage page = (SerializedPage)object;
			if (page.getData() == null)
			{
				result = restoreStrippedSerializedPage(page);
			}
			else
			{
				result = page;
			}
		}

		if (result != null)
		{
			return result;
		}
		return (Serializable)object;
	}

	/**
	 * 
	 * @return Always true for this implementation
	 */
	protected boolean storeAfterSessionReplication()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageStore#restoreAfterSerialization(java.io.Serializable)
	 */
	public Object restoreAfterSerialization(final Serializable serializable)
	{
		if (serializable == null)
		{
			return null;
		}
		else if (!storeAfterSessionReplication() || serializable instanceof IManageablePage)
		{
			return serializable;
		}
		else if (serializable instanceof SerializedPage)
		{
			SerializedPage page = (SerializedPage)serializable;
			if (page.getData() != null)
			{
				storePageData(page.getSessionId(), page.getPageId(), page.getData());
				return new SerializedPage(page.getSessionId(), page.getPageId(), null);
			}
			return page;
		}

		String type = serializable.getClass().getName();
		throw new IllegalArgumentException("Unknown object type " + type);
	}

	/**
	 * 
	 */
	protected static class SerializedPage implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int pageId;
		private final String sessionId;
		private final byte[] data;

		public SerializedPage(String sessionId, int pageId, byte[] data)
		{
			this.pageId = pageId;
			this.sessionId = sessionId;
			this.data = data;
		}

		public byte[] getData()
		{
			return data;
		}

		public int getPageId()
		{
			return pageId;
		}

		public String getSessionId()
		{
			return sessionId;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if ((obj instanceof SerializedPage) == false)
			{
				return false;
			}
			SerializedPage rhs = (SerializedPage)obj;
			return Objects.equal(getPageId(), rhs.getPageId()) &&
				Objects.equal(getSessionId(), rhs.getSessionId());
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(getPageId(), getSessionId());
		}
	}

	/**
	 * 
	 * @param sessionId
	 * @param page
	 * @return the serialized page information
	 */
	protected SerializedPage serializePage(final String sessionId, final IManageablePage page)
	{
		Args.notNull(sessionId, "sessionId");
		Args.notNull(page, "page");

		byte data[] = WicketObjects.objectToByteArray(page, applicationName);
		return new SerializedPage(sessionId, page.getPageId(), data);
	}

	/**
	 * 
	 * @param data
	 * @return page data deserialized
	 */
	protected IManageablePage deserializePage(final byte data[])
	{
		return (IManageablePage)WicketObjects.byteArrayToObject(data);
	}

	/**
	 * Cache that stores serialized pages. This is important to make sure that a single page is not
	 * serialized twice or more when not necessary.
	 * <p>
	 * For example a page is serialized during request, but it might be also later serialized on
	 * session replication. The purpose of this cache is to make sure that the data obtained from
	 * first serialization is reused on second serialization.
	 * 
	 * @author Matej Knopp
	 */
	static class SerializedPagesCache
	{
		private final int size;

		private final List<SoftReference<SerializedPage>> cache;

		/**
		 * Construct.
		 * 
		 * @param size
		 */
		public SerializedPagesCache(final int size)
		{
			this.size = size;
			cache = new ArrayList<SoftReference<SerializedPage>>(size);
		}

		/**
		 * 
		 * @param sessionId
		 * @param id
		 * @return the removed {@link SerializedPage} or <code>null</code> - otherwise
		 */
		public SerializedPage removePage(final String sessionId, final int id)
		{
			Args.notNull(sessionId, "sessionId");

			if (size > 0)
			{
				synchronized (cache)
				{
					for (Iterator<SoftReference<SerializedPage>> i = cache.iterator(); i.hasNext();)
					{
						SoftReference<SerializedPage> ref = i.next();
						SerializedPage entry = ref.get();
						if (entry != null && entry.getPageId() == id &&
							entry.getSessionId().equals(sessionId))
						{
							i.remove();
							return entry;
						}
					}
				}
			}
			return null;
		}

		/**
		 * Removes all {@link SerializedPage}s for the session with <code>sessionId</code> from the
		 * cache.
		 * 
		 * @param sessionId
		 */
		public void removePages(String sessionId)
		{
			Args.notNull(sessionId, "sessionId");

			if (size > 0)
			{
				synchronized (cache)
				{
					for (Iterator<SoftReference<SerializedPage>> i = cache.iterator(); i.hasNext();)
					{
						SoftReference<SerializedPage> ref = i.next();
						SerializedPage entry = ref.get();
						if (entry != null && entry.getSessionId().equals(sessionId))
						{
							i.remove();
						}
					}
				}
			}
		}

		/**
		 * Returns a {@link SerializedPage} by looking it up by <code>sessionId</code> and
		 * <code>pageId</code>. If there is a match then it is <i>touched</i>, i.e. it is moved at
		 * the top of the cache.
		 * 
		 * @param sessionId
		 * @param pageId
		 * @return the found serialized page or <code>null</code> when not found
		 */
		public SerializedPage getPage(String sessionId, int pageId)
		{
			Args.notNull(sessionId, "sessionId");

			SerializedPage result = null;
			if (size > 0)
			{
				synchronized (cache)
				{
					for (Iterator<SoftReference<SerializedPage>> i = cache.iterator(); i.hasNext();)
					{
						SoftReference<SerializedPage> ref = i.next();
						SerializedPage entry = ref.get();
						if (entry != null && entry.getPageId() == pageId &&
							entry.getSessionId().equals(sessionId))
						{
							i.remove();
							result = entry;
							break;
						}
					}

					if (result != null)
					{
						// move to top
						storePage(result);
					}
				}
			}
			return result;
		}

		/**
		 * Store the serialized page in cache
		 * 
		 * @param sessionId
		 * @param page
		 */
		void storePage(SerializedPage page)
		{
			SoftReference<SerializedPage> ref = new SoftReference<SerializedPage>(page);

			if (size > 0)
			{
				synchronized (cache)
				{
					for (Iterator<SoftReference<SerializedPage>> i = cache.iterator(); i.hasNext();)
					{
						SoftReference<SerializedPage> r = i.next();
						SerializedPage entry = r.get();
						if (entry != null && entry.equals(page))
						{
							i.remove();
							break;
						}
					}

					cache.add(ref);
					if (cache.size() > size)
					{
						cache.remove(0);
					}
				}
			}
		}
	}
}
