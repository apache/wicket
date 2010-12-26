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

import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.page.PersistentPageManager;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;

/**
 * {@link IPageManagerProvider} implementation that creates new instance of {@link IPageManager}
 * that persists the pages in {@link DiskDataStore}
 */
public class DefaultPageManagerProvider implements IPageManagerProvider
{
	private static final int DEFAULT_CACHE_SIZE = 40;

	private static final int DEFAULT_FILE_CHANNEL_POOL_CAPACITY = 50;

	private static final int DEFAULT_MAX_SIZE_PER_SESSION = 1000000;

	private final Application application;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public DefaultPageManagerProvider(Application application)
	{
		this.application = application;
	}

	public IPageManager get(IPageManagerContext pageManagerContext)
	{
		IDataStore dataStore = new DiskDataStore(application.getName(), getMaxSizePerSession(),
			getFileChannelPoolCapacity());
		IPageStore pageStore = new DefaultPageStore(application.getName(), dataStore,
			getCacheSize());
		return new PersistentPageManager(application.getName(), pageStore, pageManagerContext);

	}

	protected int getMaxSizePerSession()
	{
		return DEFAULT_MAX_SIZE_PER_SESSION;
	}

	protected int getCacheSize()
	{
		return DEFAULT_CACHE_SIZE;
	}

	protected int getFileChannelPoolCapacity()
	{
		return DEFAULT_FILE_CHANNEL_POOL_CAPACITY;
	}
}