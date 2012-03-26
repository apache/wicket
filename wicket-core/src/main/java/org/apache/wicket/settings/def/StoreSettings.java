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
package org.apache.wicket.settings.def;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IStoreSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;

/**
 * The implementation of {@link IStoreSettings}
 */
public class StoreSettings implements IStoreSettings
{
	private static final int DEFAULT_CACHE_SIZE = 40;

	private static final Bytes DEFAULT_MAX_SIZE_PER_SESSION = Bytes.megabytes(10);

	private static final int DEFAULT_ASYNCHRONOUS_QUEUE_CAPACITY = 100;

	private int inmemoryCacheSize = DEFAULT_CACHE_SIZE;

	private Bytes maxSizePerSession = DEFAULT_MAX_SIZE_PER_SESSION;

	private File fileStoreFolder = null;

	private int asynchronousQueueCapacity = DEFAULT_ASYNCHRONOUS_QUEUE_CAPACITY;

	private boolean isAsynchronous = true;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public StoreSettings(final Application application)
	{
	}

	@Override
	public int getInmemoryCacheSize()
	{
		return inmemoryCacheSize;
	}

	@Override
	public void setInmemoryCacheSize(int inmemoryCacheSize)
	{
		this.inmemoryCacheSize = inmemoryCacheSize;
	}

	@Override
	public Bytes getMaxSizePerSession()
	{
		return maxSizePerSession;
	}

	@Override
	public void setMaxSizePerSession(final Bytes maxSizePerSession)
	{
		this.maxSizePerSession = Args.notNull(maxSizePerSession, "maxSizePerSession");
	}

	@Override
	public File getFileStoreFolder()
	{
		if (fileStoreFolder == null)
		{
			if (Application.exists())
			{
				fileStoreFolder = (File)((WebApplication)Application.get()).getServletContext()
					.getAttribute("javax.servlet.context.tempdir");
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

	@Override
	public void setFileStoreFolder(final File fileStoreFolder)
	{
		this.fileStoreFolder = Args.notNull(fileStoreFolder, "fileStoreFolder");
	}

	@Override
	public int getAsynchronousQueueCapacity()
	{
		return asynchronousQueueCapacity;
	}

	@Override
	public void setAsynchronousQueueCapacity(int queueCapacity)
	{
		if (queueCapacity < 1)
		{
			throw new IllegalArgumentException(
				"The capacity of the asynchronous queue should be at least 1.");
		}
		asynchronousQueueCapacity = queueCapacity;
	}

	@Override
	public void setAsynchronous(boolean async)
	{
		isAsynchronous = async;
	}

	@Override
	public boolean isAsynchronous()
	{
		return isAsynchronous;
	}
}
