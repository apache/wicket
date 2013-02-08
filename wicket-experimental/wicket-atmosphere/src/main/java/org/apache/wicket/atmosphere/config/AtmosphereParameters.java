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
package org.apache.wicket.atmosphere.config;

import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;

/**
 * Contains the parameters passed to the Atmosphere JQuery plugin. See
 * {@link "https://github.com/Atmosphere/atmosphere/wiki/jQuery.atmosphere.js-API"} for details.
 * 
 * @author papegaaij
 */
public class AtmosphereParameters
{
	private Integer connectTimeout;
	private Integer reconnectInterval;
	private Integer timeout;
	private AtmosphereMethod method;
	private List<String> headers;
	private String contentType;
	private String data;
	private Boolean suspend;
	private Integer maxRequest;
	private Long maxStreamingLength;
	private AtmosphereLogLevel logLevel;
	private AtmosphereTransport transport = AtmosphereTransport.WEBSOCKET;
	private AtmosphereTransport fallbackTransport;
	private AtmosphereMethod fallbackMethod;
	private String webSocketImpl;
	private String webSocketUrl;
	private String webSocketPathDelimiter;
	private Boolean enableXDR;
	private Boolean rewriteURL;
	private Boolean attachHeadersAsQueryString;
	private Boolean dropAtmosphereHeaders;
	private Boolean executeCallbackBeforeReconnect;
	private Boolean withCredentials;
	private Boolean trackMessageLength = true;
	private String messageDelimiter = "<|msg|>";
	private Boolean shared;
	private Boolean readResponsesHeaders;
	private Integer maxReconnectOnClose;

	/**
	 * @return The connect timeout. If the client fail to connect, the fallbackTransport will be
	 *         used.
	 */
	public Integer getConnectTimeout()
	{
		return connectTimeout;
	}

	/**
	 * The connect timeout. If the client fail to connect, the fallbackTransport will be used.
	 * 
	 * @param connectTimeout
	 */
	public void setConnectTimeout(Integer connectTimeout)
	{
		this.connectTimeout = connectTimeout;
	}

	/**
	 * @return The interval before an attempt to reconnect will be made.
	 */
	public Integer getReconnectInterval()
	{
		return reconnectInterval;
	}

	/**
	 * The interval before an attempt to reconnect will be made.
	 * 
	 * @param reconnectInterval
	 */
	public void setReconnectInterval(Integer reconnectInterval)
	{
		this.reconnectInterval = reconnectInterval;
	}

	/**
	 * @return The maximum time a connection stay opened when no message (or event) are sent or
	 *         received.
	 */
	public Integer getTimeout()
	{
		return timeout;
	}

	/**
	 * The maximum time a connection stay opened when no message (or event) are sent or received.
	 * 
	 * @param timeout
	 */
	public void setTimeout(Integer timeout)
	{
		this.timeout = timeout;
	}

	/**
	 * @return The HTTP method to use.
	 */
	public AtmosphereMethod getMethod()
	{
		return method;
	}

	/**
	 * The HTTP method to use.
	 * 
	 * @param method
	 */
	public void setMethod(AtmosphereMethod method)
	{
		this.method = method;
	}

	/**
	 * @return A list of headers to send
	 */
	public List<String> getHeaders()
	{
		return headers;
	}

	/**
	 * A list of headers to send
	 * 
	 * @param headers
	 */
	public void setHeaders(List<String> headers)
	{
		this.headers = headers;
	}

	/**
	 * @return The request's content-type
	 */
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * The request's content-type
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * @return The request body (required when doing a POST or PUT)
	 */
	public String getData()
	{
		return data;
	}

	/**
	 * The request body (required when doing a POST or PUT)
	 * 
	 * @param data
	 */
	public void setData(String data)
	{
		this.data = data;
	}

	/**
	 * @return Suspend the request, always reconnect if the connection gets closed (for whatever
	 *         reason), independently of the transport used.
	 */
	public Boolean getSuspend()
	{
		return suspend;
	}

	/**
	 * Suspend the request, always reconnect if the connection gets closed (for whatever reason),
	 * independently of the transport used.
	 * 
	 * @param suspend
	 */
	public void setSuspend(Boolean suspend)
	{
		this.suspend = suspend;
	}

	/**
	 * @return The maximum number of requests that will be executed. Once the maximum gets reached,
	 *         the connection will be closed.
	 */
	public Integer getMaxRequest()
	{
		return maxRequest;
	}

