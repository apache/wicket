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

import java.util.concurrent.ScheduledExecutorService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.websocket.charts.ChartUpdater;
import org.apache.wicket.examples.websocket.charts.WebSocketChart;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.https.RequireHttps;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.time.Duration;

@RequireHttps
public class WebSocketBehaviorDemoPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;
	private final WebMarkupContainer downloadingContainer = new WebMarkupContainer("downloading");

	@Override
	protected void onInitialize() {
		super.onInitialize();

		ResourceStreamResource resource = new ResourceStreamResource()
		{
			private static final long serialVersionUID = 1L;
			private int i = 42;

			@Override
			protected IResourceStream getResourceStream(IResource.Attributes attributes)
			{
				return new StringResourceStream("downloaded via ajax " + (i++) + " times");
			}
		}.setContentDisposition(ContentDisposition.ATTACHMENT)
			.setFileName("File-from-IResource.txt")
			.setCacheDuration(Duration.NONE);

		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(resource)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}
		};
		add(download);
		WebSocketChart chartPanel = new WebSocketChart("chartPanel");
		chartPanel.add(new WebSocketBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConnect(ConnectedMessage message)
			{
				super.onConnect(message);

				ScheduledExecutorService service = JSR356Application.get().getScheduledExecutorService();
				ChartUpdater.start(message, service);
			}
		});
		add(downloadingContainer.setOutputMarkupPlaceholderTag(true).setVisible(false));
		add(chartPanel);

		add(new WebSocketBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
			{
				if ("start".equals(message.getText()))
				{
					download.initiate(handler);
				}
			}
		});
	}
}
