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
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.disk.NestedFolders;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage of pages in files.
 * <p>
 * While {@link DiskPageStore} uses a single file per session, this implementation stores each page
 * in its own file. This improves on a {@link DiskPageStore disadvantage of DiskPageStore} surfacing
 * with alternating Ajax requests from different browser tabs.  
 */
public class FilePageStore implements IPersistentPageStore
{
	private static final String ATTRIBUTE_PAGE_TYPE = "user.wicket_page_type";

	private static final String FILE_SUFFIX = ".data";

	private static final Logger log = LoggerFactory.getLogger(FilePageStore.class);

	private static final String KEY = "wicket:FilePageStore";

	/**
	 * A cache that holds all page stores.
	 */
	private static final ConcurrentMap<String, FilePageStore> FILE_STORES = new ConcurrentHashMap<>();

	private final String applicationName;

	private final ISerializer serializer;

	private final Bytes maxSizePerSession;

	private final NestedFolders folders;

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
	public FilePageStore(String applicationName, File fileStoreFolder, Bytes maxSizePerSession)
	{
		this(applicationName, fileStoreFolder, maxSizePerSession, null);
	}

	/**
	 * Create a store to files.
	 * 
	 * @param applicationName
	 *            name of application
	 * @param fileStoreFolder
	 *            folder to store to
	 * @param maxSizePerSession
	 *            maximum size per session
	 * @param serializer
	 *            for serialization of pages
	 */
	public FilePageStore(String applicationName, File fileStoreFolder, Bytes maxSizePerSession,
		ISerializer serializer)
	{
		this.applicationName = Args.notNull(applicationName, "applicationName");
		this.folders = new NestedFolders(new File(fileStoreFolder, applicationName + "-filestore"));
		this.maxSizePerSession = Args.notNull(maxSizePerSession, "maxSizePerSession");
		this.serializer = serializer; // optional

		if (FILE_STORES.containsKey(applicationName))
		{
			throw new IllegalStateException(
				"Store for application with key '" + applicationName + "' already exists.");
		}
		FILE_STORES.put(applicationName, this);
	}

	/**
	 * Versioning is supported if a serializer was provided to the constructor.
	 */
	@Override
	public boolean supportsVersioning()
	{
		return serializer != null;
	}

	@Override
	public void destroy()
	{
		log.debug("Destroying...");

		FILE_STORES.remove(applicationName);

		log.debug("Destroyed.");
	}

	private File getPageFile(String sessionId, int id, boolean create)
	{
		File folder = folders.get(sessionId, create);

		return new File(folder, id + FILE_SUFFIX);
	}

	@Override
	public boolean canBeAsynchronous(IPageContext context)
	{
		// session attribute must be added here *before* any asynchronous calls
		// when session is no longer available
		getSessionAttribute(context, true);

		return true;
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		SessionAttribute attribute = getSessionAttribute(context, false);
		if (attribute == null)
		{
			return null;
		}

		byte[] data = readFile(attribute.identifier, id);
		if (data == null) {
			return null;
		}
		
		IManageablePage page;
		if (serializer == null)
		{
			page = new SerializedPage(id, "unknown", data);
		}
		else
		{
			page = (IManageablePage)serializer.deserialize(data);
		}

		return page;
	}

	private byte[] readFile(String identifier, int id)
	{
		File file = getPageFile(identifier, id, false);
		if (file.exists() == false)
		{
			return null;
		}

		byte[] data = null;

		try
		{
			FileChannel channel = FileChannel.open(file.toPath());
			try
			{
				int size = (int)channel.size();
				MappedByteBuffer buf = channel.map(MapMode.READ_ONLY, 0, size);
				data = new byte[size];
				buf.get(data);
			}
			finally
			{
				IOUtils.closeQuietly(channel);
			}
		}
		catch (IOException ex)
		{
			log.warn("cannot read page data for session {} page {}", identifier, id, ex);
		}

		return data;
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		SessionAttribute attribute = getSessionAttribute(context, false);
		if (attribute == null)
		{
			return;
		}

		File file = getPageFile(attribute.identifier, page.getPageId(), false);
		if (file.exists())
		{
			if (!file.delete())
			{
				log.warn("cannot remove page data for session {} page {}", attribute.identifier,
					page.getPageId());
			}
		}
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		SessionAttribute attribute = getSessionAttribute(context, false);
		if (attribute == null)
		{
			return;
		}

		removeFolder(attribute.identifier);
	}

	private void removeFolder(String identifier)
	{
		folders.remove(identifier);
	}

	/**
	 * Supports {@link SerializedPage}s too - for this to work the delegating {@link IPageStore}
	 * must use the same {@link ISerializer} as this one.
	 */
	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		SessionAttribute attribute = getSessionAttribute(context, false);
		if (attribute == null)
		{
			return;
		}

		String type;
		byte[] data;
		if (page instanceof SerializedPage)
		{
			type = ((SerializedPage)page).getPageType();
			data = ((SerializedPage)page).getData();
		}
		else
		{
			if (serializer == null)
			{
				throw new WicketRuntimeException("FilePageStore not configured for serialization");
			}
			type = Classes.name(page.getClass());
			data = serializer.serialize(page);
		}

		writeFile(attribute.identifier, page.getPageId(), type, data);

