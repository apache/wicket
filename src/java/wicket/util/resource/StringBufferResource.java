/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import wicket.util.time.Time;

/**
 * A string resource that can be appended to.
 * 
 * @author Jonathan Locke
 */
public class StringBufferResource extends AbstractStringResource
{
	/** Stylesheet information */
	private StringBuffer buffer = new StringBuffer();

	/**
	 * Constructor.
	 */
	public StringBufferResource()
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or
	 *            "text/html"
	 */
	public StringBufferResource(final String contentType)
	{
		super(contentType);
	}

	/**
	 * Adds to this string buffer resource
	 * 
	 * @param s
	 *            The string to add
	 */
	public void append(final String s)
	{
		buffer.append(s);
		setLastModified(Time.now());
	}

	/**
	 * Prepends to this string buffer resource
	 * 
	 * @param s
	 *            The string to prepend
	 */
	public void prepend(final String s)
	{
		buffer.insert(0, s);
		setLastModified(Time.now());
	}

	/**
	 * Clears the string buffer resource.
	 */
	public void clear()
	{
		buffer.delete(0, buffer.length());
	}

	/**
	 * @see wicket.util.resource.AbstractStringResource#getString()
	 */
	protected String getString()
	{
		return buffer.toString();
	}
	
	/**
	 * @see wicket.util.resource.AbstractResource#asString()
	 */
	public String asString()
	{
		return getString();
	}
}
