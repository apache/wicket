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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.disk.NestedFolders;
import org.apache.wicket.pageStore.disk.PageWindowManager;
import org.apache.wicket.pageStore.disk.PageWindowManager.FileWindow;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage of pages on disk.
 * <p>
 * All pages passed into this store are restricted to be {@link SerializedPage}s.
 * <p>
 * Implementation note: {@link DiskPageStore} writes pages into a single file, appending new pages while overwriting the oldest pages.
 * Since Ajax requests do not change the id of a page, {@link DiskPageStore} offers an optimization to overwrite the most recently written
 * page, if it has the same id as a new page to write.<p>
 * However this does not help in case of alternating requests between multiple browser windows: In this case requests are processed for
 * different page ids and the oldest pages are constantly overwritten (this can easily happen with Ajax timers on one or more pages).
 * This leads to pages with identical id superfluously kept in the file, while older pages are prematurely expelled.
 * Any following request to these older pages will then fail with {@link PageExpiredException}.   
 */
public class DiskPageStore extends AbstractPersistentPageStore implements IPersistentPageStore
{
	private static final Logger log = LoggerFactory.getLogger(DiskPageStore.class);

	/**
	 * Name of the file where the page index is stored.
	 */
	private static final String INDEX_FILE_NAME = "DiskPageStoreIndex";

	private final Bytes maxSizePerSession;

	private final NestedFolders folders;

	private final ConcurrentMap<String, DiskData> diskDatas;

	/**
	 * Create a store that supports {@link SerializedPage}s only.
	 * 
	 * @param applicationName
	 *            name of application
	 * @param fileStoreFolder
	 *            folder to store to
	 * @param maxSizePerSession
	 *            maximum size per session
	 * 
	 * @see SerializingPageStore
	 */
	public DiskPageStore(String applicationName, File fileStoreFolder, Bytes maxSizePerSession)
	{
		super(applicationName);
		
		this.folders = new NestedFolders(new File(fileStoreFolder, applicationName + "-filestore"));
		this.maxSizePerSession = Args.notNull(maxSizePerSession, "maxSizePerSession");

		this.diskDatas = new ConcurrentHashMap<>();

		try
		{
			if (folders.getBase().exists() || folders.getBase().mkdirs())
			{
				loadIndex();
			}
			else
			{
				log.warn("Cannot create file store folder for some reason.");
			}
		}
		catch (SecurityException e)
		{
			throw new WicketRuntimeException(
				"SecurityException occurred while creating DiskPageStore. Consider using a non-disk based IPageStore implementation. "
					+ "See org.apache.wicket.Application.setPageManagerProvider(IPageManagerProvider)",
				e);
		}
	}

	/**
	 * Pages are already serialized.
	 */
	@Override
	public boolean supportsVersioning()
	{
		return true;
	}
	
	@Override
	public void destroy()
	{
		log.debug("Destroying...");
		saveIndex();

		super.destroy();
		log.debug("Destroyed.");
	}

	@Override
	protected IManageablePage getPersistedPage(String sessionIdentifier, int id)
	{
		DiskData diskData = getDiskData(sessionIdentifier, false);
		if (diskData != null)
		{
			byte[] data = diskData.loadPage(id);
			if (data != null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Returning page with id '{}' in session with id '{}'", id, sessionIdentifier);
				}
				
				return new SerializedPage(id, "unknown", data);
			}
		}
		
