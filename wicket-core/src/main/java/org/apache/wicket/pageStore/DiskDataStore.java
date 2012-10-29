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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.pageStore.PageWindowManager.PageWindow;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A data store implementation which stores the data on disk (in a file system)
 */
public class DiskDataStore implements IDataStore
{
	private static final Logger log = LoggerFactory.getLogger(DiskDataStore.class);

	private static final String INDEX_FILE_NAME = "DiskDataStoreIndex";

	private final String applicationName;

	private final Bytes maxSizePerPageSession;

	private final File fileStoreFolder;

	private final ConcurrentMap<String, SessionEntry> sessionEntryMap;

	/**
	 * Construct.
	 * 
	 * @param applicationName
	 * @param fileStoreFolder
	 * @param maxSizePerSession
	 * @param fileChannelPoolCapacity
	 */
	public DiskDataStore(final String applicationName, final File fileStoreFolder,
		final Bytes maxSizePerSession)
	{
		this.applicationName = applicationName;
		this.fileStoreFolder = fileStoreFolder;
		maxSizePerPageSession = Args.notNull(maxSizePerSession, "maxSizePerSession");
		sessionEntryMap = new ConcurrentHashMap<String, SessionEntry>();

		try
		{
			if (this.fileStoreFolder.exists() || this.fileStoreFolder.mkdirs())
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
				"SecurityException occurred while creating DiskDataStore. Consider using a non-disk based IDataStore implementation. "
					+ "See org.apache.wicket.Application.setPageManagerProvider(IPageManagerProvider)",
				e);
		}
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#destroy()
	 */
	public void destroy()
	{
		log.debug("Destroying...");
		saveIndex();
		log.debug("Destroyed.");
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#getData(java.lang.String, int)
	 */
	public byte[] getData(final String sessionId, final int id)
	{
		byte[] pageData = null;
		SessionEntry sessionEntry = getSessionEntry(sessionId, false);
		if (sessionEntry != null)
		{
			pageData = sessionEntry.loadPage(id);
		}

		log.debug("Returning data{} for page with id '{}' in session with id '{}'", new Object[] {
				pageData != null ? "" : "(null)", id, sessionId });
		return pageData;
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#isReplicated()
	 */
	public boolean isReplicated()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(java.lang.String, int)
	 */
	public void removeData(final String sessionId, final int id)
	{
		SessionEntry sessionEntry = getSessionEntry(sessionId, false);
		if (sessionEntry != null)
		{
			log.debug("Removing data for page with id '{}' in session with id '{}'", new Object[] {
					id, sessionId });
			sessionEntry.removePage(id);
		}
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(java.lang.String)
	 */
	public void removeData(final String sessionId)
	{
		SessionEntry sessionEntry = getSessionEntry(sessionId, false);
		if (sessionEntry != null)
		{
			log.debug("Removing data for pages in session with id '{}'", sessionId);
			synchronized (sessionEntry)
			{
				sessionEntryMap.remove(sessionEntry.sessionId);
				sessionEntry.unbind();
			}
		}
	}

	/**
	 * @see org.apache.wicket.pageStore.IDataStore#storeData(java.lang.String, int, byte[])
	 */
	public void storeData(final String sessionId, final int id, final byte[] data)
	{
		SessionEntry sessionEntry = getSessionEntry(sessionId, true);
		if (sessionEntry != null)
		{
			log.debug("Storing data for page with id '{}' in session with id '{}'", new Object[] {
					id, sessionId });
			sessionEntry.savePage(id, data);
		}
	}

	/**
	 * 
	 * @param sessionId
	 * @param create
	 * @return the session entry
	 */
	protected SessionEntry getSessionEntry(final String sessionId, final boolean create)
	{
		if (!create)
		{
			return sessionEntryMap.get(sessionId);
		}

		SessionEntry entry = new SessionEntry(this, sessionId);
		SessionEntry existing = sessionEntryMap.putIfAbsent(sessionId, entry);
		return existing != null ? existing : entry;
	}

	/**
	 * Load the index
	 */
	@SuppressWarnings("unchecked")
	private void loadIndex()
	{
		File storeFolder = getStoreFolder();
		File index = new File(storeFolder, INDEX_FILE_NAME);
		if (index.exists() && index.length() > 0)
		{
			try
			{
				InputStream stream = new FileInputStream(index);
				ObjectInputStream ois = new ObjectInputStream(stream);
				Map<String, SessionEntry> map = (Map<String, SessionEntry>)ois.readObject();
				sessionEntryMap.clear();
				sessionEntryMap.putAll(map);

				for (Entry<String, SessionEntry> entry : sessionEntryMap.entrySet())
				{
					// initialize the diskPageStore reference
					SessionEntry sessionEntry = entry.getValue();
					sessionEntry.diskDataStore = this;
				}
				stream.close();
			}
			catch (Exception e)
			{
				log.error("Couldn't load DiskDataStore index from file " + index + ".", e);
			}
		}
		Files.remove(index);
	}

	/**
	 * 
	 */
	private void saveIndex()
	{
		File storeFolder = getStoreFolder();
		if (storeFolder.exists())
		{
			File index = new File(storeFolder, INDEX_FILE_NAME);
			Files.remove(index);
			try
			{
				OutputStream stream = new FileOutputStream(index);
				ObjectOutputStream oos = new ObjectOutputStream(stream);
				Map<String, SessionEntry> map = new HashMap<String, SessionEntry>(
					sessionEntryMap.size());
				for (Entry<String, SessionEntry> e : sessionEntryMap.entrySet())
				{
					if (e.getValue().unbound == false)
					{
						map.put(e.getKey(), e.getValue());
					}
				}
				oos.writeObject(map);
				stream.close();
			}
			catch (Exception e)
			{
				log.error("Couldn't write DiskDataStore index to file " + index + ".", e);
			}
		}
	}

	/**
	 * 
	 */
	protected static class SessionEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String sessionId;
		private transient DiskDataStore diskDataStore;
		private String fileName;
		private PageWindowManager manager;
		private boolean unbound = false;

		protected SessionEntry(DiskDataStore diskDataStore, String sessionId)
		{
			this.diskDataStore = diskDataStore;
			this.sessionId = sessionId;
		}

		public PageWindowManager getManager()
		{
			if (manager == null)
			{
				manager = new PageWindowManager(diskDataStore.maxSizePerPageSession.bytes());
			}
			return manager;
		}

		private String getFileName()
		{
			if (fileName == null)
			{
				fileName = diskDataStore.getSessionFileName(sessionId, true);
			}
			return fileName;
		}

		/**
		 * @return session id
		 */
		public String getSessionId()
		{
			return sessionId;
		}

		/**
		 * Saves the serialized page to appropriate file.
		 * 
		 * @param pageId
		 * @param data
		 */
		public synchronized void savePage(int pageId, byte data[])
		{
			if (unbound)
			{
				return;
			}
			// only save page that has some data
			if (data != null)
			{
				// allocate window for page
				PageWindow window = getManager().createPageWindow(pageId, data.length);

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
		 * Removes the page from pagemap file.
		 * 
		 * @param pageId
		 */
		public synchronized void removePage(int pageId)
		{
			if (unbound)
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
		public byte[] loadPage(PageWindow window)
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
			if (unbound)
			{
				return null;
			}
			byte[] result = null;
			PageWindow window = getManager().getPageWindow(id);
			if (window != null)
			{
				result = loadPage(window);
			}
			return result;
		}

		/**
		 * Deletes all files for this session.
		 */
		public synchronized void unbind()
		{
			File sessionFolder = diskDataStore.getSessionFolder(sessionId, false);
			if (sessionFolder.exists())
			{
				Files.removeFolder(sessionFolder);
				cleanup(sessionFolder);
			}
			unbound = true;
		}

		/**
		 * deletes the sessionFolder's parent and grandparent, if (and only if) they are empty.
		 *
		 * @see #createPathFrom(String sessionId)
		 * @param sessionFolder
		 *            must not be null
		 */
		private void cleanup(final File sessionFolder)
		{
			File high = sessionFolder.getParentFile();
			if (high.list().length == 0)
			{
				if (Files.removeFolder(high))
				{
					File low = high.getParentFile();
					if (low.list().length == 0)
					{
						Files.removeFolder(low);
					}
				}
			}
		}
	}

	/**
	 * Returns the file name for specified session. If the session folder (folder that contains the
	 * file) does not exist and createSessionFolder is true, the folder will be created.
	 * 
	 * @param sessionId
	 * @param createSessionFolder
	 * @return file name for pagemap
	 */
	private String getSessionFileName(String sessionId, boolean createSessionFolder)
	{
		File sessionFolder = getSessionFolder(sessionId, createSessionFolder);
		return new File(sessionFolder, "data").getAbsolutePath();
	}

	/**
	 * This folder contains sub-folders named as the session id for which they hold the data.
	 * 
	 * @return the folder where the pages are stored
	 */
	protected File getStoreFolder()
	{
		return new File(fileStoreFolder, applicationName + "-filestore");
	}

	/**
	 * Returns the folder for the specified sessions. If the folder doesn't exist and the create
	 * flag is set, the folder will be created.
	 * 
	 * @param sessionId
	 * @param create
	 * @return folder used to store session data
	 */
	protected File getSessionFolder(String sessionId, final boolean create)
	{
		File storeFolder = getStoreFolder();

		sessionId = sessionId.replace('*', '_');
		sessionId = sessionId.replace('/', '_');
		sessionId = sessionId.replace(':', '_');

		sessionId = createPathFrom(sessionId);

		File sessionFolder = new File(storeFolder, sessionId);
		if (create && sessionFolder.exists() == false)
		{
			Files.mkdirs(sessionFolder);
		}
		return sessionFolder;
	}

	/**
	 * creates a three-level path from the sessionId in the format 0000/0000/<sessionId>. The two
	 * prefixing directories are created from the sessionId's hashcode and thus, should be well
	 * distributed.
	 *
	 * This is used to avoid problems with Filesystems allowing no more than 32k entries in a
	 * directory.
	 *
	 * Note that the prefix paths are created from Integers and not guaranteed to be four chars
	 * long.
	 *
	 * @param sessionId
	 *      must not be null
	 * @return path in the form 0000/0000/sessionId
	 */
	private String createPathFrom(final String sessionId)
	{
		int hash = Math.abs(sessionId.hashCode());
		String low = String.valueOf(hash % 9973);
		String high = String.valueOf((hash / 9973) % 9973);
		StringBuilder bs = new StringBuilder(sessionId.length() + 10);
		bs.append(low);
		bs.append(File.separator);
		bs.append(high);
		bs.append(File.separator);
		bs.append(sessionId);

		return bs.toString();
	}
}
