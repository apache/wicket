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

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsynchronousDataStore implements DataStore
{
	private final DataStore dataStore;

	public AsynchronousDataStore(DataStore dataStore)
	{
		this.dataStore = dataStore;

		new Thread(new PageSavingRunnable(), "PageSavingThread").start();
	}

	public void destroy()
	{
		destroy.set(true);
		synchronized (entries)
		{
			entries.notify(); // let the saving thread continue
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

	public byte[] getData(String sessionId, int id)
	{
		String key = getKey(id, sessionId);
		Entry entry = entryMap.get(key);
		if (entry != null)
		{
			return entry.getData();
		}
		else
		{
			return dataStore.getData(sessionId, id);
		}
	}

	public boolean isReplicated()
	{
		return dataStore.isReplicated();
	}

	protected int getMaxQueuedEntries()
	{
		return 100;
	}

	public void removeData(String sessionId, int id)
	{
		synchronized (WRITE_LOCK)
		{
			String key = getKey(id, sessionId);
			Entry entry = entryMap.get(key);
			if (entry != null)
			{
				entryMap.remove(key);
				entries.remove(entry);
			}
		}
		dataStore.removeData(sessionId, id);
	}

	public void removeData(String sessionId)
	{
		synchronized (WRITE_LOCK)
		{
			for (Iterator<Entry> i = entries.iterator(); i.hasNext();)
			{
				Entry e = i.next();
				if (e.getSessionId().equals(sessionId))
				{
					i.remove();
					String key = getKey(e.getPageId(), e.getSessionId());
					entryMap.remove(key);
				}
			}
		}
		dataStore.removeData(sessionId);
	}

	public void storeData(String sessionId, int id, byte[] data)
	{
		if (entryMap.size() > getMaxQueuedEntries())
		{
			dataStore.storeData(sessionId, id, data);
		}
		else
		{
			Entry entry = new Entry(sessionId, id, data);
			entryMap.put(getKey(id, sessionId), entry);
			entries.add(entry);
			synchronized (entries)
			{
				entries.notify();
			}
		}
	}

	private final Queue<Entry> entries = new ConcurrentLinkedQueue<Entry>();
	private final Map<String, Entry> entryMap = new ConcurrentHashMap<String, Entry>();

	private String getKey(int pageId, String sessionId)
	{
		return pageId + "::: " + sessionId;
	}

	private static final Object WRITE_LOCK = new Object();

	private static class Entry
	{
		private final String sessionId;
		private final int pageId;
		private final byte data[];

		public Entry(String sessionId, int pageId, byte data[])
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

	private final AtomicBoolean destroy = new AtomicBoolean(false);

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
						String key = getKey(entry.getPageId(), entry.getSessionId());
						entryMap.remove(key);
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
	};
}
