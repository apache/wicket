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
 *
 */
public class Http2Settings
{
	private static final MetaDataKey<Http2Settings> KEY = new MetaDataKey<Http2Settings>()
	{
	};

	/**
	 * Holds this Http2Settings in the Application's metadata.
	 * This way wicket-core module doesn't have reference to wicket-http2-core
	 */
	public static final class Holder
	{
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

		public static void set(Application application, Http2Settings settings)
		{
			application.setMetaData(KEY, settings);
		}
	}

	private PushBuilder pushBuilder = NoopPushBuilder.INSTANCE;

	public Http2Settings setPushBuilder(PushBuilder pushBuilder) {
		this.pushBuilder = Args.notNull(pushBuilder, "pushBuilder");
		return this;
	}

	public PushBuilder getPushBuilder() {
		return pushBuilder;
	}
}
