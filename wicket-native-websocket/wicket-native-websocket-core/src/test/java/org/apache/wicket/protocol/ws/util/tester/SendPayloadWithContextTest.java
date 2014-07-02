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
package org.apache.wicket.protocol.ws.util.tester;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-5627.
 * Uses WebSocketBehavior.
 *
 * @since 6.17.0
 */
public class SendPayloadWithContextTest extends Assert
{
	final AtomicBoolean context = new AtomicBoolean(false);

	WicketTester tester;

	@Before
	public void before()
	{
		tester = new WicketTester();
		WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(tester.getApplication());
		webSocketSettings.setSendPayloadExecutor(new WebSocketSettings.SameThreadExecutor() {
			@Override
			public void run(Runnable command)
			{
				context.set(true);
				super.run(command);
				context.set(false);
			}
		});
	}

	@After
	public void after()
	{
		tester.destroy();
	}

	@Test
	public void sendPayloadWithContext()
	{
		SendPayloadWithContextTestPage page = new SendPayloadWithContextTestPage();
		tester.startPage(page);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page) {
			@Override
			protected void onOutMessage(String message)
			{
				assertThat(Boolean.parseBoolean(message), is(equalTo(Boolean.TRUE)));
			}
		};
		assertThat(context.get(), is(false));
		webSocketTester.sendMessage("trigger web socket communication");
		assertThat(context.get(), is(false));
		webSocketTester.destroy();
	}

	class SendPayloadWithContextTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		SendPayloadWithContextTestPage()
		{
			add(new WebSocketBehavior()
			{
				@Override
				protected void onMessage(WebSocketRequestHandler handler, TextMessage ignored)
				{
					// send an outbound message with the current context encoded as String
					handler.push(String.valueOf(context.get()));
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}

}
