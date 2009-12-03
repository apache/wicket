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
package org.apache.wicket.ng.page.persistent;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.lang.Objects;

public class DefaultPageStore implements PageStore
{
	private final String applicationName;

	private final SerializedPagesCache serializedPagesCache;

	private final DataStore pageDataStore;

	public DefaultPageStore(String applicationName, DataStore dataStore, int cacheSize)
	{
		Checks.argumentNotNull(applicationName, "applicationName");
		Checks.argumentNotNull(dataStore, "DataStore");

		this.applicationName = applicationName;
		pageDataStore = dataStore;
		serializedPagesCache = new SerializedPagesCache(cacheSize);
	}

	public void destroy()
	{
		pageDataStore.destroy();
	}

	protected byte[] getPageData(String sessionId, int pageId)
	{
		return pageDataStore.getData(sessionId, pageId);
	}

	protected void removePageData(String sessionId, int pageId)
	{
		pageDataStore.removeData(sessionId, pageId);
	}

	protected void removePageData(String sessionId)
	{
		pageDataStore.removeData(sessionId);
	}

	protected void storePageData(String sessionId, int pageId, byte[] data)
	{
		pageDataStore.storeData(sessionId, pageId, data);
	}

	public String getApplicationName()
	{
		return applicationName;
	}

	public ManageablePage getPage(String sessionId, int id)
	{
		SerializedPage fromCache = serializedPagesCache.getPage(sessionId, id);
		if (fromCache != null)
		{
			return deserializePage(fromCache.data);
		}
		else
		{
			byte[] data = getPageData(sessionId, id);
			if (data != null)
			{
				return deserializePage(data);
			}
			else
			{
				return null;
			}
		}
	}

	public void removePage(String sessionId, int id)
	{
		serializedPagesCache.removePage(sessionId, id);
		removePageData(sessionId, id);
	}

	public void storePage(String sessionId, ManageablePage page)
	{
		SerializedPage serialized = serializePage(sessionId, page);
		serializedPagesCache.storePage(serialized);
		storePageData(sessionId, serialized.getPageId(), serialized.getData());
	}

	public void unbind(String sessionId)
	{
		removePageData(sessionId);
		serializedPagesCache.removePages(sessionId);
	}

	public ManageablePage convertToPage(Object object)
	{
		if (object instanceof ManageablePage)
		{
			return (ManageablePage)object;
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
			else
			{
				return null;
			}
		}
		else if (object == null)
		{
			return null;
		}
		else
		{
			String type = object != null ? object.getClass().getName() : null;
			throw new IllegalArgumentException("Unknown object type " + type);
		}
	}

	private SerializedPage restoreStrippedSerializedPage(SerializedPage serializedPage)
	{
		SerializedPage result = serializedPagesCache.getPage(serializedPage.getSessionId(),
			serializedPage.getPageId());
		if (result == null)
		{
			byte data[] = getPageData(serializedPage.getSessionId(), serializedPage.getPageId());
			return new SerializedPage(serializedPage.getSessionId(), serializedPage.getPageId(),
				data);
		}
		else
		{
			return result;
		}
	}

	public Serializable prepareForSerialization(String sessionId, Object object)
	{
		if (pageDataStore.isReplicated())
		{
			return null;
		}

		SerializedPage result = null;

		if (object instanceof ManageablePage)
		{
			ManageablePage page = (ManageablePage)object;
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
		else
		{
			return (Serializable)object;
		}
	}

	protected boolean storeAfterSessionReplication()
	{
		return true;
	}

	public Object restoreAfterSerialization(Serializable serializable)
	{
		if (!storeAfterSessionReplication() || serializable instanceof Page)
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
			else
			{
				return page;
			}
		}
		else if (serializable == null)
		{
			return null;
		}
		else
		{
			String type = serializable != null ? serializable.getClass().getName() : null;
			throw new IllegalArgumentException("Unknown object type " + type);
		}

	}

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
			if (obj instanceof SerializedPage == false)
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
	};

	protected SerializedPage serializePage(String sessionId, ManageablePage page)
	{
		Checks.argumentNotNull(sessionId, "sessionId");
		Checks.argumentNotNull(page, "page");

		byte data[] = Objects.objectToByteArray(page, applicationName);
		return new SerializedPage(sessionId, page.getPageId(), data);
	}

	protected ManageablePage deserializePage(byte data[])
	{
		return (ManageablePage)Objects.byteArrayToObject(data);
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
	class SerializedPagesCache
	{
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

		private final int size;

		private final List<SoftReference<SerializedPage>> cache;

		public SerializedPage removePage(String sessionId, int id)
		{
			Checks.argumentNotNull(sessionId, "sessionId");

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

		public void removePages(String sessionId)
		{
			Checks.argumentNotNull(sessionId, "sessionId");

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

		public SerializedPage getPage(String sessionId, int id)
		{
			Checks.argumentNotNull(sessionId, "sessionId");

			SerializedPage result = null;
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
							result = entry;
							break;
						}
					}

					if (result != null)
					{
						// move to top
						cache.add(new SoftReference<SerializedPage>(result));
					}
				}
			}
			return result;
		}

		/**
		 * Store the serialized page in cache
		 * 
		 * @return serialized page
		 * @param sessionId
		 * @param page
		 * @param pagesList
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
