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
package org.apache.wicket.examples.websocket.progress;

import java.io.Serializable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that uses the web connection to push components updates to the client.
 */
public class ProgressUpdater
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressUpdater.class);

	public static ProgressUpdateTask start(ConnectedMessage message, ScheduledExecutorService scheduledExecutorService)
	{
		// create an asynchronous task that will write the data to the client
		ProgressUpdateTask progressUpdateTask = new ProgressUpdateTask(message.getApplication(), message.getSessionId(), message.getKey());
		scheduledExecutorService.schedule(progressUpdateTask, 1, TimeUnit.SECONDS);
		return progressUpdateTask;
	}

	public static void restart(ProgressUpdateTask progressUpdateTask, ScheduledExecutorService scheduledExecutorService) {
		scheduledExecutorService.schedule(progressUpdateTask, 1, TimeUnit.SECONDS);
	}

	/**
	 * A push message used to update progress.
	 */
	public static class ProgressUpdate implements IWebSocketPushMessage
	{

		private final int progress;

		public ProgressUpdate(int progress)
		{
			this.progress = progress;
		}

		public int getProgress()
		{
			return progress;
		}
	}

	/**
	 * A task that sends data to the client by pushing it to the web socket connection
	 */
	public static class ProgressUpdateTask implements Runnable, Serializable
	{
		/**
		 * The following fields are needed to be able to lookup the IWebSocketConnection from
		 * IWebSocketConnectionRegistry
		 */
		private final String applicationName;
		private final String sessionId;
		private final IKey key;

		private boolean canceled = false;
		private boolean running = false;

		private ProgressUpdateTask(Application application, String sessionId, IKey key)
		{
			this.applicationName = application.getName();
			this.sessionId = sessionId;
			this.key = key;
		}

		@Override
		public void run()
		{
			running = true;
			Application application = Application.get(applicationName);
			WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
			IWebSocketConnectionRegistry webSocketConnectionRegistry = webSocketSettings.getConnectionRegistry();

			int progress = 0;

			while (progress <= 100)
			{
				IWebSocketConnection connection = webSocketConnectionRegistry.getConnection(application, sessionId, key);
				try
				{
					if (connection == null || !connection.isOpen())
					{
						running = false;
						// stop if the web socket connection is closed
						return;
					}

					if (canceled)
					{
						canceled = false;
						running = false;
						return;
					}

					WebSocketPushBroadcaster broadcaster =
							new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
					broadcaster.broadcast(new ConnectedMessage(application, sessionId, key), new ProgressUpdate(progress));

					// sleep for a while to simulate work
					TimeUnit.SECONDS.sleep(1);
					progress++;
				}
				catch (InterruptedException x)
				{
					Thread.currentThread().interrupt();
					break;
				}
				catch (Exception e)
				{
					LOGGER.error("unexpected exception", e);
					break;
				}
			}
			running = false;
		}

		public boolean isRunning() {
			return running;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void cancel() {
			this.canceled = true;
			this.running = false;
		}
	}
}
