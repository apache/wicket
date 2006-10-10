/*
 * $Id: NullResponse.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
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

import java.io.IOException;
import java.io.OutputStream;

import wicket.Response;

/**
 * Response implementation that discards all output.
 * 
 * @author Jonathan Locke
 */
public class NullResponse extends Response
{
	/** The one and only instance of NullResponse */
	private static final NullResponse instance = new NullResponse();

	/**
	 * Private constructor to force use of static factory method.
	 */
	private NullResponse()
	{
	}

	/**
	 * @return The one and only instance of NullResponse
	 */
	public static final NullResponse getInstance()
	{
		return instance;
	}

	/**
	 * @see wicket.Response#write(CharSequence)
	 */
	@Override
	public void write(CharSequence string)
	{
		// Does nothing
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream()
	{
		return new OutputStream()
		{
			@Override
			public void write(int b) throws IOException
			{
			}
		};
	}
}
