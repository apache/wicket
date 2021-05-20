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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that uses the web connection to push components updates to the client.
 */
public class ProgressUpdater
{
	/**
	 * Marks a page as a listener to task progress.
	 */
	public interface ITaskProgressListener {

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressUpdater.class);

	public static ProgressUpdateTask start(Application application, String session, ScheduledExecutorService scheduledExecutorService)
	{
		// create an asynchronous task that will write the data to the client
		ProgressUpdateTask progressUpdateTask = new ProgressUpdateTask(application, session);
		scheduledExecutorService.schedule(progressUpdateTask, 1, TimeUnit.SECONDS);
		return progressUpdateTask;
	}

	/**
	 * Signal task was canceled.
	 */
	public static class TaskCanceled implements IWebSocketPushMessage
	{
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

		private volatile boolean canceled = false;
		private volatile boolean running = false;

		private ProgressUpdateTask(Application application, String sessionId)
		{
			this.applicationName = application.getName();
			this.sessionId = sessionId;
		}

		@Override
		public void run()
		{
			running = true;
			Application application = Application.get(applicationName);
			WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);

			int progress = 0;

			while (progress <= 100)
			{
				try
				{
					WebSocketPushBroadcaster broadcaster =
							new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());

					if (canceled)
					{
						canceled = false;
						running = false;
						broadcaster.broadcastAllMatchingFilter(application, (sessionId, key) ->
								ProgressUpdateTask.this.sessionId.equals(sessionId) && key instanceof PageIdKey
										&& ITaskProgressListener.class.isAssignableFrom(getPageClass(application, key)),
								new TaskCanceled());
						return;
					}
					broadcaster.broadcastAllMatchingFilter(application, (sessionId, key) ->
							ProgressUpdateTask.this.sessionId.equals(sessionId) && key instanceof PageIdKey &&
									ITaskProgressListener.class.isAssignableFrom(getPageClass(application, key)),
							new ProgressUpdate(progress));

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

		protected Class<?> getPageClass(Application application, IKey iKey) {
			try {
				return application.getApplicationSettings().getClassResolver().resolveClass(iKey.getContext());
			} catch (ClassNotFoundException e) {
				throw new WicketRuntimeException(e);
			}
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