	/**
	 * The maximum number of requests that will be executed. Once the maximum gets reached, the
	 * connection will be closed.
	 * 
	 * @param maxRequest
	 */
	public void setMaxRequest(Integer maxRequest)
	{
		this.maxRequest = maxRequest;
	}

	/**
	 * @return When the streaming transport is used, the maximum size of the body received. Once
	 *         reached the connection will be closed and re-opened
	 */
	public Long getMaxStreamingLength()
	{
		return maxStreamingLength;
	}

	/**
	 * When the streaming transport is used, the maximum size of the body received. Once reached the
	 * connection will be closed and re-opened
	 * 
	 * @param maxStreamingLength
	 */
	public void setMaxStreamingLength(Long maxStreamingLength)
	{
		this.maxStreamingLength = maxStreamingLength;
	}

	/**
	 * @return The log level. Value allowed are 'info', 'debug' and 'error'
	 */
	public AtmosphereLogLevel getLogLevel()
	{
		return logLevel;
	}

	/**
	 * The log level. Value allowed are 'info', 'debug' and 'error'
	 * 
	 * @param logLevel
	 */
	public void setLogLevel(AtmosphereLogLevel logLevel)
	{
		this.logLevel = logLevel;
	}

	/**
	 * @return The transport Atmosphere will use between the client and server. Allowed value are
	 *         polling, long-polling, streaming, jsonp, sse and websocket
	 */
	public AtmosphereTransport getTransport()
	{
		return transport;
	}

	/**
	 * The transport Atmosphere will use between the client and server. Allowed value are polling,
	 * long-polling, streaming, jsonp, sse and websocket
	 * 
	 * @param transport
	 */
	public void setTransport(AtmosphereTransport transport)
	{
		this.transport = transport;
	}

	/**
	 * @return If either the client or server isn't able to support the selected 'transport', the
	 *         fallback value will be used. Allowed value are polling, long-polling, streaming,
	 *         jsonp, sse and websocket
	 */
	public AtmosphereTransport getFallbackTransport()
	{
		return fallbackTransport;
	}

	/**
	 * If either the client or server isn't able to support the selected 'transport', the fallback
	 * value will be used. Allowed value are polling, long-polling, streaming, jsonp, sse and
	 * websocket
	 * 
	 * @param fallbackTransport
	 */
	public void setFallbackTransport(AtmosphereTransport fallbackTransport)
	{
		this.fallbackTransport = fallbackTransport;
	}

	/**
	 * @return Used when the fallbackTransport gets used.
	 */
	public AtmosphereMethod getFallbackMethod()
	{
		return fallbackMethod;
	}

	/**
	 * Used when the fallbackTransport gets used.
	 * 
	 * @param fallbackMethod
	 */
	public void setFallbackMethod(AtmosphereMethod fallbackMethod)
	{
		this.fallbackMethod = fallbackMethod;
	}

	/**
	 * @return The WebSocket API to use. As an example, you can use Flash WebSocket
	 */
	public String getWebSocketImpl()
	{
		return webSocketImpl;
	}

	/**
	 * The WebSocket API to use. As an example, you can use Flash WebSocket
	 * 
	 * @param webSocketImpl
	 */
	public void setWebSocketImpl(String webSocketImpl)
	{
		this.webSocketImpl = webSocketImpl;
	}

	/**
	 * @return The webSocketUrl appended to the request.data when defined
	 */
	public String getWebSocketUrl()
	{
		return webSocketUrl;
	}

	/**
	 * The webSocketUrl appended to the request.data when defined. This is useful if the Atmosphere
	 * Server Side Component is using a custom implementation of WebSocketProtocol implementation
	 * and will be appended to WebSocket messages send to the server. The message will looks like:
	 * 
	 * <pre>
	 * data = webSocketPathDelimiter + webSocketUrl + webSocketPathDelimiter + data;
	 * </pre>
	 * 
	 * @param webSocketUrl
	 */
	public void setWebSocketUrl(String webSocketUrl)
	{
		this.webSocketUrl = webSocketUrl;
	}

	/**
	 * @return The token delimiter used to wrap request.data when websockets messages are sent. This
	 *         value is used with the webSocketUrl attribute.
	 */
	public String getWebSocketPathDelimiter()
	{
		return webSocketPathDelimiter;
	}

