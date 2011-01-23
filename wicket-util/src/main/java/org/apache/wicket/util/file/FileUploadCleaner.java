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

import java.io.File;

/**
 * Default implementation of {@link IFileUploadCleaner} that uses Apache commons-io
 * {@link FileCleaningTracker} to track and clean the temporary created files.
 * <p>
 * Note: this implementation starts a daemon thread to do the actual work, so it may not be used in
 * some environments like Google AppEngine.
 */
public class FileUploadCleaner implements IFileUploadCleaner
{
	private final FileCleaningTracker cleaner;

	/**
	 * Construct.
	 */
	public FileUploadCleaner()
	{
		cleaner = new FileCleaningTracker();
	}

	/**
	 * {@inheritDoc}
	 */
	public void track(final File file, final Object marker)
	{
		cleaner.track(file, marker);
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy()
	{
		cleaner.exitWhenFinished();
	}
}
