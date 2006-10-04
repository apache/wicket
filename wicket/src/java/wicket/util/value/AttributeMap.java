/*
 * $Id: AttributeMap.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 22:46:21 +0000 (Thu, 25 May
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
package wicket.util.value;

import java.util.Map;

/**
 * ValueMap for attributes.
 * 
 * @author Eelco Hillenius
 * 
 * @deprecated since 2.0 use ValueMap instead
 */
public final class AttributeMap extends ValueMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty map.
	 */
	public AttributeMap()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            Map to be copied
	 */
	public AttributeMap(Map<String, Object> map)
	{
		super(map);
	}
}
