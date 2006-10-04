/*
 * $Id: JavaScriptReference.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25
 * May 2006) $
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
package wicket.markup.html.resources;

import wicket.MarkupContainer;
import wicket.ResourceReference;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.value.IValueMap;

/**
 * Link to a packaged JavaScript file.
 * 
 * @author Eelco Hillenius
 */
public class JavaScriptReference extends PackagedResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param referer
	 *            the class that is refering; is used as the relative root for
	 *            gettting the resource
	 * @param file
	 *            reference as a string
	 */
	public JavaScriptReference(MarkupContainer parent, String id, Class referer, String file)
	{
		super(parent, id, referer, file, "src");
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param referer
	 *            the class that is refering; is used as the relative root for
	 *            gettting the resource
	 * @param file
	 *            reference as a string. The model must provide an instance of
	 *            {@link String}
	 */
	public JavaScriptReference(MarkupContainer parent, String id, Class referer, IModel<String> file)
	{
		super(parent, id, referer, file, "src");
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param resourceReference
	 *            resource reference
	 */
	public JavaScriptReference(MarkupContainer parent, String id,
			ResourceReference resourceReference)
	{
		super(parent, id, resourceReference, "src");
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param resourceReference
	 *            resource reference. The model must provide an instance of
	 *            {@link ResourceReference}
	 */
	public JavaScriptReference(MarkupContainer parent, String id,
			IModel<ResourceReference> resourceReference)
	{
		super(parent, id, resourceReference, "src");
	}

	/**
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		// Must be attached to a script tag
		checkComponentTag(tag, "script");
		IValueMap attributes = tag.getAttributes();
		attributes.put("type", "text/javascript");
	}
}