	/**
	 * The token delimiter used to wrap request.data when websockets messages are sent. This value
	 * is used with the webSocketUrl attribute.
	 * 
	 * @param webSocketPathDelimiter
	 */
	public void setWebSocketPathDelimiter(String webSocketPathDelimiter)
	{
		this.webSocketPathDelimiter = webSocketPathDelimiter;
	}

	/**
	 * @return Enable CORS Cross Origin Resource Sharing.
	 */
	public Boolean getEnableXDR()
	{
		return enableXDR;
	}

	/**
	 * Enable CORS Cross Origin Resource Sharing.
	 * 
	 * @param enableXDR
	 */
	public void setEnableXDR(Boolean enableXDR)
	{
		this.enableXDR = enableXDR;
	}

	/**
	 * 
	 * @return When enableXDR is set to true, the rewriteURL will be used to decide if the
	 *         JSESSION_ID cookie be send to the remote server.
	 */
	public Boolean getRewriteURL()
	{
		return rewriteURL;
	}

	/**
	 * When enableXDR is set to true, the rewriteURL will be used to decide if the JSESSION_ID
	 * cookie be send to the remote server.
	 * 
	 * @param rewriteURL
	 */
	public void setRewriteURL(Boolean rewriteURL)
	{
		this.rewriteURL = rewriteURL;
	}

	/**
	 * @return Pass all headers as query string.
	 */
	public Boolean getAttachHeadersAsQueryString()
	{
		return attachHeadersAsQueryString;
	}

	/**
	 * Pass all headers as query string. Some browser only support the GET method with some
	 * transport and prevent setting headers. As an example, the WebSocket API doesn't allow setting
	 * headers, and instead the headers will be passed as a query string.
	 * 
	 * @param attachHeadersAsQueryString
	 */
	public void setAttachHeadersAsQueryString(Boolean attachHeadersAsQueryString)
	{
		this.attachHeadersAsQueryString = attachHeadersAsQueryString;
	}

	/**
	 * @return Enable to drop the Atmosphere headers.
	 */
	public Boolean getDropAtmosphereHeaders()
	{
		return dropAtmosphereHeaders;
	}

	/**
	 * By default Atmosphere adds headers like X-Atmosphere-Transport, X-Cache-Date etc. used by the
	 * server to track the browser state. The same information is passed as a query string by
	 * default (attachHeadersAsQueryString) so if you aren't planning to add any extra headers, set
	 * that value to true and instead let the attachHeadersAsQueryString pass the same information.
	 * Setting that value to true also facilitate CORS requests handling because no extra headers
	 * are added.
	 * 
	 * @param dropAtmosphereHeaders
	 */
	public void setDropAtmosphereHeaders(Boolean dropAtmosphereHeaders)
	{
		this.dropAtmosphereHeaders = dropAtmosphereHeaders;
	}

	/**
	 * @return Execute the request's callback before or after reconnecting again to the server.
	 */
	public Boolean getExecuteCallbackBeforeReconnect()
	{
		return executeCallbackBeforeReconnect;
	}

	/**
	 * Execute the request's callback before or after reconnecting again to the server.
	 * 
	 * @param executeCallbackBeforeReconnect
	 */
	public void setExecuteCallbackBeforeReconnect(Boolean executeCallbackBeforeReconnect)
	{
		this.executeCallbackBeforeReconnect = executeCallbackBeforeReconnect;
	}

	/**
	 * @return True when user credentials are to be included in a cross-origin request. False when
	 *         they are to be excluded in a cross-origin request and when cookies are to be ignored
	 *         in its response.
	 */
	public Boolean getWithCredentials()
	{
		return withCredentials;
	}

	/**
	 * True when user credentials are to be included in a cross-origin request. False when they are
	 * to be excluded in a cross-origin request and when cookies are to be ignored in its response.
	 * 
	 * @param withCredentials
	 */
	public void setWithCredentials(Boolean withCredentials)
	{
		this.withCredentials = withCredentials;
	}

	/**
	 * @return Track the size of the received request.
	 */
	public Boolean getTrackMessageLength()
	{
		return trackMessageLength;
	}

