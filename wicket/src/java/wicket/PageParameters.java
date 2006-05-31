/*
 * $Id: PageParameters.java 5377 2006-04-14 14:30:26 -0700 (Fri, 14 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-14 14:30:26 -0700 (Fri, 14 Apr
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
package wicket;

import java.util.Map;

import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.value.ValueMap;

/**
 * A typesafe abstraction and container for parameters to a requested page. Page
 * parameters in HTTP are query string values in the request URL. In other
 * protocols, the parameters to a page might come from some other source.
 * <p>
 * Pages which take a PageParameters object as an argument to their constructor
 * can be accessed directly from a URL and are known as "bookmarkable" pages
 * since the URL is stable across sessions and can be stored in a browser's
 * bookmark database.
 * 
 * @author Jonathan Locke
 */
public final class PageParameters extends ValueMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * Null value for page parameters
	 */
	public static final PageParameters NULL = new PageParameters();

	/**
	 * Constructor
	 */
	public PageParameters()
	{
	}

	/**
	 * Copy constructor.
	 * 
	 * @param parameterMap
	 *            The map to copy
	 * @see ValueMap#ValueMap(java.util.Map)
	 */
	public PageParameters(final Map<? extends String, ? extends Object> parameterMap)
	{
		super(parameterMap);
	}

	/**
	 * Construct.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by commas. For example,
	 *            "param1=foo,param2=bar"
	 * @see ValueMap#ValueMap(String)
	 */
	public PageParameters(final String keyValuePairs)
	{
		this(keyValuePairs, ",");
	}

	/**
	 * Construct.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by commas. For example,
	 *            "param1=foo,param2=bar"
	 * @param delimiter
	 *            Delimiter string used to separate key/value pairs
	 * @see ValueMap#ValueMap(String)
	 */
	public PageParameters(final String keyValuePairs, final String delimiter)
	{
		super();

		// We can not use ValueMaps constructor as it uses
		// VariableAssignmentParser which is more suitable for markup
		// attributes, rather than URL parameters. URL param keys for
		// examples are allowed to start with a digit (e.g. 0=xxx)
		// and quotes are not "quotes".

		// Get list of strings separated by the delimiter
		final StringList pairs = StringList.tokenize(keyValuePairs, delimiter);

		// Go through each string in the list
		for (IStringIterator iterator = pairs.iterator(); iterator.hasNext();)
		{
			// Get the next key value pair
			final String pair = iterator.next();

			final int pos = pair.indexOf('=');
			if (pos == 0)
			{
				throw new IllegalArgumentException("URL parameter is missing the lvalue: " + pair);
			}
			else if (pos != -1)
			{
				final String key = pair.substring(0, pos).trim();
				final String value = pair.substring(pos + 1).trim();

				put(key, value);
			}
			else
			{
				final String key = pair.trim();
				final String value = null;

				put(key, value);
			}
		}
	}
}