		checkMaxSize(attribute.identifier);
	}

	private void writeFile(String identifier, int pageId, String pageType, byte[] data)
	{
		File file = getPageFile(identifier, pageId, true);
		try
		{
			FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			try
			{
				ByteBuffer buffer = ByteBuffer.wrap(data);
				channel.write(buffer);
			}
			finally
			{
				IOUtils.closeQuietly(channel);
			}
		}
		catch (IOException ex)
		{
			log.warn("cannot store page data for session {} page {}", identifier, pageId, ex);
		}

		setPageType(file, pageType);
	}

	private void checkMaxSize(String identifier)
	{
		File[] files = folders.get(identifier, true).listFiles();
		Arrays.sort(files, new LastModifiedComparator());

		long total = 0;
		for (int f = 0; f < files.length; f++)
		{
			File candidate = files[f];

			total += candidate.length();

			if (total > maxSizePerSession.bytes())
			{
				if (!Files.remove(candidate))
				{
					log.warn("cannot remove page data for session {} page {}", identifier,
						candidate.getName());
				}
			}
		}
	}

	protected SessionAttribute getSessionAttribute(IPageContext context, boolean create)
	{
		SessionAttribute attribute = context.getSessionAttribute(KEY);
		if (attribute == null && create)
		{
			attribute = new SessionAttribute(applicationName, context.getSessionId());
			context.setSessionAttribute(KEY, attribute);
		}
		return attribute;
	}

	/**
	 * Attribute held in session.
	 */
	static class SessionAttribute implements Serializable, HttpSessionBindingListener
	{

		private final String applicationName;

		/**
		 * The identifier of the session, must not be equal to {@link Session#getId()}, e.g. when
		 * the container changes the id after authorization.
		 */
		public final String identifier;

		public SessionAttribute(String applicationName, String sessionIdentifier)
		{
			this.applicationName = Args.notNull(applicationName, "applicationName");
			this.identifier = Args.notNull(sessionIdentifier, "sessionIdentifier");
		}


		@Override
		public void valueBound(HttpSessionBindingEvent event)
		{
		}

		@Override
		public void valueUnbound(HttpSessionBindingEvent event)
		{
			FilePageStore store = FILE_STORES.get(applicationName);
			if (store == null)
			{
				log.warn(
					"Cannot remove data '{}' because disk store for application '{}' is no longer present.",
					identifier, applicationName);
			}
			else
			{
				store.removeFolder(identifier);
			}
		}
	}

	public class LastModifiedComparator implements Comparator<File>
	{

		@Override
		public int compare(File f1, File f2)
		{
			return Long.compare(f2.lastModified(), f1.lastModified());
		}

	}

	@Override
	public String getContextIdentifier(IPageContext context)
	{
		return getSessionAttribute(context, true).identifier;
	}

	@Override
	public Set<String> getContextIdentifiers()
	{
		Set<String> identifiers = new HashSet<>();

		for (File folder : folders.getAll())
		{
			identifiers.add(folder.getName());
		}

		return identifiers;
	}

	@Override
	public List<IPersistedPage> getPersistentPages(String identifier)
	{
		List<IPersistedPage> pages = new ArrayList<>();

		File folder = folders.get(identifier, false);
		if (folder.exists())
		{
			File[] files = folder.listFiles();
			Arrays.sort(files, new LastModifiedComparator());
			for (File file : files)
			{
				String name = file.getName();
				if (name.endsWith(FILE_SUFFIX))
				{
					int pageId;
					try
					{
						pageId = Integer
							.valueOf(name.substring(0, name.length() - FILE_SUFFIX.length()));
					}
					catch (Exception ex)
					{
						log.debug("unexpected file {}", file.getAbsolutePath());
						continue;
					}

					String pageType = getPageType(file);

					pages.add(new PersistedPage(pageId, pageType, file.length()));
				}
			}
		}

		return pages;
	}

	/**
	 * Get the type of page from the given file.
	 * <p>
	 * This is an optional operation that returns <code>null</code> in case of any error. 
	 * 
	 * @param file
	 * @return pageType
	 */
	protected String getPageType(File file)
	{
		String pageType = null;
		try
		{
			UserDefinedFileAttributeView view = java.nio.file.Files
				.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
			ByteBuffer buffer = ByteBuffer.allocate(view.size(ATTRIBUTE_PAGE_TYPE));
			view.read(ATTRIBUTE_PAGE_TYPE, buffer);
			buffer.flip();
			pageType = Charset.defaultCharset().decode(buffer).toString();
		}
		catch (IOException ex)
		{
			log.debug("cannot get pageType for {}", file);
		}

		return pageType;
	}

	/**
	 * Set the type of page on the given file.
	 * <p>
	 * This is an optional operation that silently fails in case of an error. 
	 * 
	 * @param file
	 * @param pageType
	 */
	protected void setPageType(File file, String pageType)
	{
		try
		{
			UserDefinedFileAttributeView view = java.nio.file.Files
				.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
			view.write(ATTRIBUTE_PAGE_TYPE, Charset.defaultCharset().encode(pageType));
		}
		catch (IOException ex)
		{
			log.debug("cannot set pageType for {}", file, ex);
		}
	}

	@Override
	public Bytes getTotalSize()
	{
		long total = 0;

		for (File folder : folders.getAll())
		{
			for (File file : folder.listFiles())
			{
				String name = file.getName();
				if (name.endsWith(FILE_SUFFIX))
				{
					total += file.length();
				}
			}
		}

		return Bytes.bytes(total);
	}

	private static class PersistedPage implements IPersistedPage
	{
		private int pageId;

		private String pageType;

		private long pageSize;

		public PersistedPage(int pageId, String pageType, long pageSize)
		{
			this.pageId = pageId;
			this.pageType = pageType;
			this.pageSize = pageSize;
		}

		@Override
		public int getPageId()
		{
			return pageId;
		}

		@Override
		public Bytes getPageSize()
		{
			return Bytes.bytes(pageSize);
		}

		@Override
		public String getPageType()
		{
			return pageType;
		}

	}
}