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
package org.apache.wicket.protocol.ws.javax.app.charts;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;

/**
 * A panel that initializes a Google Line chart and uses WebSocketBehavior to register an asynchronous
 * task that will push some data through the web socket connection.
 */
public class WebSocketChart extends Panel
{
	public WebSocketChart(final String id)
	{
		super(id);

		add(new ChartUpdatingBehavior());
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ChartsResourceReference()));
	}

	/**
	 * A web socket behavior that schedules an asynchronous task that will write some data when
	 * a client connects
	 */
	private static class ChartUpdatingBehavior extends WebSocketBehavior
	{
		@Override
		protected void onConnect(ConnectedMessage message)
		{
			super.onConnect(message);

			Record[] data = generateData();

			// create an asynchronous task that will write the data to the client
			UpdateTask updateTask = new UpdateTask(message.getApplication(), message.getSessionId(), message.getPageId(), data);
			Executors.newScheduledThreadPool(1).schedule(updateTask, 1, TimeUnit.SECONDS);
		}

		/**
		 * Generates some random data to send to the client
		 * @return records with random data
		 */
		private Record[] generateData()
		{
			Random randomGenerator = new Random();
			Record[] data = new Record[1000];
			for (int i = 0; i < 1000; i++)
			{
				Record r = new Record();
				r.year = 2000 + i;
				r.field = (i % 2 == 0) ? "Company 1" : "Company 2";
				r.value = randomGenerator.nextInt(1500);
				data[i] = r;
			}
			return data;
		}
    }

	/**
	 * A task that sends data to the client by pushing it to the web socket connection
	 */
	private static class UpdateTask implements Runnable
	{
		private static final String JSON_SKELETON = "{ \"year\": \"%s\", \"field\": \"%s\", \"value\": %s }";

		/**
		 * The following fields are needed to be able to lookup the IWebSocketConnection from
		 * IWebSocketConnectionRegistry
		 */
		private final String applicationName;
		private final String sessionId;
		private final int pageId;

		/**
		 * The data that has to be sent to the client
		 */
		private final Record[] data;

		private UpdateTask(Application application, String sessionId, int pageId, Record[] data)
		{
			this.applicationName = application.getName();
			this.sessionId = sessionId;
			this.pageId = pageId;
			this.data = data;
		}

		@Override
		public void run()
		{
			Application application = Application.get(applicationName);
			IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(application);
			IWebSocketConnectionRegistry webSocketConnectionRegistry = webSocketSettings.getConnectionRegistry();
			IWebSocketConnection connection = webSocketConnectionRegistry.getConnection(application, sessionId, pageId);

			int dataIndex = 0;

			while (dataIndex < data.length)
			{
				try
				{
					Record record = data[dataIndex++];
					String json = String.format(JSON_SKELETON, record.year, record.field, record.value);

					if (connection == null || !connection.isOpen())
					{
						// stop if the web socket connection is closed
						return;
					}
					connection.sendMessage(json);

					// sleep for a while to simulate work
					TimeUnit.SECONDS.sleep(1);
				}
				catch (Exception x)
				{
					x.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * The data that is being sent to the client in JSON format
	 */
	private static class Record
	{
		private int year;
		private String field;
		private int value;
	}
}
