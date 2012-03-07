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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.watch.IModifiable;


/**
 * Interface to a streamed resource. The resource stream can be retrieved by calling
 * getInputStream(), but the resource should later be closed by calling close() on the
 * IResourceStream (as opposed to calling close on the InputStream returned by getInputStream()).
 * <p>
 * Once a resource has been closed with a call to close(), it is normally possible to call
 * getInputStream() again to retrieve a new input stream on the same resource.
 * <p>
 * Implementations of this interface are typically unsafe for use from multiple threads.
 * 
 * @author Jonathan Locke
 */
public interface IResourceStream extends IModifiable, IClusterable, Closeable
{
	/**
	 * Gets the mime type of this resource
	 * 
	 * @return The mime type of this resource, such as "image/jpeg" or "text/html". Return null to
	 *         let ResourceStreamRequestHandler handle the Content-Type automatically
	 */
	String getContentType();

	/**
	 * Gets the size of this resource
	 * 
	 * @return The size of this resource in the number of bytes, or <code>null</code> if unknown
	 */
	Bytes length();

	/**
	 * Gets the resource stream. You should not directly close this stream. Instead call the close()
	 * method on IResourceStream.
	 * 
	 * @see IResourceStream#close()
	 * @return Returns the inputStream.
	 * @throws ResourceStreamNotFoundException
	 */
	InputStream getInputStream() throws ResourceStreamNotFoundException;

	/**
	 * Closes the resource. Normally, this includes closing any underlying input stream returned by
	 * getInputStream().
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * @return The Locale where this stream did resolve to
	 */
	Locale getLocale();

	/**
	 * This method shouldn't be used from the outside. It is used by the Loaders to set the resolved
	 * locale.
	 * 
	 * @param locale
	 *            The Locale where this stream did resolve to.
	 */
	void setLocale(Locale locale);

	/**
	 * @return The Style where this stream did resolve to
	 */
	String getStyle();

	/**
	 * This method shouldn't be used from the outside. It is used by the Loaders to set the resolved
	 * Style.
	 * 
	 * @param style
	 *            The style where this stream did resolve to.
	 */
	void setStyle(String style);

	/**
	 * @return The Variation where this stream did resolve to
	 */
	String getVariation();

	/**
	 * This method shouldn't be used from the outside. It is used by the Loaders to set the resolved
	 * variation.
	 * 
	 * @param variation
	 *            The Variation where this stream did resolve to.
	 */
	void setVariation(String variation);
}
