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
package org.apache.wicket.dbsc;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketConnection;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.tester.WicketTester;

/**
 * A WebSocket connection to a page whose handshake carries the cookies a test chooses, which
 * {@code WebSocketTester} cannot send.
 */
final class TestSocket extends AbstractWebSocketProcessor
{
	final List<String> sent = new CopyOnWriteArrayList<>();

	private boolean open;

	private String closeReason;

	/**
	 * @param cookieHeader
	 *            the {@code Cookie} header of the handshake, or {@code null} for none
	 */
	TestSocket(WicketTester tester, Page page, String cookieHeader)
	{
		super(handshake(tester, page, cookieHeader), (WebApplication)page.getApplication());
	}

	private static HttpServletRequest handshake(WicketTester tester, Page page,
		String cookieHeader)
	{
		if (tester.getApplication().getWicketFilter().getFilterPath() == null)
		{
			tester.getApplication().getWicketFilter().setFilterPath("");
		}
		MockHttpServletRequest request = new MockHttpServletRequest(tester.getApplication(),
			tester.getHttpSession(), tester.getServletContext());
		request.addParameter("pageId", String.valueOf(page.getPageId()));
		request.addParameter("context", page.getClass().getName());
		request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");
		if (cookieHeader != null)
		{
			request.setHeader("Cookie", cookieHeader);
		}
		return request;
	}

	@Override
	public void onOpen(Object containerConnection)
	{
		open = true;
		onConnect(new Connection());
	}

	boolean isOpen()
	{
		return open;
	}

	String getCloseReason()
	{
		return closeReason;
	}

	private final class Connection extends AbstractWebSocketConnection
	{
		Connection()
		{
			super(TestSocket.this);
		}

		@Override
		public boolean isOpen()
		{
			return open;
		}

		@Override
		public void close(int code, String reason)
		{
			open = false;
			closeReason = reason;
		}

		@Override
		public IWebSocketConnection sendMessage(String message)
		{
			sent.add(message);
			return this;
		}

		@Override
		public Future<Void> sendMessageAsync(String message)
		{
			sendMessage(message);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public Future<Void> sendMessageAsync(String message, long timeout)
		{
			return sendMessageAsync(message);
		}

		@Override
		public IWebSocketConnection sendMessage(byte[] message, int offset, int length)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Future<Void> sendMessageAsync(byte[] message, int offset, int length)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Future<Void> sendMessageAsync(byte[] message, int offset, int length,
			long timeout)
		{
			throw new UnsupportedOperationException();
		}
	}
}
