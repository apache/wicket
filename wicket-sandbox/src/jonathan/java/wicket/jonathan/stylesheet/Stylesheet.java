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
package wicket.jonathan.stylesheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import wicket.markup.html.WebResource;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * A stylesheet resource.
 * 
 * @author Jonathan Locke
 */
public class Stylesheet extends WebResource
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 209001445308790198L;

	/** Stylesheet information */
	private StringBuffer buffer = new StringBuffer();
	
	/** The last time this stylesheet was modified */
	private Time lastModified;

	/**
	 * Adds to this stylesheet
	 * 
	 * @param s
	 *            The string to add
	 */
	void append(final String s)
	{
		buffer.append(s);
		lastModified = Time.now();
	}
	
	/**
	 * @see WebResource#getResourceStream()
	 */
	protected IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			public String getContentType()
			{
				return "text/css";
			}

			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				final StringReader reader = new StringReader(buffer.toString());
				return new InputStream()
				{
					public int read() throws IOException
					{
						return reader.read();
					}
				};
			}

			public void close() throws IOException
			{
			}

			public Time lastModifiedTime()
			{
				return lastModified;
			}			
		};
	}
}
