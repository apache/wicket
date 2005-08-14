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
package wicket.util.parse.metapattern;

import java.util.List;

/**
 * Makes any MetaPattern optional by enclosing the pattern in an optionality
 * expression.	The expression will be something equivalent to "(?:&lt;pattern&gt;)?".
 * 
 * @author Jonathan Locke
 */
public final class OptionalMetaPattern extends MetaPattern
{
	/**
	 * Constructor
	 * 
	 * @param pattern
	 */
	public OptionalMetaPattern(final String pattern)
	{
		super(pattern);
	}

	/**
	 * Constructor
	 * 
	 * @param pattern
	 *			  MetaPattern to make optional
	 */
	public OptionalMetaPattern(final MetaPattern pattern)
	{
		super(pattern);
	}

	/**
	 * Constructor
	 * 
	 * @param patterns
	 */
	public OptionalMetaPattern(final List patterns)
	{
		super(patterns);
	}

	/**
	 * Constructor
	 * 
	 * @param patterns
	 */
	public OptionalMetaPattern(final MetaPattern[] patterns)
	{
		super(patterns);
	}

	/**
	 * @return String representation of this pattern
	 */
	public String toString()
	{
		return "(?:" + super.toString() + ")?";
	}
}
