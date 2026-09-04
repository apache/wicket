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

import org.apache.wicket.pageStore.DiskPageStore;

/**
 * JMX MBean for Application's StoreSettings
 */
public interface StoreSettingsMBean
{

	/**
	 * @return maximum page size. After this size is exceeded, the {@link DiskPageStore} will start
	 *         saving the pages at the beginning of file.
	 */
	long getMaxSizePerSession();

	/**
	 * @return the location of the folder where {@link DiskPageStore} will store the files with page
	 *         instances per session
	 */
	String getFileStoreFolder();

	/**
	 * @return the capacity of the queue used to store the pages which will be stored asynchronously
	 */
	int getAsynchronousQueueCapacity();

	/**
	 * @return {@code true} when the HTTP worker thread doesn't wait for the storing of the page's
	 *         bytes in {@link org.apache.wicket.pageStore.IPageStore}
	 */
	boolean isAsynchronous();
}
