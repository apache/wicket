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
package org.apache.wicket.settings;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;

/**
 * A class for settings related to the the storages where page instances are persisted, used by
 * {@link org.apache.wicket.pageStore.IPageStore} {@link org.apache.wicket.page.IPageManager}.
 * <p>
 * For more information about page storages read <a
 * href="https://cwiki.apache.org/confluence/x/qIaoAQ">Page Storage - Wiki page</a>
 * </p>
 *
 * @since 1.5
 */
public class StoreSettings
{
	private static final Bytes DEFAULT_MAX_SIZE_PER_SESSION = Bytes.megabytes(10);

	private static final int DEFAULT_ASYNCHRONOUS_QUEUE_CAPACITY = 100;

	private Bytes maxSizePerSession = DEFAULT_MAX_SIZE_PER_SESSION;

	private File fileStoreFolder = null;

	private int asynchronousQueueCapacity = DEFAULT_ASYNCHRONOUS_QUEUE_CAPACITY;

	private boolean asynchronous = true;
	
	private boolean encrypted = false;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public StoreSettings(final Application application)
	{
	}

	/**
	 * @return maximum page size. After this size is exceeded,
	 * the {@link org.apache.wicket.pageStore.DiskPageStore} will start saving the
	 * pages at the beginning of file.
	 */
	public Bytes getMaxSizePerSession()
	{
		return maxSizePerSession;
	}

	/**
	 * Sets the maximum size of the {@link File} where page instances per session are stored. After
	 * reaching this size the {@link org.apache.wicket.pageStore.DiskPageStore} will start overriding the
	 * oldest pages at the beginning of the file.
	 *
	 * @param maxSizePerSession
	 *            the maximum size of the file where page instances are stored per session. In
	 *            bytes.
	 * @return {@code this} object for chaining
	 */
	public StoreSettings setMaxSizePerSession(final Bytes maxSizePerSession)
	{
		this.maxSizePerSession = Args.notNull(maxSizePerSession, "maxSizePerSession");
		return this;
	}

	/**
	 * @return the location of the folder where {@link org.apache.wicket.pageStore.DiskPageStore} will store the files with page
	 *         instances per session
	 */
	public File getFileStoreFolder()
	{
		if (fileStoreFolder == null)
		{
			if (WebApplication.exists())
			{
				fileStoreFolder = (File) WebApplication.get().getServletContext()
					.getAttribute("jakarta.servlet.context.tempdir");
			}

			if (fileStoreFolder != null)
			{
				return fileStoreFolder;
			}

			try
			{
				fileStoreFolder = File.createTempFile("file-prefix", null).getParentFile();
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
		return fileStoreFolder;
	}

	/**
	 * Sets the folder where {@link org.apache.wicket.pageStore.DiskPageStore} will store the files with page instances per
	 * session
	 *
	 * @param fileStoreFolder
	 *            the new location
	 * @return {@code this} object for chaining
	 */
	public StoreSettings setFileStoreFolder(final File fileStoreFolder)
	{
		this.fileStoreFolder = Args.notNull(fileStoreFolder, "fileStoreFolder");
		return this;
	}

	/**
	 * @return the capacity of the queue used to store the pages which will be stored asynchronously
	 * @see org.apache.wicket.pageStore.AsynchronousPageStore
	 */
	public int getAsynchronousQueueCapacity()
	{
		return asynchronousQueueCapacity;
	}

	/**
	 * Sets the capacity of the queue used to store the pages which will be stored asynchronously
	 *
	 * @param queueCapacity
	 *            the capacity of the queue
	 * @see org.apache.wicket.pageStore.AsynchronousPageStore
	 * @return {@code this} object for chaining
	 */
	public StoreSettings setAsynchronousQueueCapacity(int queueCapacity)
	{
		if (queueCapacity < 1)
		{
			throw new IllegalArgumentException(
				"The capacity of the asynchronous queue should be at least 1.");
		}
		asynchronousQueueCapacity = queueCapacity;
		return this;
	}

	/**
	 * Sets a flag whether to wrap the configured {@link org.apache.wicket.pageStore.IPageStore} with
	 * {@link org.apache.wicket.pageStore.AsynchronousPageStore}. By doing this the HTTP worker thread will not wait for the
	 * actual write of the page's bytes into the wrapped {@link org.apache.wicket.pageStore.IPageStore}.
	 *
	 * @param async
	 *            {@code true} to make it asynchronous, {@code false} - otherwise
	 * @return {@code this} object for chaining
	 */
	public StoreSettings setAsynchronous(boolean async)
	{
		asynchronous = async;
		return this;
	}

	/**
	 * @return {@code true} if the storing of page is asynchronous
	 */
	public boolean isAsynchronous()
	{
		return asynchronous;
	}
	
	/**
	 * Sets a flag whether to wrap the configured {@link org.apache.wicket.pageStore.IPageStore} with
	 * {@link org.apache.wicket.pageStore.CryptingPageStore}.
	 *
	 * @param encrypted
	 *            {@code true} to encrypt, {@code false} - otherwise
	 * @return {@code this} object for chaining
	 */
	public StoreSettings setEncrypted(boolean encrypted)
	{
		this.encrypted = encrypted;
		return this;
	}
	
	/**
	 * @return {@code true} if the storing of page is encrypted
	 */
	public boolean isEncrypted()
	{
		return encrypted;
	}
}
