/*
 * $Id: ComponentStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat,
 * 20 May 2006) $
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
package wicket.resource;

import wicket.util.value.ValueMap;

/**
 * Reloadable properties. It is not a 100% replacement for java.util.Properties
 * as it does not provide the same interface. But is serves kind of the same
 * purpose with Wicket specific features.
 * 
 * @author Juergen Donnerstag
 */
public final class Properties
{
	/** Log. */
	// private static final Log log = LogFactory.getLog(Properties.class);
	/** The resource key for the properties file */
	private final String key;

	/** Property values */
	private final ValueMap strings;

	/**
	 * Construct
	 * 
	 * @param key
	 *            The key
	 * @param strings
	 *            Properties values
	 */
	public Properties(final String key, final ValueMap strings)
	{
		this.key = key;
		this.strings = strings;
	}

	/**
	 * Get all values from the properties file
	 * 
	 * @return map
	 */
	public final ValueMap getAll()
	{
		return strings;
	}

	/**
	 * Get the property message identified by 'key'
	 * 
	 * @param key
	 * @return property message
	 */
	public final String getString(final String key)
	{
		return strings.getString(key);
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return this.key;
	}
}