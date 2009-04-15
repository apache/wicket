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
package org.apache.wicket.protocol.http.portlet;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

import org.apache.wicket.RequestContext;
import org.apache.wicket.settings.IRequestCycleSettings;

/**
 * Temporarily holds the current state of a Wicket response when invoked from WicketPortlet: buffer,
 * headers, state and the redirect location to be processed afterwards within WicketPortlet
 * 
 * @author Ate Douma
 */
public class WicketResponseState
{
	private static class CharArrayWriterBuffer extends CharArrayWriter
	{
		public char[] getBuffer()
		{
			return buf;
		}

		public int getCount()
		{
			return count;
		}
	}

	private final boolean isActionResponse;
	private final boolean isEventResponse;
	private final boolean isRenderResponse;
	private final boolean isResourceResponse;
	private final boolean isMimeResponse;
	private final boolean isStateAwareResponse;
	private final Locale defaultLocale;
	private final PortletResponse response;
	private boolean flushed;

	private ByteArrayOutputStream byteOutputBuffer;
	private CharArrayWriterBuffer charOutputBuffer;
	private ServletOutputStream outputStream;
	private PrintWriter printWriter;
	private HashMap<String, ArrayList<String>> headers;
	private ArrayList<Cookie> cookies;
	private boolean committed;
	private boolean hasStatus;
	private boolean hasError;
	private Locale locale;
	private boolean setContentTypeAfterEncoding;
	private boolean closed;
	private String characterEncoding;
	private int contentLength = -1;
	private String contentType;
	private int statusCode;

	/**
	 * FIXME javadoc
	 * 
	 * Stores the effective wicket url which is used by {@link WicketPortlet} in the view phase to
	 * request a render from wicket core.
	 * 
	 * @see IRequestCycleSettings#REDIRECT_TO_RENDER
	 * @see WicketFilterPortletHelper#initFilter
	 */
	private String redirectLocation;


	public WicketResponseState(PortletRequest request, PortletResponse response)
	{
		String lifecyclePhase = (String)request.getAttribute(PortletRequest.LIFECYCLE_PHASE);
		isActionResponse = PortletRequest.ACTION_PHASE.equals(lifecyclePhase);
		isEventResponse = PortletRequest.EVENT_PHASE.equals(lifecyclePhase);
		isRenderResponse = PortletRequest.RENDER_PHASE.equals(lifecyclePhase);
		isResourceResponse = PortletRequest.RESOURCE_PHASE.equals(lifecyclePhase);
		isStateAwareResponse = isActionResponse || isEventResponse;
		isMimeResponse = isRenderResponse || isResourceResponse;
		this.response = response;
		defaultLocale = isMimeResponse ? ((MimeResponse)response).getLocale() : null;
	}

	private ArrayList<String> getHeaderList(String name, boolean create)
	{
		if (headers == null)
		{
			headers = new HashMap<String, ArrayList<String>>();
		}
		ArrayList<String> headerList = headers.get(name);
		if (headerList == null && create)
		{
			headerList = new ArrayList<String>();
			headers.put(name, headerList);
		}
		return headerList;
	}

	private void failIfCommitted()
	{
		if (committed)
		{
			throw new IllegalStateException("Response is already committed");
		}
	}

	public boolean isActionResponse()
	{
		return isActionResponse;
	}

	public boolean isEventResponse()
	{
		return isEventResponse;
	}

	public boolean isRenderResponse()
	{
		return isRenderResponse;
	}

	public boolean isResourceResponse()
	{
		return isResourceResponse;
	}

	public boolean isMimeResponse()
	{
		return isMimeResponse;
	}

