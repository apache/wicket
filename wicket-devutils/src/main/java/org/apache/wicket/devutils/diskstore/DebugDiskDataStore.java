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
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.PageWindowManager;
import org.apache.wicket.pageStore.PageWindowManager.PageWindow;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Bytes;

/**
 * An extension of {@link DiskDataStore} that is able to browse the content of the file storage.
 * <p>
 * To enable it add in YourApplication#init():
 * 
 * <pre>
 * <code>
 * DebugDiskDataStore.register(this);
 * </code>
 * </pre>
 * 
 * </p>
 * <p>
 * The data can be browsed at: <em>/wicket/internal/debug/diskDataStore</em>
 */
public class DebugDiskDataStore extends DiskDataStore
{

	/**
	 * Construct.
	 * 
	 * @param applicationName
	 * @param fileStoreFolder
	 * @param maxSizePerSession
	 */
	public DebugDiskDataStore(String applicationName, File fileStoreFolder, Bytes maxSizePerSession)
	{
		super(applicationName, fileStoreFolder, maxSizePerSession);

	}

	/**
	 * 
	 * @param sessionId
	 * @param count
	 * @return a list of the last N page windows
	 */
	public List<PageWindow> getLastPageWindows(String sessionId, int count)
	{
		List<PageWindow> pageWindows = new ArrayList<>();

		SessionEntry sessionEntry = getSessionEntry(sessionId, false);
		if (sessionEntry != null)
		{
			PageWindowManager windowManager = sessionEntry.getManager();
			pageWindows.addAll(windowManager.getLastPageWindows(count));
		}
		return pageWindows;
	}

	@Override
	public File getStoreFolder()
	{
		return super.getStoreFolder();
	}

	/**
	 * Configures the page manager provider and mounts the page at
	 * <em>wicket/internal/debug/diskDataStore</em>
	 * 
	 * @param application
	 */
	public static void register(final Application application)
	{
		application.setPageManagerProvider(new DebugPageManagerProvider(application));

		((WebApplication)application).mountPage("wicket/internal/debug/diskDataStore",
			DiskStoreBrowserPage.class);
	}

}
