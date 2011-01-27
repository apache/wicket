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
package org.apache.wicket.extensions.ajax.markup.html.form.upload;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileUploadException;

/**
 * A request object that stores information about the current upload in session so it is accessible
 * to the {@link UploadProgressBar}.
 * <p>
 * This request object is necessary for the UploadProgressBar to function properly. It is installed
 * like so:
 * 
 * <code>
 * class MyApplication extends WebApplication {
 * ...
 * 
 * @Override protected WebRequest newWebRequest(HttpServletRequest servletRequest, String
 *           filterPath) { return new UploadWebRequest(servletRequest, filterPath); } ... } </code>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class UploadWebRequest extends ServletWebRequest
{

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * @param filterPrefix
	 */
	public UploadWebRequest(final HttpServletRequest httpServletRequest, final String filterPrefix)
	{
		this(httpServletRequest, filterPrefix, null);
	}

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * @param filterPrefix
	 * @param url
	 */
	public UploadWebRequest(final HttpServletRequest httpServletRequest, final String filterPrefix,
		final Url url)
	{
		super(httpServletRequest, filterPrefix, url);
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.ServletWebRequest#newMultipartWebRequest(org.apache.wicket.util.lang.Bytes)
	 */
	@Override
	public MultipartServletWebRequest newMultipartWebRequest(final Bytes maxsize)
		throws FileUploadException
	{
		return new MultipartRequest(getContainerRequest(), getFilterPrefix(), maxsize);
	}

	private static final String SESSION_KEY = UploadWebRequest.class.getName();

	/**
	 * Retrieves {@link UploadInfo} from session, null if not found
	 * 
	 * @param req
	 * @return {@link UploadInfo} object from session, or null if not found
	 */
	static UploadInfo getUploadInfo(final HttpServletRequest req)
	{
		if (req == null)
		{
			throw new IllegalArgumentException("req cannot be null");
		}
		return (UploadInfo)req.getSession().getAttribute(SESSION_KEY);
	}

	/**
	 * Sets the {@link UploadInfo} object into session
	 * 
	 * @param req
	 * @param uploadInfo
	 */
	static void setUploadInfo(final HttpServletRequest req, final UploadInfo uploadInfo)
	{
		if (req == null)
		{
			throw new IllegalArgumentException("req cannot be null");
		}
		if (uploadInfo == null)
		{
			throw new IllegalArgumentException("uploadInfo cannot be null");
		}
		req.getSession().setAttribute(SESSION_KEY, uploadInfo);
	}

	/**
	 * Clears the {@link UploadInfo} object from session if one exists
	 * 
	 * @param req
	 */
	static void clearUploadInfo(final HttpServletRequest req)
	{
		if (req == null)
		{
			throw new IllegalArgumentException("req cannot be null");
		}
		req.getSession().removeAttribute(SESSION_KEY);
	}
}