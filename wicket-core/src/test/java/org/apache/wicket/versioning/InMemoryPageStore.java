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
package org.apache.wicket.versioning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.pageStore.IDataStore;

/**
 * An implementation of {@link IDataStore} that stores the data in memory. Used only for testing
 * purposes.
 * 
 * @author martin-g
 */
public class InMemoryPageStore implements IDataStore
{

	/**
	 * A map of : sessionId => pageId => pageAsBytes
	 */
	private final ConcurrentHashMap<String, Map<Integer, byte[]>> store;

	/**
	 * Construct.
	 */
	public InMemoryPageStore()
	{
		store = new ConcurrentHashMap<String, Map<Integer, byte[]>> ();
	}

	@Override
	public void destroy()
	{
		store.clear();
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#getData(java.lang.String, int)
	 */
	@Override
	public byte[] getData(String sessionId, int pageId)
	{
		byte[] pageAsBytes = null;

		final Map<Integer, byte[]> sessionPages = store.get(sessionId);
		if (sessionPages != null)
		{
			pageAsBytes = sessionPages.get(pageId);
		}

		return pageAsBytes;
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(java.lang.String, int)
	 */
	@Override
	public void removeData(String sessionId, int pageId)
	{
		final Map<Integer, byte[]> sessionPages = store.get(sessionId);
		if (sessionPages != null)
		{
			sessionPages.remove(pageId);
		}
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(java.lang.String)
	 */
	@Override
	public void removeData(String sessionId)
	{
		store.remove(sessionId);
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#storeData(java.lang.String, int, byte[])
	 */
	@Override
	public void storeData(String sessionId, int pageId, byte[] pageAsBytes)
	{
		Map<Integer, byte[]> sessionPages = store.get(sessionId);
		if (sessionPages == null)
		{
			sessionPages = new ConcurrentHashMap<Integer, byte[]>();
			Map<Integer, byte[]> tmpSessionPages = store.putIfAbsent(sessionId, sessionPages);
			if (tmpSessionPages != null)
			{
				sessionPages = tmpSessionPages;
			}
		}

		sessionPages.put(pageId, pageAsBytes);
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#isReplicated()
	 */
	@Override
	public boolean isReplicated()
	{
		return false;
	}

	@Override
	public boolean canBeAsynchronous()
	{
		return false;
	}

}