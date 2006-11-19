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
package wicket.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;

import wicket.util.watch.IModifiable;

/**
 * Interface to a streamed resource. The resource stream can be retrieved by
 * calling getInputStream(), but the resource should later be closed by calling
 * close() on the IResourceStream (as opposed to calling close on the
 * InputStream returned by getInputStream()).
 * <p>
 * Once a resource has been closed with a call to close(), it is normally
 * possible to call getInputStream() again to retrieve a new input stream on the
 * same resource.
 * <p>
 * Implementations of this interface are typically unsafe for use from multiple
 * threads.
 * 
 * @author Jonathan Locke
 */
public interface IResourceStream extends IModifiable, Serializable
{
	/**
	 * Gets the mime type of this resource
	 * 
	 * @return The mime type of this resource, such as "image/jpeg" or
	 *         "text/html"
	 */
	String getContentType();

	/**
	 * Gets the size of this resource
	 * 
	 * @return The size of this resource in the number of bytes
	 */
	long length();

	/**
	 * Gets the resource stream. You should not directly close this stream.
	 * Instead call the close() method on IResourceStream.
	 * 
	 * @see IResourceStream#close()
	 * @return Returns the inputStream.
	 * @throws ResourceStreamNotFoundException
	 */
	InputStream getInputStream() throws ResourceStreamNotFoundException;

	/**
	 * Closes the resource. Normally, this includes closing any underlying input
	 * stream returned by getInputStream().
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * @return The Locale where this stream did resolve to
	 */
	Locale getLocale();

	/**
	 * This method shouldn't be used for the outside, It is used by the Loaders
	 * to set the resolved locale.
	 * 
	 * @param locale
	 *            The Locale where this stream did resolve to.
	 */
	void setLocale(Locale locale);
}
