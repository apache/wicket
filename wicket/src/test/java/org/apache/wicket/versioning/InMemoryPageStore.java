package org.apache.wicket.versioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.pageStore.IDataStore;

/**
 * An implementation of {@link IDataStore} that stores the data in memory. Used only for testing
 * purposes.
 * 
 * @author martin-g
 */
class InMemoryPageStore implements IDataStore
{

	private final Map<String, Map<Integer, byte[]>> store;

	InMemoryPageStore()
	{
		store = new HashMap<String, Map<Integer, byte[]>>();
	}

	public void destroy()
	{
		store.clear();
	}

	public byte[] getData(String sessionId, int pageId)
	{
		byte[] pageAsBytes = null;

		final Map<Integer, byte[]> sessionPages = store.get(sessionId);
		if (sessionPages != null)
		{
			pageAsBytes = sessionPages.get(Integer.valueOf(pageId));
		}

		return pageAsBytes;
	}

	public void removeData(String sessionId, int pageId)
	{
		final Map<Integer, byte[]> sessionPages = store.get(sessionId);
		if (sessionPages != null)
		{
			sessionPages.remove(Integer.valueOf(pageId));
		}
	}

	public void removeData(String sessionId)
	{
		store.remove(sessionId);
	}

	public void storeData(String sessionId, int pageId, byte[] pageAsBytes)
	{
		Map<Integer, byte[]> sessionPages = store.get(sessionId);
		if (sessionPages == null)
		{
			sessionPages = new HashMap<Integer, byte[]>();
			store.put(sessionId, sessionPages);
		}

		sessionPages.put(Integer.valueOf(pageId), pageAsBytes);
	}

	public boolean isReplicated()
	{
		return false;
	}

}