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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.disk.NestedFolders;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage of pages in files.
 * <p>
 * All pages passed into this store are restricted to be {@link SerializedPage}s.
 * <p>
 * While {@link DiskPageStore} uses a single file per session, this implementation stores each page
 * in its own file. This improves on a {@link DiskPageStore disadvantage of DiskPageStore} surfacing
 * with alternating Ajax requests from different browser tabs.  
 */
public class FilePageStore extends AbstractPersistentPageStore implements IPersistentPageStore
{
	private static final String ATTRIBUTE_PAGE_TYPE = "user.wicket_page_type";

	private static final String FILE_SUFFIX = ".data";

	private static final Logger log = LoggerFactory.getLogger(FilePageStore.class);

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
	public FilePageStore(String applicationName, File fileStoreFolder, Bytes maxSizePerSession, ISerializer serializer)
	{
		super(applicationName);
		
		this.folders = new NestedFolders(new File(fileStoreFolder, applicationName + "-filestore"));
		this.maxSizePerSession = Args.notNull(maxSizePerSession, "maxSizePerSession");
	}

	/**
	 * Pages are always serialized, so versioning is supported.
	 */
	@Override
	public boolean supportsVersioning()
	{
		return true;
	}

	private File getPageFile(String sessionId, int id, boolean create)
	{
		File folder = folders.get(sessionId, create);

		return new File(folder, id + FILE_SUFFIX);
	}

	@Override
	protected IManageablePage getPersistedPage(String sessionIdentifier, int id)
	{
		byte[] data = readFile(sessionIdentifier, id);
		if (data == null)
		{
			return null;
		}
		
		return new SerializedPage(id, "unknown", data);
	}

	private byte[] readFile(String sessionIdentifier, int id)
	{
		File file = getPageFile(sessionIdentifier, id, false);
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
			log.warn("cannot read page data for session {} page {}", sessionIdentifier, id, ex);
		}

		return data;
	}

	@Override
	protected void removePersistedPage(String sessionIdentifier, IManageablePage page)
	{
		File file = getPageFile(sessionIdentifier, page.getPageId(), false);
		if (file.exists())
		{
			if (!file.delete())
			{
				log.warn("cannot remove page data for session {} page {}", sessionIdentifier, page.getPageId());
			}
		}
	}

	@Override
	protected void removeAllPersistedPages(String sessionIdentifier)
	{
		folders.remove(sessionIdentifier);
	}

	@Override
	protected void addPersistedPage(String sessionIdentifier, IManageablePage page)
	{
		if (page instanceof SerializedPage == false)
		{
			throw new WicketRuntimeException("FilePageStore works with serialized pages only");
		}
		SerializedPage serializedPage = (SerializedPage) page;

		String type = serializedPage.getPageType();
		byte[] data = serializedPage.getData();

		writeFile(sessionIdentifier, serializedPage.getPageId(), type, data);

		checkMaxSize(sessionIdentifier);
	}

	private void writeFile(String sessionIdentifier, int pageId, String pageType, byte[] data)
	{
		File file = getPageFile(sessionIdentifier, pageId, true);
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
			log.warn("cannot store page data for session {} page {}", sessionIdentifier, pageId, ex);
		}

		setPageType(file, pageType);
	}

	private void checkMaxSize(String sessionIdentifier)
	{
		File[] files = folders.get(sessionIdentifier, true).listFiles();
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
					log.warn("cannot remove page data for session {} page {}", sessionIdentifier,
						candidate.getName());
				}
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
	public Set<String> getSessionIdentifiers()
	{
		Set<String> sessionIdentifiers = new HashSet<>();

		for (File folder : folders.getAll())
		{
			sessionIdentifiers.add(folder.getName());
		}

		return sessionIdentifiers;
	}

	@Override
	public List<IPersistedPage> getPersistedPages(String sessionIdentifier)
	{
		List<IPersistedPage> pages = new ArrayList<>();

		File folder = folders.get(sessionIdentifier, false);
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
}