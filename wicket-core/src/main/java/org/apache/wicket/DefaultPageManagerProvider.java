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
package org.apache.wicket;

import java.io.File;

import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.PageManager;
import org.apache.wicket.pageStore.AsynchronousPageStore;
import org.apache.wicket.pageStore.CachingPageStore;
import org.apache.wicket.pageStore.CryptingPageStore;
import org.apache.wicket.pageStore.DiskPageStore;
import org.apache.wicket.pageStore.FilePageStore;
import org.apache.wicket.pageStore.GroupingPageStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.InMemoryPageStore;
import org.apache.wicket.pageStore.InSessionPageStore;
import org.apache.wicket.pageStore.NoopPageStore;
import org.apache.wicket.pageStore.RequestPageStore;
import org.apache.wicket.pageStore.SerializedPage;
import org.apache.wicket.pageStore.SerializingPageStore;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.settings.FrameworkSettings;
import org.apache.wicket.settings.StoreSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;

/**
 * A provider of a {@link PageManager} managing @link IManageablePage}s with a default chain of {@link IPageStore}s:
 * <ol>
 * <li>{@link RequestPageStore} keeping pages until end of the request</li>
 * <li>{@link CachingPageStore} caching with an ...</li>
 * <li>{@link InSessionPageStore} keeping the last accessed page in the session</li>
 * <li>{@link SerializingPageStore} serializing all pages (so they are available for back-button)</li>
 * <li>{@link AsynchronousPageStore} moving storage of pages to an asynchronous worker thread (enabled by default with {@link StoreSettings#isAsynchronous()})</li>
 * <li>{@link CryptingPageStore} encrypting all pages (disabled by default in {@link StoreSettings#isEncrypted()})</li>
 * <li>{@link DiskPageStore} persisting all pages, configured according to {@link StoreSettings}</li>
 * </ol>
 * An alternative chain with all pages held in-memory could be:
 * <ol>
 * <li>{@link RequestPageStore} keeping pages until end of the request</li>
 * <li>{@link CachingPageStore} caching with an ...</li>
 * <li>{@link InSessionPageStore} keeping the last accessed page in the session</li>
 * <li>{@link SerializingPageStore} serializing all pages (so they are available for back-button)</li>
 * <li>{@link AsynchronousPageStore} moving storage of pages to a worker thread</li>
 * <li>{@link InMemoryPageStore} keeping all pages in memory</li>
 * </ol>
 * ... or if all pages should be kept in the session only, without any serialization (no back-button
 * support though):
 * <ul>
 * <li>{@link RequestPageStore} caching pages until end of the request</li>
 * <li>{@link InSessionPageStore} keeping a limited count of pages in the session, e.g. 10</li>
 * </ul>
 * The chain's initial store should always be a {@link RequestPageStore}, buffering all adding of pages until the end of the request.
 * Several stores accept {@link SerializedPage} only, these have to be preceded by a {@link SerializingPageStore}.
 * <p> 
 * For back-button support <em>at least one</em> store in the chain must create copies of stored
 * pages (usually through serialization), otherwise any following request will work on an identical
 * page instance and the previous state of page is no longer accessible.
 * <p>
 * Other stores be may inserted ad libitum, e.g.
 * <ul>
 * <li>{@link NoopPageStore} discards all pages</li>
 * <li>{@link GroupingPageStore} groups pages, e.g. to limit storage size on a per-group basis</li>
 * <li>{@link FilePageStore} as an alternative to the trusted {@link DiskPageStore}</li>
 * <li>other implementations from <a href="https://github.com/wicketstuff/core/tree/master/datastores-parent">wicketstuff-datastores</a></li>
 * </ul>
 */
public class DefaultPageManagerProvider implements IPageManagerProvider
{
	protected final Application application;

	/**
	 * Constructor.
	 *
	 * @param application
	 *            The application instance
	 */
	public DefaultPageManagerProvider(Application application)
	{
		this.application = Args.notNull(application, "application");
	}

	@Override
	public IPageManager get()
	{
		IPageStore store = newPersistentStore();
		
		store = newCryptingStore(store);

		store = newAsynchronousStore(store);
		
		store = newSerializingStore(store);

		store = newCachingStore(store);

		store = newRequestStore(store);

		return new PageManager(store);
	}

	/**
	 * Get the {@link ISerializer} to use for serializing of pages.
	 * <p>
	 * By default the serializer of the applications {@link FrameworkSettings}.
	 * 
	 * @return how to serialize pages if needed for any {@link IPageStore}
	 * 
	 * @see FrameworkSettings#getSerializer()
	 */
	protected ISerializer getSerializer()
	{
		return application.getFrameworkSettings().getSerializer();
	}

	/**
	 * Keep pages in the request until it is finished.
	 * 
	 * @see RequestPageStore
	 */
	protected IPageStore newRequestStore(IPageStore pageStore)
	{
		return new RequestPageStore(pageStore);
	}

	/**
	 * Cache last page non-serialized in the session for fast access.
	 * <p>
	 * On session serialization the cached page will be dropped and re-acquired from
	 * a persistent store. 
	 * 
	 * @see InSessionPageStore
	 */
	protected IPageStore newCachingStore(IPageStore pageStore)
	{
		return new CachingPageStore(pageStore, new InSessionPageStore(1));
	}

	/**
	 * Store pages asynchronously into the persistent store, if enabled in {@link StoreSettings#isAsynchronous()}.
	 * 
	 * @see AsynchronousPageStore
	 */
	protected IPageStore newAsynchronousStore(IPageStore pageStore)
	{
		StoreSettings storeSettings = application.getStoreSettings();

		if (storeSettings.isAsynchronous())
		{
			int capacity = storeSettings.getAsynchronousQueueCapacity();
			pageStore = new AsynchronousPageStore(pageStore, capacity);
		}

		return pageStore;
	}

	/**
	 * Serialize pages.
	 * 
	 * @see SerializingPageStore
	 */
	protected IPageStore newSerializingStore(IPageStore pageStore)
	{
		return new SerializingPageStore(pageStore, getSerializer());
	}

	/**
	 * Crypt all pages, if enabled in {@link StoreSettings#isEncrypted()}.
	 * 
	 * @see CryptingPageStore
	 */
	protected IPageStore newCryptingStore(IPageStore pageStore)
	{
		StoreSettings storeSettings = application.getStoreSettings();
		
		if (storeSettings.isEncrypted())
		{
			pageStore = new CryptingPageStore(pageStore, application);
		}

		return pageStore;
	}

	/**
	 * Keep persistent copies of all pages on disk.
	 * 
	 * @see DiskPageStore
	 * @see StoreSettings#getMaxSizePerSession()
	 * @see StoreSettings#getFileStoreFolder()
	 */
	protected IPageStore newPersistentStore()
	{
		StoreSettings storeSettings = application.getStoreSettings();
		Bytes maxSizePerSession = storeSettings.getMaxSizePerSession();
		File fileStoreFolder = storeSettings.getFileStoreFolder();

		return new DiskPageStore(application.getName(), fileStoreFolder, maxSizePerSession);
	}
}