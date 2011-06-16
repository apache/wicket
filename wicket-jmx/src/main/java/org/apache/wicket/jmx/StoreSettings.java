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
package org.apache.wicket.jmx;

import java.io.File;

import org.apache.wicket.Application;
import org.apache.wicket.util.lang.Bytes;

/**
 * Exposes Application's StoreSettings for JMX.
 */
public class StoreSettings implements StoreSettingsMBean
{
	private final Application application;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public StoreSettings(final Application application)
	{
		this.application = application;
	}

	public int getFileChannelPoolCapacity()
	{
		return application.getStoreSettings().getFileChannelPoolCapacity();
	}

	public void setFileChannelPoolCapacity(int capacity)
	{
		application.getStoreSettings().setFileChannelPoolCapacity(capacity);
	}

	public int getInmemoryCacheSize()
	{
		return application.getStoreSettings().getInmemoryCacheSize();
	}

	public void setInmemoryCacheSize(int inmemoryCacheSize)
	{
		application.getStoreSettings().setInmemoryCacheSize(inmemoryCacheSize);
	}

	public long getMaxSizePerSession()
	{
		return application.getStoreSettings().getMaxSizePerSession().bytes();
	}

	public void setMaxSizePerSession(long maxSizePerSession)
	{
		Bytes bytes = Bytes.bytes(maxSizePerSession);
		application.getStoreSettings().setMaxSizePerSession(bytes);
	}

	public String getFileStoreFolder()
	{
		return application.getStoreSettings().getFileStoreFolder().getAbsolutePath();
	}

	public void setFileStoreFolder(String fileStoreFolder)
	{
		File storeFolder = new File(fileStoreFolder);
		application.getStoreSettings().setFileStoreFolder(storeFolder);
	}

}
