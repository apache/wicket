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
package org.apache.wicket.protocol.ws.tomcat7;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates
 * with Tomcat 7.x {@link WsOutbound web socket} implementation.
 *
 * @since 6.0
 */
public class TomcatWebSocketProcessor extends AbstractWebSocketProcessor
{
	public class TomcatWebSocket extends MessageInbound
	{
		@Override
		protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException
		{
			byte[] bytes = byteBuffer.array();
			int offset = byteBuffer.position();
			int length = byteBuffer.limit() - offset;
			TomcatWebSocketProcessor.this.onMessage(bytes, offset, length);
		}

		@Override
		protected void onTextMessage(CharBuffer charBuffer) throws IOException
		{
			TomcatWebSocketProcessor.this.onMessage(charBuffer.toString());
		}

		@Override
		protected void onOpen(WsOutbound outbound)
		{
			TomcatWebSocketProcessor.this.onOpen(outbound);
		}

		@Override
		protected void onClose(int status)
		{
			TomcatWebSocketProcessor.this.onClose(status, "Connection closed by client");
		}
	}
	public TomcatWebSocketProcessor(final HttpServletRequest request, final Application application)
	{
		super(request, application);
	}

	@Override
	public void onOpen(Object containerConnection)
	{
		if (!(containerConnection instanceof WsOutbound))
		{
			throw new IllegalArgumentException(TomcatWebSocketProcessor.class.getName() +
					" can work only with " + WsOutbound.class.getName());
		}
		onConnect(new TomcatWebSocketConnection((WsOutbound) containerConnection));
	}

}
