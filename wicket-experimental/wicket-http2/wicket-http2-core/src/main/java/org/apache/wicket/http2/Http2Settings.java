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
package org.apache.wicket.http2;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.http2.markup.head.NoopPushBuilder;
import org.apache.wicket.http2.markup.head.PushBuilder;
import org.apache.wicket.util.lang.Args;

/**
 * The http2 settings used to get the vendor specific push builder API
 * 
 * @author Martin Grigorov
 */
public class Http2Settings
{
	/**
	 * The meta data key of the http2 settings
	 */
	private static final MetaDataKey<Http2Settings> KEY = new MetaDataKey<Http2Settings>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Holds this Http2Settings in the Application's meta data. This way wicket-core module doesn't
	 * have reference to wicket-http2-core
	 */
	public static final class Holder
	{
		/**
		 * Gets the http2 settings from the given application
		 * 
		 * @param application
		 *            the application to get the meta data from
		 * @return the http2 settings
		 */
		public static Http2Settings get(Application application)
		{
			Http2Settings settings = application.getMetaData(KEY);
			if (settings == null)
			{
				synchronized (application)
				{
					if (settings == null)
					{
						settings = new Http2Settings();
						set(application, settings);
					}
				}
			}
			return settings;
		}

		/**
		 * Sets the given http2 settings to the given application
		 * 
		 * @param application
		 *            the application to set the meta data key to
		 * @param settings
		 *            the http2 settings to be set to the application
		 */
		public static void set(Application application, Http2Settings settings)
		{
			application.setMetaData(KEY, settings);
		}
	}

	private PushBuilder pushBuilder = NoopPushBuilder.INSTANCE;

	/**
	 * Sets the push builder that has been initialized
	 * 
	 * @param pushBuilder
	 *            the push builder to be used after the initialization
	 * @return the push builder
	 */
	public Http2Settings setPushBuilder(PushBuilder pushBuilder)
	{
		this.pushBuilder = Args.notNull(pushBuilder, "pushBuilder");
		return this;
	}

	/**
	 * Gets the push builder which has been initialized
	 * 
	 * @return the push builder
	 */
	public PushBuilder getPushBuilder()
	{
		return pushBuilder;
	}
}
