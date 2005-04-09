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
package wicket.markup.html.link;

import wicket.ResourceReference;

/**
 * A link to any ResourceReference.
 * 
 * @author Jonathan Locke
 */
public class ResourceLink extends Link
{
	/** Resource reference */
	private final ResourceReference resourceReference;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 */
	public ResourceLink(final String id, final ResourceReference resourceReference)
	{
		super(id);
		this.resourceReference = resourceReference;
	}

	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
	}
	
	/**
	 * @see wicket.markup.html.link.Link#getURL()
	 */
	protected String getURL()
	{
		return getPage().urlFor(resourceReference.getPath());
	}
}
