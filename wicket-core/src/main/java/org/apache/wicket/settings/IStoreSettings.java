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

import org.apache.wicket.page.IPageManager;
import org.apache.wicket.pageStore.AsynchronousDataStore;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.util.lang.Bytes;

/**
 * An interface for settings related to the the storages where page instances are persisted -
 * {@link IPageStore}, {@link IDataStore} and {@link IPageManager}.
 * <p>
 * For more information about page storages read <a
 * href="https://cwiki.apache.org/confluence/x/qIaoAQ">Page Storage - Wiki page</a>
 * </p>
 * 
 * @since 1.5
 */
public interface IStoreSettings
{
	/**
	 * @return the number of page instances which will be stored in the application scoped cache for
	 *         faster retrieval
	 */
	int getInmemoryCacheSize();

	/**
	 * Sets the maximum number of page instances which will be stored in the application scoped
	 * second level cache for faster retrieval
	 * 
	 * @param inmemoryCacheSize
	 *            the maximum number of page instances which will be held in the application scoped
	 *            cache
	 */
	void setInmemoryCacheSize(int inmemoryCacheSize);

	/**
	 * @return maximum page size. After this size is exceeded, the {@link DiskDataStore} will start
	 *         saving the pages at the beginning of file.
	 */
	Bytes getMaxSizePerSession();

	/**
	 * Sets the maximum size of the {@link File} where page instances per session are stored. After
	 * reaching this size the {@link DiskDataStore} will start overriding the oldest pages at the
	 * beginning of the file.
	 * 
	 * @param maxSizePerSession
	 *            the maximum size of the file where page instances are stored per session. In
	 *            bytes.
	 */
	void setMaxSizePerSession(Bytes maxSizePerSession);

	/**
	 * @return the location of the folder where {@link DiskDataStore} will store the files with page
	 *         instances per session
	 */
	File getFileStoreFolder();

	/**
	 * Sets the folder where {@link DiskDataStore} will store the files with page instances per
	 * session
	 * 
	 * @param fileStoreFolder
	 *            the new location
	 */
	void setFileStoreFolder(File fileStoreFolder);

	/**
	 * @return the capacity of the queue used to store the pages which will be stored asynchronously
	 * @see AsynchronousDataStore
	 */
	int getAsynchronousQueueCapacity();

	/**
	 * Sets the capacity of the queue used to store the pages which will be stored asynchronously
	 * 
	 * @param capacity
	 *            the capacity of the queue
	 * @see AsynchronousDataStore
	 */
	void setAsynchronousQueueCapacity(int capacity);

	/**
	 * Sets a flag whether to wrap the configured {@link IDataStore} with
	 * {@link AsynchronousDataStore}. By doing this the HTTP worker thread will not wait for the
	 * actual write of the page's bytes into the wrapped {@link IDataStore}.
	 * 
	 * @param async
	 *            {@code true} to make it asynchronous, {@code false} - otherwise
	 */
	void setAsynchronous(boolean async);

	/**
	 * @return {@code true} if the storing of page's bytes is asynchronous
	 */
	boolean isAsynchronous();
}
