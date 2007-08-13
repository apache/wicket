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
package org.apache.wicket.protocol.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.collections.IntHashMap;
import org.apache.wicket.util.concurrent.ConcurrentHashMap;
import org.apache.wicket.util.lang.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Stores pages on disk.
 * <p>
 * Override {@link #getWorkDir()} to change the default directory for pages,
 * which is configured from the javax.servlet.context.tempdir attribute in the
 * servlet context.
 * 
 * @author jcompagner
 */
public class FilePageStore implements IPageStore
{

	protected int getSleepTimeMs()
	{
		return 1000;
	}

	private class PageSavingThread implements Runnable
	{
		private volatile boolean stop = false;
		private long totalSavingTime = 0;
		private int saved;
		private int bytesSaved;

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (!stop)
			{
				try
				{
					while (pagesToBeSaved.size() == 0)
					{
						Thread.sleep(getSleepTimeMs());
						if (stop)
						{
							return;
						}
					}
					// if ( pagesToBeSaved.size() > 100)
					// {
					// System.err.println("max");
					// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					// }
					// else if ( pagesToBeSaved.size() > 25)
					// {
					// System.err.println("normal");
					// Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
					// }
					// else
					// {
					// System.err.println("min");
					// Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					// }
					Iterator it = pagesToBeSaved.entrySet().iterator();
					while (it.hasNext())
					{
						Map.Entry entry = (Entry)it.next();
						SessionPageKey key = (SessionPageKey)entry.getKey();
						if (key.data instanceof byte[])
						{
							savePage(key, (byte[])key.data);
						}
						it.remove();
					}
				}
				catch (Exception e)
				{
					log.error("Error in page save thread", e);
				}
			}
		}

		/**
		 * Stops this thread.
		 */
		public void stop()
		{
			if (log.isDebugEnabled())
			{
				log.debug("Total time in saving: " + totalSavingTime);
				log.debug("Bytes saved: " + bytesSaved);
				log.debug("Pages saved: " + saved);
			}
			stop = true;
		}


		/**
		 * @param sessionId
		 * @param key
		 * @param bytes
		 */
		private void savePage(SessionPageKey key, byte[] bytes)
		{
			long t1 = System.currentTimeMillis();
			long length = FilePageStore.this.savePage(key, bytes);
			long t3 = System.currentTimeMillis();
			if (log.isDebugEnabled())
			{
				log.debug("storing page " + key.pageClassName + "[" + key.id + "," +
						key.versionNumber + "] size: " + length + " for session " + key.sessionId +
						" took " + (t3 - t1) + " miliseconds to save");
			}
			totalSavingTime += (t3 - t1);
			saved++;
			bytesSaved += length;
		}

	}

	protected long savePage(SessionPageKey key, byte[] bytes)
	{
		return doSavePage(key, bytes);
	}

	protected long doSavePage(SessionPageKey key, byte[] bytes)
	{
		File sessionDir = new File(getWorkDir(), key.sessionId);
		sessionDir.mkdirs();
		File pageFile = getPageFile(key, sessionDir);

		FileOutputStream fos = null;
		int length = 0;
		try
		{
			fos = new FileOutputStream(pageFile);
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			fos.getChannel().write(bb);
			length = bytes.length;
		}
		catch (Exception e)
		{
			log.error("Error saving page " + key.pageClassName + " [" + key.id + "," +
					key.versionNumber + "] for the sessionid " + key.sessionId);
		}
		finally
		{
			try
			{
				if (fos != null)
				{
					fos.close();
				}
			}
			catch (IOException ex)
			{
				// ignore
			}
		}
		return length;
	}

	private class PageSerializingThread implements Runnable
	{
		private volatile boolean stop = false;

		private int serializedInThread = 0;

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (!stop)
			{
				try
				{
					while (pagesToBeSerialized.size() == 0)
					{
						Thread.sleep(getSleepTimeMs());
						if (stop)
						{
							return;
						}
					}

					Iterator it = pagesToBeSerialized.entrySet().iterator();
					outer : while (it.hasNext())
					{
						Map.Entry entry = (Entry)it.next();
						List sessionList = (List)entry.getValue();
						while (true)
						{
							Page page = null;
							SessionPageKey key = null;
							synchronized (sessionList)
							{
								if (sessionList.size() != 0)
								{
									key = (SessionPageKey)sessionList.get(0);
									if (key.data instanceof Page)
									{
										page = (Page)key.data;
										key.setObject(SERIALIZING);
									}
									else
									{
										sessionList.remove(0);
										continue;
									}
								}
								// no key found in the current list.
								if (key == null)
								{
									// the list is removed now!
									// but it could be that a request add
									// something to the list now.
									// thats why a request has to check it
									// again.
									pagesToBeSerialized.remove(entry.getKey());
									continue outer;
								}
							}

							byte[] pageBytes = serializePage(key, page);
							serializedInThread++;
							synchronized (sessionList)
							{
								key.setObject(pageBytes);
								sessionList.remove(key);
								sessionList.notifyAll();
							}
							pagesToBeSaved.put(key, key);
						}
					}
				}
				catch (Exception e)
				{
					log.error("Error in page save thread", e);
				}
			}
		}

		/**
		 * Stops this thread.
		 */
		public void stop()
		{
			if (log.isDebugEnabled())
			{
				log.debug("Total time in serialization: " + totalSerializationTime);
				log.debug("Total Pages serialized: " + serialized);
				log.debug("Pages serialized by thread: " + serializedInThread);
			}
			stop = true;
		}
	}

	/**
	 * Key based on session id, page id, version numbers, etc. We don't
	 * serialize it anywhere. It's made serializable for convenience if a
	 * subclass needs to serialize it
	 */
	protected static class SessionPageKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String sessionId;
		private final int id;
		private final int versionNumber;
		private final int ajaxVersionNumber;
		private final String pageMap;
		private final String pageClassName;

		private volatile Object data;

		SessionPageKey(String sessionId, int id, int versionNumber, int ajaxVersionNumber,
				String pagemap, Class pageClass)
		{
			this(sessionId, id, versionNumber, ajaxVersionNumber, pagemap, pageClass, null);
		}

		SessionPageKey(String sessionId, int id, int versionNumber, int ajaxVersionNumber,
				String pagemap, Class pageClass, Page page)
		{
			this.sessionId = sessionId;
			this.id = id;
			this.versionNumber = versionNumber;
			this.ajaxVersionNumber = ajaxVersionNumber;
			pageClassName = pageClass == null ? null : pageClass.getName();
			pageMap = pagemap;
			data = page;
		}

		SessionPageKey(String sessionId, Page page)
		{
			this(sessionId, page.getNumericId(), page.getCurrentVersionNumber(), page
					.getAjaxVersionNumber(), page.getPageMapName(), page.getClass(), page);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (obj instanceof SessionPageKey)
			{
				SessionPageKey key = (SessionPageKey)obj;
				return id == key.id &&
						versionNumber == key.versionNumber &&
						ajaxVersionNumber == key.ajaxVersionNumber &&
						((pageMap != null && pageMap.equals(key.pageMap)) || (pageMap == null && key.pageMap == null)) &&
						sessionId.equals(key.sessionId);
			}
			return false;
		}

		/**
		 * @return The current object inside the SessionPageKey
		 */
		public Object getObject()
		{
			return data;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			// TODO with Java 5, replace by .valueOf usage
			return Objects.hashCode(new Object[] { new Integer(id), new Integer(versionNumber),
					new Integer(ajaxVersionNumber), pageMap, sessionId });
		}

		/**
		 * Sets the current object inside the SessionPageKey
		 * 
		 * @param o
		 *            The object
		 */
		public void setObject(Object o)
		{
			data = o;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "SessionPageKey[" + sessionId + "," + id + "," + versionNumber + "," +
					ajaxVersionNumber + ", " + pageMap + ", " + data + "]";
		}
	}

	private static final Object SERIALIZING = new Object();
	/** log. */
	protected static Logger log = LoggerFactory.getLogger(FilePageStore.class);


	private final File defaultWorkDir;
	private final PageSerializingThread serThread;


	private final ConcurrentHashMap pagesToBeSerialized;

	private final PageSavingThread saveThread;
	private final ConcurrentHashMap pagesToBeSaved;

	private final String appName;

	private volatile int serialized;

	private volatile long totalSerializationTime = 0;

	/**
	 * Construct.
	 */
	public FilePageStore()
	{
		this((File)((WebApplication)Application.get()).getServletContext().getAttribute(
				"javax.servlet.context.tempdir"));
	}

	/**
	 * Construct.
	 * 
	 * @param dir
	 *            The directory to save to.
	 */
	public FilePageStore(File dir)
	{
		defaultWorkDir = new File(dir, "sessions/");
		defaultWorkDir.mkdirs();

		pagesToBeSerialized = new ConcurrentHashMap();
		pagesToBeSaved = new ConcurrentHashMap();
		appName = Application.get().getApplicationKey();

		saveThread = new PageSavingThread();
		Thread t = new Thread(saveThread, "FilePageSavingThread-" + appName);
		t.setDaemon(true);
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();

		serThread = new PageSerializingThread();
		t = new Thread(serThread, "FilePageSerializingThread-" + appName);
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();

		log.info("storing sessions in " + dir + "/sessions");
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		saveThread.stop();
		serThread.stop();
	}

	protected byte[] doLoadPage(SessionPageKey key)
	{
		File sessionDir = new File(getWorkDir(), key.sessionId);
		byte[] pageData = null;

		if (sessionDir.exists())
		{
			File pageFile = getPageFile(key, sessionDir);
			if (pageFile.exists())
			{
				long t1 = System.currentTimeMillis();
				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(pageFile);
					int length = (int)pageFile.length();
					ByteBuffer bb = ByteBuffer.allocate(length);
					fis.getChannel().read(bb);
					if (bb.hasArray())
					{
						pageData = bb.array();
					}
					else
					{
						pageData = new byte[length];
						bb.get(pageData);
					}
				}
				catch (Exception e)
				{
					log.debug("Error loading page " + key.id + "," + key.versionNumber +
							" for the sessionid " + key.sessionId + " from disk", e);
				}
				finally
				{
					try
					{
						if (fis != null)
						{
							fis.close();
						}
					}
					catch (IOException ex)
					{
						// ignore
					}
				}
			}
		}
		return pageData;
	}

	protected byte[] loadPage(SessionPageKey key)
	{
		return doLoadPage(key);
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#getPage(java.lang.String,
	 *      int, int)
	 */
	public Page getPage(String sessionId, String pagemapName, int id, int versionNumber,
			int ajaxVersionNumber)
	{
		// first try to figure out correct versionNumber and ajaxVersionNumeber
		// if necessary
		if (versionNumber == -1 || ajaxVersionNumber == -1)
		{
			PageVersions versions = findPageVersion(sessionId, pagemapName, id, versionNumber);
			if (versions == null)
			{
				return null;
			}
			else
			{
				versionNumber = versions.versionid;
				ajaxVersionNumber = versions.ajaxversionid;
			}
		}

		SessionPageKey currentKey = new SessionPageKey(sessionId, id, versionNumber,
				ajaxVersionNumber, pagemapName, null);
		long t1 = System.currentTimeMillis();
		byte[] bytes = testMap(currentKey);
		if (bytes != null)
		{
			return readPage(versionNumber, bytes);
		}
		long t2 = System.currentTimeMillis();
		byte pageData[] = loadPage(currentKey);
		if (pageData != null)
		{
			Page page = readPage(versionNumber, pageData);
			if (page != null && log.isDebugEnabled())
			{
				long t3 = System.currentTimeMillis();
				log.debug("restoring page " + page.getClass() + "[" + page.getNumericId() + "," +
						page.getCurrentVersionNumber() + "] size: " + pageData.length +
						" for session " + sessionId + " took " + (t2 - t1) +
						" miliseconds to read in and " + (t3 - t2) + " miliseconds to deserialize");
			}
			return page;
		}
		else
		{
			return null;
		}
	}

	/**
	 * @param versionNumber
	 * @param bytes
	 * @return page instance
	 */
	private Page readPage(int versionNumber, byte[] bytes)
	{
		boolean set = Page.serializer.get() == null;
		Page page = null;
		try
		{
			if (set)
			{
				Page.serializer.set(new PageSerializer(null));
			}
			IPageMapEntry entry = (IPageMapEntry)Objects.byteArrayToObject(bytes);
			if (entry != null)
			{
				page = entry.getPage();
				page = page.getVersion(versionNumber);
			}
		}
		finally
		{
			if (set)
			{
				Page.serializer.set(null);
			}
		}
		return page;
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#pageAccessed(java.lang.String,
	 *      org.apache.wicket.Page)
	 */
	public void pageAccessed(String sessionId, Page page)
	{
		SessionPageKey currentKey = new SessionPageKey(sessionId, page);
		testMap(currentKey);
	}


	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#removePage(java.lang.String,
	 *      org.apache.wicket.Page)
	 */
	public void removePage(String sessionId, String pageMapName, int pageId)
	{
		removePageFromPendingMap(sessionId, pageMapName, pageId);
		removeFiles(sessionId, pageMapName, pageId);
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#storePage(java.lang.String,
	 *      org.apache.wicket.Page)
	 */
	public void storePage(String sessionId, Page page)
	{
		List list = (List)pagesToBeSerialized.get(sessionId);
		if (list == null)
		{
			list = new LinkedList();
		}
		synchronized (list)
		{
			list.add(new SessionPageKey(sessionId, page));
		}
		// do really put it back in.. The writer thread could have removed it.
		pagesToBeSerialized.put(sessionId, list);

		putPageVersion(sessionId, page.getPageMapName(), new PageVersions(page.getNumericId(), page
				.getCurrentVersionNumber(), page.getAjaxVersionNumber()));
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		removeSessionFromPendingMap(sessionId);
		removePageVersions(sessionId);
	}

	/**
	 * @param key
	 * @param sessionDir
	 * @return The file pointing to the page
	 */
	private File getPageFile(SessionPageKey key, File sessionDir)
	{
		return new File(sessionDir, appName + "-pm-" + key.pageMap + "-p-" + key.id + "-v-" +
				key.versionNumber + "-a-" + key.ajaxVersionNumber);
	}


	private void removeFiles(String sessionId, String pageMap, int id)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			final String filepart;
			if (id != -1)
			{
				filepart = appName + "-pm-" + pageMap + "-p-" + id;
			}
			else
			{
				filepart = appName + "-pm-" + pageMap;
			}
			File[] listFiles = sessionDir.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.startsWith(filepart);
				}
			});
			for (int i = 0; i < listFiles.length; i++)
			{
				listFiles[i].delete();
			}
			if (id == -1)
			{
				removePageVersions(sessionId, pageMap);
			}
		}

	}

	/**
	 * @param sessionId
	 *            The session of the page that must be removed
	 * @param pageMap
	 *            The pagemap of the page that must be removed
	 * @param id
	 *            The id of the page.
	 */
	private void removePageFromPendingMap(String sessionId, String pageMap, int id)
	{
		List list = (List)pagesToBeSerialized.get(sessionId);

		if (list == null)
		{
			return;
		}

		synchronized (list)
		{
			Iterator iterator = list.iterator();
			while (iterator.hasNext())
			{
				SessionPageKey key = (SessionPageKey)iterator.next();

				if ((id == -1 || key.id == id) && Objects.equal(key.sessionId, sessionId) &&
						Objects.equal(key.pageMap, pageMap))
				{
					iterator.remove();
				}
			}
		}
	}

	private void removeSession(String sessionId)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File[] files = sessionDir.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					files[i].delete();
				}
			}
			if (!sessionDir.delete())
			{
				sessionDir.deleteOnExit();
			}
		}
	}

	private void removeSessionFromPendingMap(String sessionId)
	{
		pagesToBeSerialized.remove(sessionId);
		// TODO remove from pagesToBeSaved..
		removeSession(sessionId);

	}

	private byte[] serializePage(SessionPageKey key, Page page)
	{
		byte[] bytes = null;
		// System.err.println("SERIALIZING " + key);
		long t1 = System.currentTimeMillis();
		Page.serializer.set(new PageSerializer(key));
		try
		{
			bytes = Objects.objectToByteArray(page.getPageMapEntry());
			totalSerializationTime += (System.currentTimeMillis() - t1);
			serialized++;
			if (log.isDebugEnabled() && bytes != null)
			{
				log.debug("serializing page " + key.pageClassName + "[" + key.id + "," +
						key.versionNumber + "] size: " + bytes.length + " for session " +
						key.sessionId + " took " + (System.currentTimeMillis() - t1) +
						" miliseconds to serialize");
			}
		}
		finally
		{
			Page.serializer.set(null);
		}
		// System.err.println("SERIALIZING " + key + " bytes: " + bytes);
		return bytes;
	}

	private byte[] testMap(final SessionPageKey currentKey)
	{
		// System.err.println("TESTMAP:" + currentKey);
		byte[] bytes = null;
		List list = (List)pagesToBeSerialized.get(currentKey.sessionId);
		if (list == null)
		{
			SessionPageKey previousPage = (SessionPageKey)pagesToBeSaved.get(currentKey);
			if (previousPage != null)
			{
				bytes = (byte[])previousPage.data;
			}
		}
		else
		{
			while (true)
			{
				SessionPageKey listKey = null;
				synchronized (list)
				{
					if (list.size() > 0)
					{
						listKey = (SessionPageKey)list.get(list.size() - 1);
						Object object = listKey.data;
						if (object instanceof Page)
						{
							list.remove(list.size() - 1);
						}
						else if (object == SERIALIZING)
						{
							try
							{
								list.wait();
							}
							catch (InterruptedException ex)
							{
								throw new RuntimeException(ex);
							}
						}
					}
					else
					{
						break;
					}
				}
				if (listKey.data instanceof Page)
				{
					byte[] ser = serializePage(listKey, (Page)listKey.data);
					if (ser != null)
					{
						listKey.setObject(ser);
						pagesToBeSaved.put(listKey, listKey);
					}
				}
				if (listKey.equals(currentKey) && listKey.data instanceof byte[])
				{
					bytes = (byte[])listKey.data;
				}
			}
		}
		if (bytes == null)
		{
			SessionPageKey previousPage = (SessionPageKey)pagesToBeSaved.get(currentKey);
			if (previousPage != null)
			{
				bytes = (byte[])previousPage.data;
			}
		}
		return bytes;
	}

	/**
	 * Returns the working directory for this disk-based PageStore. Override
	 * this to configure a different location. The default is
	 * javax.servlet.context.tempdir from the servlet context.
	 * 
	 * @return Working directory
	 */
	protected File getWorkDir()
	{
		return defaultWorkDir;
	}

	private class PageSerializer implements Page.IPageSerializer
	{
		private SessionPageKey current;

		private final List previous = new ArrayList();
		private final List completed = new ArrayList();


		/**
		 * Construct.
		 * 
		 * @param key
		 */
		public PageSerializer(SessionPageKey key)
		{
			current = key;
		}

		/**
		 * @throws IOException
		 * @see org.apache.wicket.Page.IPageSerializer#serializePage(org.apache.wicket.Page)
		 */
		public void serializePage(Page page, ObjectOutputStream stream) throws IOException
		{
			if (current.id == page.getNumericId())
			{
				stream.writeBoolean(false);
				stream.defaultWriteObject();
				return;
			}
			SessionPageKey spk = new SessionPageKey(current.sessionId, page);
			if (!completed.contains(spk) && !previous.contains(spk))
			{
				previous.add(current);
				current = spk;
				byte[] bytes = Objects.objectToByteArray(page.getPageMapEntry());
				current.setObject(bytes);
				pagesToBeSaved.put(spk, spk);
				completed.add(current);
				current = (SessionPageKey)previous.remove(previous.size() - 1);
			}
			stream.writeBoolean(true);
			stream.writeObject(new PageHolder(page));
		}

		public Page deserializePage(int id, String name, Page page, ObjectInputStream stream)
				throws IOException, ClassNotFoundException
		{
			HashMap pageMaps = (HashMap)SecondLevelCacheSessionStore.usedPages.get();
			if (pageMaps == null)
			{
				pageMaps = new HashMap();
				SecondLevelCacheSessionStore.usedPages.set(pageMaps);
			}
			IntHashMap pages = (IntHashMap)pageMaps.get(name);
			if (pages == null)
			{
				pages = new IntHashMap();
				pageMaps.put(name, pages);
			}
			pages.put(id, page);
			boolean b = stream.readBoolean();
			if (b == false)
			{
				stream.defaultReadObject();
				return page;
			}
			else
			{
				// the object will resolve to a Page (probably PageHolder)
				return (Page)stream.readObject();
			}
		}
	}

	/**
	 * Class that resolves to page instance
	 */
	private static class PageHolder implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int pageid;
		private final String pagemap;

		PageHolder(Page page)
		{
			pageid = page.getNumericId();
			pagemap = page.getPageMapName();
		}

		protected Object readResolve() throws ObjectStreamException
		{
			return Session.get().getPage(pagemap, Integer.toString(pageid), -1);
		}
	}

	// this is necessary to keep last version/pageversion informations (when -1
	// is specified in getpage)
	private static class PageVersions
	{
		private static final long serialVersionUID = 1L;

		private final int pageid;
		private final int versionid;
		private final int ajaxversionid;

		PageVersions(int pageid, int versionid, int ajaxversionid)
		{
			this.pageid = pageid;
			this.versionid = versionid;
			this.ajaxversionid = ajaxversionid;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (obj instanceof PageVersions)
			{
				return ((PageVersions)obj).pageid == pageid &&
						((PageVersions)obj).versionid == versionid;
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return pageid;
		}
	}

	// sessionId->(pageMapName->List<PageVersion>)
	private final Map /* <String, Map<String, List<PageVersions>> */pageVersionMap = new ConcurrentHashMap();

	private List /* <PageVersions> */getPageVersions(String sessionId, String pageMapName,
			boolean createIfDoesNotExist)
	{
		if (pageMapName == null)
			pageMapName = "";

		Map pageMapToList = (Map)pageVersionMap.get(sessionId);
		if (pageMapToList == null && createIfDoesNotExist == false)
		{
			return null;
		}
		else if (pageMapToList == null && createIfDoesNotExist == true)
		{
			pageMapToList = new ConcurrentHashMap();
			pageVersionMap.put(sessionId, pageMapToList);
		}

		List versions = (List)pageMapToList.get(pageMapName);
		if (versions == null && createIfDoesNotExist)
		{
			versions = new ArrayList();
			pageMapToList.put(pageMapName, versions);
		}
		return versions;
	}

	private void removePageVersions(String sessionId)
	{
		pageVersionMap.remove(sessionId);
	}

	private void removePageVersions(String sessionId, String pageMapName)
	{
		if (pageMapName == null)
		{
			pageMapName = "";
		}
		else
		{
			Map map = (Map)pageVersionMap.get(sessionId);
			if (map != null)
			{
				map.remove(pageMapName);
			}
		}
	}

	private PageVersions findPageVersion(String sessionId, String pageMapName, int pageId,
			int pageVersion)
	{
		List list = getPageVersions(sessionId, pageMapName, false);
		PageVersions result = null;
		if (list != null)
		{
			synchronized (list)
			{
				if (pageVersion == -1)
				{
					// get the last touched page with given id
					for (int index = list.size() - 1; index >= 0; --index)
					{
						if (((PageVersions)list.get(index)).pageid == pageId)
						{
							result = (PageVersions)list.get(index);
							break;
						}
					}
				}
				else
				{
					// we need to find last ajax version for given page version
					int index = list.indexOf(new PageVersions(pageId, pageVersion, -1));
					if (index != -1)
					{
						result = (PageVersions)list.get(index);
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param recordPageVersionCount
	 */
	public void setRecordPageVersionCount(int recordPageVersionCount)
	{
		this.recordPageVersionCount = recordPageVersionCount;
	}

	/**
	 * @return
	 */
	public int getRecordPageVersionCount()
	{
		return recordPageVersionCount;
	}

	private int recordPageVersionCount = 100;

	private void putPageVersion(String sessionId, String pageMapName, PageVersions pv)
	{
		List list = getPageVersions(sessionId, pageMapName, true);
		synchronized (list)
		{
			list.remove(pv);
			list.add(pv);
			if (list.size() > getRecordPageVersionCount())
			{
				list.remove(0);
			}
		}
	}

	public boolean containsPage(String sessionId, String pageMapName, int pageId, int pageVersion)
	{
		List list = getPageVersions(sessionId, pageMapName, false);
		if (list != null)
		{
			synchronized (list)
			{
				for (Iterator i = list.iterator(); i.hasNext();)
				{
					PageVersions v = (PageVersions)i.next();
					if (v.pageid == pageId && v.versionid == pageVersion)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}