		return null;
	}

	@Override
	protected void removePersistedPage(String sessionIdentifier, IManageablePage page)
	{
		DiskData diskData = getDiskData(sessionIdentifier, false);
		if (diskData != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Removing page with id '{}' in session with id '{}'", page.getPageId(), sessionIdentifier);
			}
			
			diskData.removeData(page.getPageId());
		}
	}

	@Override
	protected void removeAllPersistedPages(String sessionIdentifier)
	{
		DiskData diskData = getDiskData(sessionIdentifier, false);
		if (diskData != null)
		{
			synchronized (diskDatas)
			{
				diskDatas.remove(diskData.sessionIdentifier);
				diskData.unbind();
			}
		}
	}

	@Override
	protected void addPersistedPage(String sessionIdentifier, IManageablePage page)
	{
		if (page instanceof SerializedPage == false)
		{
			throw new WicketRuntimeException("DiskPageStore works with serialized pages only");
		}
		SerializedPage serializedPage = (SerializedPage) page;

		DiskData diskData = getDiskData(sessionIdentifier, true);

		log.debug("Storing data for page with id '{}' in session with id '{}'", serializedPage.getPageId(), sessionIdentifier);

		byte[] data = serializedPage.getData();
		String type = serializedPage.getPageType();

		diskData.savePage(serializedPage.getPageId(), type, data);
	}

	/**
	 * Get the data on disk for the given session identifier.
	 * 
	 * @param sessionIdentifier identifier of session
	 * @return matching data
	 */
	protected DiskData getDiskData(String sessionIdentifier, boolean create)
	{
		if (!create)
		{
			return diskDatas.get(sessionIdentifier);
		}

		DiskData data = new DiskData(this, sessionIdentifier);
		DiskData existing = diskDatas.putIfAbsent(sessionIdentifier, data);
		return existing != null ? existing : data;
	}

	/**
	 * Load the index
	 */
	@SuppressWarnings("unchecked")
	private void loadIndex()
	{
		File storeFolder = folders.getBase();

		File index = new File(storeFolder, INDEX_FILE_NAME);
		if (index.exists() && index.length() > 0)
		{
			try (InputStream stream = new FileInputStream(index))
			{
				ObjectInputStream ois = new ObjectInputStream(stream);

				diskDatas.clear();

				for (DiskData diskData : (List<DiskData>)ois.readObject())
				{
					diskData.pageStore = this;
					diskDatas.put(diskData.sessionIdentifier, diskData);
				}
			}
			catch (Exception e)
			{
				log.error("Couldn't load DiskPageStore index from file " + index + ".", e);
			}
		}
		Files.remove(index);
	}

	private void saveIndex()
	{
		File storeFolder = folders.getBase();
		if (storeFolder.exists())
		{
			File index = new File(storeFolder, INDEX_FILE_NAME);
			Files.remove(index);
			try (OutputStream stream = new FileOutputStream(index))
			{
				ObjectOutputStream oos = new ObjectOutputStream(stream);
				
				ArrayList<DiskData> list = new ArrayList<>(diskDatas.size());
				for (DiskData diskData : diskDatas.values())
				{
					if (diskData.sessionIdentifier != null)
					{
						list.add(diskData);
					}
				}
				oos.writeObject(list);
			}
			catch (Exception e)
			{
				log.error("Couldn't write DiskPageStore index to file " + index + ".", e);
			}
		}
	}

	@Override
	public Set<String> getSessionIdentifiers()
	{
		return Collections.unmodifiableSet(diskDatas.keySet());
	}

	/**
	 * 
	 * @param sessionIdentifier
	 *            key
	 * @return a list of the last N page windows
	 */
	@Override
	public List<IPersistedPage> getPersistedPages(String sessionIdentifier)
	{
		List<IPersistedPage> pages = new ArrayList<>();

		DiskData diskData = getDiskData(sessionIdentifier, false);
		if (diskData != null)
		{
			PageWindowManager windowManager = diskData.getManager();

			pages.addAll(windowManager.getFileWindows());
		}
		return pages;
	}

	@Override
	public Bytes getTotalSize()
	{
		long size = 0;

		synchronized (diskDatas)
		{
			for (DiskData diskData : diskDatas.values())
			{
				size = size + diskData.size();
			}
		}

		return Bytes.bytes(size);
	}

	/**
	 * Data held on disk.
	 */
	protected static class DiskData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private transient DiskPageStore pageStore;

		private transient String fileName;

		private String sessionIdentifier;

		private PageWindowManager manager;

		protected DiskData(DiskPageStore pageStore, String sessionIdentifier)
		{
			this.pageStore = pageStore;

			this.sessionIdentifier = sessionIdentifier;
		}

		public long size()
		{
			return manager.getTotalSize();
		}

		public PageWindowManager getManager()
		{
			if (manager == null)
			{
				manager = new PageWindowManager(pageStore.maxSizePerSession.bytes());
			}
			return manager;
		}

		private String getFileName()
		{
			if (fileName == null)
			{
				fileName = pageStore.getSessionFileName(sessionIdentifier);
			}
			return fileName;
		}

		/**
		 * @return session id
		 */
		public String getKey()
		{
			return sessionIdentifier;
		}

		/**
		 * Saves the serialized page to appropriate file.
		 * 
		 * @param pageId
		 * @param pageType
		 * @param data
		 */
		public synchronized void savePage(int pageId, String pageType, byte data[])
		{
			if (sessionIdentifier == null)
			{
				return;
			}

			// only save page that has some data
			if (data != null)
			{
				// allocate window for page
				FileWindow window = getManager().createPageWindow(pageId, pageType, data.length);

				FileChannel channel = getFileChannel(true);
				if (channel != null)
				{
					try
					{
						// write the content
						channel.write(ByteBuffer.wrap(data), window.getFilePartOffset());
					}
					catch (IOException e)
					{
						log.error("Error writing to a channel " + channel, e);
					}
					finally
					{
						IOUtils.closeQuietly(channel);
					}
				}
				else
				{
					log.warn(
						"Cannot save page with id '{}' because the data file cannot be opened.",
						pageId);
				}
			}
		}

		/**
		 * Removes the page from disk.
		 * 
		 * @param pageId
		 */
		public synchronized void removeData(int pageId)
		{
			if (sessionIdentifier == null)
			{
				return;
			}

			getManager().removePage(pageId);
		}

		/**
		 * Loads the part of pagemap file specified by the given PageWindow.
		 * 
		 * @param window
		 * @return serialized page data
		 */
		public byte[] loadData(FileWindow window)
		{
			byte[] result = null;
			FileChannel channel = getFileChannel(false);
			if (channel != null)
			{
				ByteBuffer buffer = ByteBuffer.allocate(window.getFilePartSize());
				try
				{
					channel.read(buffer, window.getFilePartOffset());
					if (buffer.hasArray())
					{
						result = buffer.array();
					}
				}
				catch (IOException e)
				{
					log.error("Error reading from file channel " + channel, e);
				}
				finally
				{
					IOUtils.closeQuietly(channel);
				}
			}
			return result;
		}

		private FileChannel getFileChannel(boolean create)
		{
			FileChannel channel = null;
			File file = new File(getFileName());
			if (create || file.exists())
			{
				String mode = create ? "rw" : "r";
				try
				{
					RandomAccessFile randomAccessFile = new RandomAccessFile(file, mode);
					channel = randomAccessFile.getChannel();
				}
				catch (FileNotFoundException fnfx)
				{
					// can happen if the file is locked. WICKET-4176
					log.error(fnfx.getMessage(), fnfx);
				}
			}
			return channel;
		}

		/**
		 * Loads the specified page data.
		 * 
		 * @param id
		 * @return page data or null if the page is no longer in pagemap file
		 */
		public synchronized byte[] loadPage(int id)
		{
			if (sessionIdentifier == null)
			{
				return null;
			}

			FileWindow window = getManager().getPageWindow(id);
			if (window == null)
			{
				return null;
			}

			return loadData(window);
		}

		/**
		 * Deletes all files for this session.
		 */
		public synchronized void unbind()
		{
			pageStore.folders.remove(sessionIdentifier);

			sessionIdentifier = null;
		}
	}

	/**
	 * Returns the file name for specified session. If the session folder (folder that contains the
	 * file) does not exist, the folder will be created.
	 * 
	 * @param sessionIdentifier
	 * @return file name for pagemap
	 */
	private String getSessionFileName(String sessionIdentifier)
	{
		File sessionFolder = folders.get(sessionIdentifier, true);
		return new File(sessionFolder, "data").getAbsolutePath();
	}
}
