/*
 * $Id: TextTemplateLink.java 3307 2005-11-30 15:57:34 -0800 (Wed, 30 Nov 2005)
 * ivaynberg $ $Revision: 3307 $ $Date: 2005-11-30 15:57:34 -0800 (Wed, 30 Nov
 * 2005) $
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

import java.util.Map;

import wicket.MarkupContainer;
import wicket.markup.html.link.ResourceLink;

/**
 * Links to shared, interpolated text template resources created by a factory.
 * This is useful for creating dynamic JNLP descriptors, among other things.
 * 
 * @author Jonathan Locke
 */
public class TextTemplateLink extends ResourceLink
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            Component id
	 * @param factory
	 *            The factory to create resources with
	 * @param variables
	 *            Variables to interpolate into the template held by the shared
	 *            resource factory
	 */
	public TextTemplateLink(final MarkupContainer parent, final String id,
			final TextTemplateSharedResourceFactory factory, final Map variables)
	{
		super(parent, id, factory.resourceReference(variables));
	}
}
