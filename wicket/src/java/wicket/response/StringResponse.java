/*
 * $Id: StringResponse.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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
package wicket.response;

import java.io.OutputStream;

import wicket.Response;
import wicket.util.string.AppendingStringBuffer;

/**
 * Response object that writes to a StringWriter. If the StringResponse is later
 * converted to a String via toString(), the output which was written to the
 * StringResponse will be returned as a String.
 * 
 * @author Jonathan Locke
 */
public class StringResponse extends Response
{
	/** StringWriter to write to */
	protected final AppendingStringBuffer out;

	/**
	 * Constructor
	 */
	public StringResponse()
	{
		this.out = new AppendingStringBuffer(128);
	}

	/**
	 * @see wicket.Response#reset()
	 */
	@Override
	public void reset()
	{
		out.clear();
	}
	
	/**
	 * @see wicket.Response#write(CharSequence)
	 */
	@Override
	public void write(final CharSequence string)
	{
		out.append(string);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return out.toString();
	}

	/**
	 * @return The internal buffer directly as a {@link CharSequence}
	 */
	public CharSequence getBuffer()
	{
		return out;
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException("Cannot get output stream on StringResponse");
	}
}
