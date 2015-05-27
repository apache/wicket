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
package org.apache.wicket.core.util.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.io.Connections;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link org.apache.wicket.util.resource.IResourceStream} that reads data from a file in the web application
 *
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebExternalResourceStream extends AbstractResourceStream
{
	private static final Logger log = LoggerFactory.getLogger(WebExternalResourceStream.class);
	private static final long serialVersionUID = 1L;

	transient InputStream in;

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

	@Override
	public Bytes length()
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{
		// getInputStream() is not always called (WICKET-790)
		IOUtils.close(in);
	}

	@Override
	public Time lastModifiedTime()
	{
		try
		{
			final ServletContext context = ((WebApplication)Application.get()).getServletContext();
			final URL resourceURL = context.getResource(url);
			if (resourceURL == null)
			{
				throw new FileNotFoundException("Unable to find resource '" + url +
					"' in the servlet context");
			}

			return Connections.getLastModified(resourceURL);
		}
		catch (IOException e)
		{
			log.warn("failed to retrieve last modified timestamp", e);
			return null;
		}
	}

	@Override
	public String getContentType()
	{
		return WebApplication.get().getServletContext().getMimeType(url);
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		final ServletContext context = ((WebApplication)Application.get()).getServletContext();

		in = context.getResourceAsStream(url);
		if (in == null)
		{
			throw new ResourceStreamNotFoundException("The requested resource was not found: " +
				url);
		}
		return in;
	}
}
