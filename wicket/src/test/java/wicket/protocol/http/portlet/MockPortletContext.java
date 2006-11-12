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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequestDispatcher;
import javax.servlet.ServletContext;

/**
 * Mock implementation of PortletContext
 * 
 * @author Janne Hietam&auml;ki
 */
public class MockPortletContext implements PortletContext
{
	
	ServletContext ctx;
	
	/**
	 * Construct.
	 * @param ctx
	 */
	public MockPortletContext(ServletContext ctx){
		this.ctx=ctx;
	}

	public Object getAttribute(String key)
	{
		return ctx.getAttribute(key);
	}

	public Enumeration getAttributeNames()
	{
		return ctx.getAttributeNames();
	}

	public String getInitParameter(String key)
	{
		return ctx.getInitParameter(key);
	}

	public Enumeration getInitParameterNames()
	{
		return ctx.getInitParameterNames();
	}

	public int getMajorVersion()
	{
		return 1;
	}

	public String getMimeType(String key)
	{
		return ctx.getMimeType(key);
	}

	public int getMinorVersion()
	{
		return 0;
	}

	public PortletRequestDispatcher getNamedDispatcher(String key)
	{
		return null;
	}

	public String getPortletContextName()
	{
		return "mock";
	}

	public String getRealPath(String path)
	{
		return ctx.getRealPath(path);
	}

	public PortletRequestDispatcher getRequestDispatcher(String key)
	{
		//return new ctx.getRequestDispatcher(key);
		return null;
	}

	public URL getResource(String name) throws MalformedURLException
	{
		return ctx.getResource(name);
	}

	public InputStream getResourceAsStream(String path)
	{
		return ctx.getResourceAsStream(path);
	}

	public Set getResourcePaths(String name)
	{
		return ctx.getResourcePaths(name);
	}

	public String getServerInfo()
	{
		return ctx.getServerInfo();
	}

	public void log(String message)
	{
		ctx.log(message);
	}

	public void log(String message, Throwable throwable)
	{
		ctx.log(message, throwable);
	}

	public void removeAttribute(String key)
	{
		ctx.removeAttribute(key);
	}

	public void setAttribute(String key, Object value)
	{
		ctx.setAttribute(key,value);
	}
}