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
package org.apache.wicket.util.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;


/**
 * Provides the ability to 'decorate' the actual template contents before it is contributed to the
 * header. For example, to embed it inside a JavaScript tag pair.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public abstract class TextTemplateDecorator extends TextTemplate
{
	private static final long serialVersionUID = 1L;

	/**
	 * The decorated <code>TextTemplate</code>.
	 */
	protected final TextTemplate decorated;

	/**
	 * Constructor.
	 * 
	 * @param textTemplate
	 *            a <code>TextTemplate</code> to decorate
	 */
	public TextTemplateDecorator(TextTemplate textTemplate)
	{
		Args.notNull(textTemplate, "textTemplate");

		decorated = textTemplate;
	}

	/**
	 * Returns the decorated contents as a <code>String</code>.
	 * 
	 * @return the contents decorated with {@link #getBeforeTemplateContents()} and
	 *         {@link #getAfterTemplateContents()}
	 * @see org.apache.wicket.util.template.TextTemplate#asString()
	 */
	@Override
	public String asString()
	{
		return asString(Collections.<String, Object> emptyMap());
	}

	/**
	 * Returns the decorated contents as a <code>String</code>.
	 * 
	 * @return the contents decorated with {@link #getBeforeTemplateContents()} and
	 *         {@link #getAfterTemplateContents()}.
	 * @see org.apache.wicket.util.template.TextTemplate#asString(java.util.Map)
	 */
	@Override
	public String asString(Map<String, ?> variables)
	{
		StringBuilder b = new StringBuilder();
		b.append(getBeforeTemplateContents());
		b.append(decorated.asString(variables));
		b.append(getAfterTemplateContents());
		return b.toString();
	}

	/**
	 * Retrieves the <code>String</code> to put before the actual template contents, for example:
	 * 
	 * <pre>
	 *    &lt;script type=&quot;text/javascript&quot;&gt;
	 * </pre>
	 * 
	 * @return the <code>String</code> to put before the actual template contents
	 */
	public abstract String getBeforeTemplateContents();

	/**
	 * Retrieves the <code>String</code> to put after the actual template contents, for example:
	 * 
	 * <pre>
	 *    &lt;/script&gt;
	 * </pre>
	 * 
	 * @return the <code>String</code> to put after the actual template contents
	 */
	public abstract String getAfterTemplateContents();

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public boolean equals(Object obj)
	{
		return decorated.equals(obj);
	}

	@Override
	public String getContentType()
	{
		return decorated.getContentType();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return decorated.getInputStream();
	}

	@Override
	public Locale getLocale()
	{
		return decorated.getLocale();
	}

	@Override
	public int hashCode()
	{
		return decorated.hashCode();
	}

	@Override
	public Instant lastModifiedTime()
	{
		return decorated.lastModifiedTime();
	}

	@Override
	public void setCharset(Charset charset)
	{
		decorated.setCharset(charset);
	}

	@Override
	public void setLastModified(Instant lastModified)
	{
		decorated.setLastModified(lastModified);
	}

	@Override
	public void setLocale(Locale locale)
	{
		decorated.setLocale(locale);
	}

	@Override
	public String getString()
	{
		return decorated.getString();
	}

	@Override
	public String toString()
	{
		return decorated.toString();
	}
}
