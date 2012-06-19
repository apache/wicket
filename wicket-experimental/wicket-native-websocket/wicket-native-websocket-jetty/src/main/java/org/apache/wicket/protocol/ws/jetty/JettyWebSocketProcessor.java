package org.apache.wicket.protocol.ws.jetty;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.eclipse.jetty.websocket.WebSocket;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates
 * with Jetty 7.x {@link WebSocket web socket} implementation.
 *
 * @since 6.0
 */
public class JettyWebSocketProcessor extends AbstractWebSocketProcessor
{
	public class JettyWebSocket implements WebSocket.OnTextMessage, WebSocket.OnBinaryMessage
	{
		@Override
		public void onMessage(byte[] bytes, int offset, int length)
		{
			JettyWebSocketProcessor.this.onMessage(bytes, offset, length);
		}

		@Override
		public void onMessage(String message)
		{
			JettyWebSocketProcessor.this.onMessage(message);
		}

		@Override
		public void onOpen(Connection connection)
		{
			JettyWebSocketProcessor.this.onOpen(connection);
		}

		@Override
		public void onClose(int code, String message)
		{
			JettyWebSocketProcessor.this.onClose(code, message);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param request
	 *      the http request that was used to create the TomcatWebSocketProcessor
	 * @param application
	 *      the current Wicket Application
	 */
	public JettyWebSocketProcessor(final HttpServletRequest request, final Application application)
	{
		super(request, application);
	}


	@Override
	public void onOpen(Object connection)
	{
		if (!(connection instanceof WebSocket.Connection))
		{
			throw new IllegalArgumentException(JettyWebSocketProcessor.class.getName() + " can work only with " + WebSocket.Connection.class.getName());
		}
		onConnect(new JettyWebSocketConnection((WebSocket.Connection) connection));
	}
}
