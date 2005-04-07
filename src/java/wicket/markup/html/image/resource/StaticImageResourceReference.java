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
package wicket.markup.html.image.resource;

import wicket.Resource;
import wicket.ResourceReference;

/**
 * A convenience class for creating resource references to static images.
 * 
 * @author Jonathan Locke
 */
public class StaticImageResourceReference extends ResourceReference
{
	/**
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public StaticImageResourceReference(Class scope, String name)
	{
		super(scope, name);
	}

	/**
	 * @see ResourceReference#ResourceReference(String)
	 */
	public StaticImageResourceReference(String name)
	{
		super(name);
	}
	
	/**
	 * @see wicket.ResourceReference#newResource()
	 */
	protected Resource newResource()
	{
		return StaticImageResource.get(getScope().getPackage(), getName());
	}
}

