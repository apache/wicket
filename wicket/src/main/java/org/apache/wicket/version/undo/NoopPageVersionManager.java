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
package org.apache.wicket.version.undo;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.version.IPageVersionManager;

/**
 * Intermediate noop impl for wicket-ng migration
 * 
 * @author igor.vaynberg
 */
public class NoopPageVersionManager implements IPageVersionManager
{
	private final Page page;

	public NoopPageVersionManager(Page page)
	{
		this.page = page;
	}

	public void beginVersion(boolean mergeVersion)
	{
	}

	public void componentAdded(Component component)
	{
	}

	public void componentModelChanging(Component component)
	{
	}

	public void componentRemoved(Component component)
	{
	}

	public void componentStateChanging(Change change)
	{
	}

	public void endVersion(boolean mergeVersion)
	{
	}

	public void expireOldestVersion()
	{
	}

	public int getAjaxVersionNumber()
	{
		return 0;
	}

	public int getCurrentVersionNumber()
	{
		return 0;
	}

	public Page getVersion(int versionNumber)
	{
		return page;
	}

	public int getVersions()
	{
		return 0;
	}

	public void ignoreVersionMerge()
	{
	}

	public Page rollbackPage(int numberOfVersions)
	{
		return page;
	}

}
