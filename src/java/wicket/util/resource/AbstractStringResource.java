/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import wicket.util.time.Time;

/**
 * Base class for string resources.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractStringResource extends AbstractResource
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 209001445308790198L;

	/** Name of char set for encoding */
	private String charset = null;

	/** MIME content type */
	private final String contentType;
	
	/** The last time this stylesheet was modified */
	private Time lastModified = null;
	
	/**
	 * Constructor.
	 */
	public AbstractStringResource()
	{
		this("text");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param contentType The mime type of this resource, such as "image/jpeg" or
	 *         "text/html"
	 */
	public AbstractStringResource(final String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * @see wicket.util.resource.IResourceStream#close()
	 */
	public void close() throws IOException
	{
	}
	
	/**
	 * @see wicket.util.resource.IResource#getContentType()
	 */
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @see wicket.util.resource.IResourceStream#getInputStream()
	 */
	public InputStream getInputStream() throws ResourceNotFoundException
	{
		final byte[] bytes;
		if (charset != null)
		{
			try
			{
				bytes = getString().getBytes(charset);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new ResourceNotFoundException("Could not encode resource", e);
			}
		}
		else
		{
			bytes = getString().getBytes();
		}
		return new ByteArrayInputStream(bytes);
	}
	
	/**
	 * @see wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	public Time lastModifiedTime()
	{
		return lastModified;
	}	
	
	/**
	 * @param charset The charset to set.
	 */
	public void setCharset(String charset)
	{
		this.charset = charset;
	}
	
	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Time lastModified)
	{
		this.lastModified = lastModified;
	}

	/** 
	 * @return The string resource
	 */
	protected abstract String getString();
}
