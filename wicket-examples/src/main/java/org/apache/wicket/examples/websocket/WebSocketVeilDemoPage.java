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
package org.apache.wicket.examples.websocket;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.ajax.veil.LocalVeilBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.request.resource.CssResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Veils a panel while the server recomputes it, then pushes the result through a WebSocket
 * connection, see {@link LocalVeilBehavior#getVeilMessage()}.
 */
public class WebSocketVeilDemoPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketVeilDemoPage.class);

	private static final int ROUNDS = 10;

	/** The running tasks, by session and page, so a page runs at most one. */
	private static final Map<String, PushTask> TASKS = new ConcurrentHashMap<>();

	private final WebMarkupContainer counterPanel;

	private final LocalVeilBehavior veil = new LocalVeilBehavior();

	private final Label counter;

	private final Label lastWork;

	private int panelRequests;

	/**
	 * Constructor.
	 */
	public WebSocketVeilDemoPage()
	{
		add(new WebSocketBehavior()
		{
			private static final long serialVersionUID = 1L;
		});

		counterPanel = new WebMarkupContainer("counterPanel");
		counterPanel.add(veil);
		add(counterPanel);

		counter = new Label("counter", Model.of(0));
		counterPanel.add(counter);
		lastWork = new Label("lastWork", Model.of("No update pushed yet."));
		counterPanel.add(lastWork);
		counterPanel.add(new Label("panelRequests", () -> panelRequests));
		counterPanel.add(new AjaxLink<Void>("strayUnveil")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				sendStrayUnveil();
				sleep(Duration.ofSeconds(2));
				panelRequests++;
				target.add(counterPanel);
			}
		});

		add(new AjaxLink<Void>("start")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				startPushes();
			}
		});

		add(new AjaxLink<Void>("stop")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				PushTask task = TASKS.get(taskKey());
				if (task != null)
				{
					task.cancel();
				}
			}
		});
	}

	private void startPushes()
	{
		String key = taskKey();
		PushTask task = new PushTask(key, getApplication().getName(), getSession().getId(),
			getPageId(), veil.getVeilMessage());
		if (TASKS.putIfAbsent(key, task) == null)
		{
			JSR356Application.get().getScheduledExecutorService().execute(task);
		}
	}

	private void sendStrayUnveil()
	{
		String applicationName = getApplication().getName();
		String sessionId = getSession().getId();
		int pageId = getPageId();
		String unveilMessage = veil.getUnveilMessage();
		JSR356Application.get().getScheduledExecutorService().schedule(() -> {
			IWebSocketConnection connection = connection(applicationName, sessionId, pageId);
			if (connection != null && connection.isOpen())
			{
				try
				{
					connection.sendMessage(unveilMessage);
				}
				catch (IOException e)
				{
					LOGGER.error("Sending the unveil message failed", e);
				}
			}
		}, 500, TimeUnit.MILLISECONDS);
	}

	private static IWebSocketConnection connection(String applicationName, String sessionId,
		int pageId)
	{
		Application application = Application.get(applicationName);
		return WebSocketSettings.Holder.get(application)
			.getConnectionRegistry()
			.getConnection(application, sessionId, new PageIdKey(pageId));
	}

	private static void sleep(Duration duration)
	{
		try
		{
			Thread.sleep(duration.toMillis());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	private String taskKey()
	{
		return getSession().getId() + "#" + getPageId();
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		super.onEvent(event);

		if (event.getPayload() instanceof WebSocketPushPayload payload)
		{
			if (payload.getMessage() instanceof CounterUpdate update)
			{
				counter.setDefaultModelObject(update.value);
				lastWork.setDefaultModelObject(String.format(
					"Update %d of %d took %d ms on the server.", update.value, ROUNDS,
					update.work.toMillis()));
				payload.getHandler().add(counterPanel);
				veil.unveil(payload.getHandler());
			}
			else if (payload.getMessage() instanceof Progress progress)
			{
				lastWork.setDefaultModelObject(String.format(
					"Working on update %d of %d, halfway through.", progress.round, ROUNDS));
				payload.getHandler().add(counterPanel);
			}
		}
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(
			new CssResourceReference(WebSocketVeilDemoPage.class, "WebSocketVeilDemoPage.css")));
	}

	/**
	 * Pushes the new counter value, and how long it took to compute.
	 */
	private static class CounterUpdate implements IWebSocketPushMessage
	{
		private final int value;

		private final Duration work;

		CounterUpdate(int value, Duration work)
		{
			this.value = value;
			this.work = work;
		}
	}

	/**
	 * Reports halfway through the work, without lifting the veil.
	 */
	private static class Progress implements IWebSocketPushMessage
	{
		private final int round;

		Progress(int round)
		{
			this.round = round;
		}
	}

	/**
	 * Recomputes the counter a number of times, alternating long and short work. Both outlast the
	 * spinner delay; the long work reports its progress halfway through, which redraws the veiled
	 * panel; the short work ends within the spinner's minimum time, so the spinner stays on the
	 * redrawn panel for the rest of it.
	 */
	private static class PushTask implements Runnable
	{
		private final String key;

		private final String applicationName;

		private final String sessionId;

		private final int pageId;

		private final String veilMessage;

		private volatile boolean canceled;

		PushTask(String key, String applicationName, String sessionId, int pageId,
			String veilMessage)
		{
			this.key = key;
			this.applicationName = applicationName;
			this.sessionId = sessionId;
			this.pageId = pageId;
			this.veilMessage = veilMessage;
		}

		void cancel()
		{
			canceled = true;
		}

		@Override
		public void run()
		{
			try
			{
				for (int round = 1; round <= ROUNDS && !canceled; round++)
				{
					IWebSocketConnection connection = connection(applicationName, sessionId,
						pageId);
					if (connection == null || !connection.isOpen())
					{
						return;
					}

					connection.sendMessage(veilMessage);
					boolean longWork = round % 2 == 1;
					Duration work = Duration.ofMillis(longWork ? 3000 : 600);
					if (longWork)
					{
						TimeUnit.MILLISECONDS.sleep(work.toMillis() / 2);
						connection.sendMessage(new Progress(round));
						TimeUnit.MILLISECONDS.sleep(work.toMillis() / 2);
					}
					else
					{
						TimeUnit.MILLISECONDS.sleep(work.toMillis());
					}
					connection.sendMessage(new CounterUpdate(round, work));

					TimeUnit.SECONDS.sleep(1);
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			catch (IOException | RuntimeException e)
			{
				LOGGER.error("Pushing to the counter panel failed", e);
			}
			finally
			{
				TASKS.remove(key);
			}
		}
	}
}
