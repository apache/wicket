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
package org.apache.wicket.protocol.ws;


import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.protocol.ws.api.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.concurrent.Executor;

/**
 * Interface for websocket related settings.
 * <p>
 * More documentation is available about each setting in the setter method for the property.
 *
 * @since 6.4
 */
public interface IWebSocketSettings
{
	public static final MetaDataKey<IWebSocketSettings> KEY = new MetaDataKey<IWebSocketSettings>()
	{
	};

	/**
	 * Holds this IWebSocketSettings in the Application's metadata.
	 * This way wicket-core module doesn't have reference to wicket-native-websocket.
	 */
	public static final class Holder
	{
		public static IWebSocketSettings get(Application application)
		{
			IWebSocketSettings settings = application.getMetaData(KEY);
			if (settings == null)
			{
				synchronized (application)
				{
					if (settings == null)
					{
						settings = new WebSocketSettings();
						set(application, settings);
					}
				}
			}
			return settings;
		}

		public static void set(Application application, IWebSocketSettings settings)
		{
			application.setMetaData(KEY, settings);
		}
	}

	/**
	 * The executor for processing websocket push messages broadcasted to all sessions.
	 *
	 * @return
	 *            The executor used for processing push messages.
	 */
	Executor getWebSocketPushMessageExecutor();

	/**
	 * Set the executor for processing websocket push messages broadcasted to all sessions.
	 * Default executor does all the processing in the caller thread. Using a proper thread pool is adviced
	 * for applications that send push events from ajax calls to avoid page level deadlocks.
	 *
	 * @param executorService
	 *            The executor used for processing push messages.
	 */
	IWebSocketSettings setWebSocketPushMessageExecutor(Executor executorService);

	IWebSocketConnectionRegistry getConnectionRegistry();

	IWebSocketSettings setConnectionRegistry(IWebSocketConnectionRegistry connectionRegistry);
}
