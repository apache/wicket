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
package org.apache.wicket.request.handler;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * Request target that responds by sending its string property.
 * 
 * @author igor.vaynberg
 * @author Eelco Hillenius
 */
public class TextRequestHandler implements IRequestHandler
{
	/** the string for the response. */
	private final String string;

	/** content type for the string */
	private final String contentType;

	/** charset of the string */
	private final String encoding;


	/**
	 * Creates a string request target with content type <code>text/plain</code> and default charset
	 * (usually UTF-8)
	 * 
	 * @param string
	 *            the string for the response
	 */
	public TextRequestHandler(final String string)
	{
		this("text/plain", null, string);
	}

	/**
	 * Constructor
	 * 
	 * @param contentType
	 *            content type of the data the string represents, e.g.
	 *            <code>text/html; charset=utf-8</code>
	 * @param encoding
	 *            charset to use
	 * @param string
	 *            string for the response
	 */
	public TextRequestHandler(final String contentType, final String encoding, final String string)
	{
		this.contentType = Args.notEmpty(contentType, "contentType");
		this.string = Args.notNull(string, "string");
		this.encoding = encoding;
	}


	/**
	 * Responds by sending the string property.
	 * 
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(final IRequestCycle requestCycle)
	{
		String encoding = getEncoding(requestCycle);

		// Get servlet response to use when responding with resource
		final WebResponse response = (WebResponse)requestCycle.getResponse();
		response.setContentType(contentType + ";charset=" + encoding);

		// send string to client
		try
		{
			byte[] bytes = string.getBytes(encoding);
			response.setContentLength(bytes.length);
			response.write(bytes);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unable to render string: " + e.getMessage(), e);
		}
	}

	/**
	 * @param requestCycle
	 * @return the configured encoding or the request's one as default
	 */
	private String getEncoding(final IRequestCycle requestCycle)
	{
		String encoding = this.encoding;
		if (Strings.isEmpty(encoding))
		{
			Charset charset = requestCycle.getRequest().getCharset();
			if (charset != null)
			{
				encoding = charset.name();
			}
		}
		return encoding;
	}

	/** {@inheritDoc} */
	@Override
	public void detach(final IRequestCycle requestCycle)
	{
	}

	/**
	 * Gets the string property.
	 * 
	 * @return the string property
	 */
	public String getString()
	{
		return string;
	}

}