	/**
	 * Track the size of the received request. This attribute must be used with the help of the
	 * Atmosphere's Server Side components called TrackMessageSizeFilter. When used, the server will
	 * use the following protocol when sending messages back to the client
	 * 
	 * <pre>
	 * {@literal
	 * <message-length><message-delimiter> message <message-delimiter>}
	 * </pre>
	 * 
	 * This attribute is useful when your server side component send large chunked message. Using
	 * the trackMessageLength, the client will make sure the message has been fully received before
	 * invoking the callback. If not set, the callback might be invoked with partial message.
	 * 
	 * @param trackMessageLength
	 */
	public void setTrackMessageLength(Boolean trackMessageLength)
	{
		this.trackMessageLength = trackMessageLength;
	}

	/**
	 * 
	 * @return The token that delimit the message when the trackMessageLength attribute is used.
	 */
	public String getMessageDelimiter()
	{
		return messageDelimiter;
	}

	/**
	 * The token that delimit the message when the trackMessageLength attribute is used.
	 * 
	 * @param messageDelimiter
	 */
	public void setMessageDelimiter(String messageDelimiter)
	{
		this.messageDelimiter = messageDelimiter;
	}

	/**
	 * @return When set to true, Atmospere will share a connection between different browser tabs
	 *         and windows. Otherwise, a new connection will be established for each tab/window.
	 */
	public Boolean getShared()
	{
		return shared;
	}

	/**
	 * When set to true, Atmospere will share a connection between different browser tabs and
	 * windows. Otherwise, a new connection will be established for each tab/window.
	 * 
	 * @param shared
	 */
	public void setShared(Boolean shared)
	{
		this.shared = shared;
	}

	/**
	 * @return *undocumented*
	 */
	public Boolean getReadResponsesHeaders()
	{
		return readResponsesHeaders;
	}

	/**
	 * *undocumented*
	 * 
	 * @param readResponsesHeaders
	 */
	public void setReadResponsesHeaders(Boolean readResponsesHeaders)
	{
		this.readResponsesHeaders = readResponsesHeaders;
	}

	/**
	 * 
	 * @return *undocumented*
	 */
	public Integer getMaxReconnectOnClose()
	{
		return maxReconnectOnClose;
	}

	/**
	 * *undocumented*
	 * 
	 * @param maxReconnectOnClose
	 */
	public void setMaxReconnectOnClose(Integer maxReconnectOnClose)
	{
		this.maxReconnectOnClose = maxReconnectOnClose;
	}

	/**
	 * Transforms the paramters into a JSON object.
	 * 
	 * @return A JSON object with all set paramters.
	 */
	public JSONObject toJSON()
	{
		try
		{
			JSONObject ret = new JSONObject();
			ret.put("connectTimeout", getConnectTimeout());
			ret.put("reconnectInterval", getReconnectInterval());
			ret.put("timeout", getTimeout());
			if (getMethod() != null)
				ret.put("method", getMethod().toString());
			if (getHeaders() != null)
				ret.put("headers", getHeaders());
			ret.put("contentType", getContentType());
			ret.put("data", getData());
			ret.put("suspend", getSuspend());
			ret.put("maxRequest", getMaxRequest());
			ret.put("maxStreamingLength", getMaxStreamingLength());
			if (getLogLevel() != null)
				ret.put("logLevel", getLogLevel().toString());
			if (getTransport() != null)
				ret.put("transport", getTransport().toString());
			if (getFallbackTransport() != null)
				ret.put("fallbackTransport", getFallbackTransport().toString());
			if (getFallbackMethod() != null)
				ret.put("fallbackMethod", getFallbackMethod().toString());
			ret.put("webSocketImpl", getWebSocketImpl());
			ret.put("webSocketUrl", getWebSocketUrl());
			ret.put("webSocketPathDelimiter", getWebSocketPathDelimiter());
			ret.put("enableXDR", getEnableXDR());
			ret.put("rewriteURL", getRewriteURL());
			ret.put("attachHeadersAsQueryString", getAttachHeadersAsQueryString());
			ret.put("dropAtmosphereHeaders", getDropAtmosphereHeaders());
			ret.put("executeCallbackBeforeReconnect", getExecuteCallbackBeforeReconnect());
			ret.put("withCredentials", getWithCredentials());
			ret.put("trackMessageLength", getTrackMessageLength());
			ret.put("messageDelimiter", getMessageDelimiter());
			ret.put("shared", getShared());
			ret.put("readResponsesHeaders", getReadResponsesHeaders());
			ret.put("maxReconnectOnClose", getMaxReconnectOnClose());
			return ret;
		}
		catch (JSONException e)
		{
			throw new WicketRuntimeException(e);
		}
	}
}
