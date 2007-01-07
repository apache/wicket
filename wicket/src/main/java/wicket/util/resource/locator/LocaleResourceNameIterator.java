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
package wicket.util.resource.locator;

import java.util.Locale;

import wicket.util.string.Strings;

/**
 * Contains the logic to build the various combinations of file path, style and
 * locale required while searching for Wicket resources. The full filename will
 * be built like: &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant.
 * Combinations of these components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class LocaleResourceNameIterator implements IWicketResourceNameIterator
{
	/** The base path */
	private final String path;

	/** The locale (see Session) */
	private final Locale locale;

	/** Internal state */
	private int state = 0;

	/**
	 * While iterating the various combinations, it will always contain the
	 * current combination used to create the path
	 */
	private Locale currentLocale;

	/** Internal: used to compare with previous path to avoid duplicates */
	private String currentPath;

	/**
	 * Construct.
	 * 
	 * @param path
	 * @param locale
	 * @param style
	 */
	public LocaleResourceNameIterator(final String path, final Locale locale)
	{
		this.path = path;
		this.locale = locale;
	}

	/**
	 * @see wicket.util.resource.locator.IWicketResourceNameIterator#getLocale()
	 */
	public Locale getLocale()
	{
		return currentLocale;
	}

	/**
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return (this.state < 4);
	}

	/**
	 * 
	 * @see java.util.Iterator#next()
	 */
	public String next()
	{
		if (locale == null)
		{
			state = 999;
			return path;
		}
		
		// 1. Apply Locale default toString() implementation. See Locale.
		if (state == 0)
		{
			this.state++;
			this.currentLocale = locale;
			this.currentPath = path + '_' + locale.toString();
			return this.currentPath;
		}

		// Get language and country, either of which may be the empty string
		final String language = locale.getLanguage();
		final String country = locale.getCountry();

		// 2. If country and language are available
		if (state == 1)
		{
			this.state++;

			if (!Strings.isEmpty(language) && !Strings.isEmpty(country))
			{
				this.currentLocale = new Locale(language, country);
				String newPath = path + '_' + language + '_' + country;
				if (this.currentPath.equals(newPath) == false)
				{
					return newPath;
				}
			}
		}

		// 3. If language is available
		if (state == 2)
		{
			this.state++;
			
			if (!Strings.isEmpty(language))
			{
				this.currentLocale = new Locale(language);
				return path + '_' + language;
			}
		}

		// 4. The path only; without locale
		this.state++;
		
		this.currentLocale = null;
		return path;
	}

	/**
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
	}
}
