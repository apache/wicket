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
package org.apache._wicket.request.response;

import javax.servlet.http.Cookie;

import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;


/**
 * Base class for web-related responses.
 * 
 * @author Matej Knopp
 */
public abstract class WebResponse extends Response
{
	/**
	 * Add a cookie to the web response
	 * 
	 * @param cookie
	 */
	public abstract void addCookie(final Cookie cookie);

	/**
	 * Convenience method for clearing a cookie.
	 * 
	 * @param cookie
	 *            The cookie to set
	 * @see WebResponse#addCookie(Cookie)
	 */
	public abstract void clearCookie(final Cookie cookie);

	/**
	 * Set a header to the string value in the servlet response stream.
	 * 
	 * @param name
	 * @param value
	 */
	public abstract void setHeader(String name, String value);

	/**
	 * Set a header to the date value in the servlet response stream.
	 * 
	 * @param name
	 * @param date
	 */
	public abstract void setDateHeader(String name, long date);

	/**
	 * Set the content length on the response, if appropriate in the subclass. This default
	 * implementation does nothing.
	 * 
	 * @param length
	 *            The length of the content
	 */
	public abstract void setContentLength(final long length);

	/**
	 * Set the content type on the response, if appropriate in the subclass. This default
	 * implementation does nothing.
	 * 
	 * @param mimeType
	 *            The mime type
	 */
	public abstract void setContentType(final String mimeType);

	/**
	 * Set the contents last modified time, if appropriate in the subclass. This default
	 * implementation does nothing.
	 * 
	 * @param time
	 *            The time object
	 */
	public void setLastModifiedTime(Time time)
	{
		setDateHeader("Last-Modified", time.getMilliseconds());
	}

	/**
	 * Convenience method for setting the content-disposition:attachment header. This header is used
	 * if the response should prompt the user to download it as a file instead of opening in a
	 * browser.
	 * 
	 * @param filename
	 *            file name of the attachment
	 */
	public void setAttachmentHeader(String filename)
	{
		setHeader("Content-Disposition", "attachment" +
			((!Strings.isEmpty(filename)) ? ("; filename=\"" + filename + "\"") : ""));
	}

	/**
	 * Convenience method for setting the content-disposition:in;ine header. This header is used if
	 * the response should be shown embedded in browser window while having custom file name when
	 * user saves the response. browser.
	 * 
	 * @param filename
	 *            file name of the attachment
	 */
	public void setInlineHeader(String filename)
	{
		setHeader("Content-Disposition", "inline" +
			((!Strings.isEmpty(filename)) ? ("; filename=\"" + filename + "\"") : ""));
	}

	/**
     * Sets the status code for this response.
     * 
     * @param sc status code 
     */  
	public abstract void setStatus(int sc);
	
	
	/**
	 * Redirects the response to specified URL. The implementation is responsible for properly
	 * encoding the URL.
	 * 
	 * @param url
	 */
	protected abstract void sendRedirect(String url);
}
