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

import org.apache.wicket.Application;

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

	@Override
	public int getInmemoryCacheSize()
	{
		return application.getStoreSettings().getInmemoryCacheSize();
	}

	@Override
	public long getMaxSizePerSession()
	{
		return application.getStoreSettings().getMaxSizePerSession().bytes();
	}

	@Override
	public String getFileStoreFolder()
	{
		return application.getStoreSettings().getFileStoreFolder().getAbsolutePath();
	}

	@Override
	public int getAsynchronousQueueCapacity()
	{
		return application.getStoreSettings().getAsynchronousQueueCapacity();
	}

	@Override
	public boolean isAsynchronous()
	{
		return application.getStoreSettings().isAsynchronous();
	}

}
