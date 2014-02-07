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
package org.apache.wicket.util.file;


import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.wicket.util.lang.Args;

/**
 * Adapts IFileCleaner to FileCleaningTracker
 */
public class FileCleanerTrackerAdapter extends FileCleaningTracker
{
	private final IFileCleaner fileCleaner;

	public FileCleanerTrackerAdapter(IFileCleaner fileCleaner)
	{
		this.fileCleaner = Args.notNull(fileCleaner, "fileCleaner");
	}

	@Override
	public void track(java.io.File file, Object marker)
	{
		fileCleaner.track(file, marker);
	}

	@Override
	public void track(java.io.File file, Object marker, FileDeleteStrategy deleteStrategy)
	{
		fileCleaner.track(file, marker, deleteStrategy);
	}

	@Override
	public void track(String path, Object marker)
	{
		fileCleaner.track(new File(path), marker);
	}

	@Override
	public void track(String path, Object marker, FileDeleteStrategy deleteStrategy)
	{
		fileCleaner.track(new File(path), marker, deleteStrategy);
	}
}
