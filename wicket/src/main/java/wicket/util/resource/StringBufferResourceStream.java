/*
 * $Id: StringBufferResourceStream.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat,
 * 20 May 2006) $
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

import wicket.util.string.AppendingStringBuffer;
import wicket.util.time.Time;

/**
 * A string resource that can be appended to.
 * 
 * @author Jonathan Locke
 */
public class StringBufferResourceStream extends AbstractStringResourceStream
{
	private static final long serialVersionUID = 1L;

	/** Stylesheet information */
	private AppendingStringBuffer buffer = new AppendingStringBuffer(128);

	/**
	 * Constructor.
	 */
	public StringBufferResourceStream()
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or
	 *            "text/html"
	 */
	public StringBufferResourceStream(final String contentType)
	{
		super(contentType);
	}

	/**
	 * Adds to this string buffer resource
	 * 
	 * @param s
	 *            The string to add
	 * @return this for chaining
	 */
	public StringBufferResourceStream append(final CharSequence s)
	{
		buffer.append(s);
		setLastModified(Time.now());
		return this;
	}

	/**
	 * Prepends to this string buffer resource
	 * 
	 * @param s
	 *            The string to prepend
	 * @return this for chaining
	 */
	public StringBufferResourceStream prepend(final CharSequence s)
	{
		buffer.insert(0, s);
		setLastModified(Time.now());
		return this;
	}

	/**
	 * Clears the string buffer resource.
	 * 
	 * @return this for chaining
	 */
	public StringBufferResourceStream clear()
	{
		buffer.delete(0, buffer.length());
		return this;
	}

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#getString()
	 */
	@Override
	protected String getString()
	{
		return buffer.toString();
	}

	/**
	 * @see wicket.util.resource.AbstractResourceStream#asString()
	 */
	@Override
	public String asString()
	{
		return getString();
	}

	/**
	 * @see wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		return buffer.length();
	}
}
