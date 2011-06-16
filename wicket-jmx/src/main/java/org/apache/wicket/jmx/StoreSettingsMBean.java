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

import org.apache.wicket.pageStore.DiskDataStore;

/**
 * JMX MBean for Application's StoreSettings
 */
public interface StoreSettingsMBean
{

	/**
	 * @return the maximum number of opened file channels by {@link DiskDataStore}.
	 */
	int getFileChannelPoolCapacity();

	/**
	 * Sets the number of maximum opened file channels by {@link DiskDataStore}
	 * 
	 * @param capacity
	 *            the new maximum number of opened file channels
	 */
	void setFileChannelPoolCapacity(int capacity);

	/**
	 * @return the number of page instances which will be stored in the http session for faster
	 *         retrieval
	 */
	int getInmemoryCacheSize();

	/**
	 * Sets the maximum number of page instances which will be stored in the http session for faster
	 * retrieval
	 * 
	 * @param inmemoryCacheSize
	 *            the maximum number of page instances which will be held in the http session
	 */
	void setInmemoryCacheSize(int inmemoryCacheSize);

	/**
	 * @return maximum page size. After this size is exceeded, the {@link DiskDataStore} will start
	 *         saving the pages at the beginning of file.
	 */
	long getMaxSizePerSession();

	/**
	 * Sets the maximum size of the {@link File} where page instances per session are stored. After
	 * reaching this size the {@link DiskDataStore} will start overriding the oldest pages at the
	 * beginning of the file.
	 * 
	 * @param maxSizePerSession
	 *            the maximum size of the file where page instances are stored per session. In
	 *            bytes.
	 */
	void setMaxSizePerSession(long maxSizePerSession);

	/**
	 * @return the location of the folder where {@link DiskDataStore} will store the files with page
	 *         instances per session
	 */
	String getFileStoreFolder();

	/**
	 * Sets the folder where {@link DiskDataStore} will store the files with page instances per
	 * session
	 * 
	 * @param fileStoreFolder
	 *            the new location
	 */
	void setFileStoreFolder(String fileStoreFolder);
}