	public boolean isStateAwareResponse()
	{
		return isStateAwareResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie cookie)
	{
		if (!committed)
		{
			if (cookies == null)
			{
				cookies = new ArrayList<Cookie>();
			}
			cookies.add(cookie);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String name, long date)
	{
		addHeader(name, Long.toString(date));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String,
	 * java.lang.String)
	 */
	public void addHeader(String name, String value)
	{
		if (isMimeResponse && !committed)
		{
			getHeaderList(name, true).add(value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String name, int value)
	{
		addHeader(name, Integer.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String name)
	{
		// Note: Portlet Spec 2.0 demands this to always return false...
		return isMimeResponse && getHeaderList(name, false) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int, java.lang.String)
	 */
	public void sendError(int errorCode, String errorMessage) throws IOException
	{
		failIfCommitted();
		committed = true;
		closed = true;
		hasError = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int)
	 */
	public void sendError(int errorCode) throws IOException
	{
		sendError(errorCode, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String redirectLocation) throws IOException
	{
		if (isActionResponse || isMimeResponse)
		{
			failIfCommitted();
			closed = true;
			committed = true;

			if (isMimeResponse)
			{
				RequestContext rc = RequestContext.get();
				if (rc instanceof PortletRequestContext)
				{
					String wicketUrl = ((PortletRequestContext)rc).getLastEncodedPath(redirectLocation);
					if (wicketUrl != null)
					{
						redirectLocation = wicketUrl;
					}
					else
					{
						String contextPath = ((PortletRequestContext)rc).getPortletRequest()
							.getContextPath();
						if (redirectLocation.startsWith(contextPath + "/"))
						{
							redirectLocation = redirectLocation.substring(contextPath.length());
							if (redirectLocation.length() == 0)
							{
								redirectLocation = "/";
							}
						}
					}
				}
			}
			this.redirectLocation = redirectLocation;
		}
	}

	public String getRedirectLocation()
	{
		return redirectLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String name, long date)
	{
		setHeader(name, Long.toString(date));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String,
	 * java.lang.String)
	 */
	public void setHeader(String name, String value)
	{
		if (isMimeResponse && !committed)
		{
			ArrayList<String> headerList = getHeaderList(name, true);
			headerList.clear();
			headerList.add(value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String name, int value)
	{
		setHeader(name, Integer.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int, java.lang.String)
	 */
	public void setStatus(int statusCode, String message)
	{
		throw new UnsupportedOperationException("This method is deprecated and no longer supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
	 */
	public void setStatus(int statusCode)
	{
		if (!committed)
		{
			this.statusCode = statusCode;
			hasStatus = true;
			resetBuffer();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#flushBuffer()
	 */
	public void flushBuffer() throws IOException
	{
		if (isMimeResponse && !closed)
		{
			committed = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getBufferSize()
	 */
	public int getBufferSize()
	{
		return isMimeResponse ? Integer.MAX_VALUE : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getCharacterEncoding()
	 */
	public String getCharacterEncoding()
	{
		return isMimeResponse ? characterEncoding != null ? characterEncoding : "ISO-8859-1" : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getContentType()
	 */
	public String getContentType()
	{
		return isMimeResponse ? contentType : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getLocale()
	 */
	public Locale getLocale()
	{
		return isMimeResponse ? locale != null ? locale : defaultLocale : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException
	{
		if (isStateAwareResponse)
		{
			// Portlet Spec 2.0 requires Portlet Container to supply a "no-op" OutputStream object
			// so delegate back to current PortletServletResponseWrapper to return that one
			return null;
		}
		if (outputStream == null)
		{
			if (printWriter != null)
			{
				throw new IllegalStateException(
					"getWriter() has already been called on this response");
			}
			byteOutputBuffer = new ByteArrayOutputStream();
			outputStream = new ServletOutputStream()
			{
				@Override
				public void write(int b) throws IOException
				{
					if (!closed)
					{
						byteOutputBuffer.write(b);
						if (contentLength > -1 && byteOutputBuffer.size() >= contentLength)
						{
							committed = true;
							closed = true;
						}
					}
				}
			};
		}
		return outputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getWriter()
	 */
	public PrintWriter getWriter() throws IOException
	{
		if (isStateAwareResponse)
		{
			// Portlet Spec 2.0 requires Portlet Container to supply a "no-op" PrintWriter object
			// so delegate back to current PortletServletResponseWrapper to return that one
			return null;
		}
		if (printWriter == null)
		{
			if (outputStream != null)
			{
				throw new IllegalStateException(
					"getOutputStream() has already been called on this response");
			}
			charOutputBuffer = new CharArrayWriterBuffer();
			printWriter = new PrintWriter(charOutputBuffer);
		}
		return printWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#isCommitted()
	 */
	public boolean isCommitted()
	{
		return isMimeResponse && committed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#reset()
	 */
	public void reset()
	{
		resetBuffer(); // fails if committed
		headers = null;
		cookies = null;
		hasStatus = false;
		contentLength = -1;
		if (printWriter == null)
		{
			contentType = null;
			characterEncoding = null;
			locale = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#resetBuffer()
	 */
	public void resetBuffer()
	{
		failIfCommitted();
		if (outputStream != null)
		{
			try
			{
				outputStream.flush();
			}
			catch (Exception e)
			{
			}
			byteOutputBuffer.reset();
		}
		else if (printWriter != null)
		{
			printWriter.flush();
			charOutputBuffer.reset();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#setBufferSize(int)
	 */
	public void setBufferSize(int size)
	{
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String charset)
	{
		if (isResourceResponse && charset != null && !committed && printWriter == null)
		{
			characterEncoding = charset;
			setContentTypeAfterEncoding = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
	 */
	public void setContentLength(int len)
	{
		if (isResourceResponse && !committed && printWriter == null && len > 0)
		{
			contentLength = len;
			if (outputStream != null)
			{
				try
				{
					outputStream.flush();
				}
				catch (Exception e)
				{
				}
			}
			if (!closed && byteOutputBuffer != null && byteOutputBuffer.size() >= len)
			{
				committed = true;
				closed = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#setContentType(java.lang.String)
	 */
	public void setContentType(String type)
	{
		if (isMimeResponse && !committed)
		{
			contentType = type;
			setContentTypeAfterEncoding = false;
			if (printWriter == null)
			{
				// TODO: parse possible encoding for better return value from getCharacterEncoding()
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		if (isResourceResponse && !committed)
		{
			this.locale = locale;
		}
	}

	public void clear()
	{
		printWriter = null;
		byteOutputBuffer = null;
		charOutputBuffer = null;
		outputStream = null;
		printWriter = null;
		headers = null;
		cookies = null;
		committed = false;
		hasStatus = false;
		hasError = false;
		locale = null;
		setContentTypeAfterEncoding = false;
		closed = false;
		characterEncoding = null;
		contentLength = -1;
		contentType = null;
		statusCode = 0;
		redirectLocation = null;
	}

	public void flush() throws IOException
	{
		if (flushed)
		{
			throw new IllegalStateException("Already flushed");
		}
		flushed = true;

		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				response.addProperty(cookie);
			}
			cookies = null;
		}
		if (isMimeResponse)
		{
			MimeResponse mimeResponse = (MimeResponse)response;
			ResourceResponse resourceResponse = isResourceResponse ? (ResourceResponse)response
				: null;

			if (locale != null)
			{
				try
				{
					resourceResponse.setLocale(locale);
				}
				catch (UnsupportedOperationException usoe)
				{
					// TODO: temporary "fix" for JBoss Portal which doesn't yet support this
					// (although required by the Portlet API 2.0!)
				}
			}

			if (contentType != null)
			{
				if (characterEncoding != null)
				{
					if (setContentTypeAfterEncoding)
					{
						resourceResponse.setCharacterEncoding(characterEncoding);
						resourceResponse.setContentType(contentType);
					}
					else
					{
						resourceResponse.setContentType(contentType);
						resourceResponse.setCharacterEncoding(characterEncoding);
					}
				}
				else
				{
					mimeResponse.setContentType(contentType);
				}
			}
			else if (characterEncoding != null)
			{
				resourceResponse.setCharacterEncoding(characterEncoding);
			}

			if (headers != null)
			{
				for (Map.Entry<String, ArrayList<String>> entry : headers.entrySet())
				{
					for (String value : entry.getValue())
					{
						mimeResponse.addProperty(entry.getKey(), value);
					}
				}
				headers = null;
			}
			if (isResourceResponse && hasStatus)
			{
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
					Integer.toString(statusCode));
			}
			if (isResourceResponse && contentLength > -1)
			{
				try
				{
					resourceResponse.setContentLength(contentLength);
				}
				catch (UnsupportedOperationException usoe)
				{
					// TODO: temporary "fix" for JBoss Portal which doesn't yet support this
					// (although required by the Portlet API 2.0!)
				}
			}
			if (!hasError && redirectLocation == null)
			{
				if (outputStream != null)
				{
					if (!closed)
					{
						outputStream.flush();
					}
					OutputStream realOutputStream = mimeResponse.getPortletOutputStream();
					int len = byteOutputBuffer.size();
					if (contentLength > -1 && contentLength < len)
					{
						len = contentLength;
					}
					if (len > 0)
					{
						realOutputStream.write(byteOutputBuffer.toByteArray(), 0, len);
					}
					outputStream.close();
					outputStream = null;
					byteOutputBuffer = null;
				}
				else if (printWriter != null)
				{
					if (!closed)
					{
						printWriter.flush();
						if (charOutputBuffer.getCount() > 0)
						{
							mimeResponse.getWriter().write(charOutputBuffer.getBuffer(), 0,
								charOutputBuffer.getCount());
						}
						printWriter.close();

						printWriter = null;
						charOutputBuffer = null;
					}
				}
			}
		}
	}
}
