/*
 * $Id: StringBufferResourceStream.java 3307 2005-11-30 15:57:34 -0800 (Wed, 30
 * Nov 2005) ivaynberg $ $Revision: 3307 $ $Date: 2005-11-30 15:57:34 -0800
 * (Wed, 30 Nov 2005) $
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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import wicket.util.time.Time;

/**
 * Provides the ability to 'decorate' the actual template contents before it is
 * contributed to the header. E.g. to embed in a javascript tag pair.
 * 
 * @author Eelco Hillenius
 */
public abstract class TextTemplateDecorator extends TextTemplate
{
	/**
	 * The decorated template.
	 */
	protected final TextTemplate decorated;

	/**
	 * Construct.
	 * 
	 * @param textTemplate
	 *            The text template to decorate
	 */
	public TextTemplateDecorator(TextTemplate textTemplate)
	{
		if (textTemplate == null)
		{
			throw new IllegalArgumentException("argument textTemplate must be not null");
		}

		this.decorated = textTemplate;
	}

	/**
	 * @return the contents decorated with {@link #getBeforeTemplateContents()}
	 *         and {@link #getAfterTemplateContents()}.
	 * @see wicket.util.resource.TextTemplate#asString()
	 */
	@Override
	public String asString()
	{
		StringBuffer b = new StringBuffer();
		b.append(getBeforeTemplateContents());
		b.append(decorated.asString());
		b.append(getAfterTemplateContents());
		return b.toString();
	}

	/**
	 * @return the contents decorated with {@link #getBeforeTemplateContents()}
	 *         and {@link #getAfterTemplateContents()}.
	 * @see wicket.util.resource.TextTemplate#asString(java.util.Map)
	 */
	@Override
	public String asString(Map variables)
	{
		StringBuffer b = new StringBuffer();
		b.append(getBeforeTemplateContents());
		b.append(decorated.asString(variables));
		b.append(getAfterTemplateContents());
		return b.toString();
	}

	/**
	 * Gets the string to put before the actual template contents, e.g.
	 * 
	 * <pre>
	 *      &lt;script type=&quot;text/javascript&quot;&gt;
	 * </pre>
	 * 
	 * @return The string to put before the actual template contents
	 */
	public abstract String getBeforeTemplateContents();

	/**
	 * Gets the string to put after the actual template contents, e.g.
	 * 
	 * <pre>
	 *      &lt;/script&gt;
	 * </pre>
	 * 
	 * @return The string to put after the actual template contents
	 */
	public abstract String getAfterTemplateContents();

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#close()
	 */
	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return decorated.equals(obj);
	}

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#getContentType()
	 */
	@Override
	public String getContentType()
	{
		return decorated.getContentType();
	}

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return decorated.getInputStream();
	}

	/**
	 * @see wicket.util.resource.AbstractResourceStream#getLocale()
	 */
	@Override
	public Locale getLocale()
	{
		return decorated.getLocale();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return decorated.hashCode();
	}

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#lastModifiedTime()
	 */
	@Override
	public Time lastModifiedTime()
	{
		return decorated.lastModifiedTime();
	}

	/**
	 * @see wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		return decorated.length();
	}

	/**
	 * @see wicket.util.resource.AbstractResourceStream#setCharset(java.nio.charset.Charset)
	 */
	@Override
	public void setCharset(Charset charset)
	{
		decorated.setCharset(charset);
	}

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#setLastModified(wicket.util.time.Time)
	 */
	@Override
	public void setLastModified(Time lastModified)
	{
		decorated.setLastModified(lastModified);
	}

	/**
	 * @see wicket.util.resource.AbstractResourceStream#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale locale)
	{
		decorated.setLocale(locale);
	}

	/**
	 * @see wicket.util.resource.TextTemplate#getString()
	 */
	@Override
	public String getString()
	{
		return decorated.getString();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return decorated.toString();
	}
}