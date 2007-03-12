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
	 * @see wicket.Response#write(CharSequence)
	 */
	public void write(final CharSequence string)
	{
		out.append(string);
	}

	/**
	 * @see wicket.Response#reset()
	 */
	public void reset()
	{
		out.clear();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
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
	public OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException("Cannot get output stream on StringResponse");
	}
}
