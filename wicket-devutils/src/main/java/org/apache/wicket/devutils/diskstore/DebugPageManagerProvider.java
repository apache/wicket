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
package org.apache.wicket.devutils.diskstore;

import java.io.File;

import org.apache.wicket.Application;
import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.settings.def.StoreSettings;
import org.apache.wicket.util.lang.Bytes;

/**
 */
public class DebugPageManagerProvider extends DefaultPageManagerProvider
{

	private DebugDiskDataStore dataStore;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public DebugPageManagerProvider(Application application)
	{
		super(application);
	}

	/**
	 * @return the extended with debug information {@link DiskDataStore}
	 */
	public DebugDiskDataStore getDataStore()
	{
		return dataStore;
	}

	@Override
	protected IDataStore newDataStore()
	{
		StoreSettings storeSettings = application.getStoreSettings();
		File fileStoreFolder = storeSettings.getFileStoreFolder();
		Bytes maxSizePerSession = storeSettings.getMaxSizePerSession();
		dataStore = new DebugDiskDataStore(application.getName(), fileStoreFolder,
			maxSizePerSession);
		return dataStore;
	}
}
