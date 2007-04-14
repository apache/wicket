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
package org.apache.wicket.util.resource;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.time.Time;


/**
 * An {@link IResourceStream} that reads data from a file in the web application
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebExternalResourceStream extends AbstractResourceStream
{
	InputStream in;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(WebExternalResourceStream.class);

	/** the relative url of the external resource. */
	private final String url;

	/**
	 * Construct.
	 * 
	 * @param url
	 *            the relative url of the external resource
	 */
	public WebExternalResourceStream(String url)
	{
		if (url == null)
		{
			throw new IllegalArgumentException("Argument url must be not null");
		}

		this.url = url;
	}

	/**
	 * Not implemented, throws {@link WicketRuntimeException}
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		throw new WicketRuntimeException("Not implemented");
	}

	public void close() throws IOException
	{
		in.close();
	}

	public Time lastModifiedTime()
	{
		return null;
	}

	public String getContentType()
	{
		return null;
	}

	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		final ServletContext context = ((WebApplication)RequestCycle.get().getApplication())
				.getServletContext();

		in = context.getResourceAsStream(url);
		if (in == null)
		{
			throw new WicketRuntimeException("the requested resource was not found");
		}
		return in;
	}
}
