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
package wicket.protocol.http.portlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import wicket.protocol.http.MockHttpServletResponse;

/**
 * 
 * Mock implementation of portlet RenderResponse
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public class MockPortletRenderResponse extends MockPortletResponse implements RenderResponse
{

	/**
	 * Construct.
	 * @param resp
	 */
	public MockPortletRenderResponse(MockHttpServletResponse resp)
	{
		super(resp);
	}

	public PortletURL createActionURL()
	{
		return new MockPortletURL(false);
	}

	public PortletURL createRenderURL()
	{
		return new MockPortletURL(true);
	}

	public void flushBuffer() throws IOException
	{
		resp.flushBuffer();
	}

	public int getBufferSize()
	{
		return resp.getBufferSize();
	}

	public String getCharacterEncoding()
	{
		return resp.getCharacterEncoding();
	}

	public String getContentType()
	{
		return "text/html";
	}

	public Locale getLocale()
	{
		return resp.getLocale();
	}

	public String getNamespace()
	{
		return null;
	}

	public OutputStream getPortletOutputStream() throws IOException
	{
		return resp.getOutputStream();
	}

	public PrintWriter getWriter() throws IOException
	{
		return resp.getWriter();
	}

	public boolean isCommitted()
	{
		return resp.isCommitted();
	}

	public void reset()
	{
		resp.reset();
	}

	public void resetBuffer()
	{
		resp.resetBuffer();
	}

	public void setBufferSize(int bufferSize)
	{
		resp.setBufferSize(bufferSize);
	}

	public void setContentType(String contentType)
	{
		resp.setContentType(contentType);
	}

	public void setTitle(String title)
	{
	}

}
