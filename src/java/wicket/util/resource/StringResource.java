/*
 * $Id$ $Revision:
 * 1.9 $ $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.time.Time;

/**
 * A StringResource is an IResource implementation for strings.
 * 
 * @see wicket.util.resource.IResource
 * @see wicket.util.resource.IResourceStream
 * @see wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public final class StringResource extends AbstractResource
{
	/** Logging */
	private static Log log = LogFactory.getLog(StringResource.class);

	/** resource data */
	private final CharSequence string;

	/** content data mime type */
	private final String contentType;

	/** Current time */
	private Time modificationTime = Time.now();

	/**
	 * Construct. Content type defaults to "text/plain".
	 * <p>
	 * Note: Class String is derived from CharSequence. Thus you can easily
	 * create a StringResource like
	 * 
	 * <pre>
	 * new StringResource(&quot;&lt;html&gt;....&quot;);
	 * </pre>
	 * 
	 * @param string
	 *            The resource string
	 */
	public StringResource(final CharSequence string)
	{
		this(string, "text/plain");
	}

	/**
	 * Construct.
	 * 
	 * @param string
	 *            The resource string
	 * @param contentType
	 *            The mime type of this resource, such as "text/html" or
	 *            "text/xml"
	 */
	public StringResource(final CharSequence string, final String contentType)
	{
		this.string = string;
		this.contentType = contentType;
	}

	/**
	 * Closes this resource.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
	}

	/**
	 * @return The MIME type of this resource, such as "text/plain" or "text/html"
	 */
	public String getContentType()
	{
		return this.contentType.toString();
	}

	/**
	 * @return A readable input stream for this resource.
	 * @throws ResourceNotFoundException
	 */
	public InputStream getInputStream() throws ResourceNotFoundException
	{
		return new ByteArrayInputStream(string.toString().getBytes());
	}

	/**
	 * @see wicket.util.watch.IModifiable#lastModifiedTime()
	 * @return last modification time. Default: object creation time
	 */
	public Time lastModifiedTime()
	{
		return this.modificationTime;
	}

	/**
	 * Assuming you read the string resource from a database, you might as well
	 * use a database field to determine the strings last modifcation time.
	 * 
	 * @param time
	 *            The time the string was modified
	 */
	public void setModifiedTime(final Time time)
	{
		this.modificationTime = time;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return string.toString();
	}
}
