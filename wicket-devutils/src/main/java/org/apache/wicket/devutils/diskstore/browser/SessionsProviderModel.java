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
package org.apache.wicket.devutils.diskstore.browser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.devutils.diskstore.DebugDiskDataStore;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.pageStore.DiskDataStore;

/**
 * A model that collects the session ids from the {@link DiskDataStore} folder
 */
public class SessionsProviderModel extends LoadableDetachableModel<List<String>>
{

	@Override
	protected List<String> load()
	{
		List<String> sessionIds = new ArrayList<>();
		if (Application.exists())
		{
			DebugDiskDataStore dataStore = DataStoreHelper.getDataStore();
			File appStoreFolder = dataStore.getStoreFolder();
			if (appStoreFolder.isDirectory())
			{
				String[] sessionIdFileNames = appStoreFolder.list();
				if (sessionIdFileNames != null)
				{
					for (String sessionId : sessionIdFileNames)
					{
						sessionIds.add(sessionId);
					}
				}
			}
		}

		return sessionIds;
	}
}
