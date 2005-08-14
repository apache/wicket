/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import wicket.WicketRuntimeException;
import wicket.util.io.Streams;

/**
 * @see wicket.util.resource.IResourceStream
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractResourceStream implements IStringResourceStream
{
	/** Charset for resource */
	private Charset charset;

	/**
	 * Sets the character set used for reading this resource.
	 * 
	 * @param charset
	 *            Charset for component
	 */
	public void setCharset(final Charset charset)
	{
		this.charset = charset;
	}

	/**
	 * @return This resource as a String.
	 */
	public String asString()
	{
		try
		{
			if (charset == null)
			{
				return Streams.readString(new InputStreamReader(getInputStream()));
			}
			else
			{
				return Streams.readString(new InputStreamReader(getInputStream(), charset));
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to read resource as String", e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new WicketRuntimeException("Unable to read resource as String", e);
		}
	}

	/**
	 * @return Charset for resource
	 */
	protected Charset getCharset()
	{
		return charset;
	}
}
