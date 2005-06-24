/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.extensions.markup.html.resources;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.ResourceReference;
import wicket.markup.html.StaticResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Base class for components that render references to packaged resources.
 *
 * @author Eelco Hillenius
 */
public class PackagedResourceReference extends WebMarkupContainer
{
	/**
	 * Construct.
	 * @param id component id
	 * @param referer the class that is refering; is used as the relative
	 * root for gettting the resource
	 * @param file relative location of the packaged file
	 * @param attributeToReplace the attribute to replace of the target tag
	 */
	public PackagedResourceReference(String id, Class referer,
			String file, String attributeToReplace)
	{
		super(id);

		if (referer == null)
		{
			throw new NullPointerException("referer may not be null");
		}
		if (file == null)
		{
			throw new NullPointerException("file may not be null");
		}
		if (attributeToReplace == null)
		{
			throw new NullPointerException("attributeToReplace may not be null");
		}

		final StaticResourceReference ref = new StaticResourceReference(referer, file);

		IModel srcReplacement = new Model()
		{
			public Object getObject(Component component)
			{
				String url = getPage().urlFor(ref.getPath());
				return url;
			};
		};
		add(new AttributeModifier(attributeToReplace, true, srcReplacement));
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param resourceReference the reference to the resource
	 * @param attributeToReplace the attribute to replace of the target tag
	 */
	public PackagedResourceReference(String id,
			final ResourceReference resourceReference,
			String attributeToReplace)
	{
		super(id);

		if (resourceReference == null)
		{
			throw new NullPointerException("resourceReference may not be null");
		}
		if (attributeToReplace == null)
		{
			throw new NullPointerException("attributeToReplace may not be null");
		}

		IModel srcReplacement = new Model()
		{
			public Object getObject(Component component)
			{
				String url = getPage().urlFor(resourceReference.getPath());
				return url;
			};
		};
		add(new AttributeModifier(attributeToReplace, true, srcReplacement));
	}
}
