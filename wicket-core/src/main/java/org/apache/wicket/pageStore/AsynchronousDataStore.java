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

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Facade for {@link IDataStore} that does the actual saving in worker thread.
 * 
 * @author Matej Knopp
 */
public class AsynchronousDataStore implements IDataStore
{
	private static final Object WRITE_LOCK = new Object();

	private final AtomicBoolean destroy = new AtomicBoolean(false);

	private final IDataStore dataStore;

	private final Queue<Entry> entries = new ConcurrentLinkedQueue<Entry>();

	private final Map<String, Entry> entryMap = new ConcurrentHashMap<String, Entry>();

	/**
	 * Construct.
	 * 
	 * @param dataStore
	 */
	public AsynchronousDataStore(final IDataStore dataStore)
	{
		this.dataStore = dataStore;

		new Thread(new PageSavingRunnable(), "Wicket-PageSavingThread").start();
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#destroy()
	 */
	public void destroy()
	{
		destroy.set(true);

		synchronized (entries)
		{
			// let the saving thread continue
			entries.notify();
		}

		try
		{
			synchronized (destroy)
			{
				destroy.wait();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		dataStore.destroy();
	}

	/**
	 * Little helper
	 * 
	 * @param sessionId
	 * @param id
	 * @return Entry
	 */
	private Entry getEntry(final String sessionId, final int id)
	{
		return entryMap.get(getKey(sessionId, id));
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#getData(java.lang.String, int)
	 */
	public byte[] getData(final String sessionId, final int id)
	{
		Entry entry = getEntry(sessionId, id);
		if (entry != null)
		{
			return entry.getData();
		}
		return dataStore.getData(sessionId, id);
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#isReplicated()
	 */
	public boolean isReplicated()
	{
		return dataStore.isReplicated();
	}

	/**
	 * @return max queue size
	 */
	protected int getMaxQueuedEntries()
	{
		return 100;
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(java.lang.String, int)
	 */
	public void removeData(final String sessionId, final int id)
	{
		synchronized (WRITE_LOCK)
		{
			String key = getKey(sessionId, id);
			if (key != null)
			{
				Entry entry = entryMap.remove(key);
				if (entry != null)
				{
					entries.remove(entry);
				}
			}

		}
		dataStore.removeData(sessionId, id);
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(java.lang.String)
	 */
	public void removeData(final String sessionId)
	{
		synchronized (WRITE_LOCK)
		{
			for (Iterator<Entry> iter = entries.iterator(); iter.hasNext();)
			{
				Entry e = iter.next();
				if (e.getSessionId().equals(sessionId))
				{
					iter.remove();
					entryMap.remove(getKey(e));
				}
			}
		}

		dataStore.removeData(sessionId);
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#storeData(java.lang.String, int, byte[])
	 */
	public void storeData(final String sessionId, final int id, final byte[] data)
	{
		if (entryMap.size() > getMaxQueuedEntries())
		{
			dataStore.storeData(sessionId, id, data);
		}
		else
		{
			Entry entry = new Entry(sessionId, id, data);
			entryMap.put(getKey(sessionId, id), entry);
			entries.add(entry);
			synchronized (entries)
			{
				entries.notify();
			}
		}
	}

	/**
	 * 
	 * @param pageId
	 * @param sessionId
	 * @return generated key
	 */
	private String getKey(final String sessionId, final int pageId)
	{
		return pageId + "::: " + sessionId;
	}

	/**
	 * 
	 * @param entry
	 * @return generated key
	 */
	private String getKey(final Entry entry)
	{
		return getKey(entry.getSessionId(), entry.getPageId());
	}

	/**
	 * 
	 */
	private static class Entry
	{
		private final String sessionId;
		private final int pageId;
		private final byte data[];

		public Entry(final String sessionId, final int pageId, final byte data[])
		{
			this.sessionId = sessionId;
			this.pageId = pageId;
			this.data = data;
		}

		public String getSessionId()
		{
			return sessionId;
		}

		public int getPageId()
		{
			return pageId;
		}

		public byte[] getData()
		{
			return data;
		}
	}

	/**
	 * 
	 */
	private class PageSavingRunnable implements Runnable
	{
		public void run()
		{
			while (destroy.get() == false || !entries.isEmpty())
			{
				if (entries.isEmpty())
				{
					try
					{
						synchronized (entries)
						{
							entries.wait();
						}
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				synchronized (WRITE_LOCK)
				{
					Entry entry = entries.poll();
					if (entry != null)
					{
						dataStore.storeData(entry.getSessionId(), entry.getPageId(),
							entry.getData());
						entryMap.remove(getKey(entry));
					}
				}
			}

			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			synchronized (destroy)
			{
				destroy.notify();
			}
		}
	}
}
