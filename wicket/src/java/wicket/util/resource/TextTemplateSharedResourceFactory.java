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

import java.util.Iterator;
import java.util.Map;

import wicket.Application;
import wicket.Resource;
import wicket.ResourceReference;
import wicket.SharedResources;

/**
 * A resource (reference) factory that takes a TextTemplate and generates shared
 * resources for various interpolations of that template.
 * <p>
 * A scope for adding shared resources allows you to limit the namespace impact
 * of the shared resources created. If you omit the scope, the application-wide
 * scope Application.class will be used by default.
 * <p>
 * You may use resources created by this factory directly by calling
 * resourceReference(Map) to get a resource reference to the given shared
 * resource interpolation represented by the variables in the map. Or, for
 * convenience, you can use TextTemplateLink to link to resources created by
 * this factory.
 * <p>
 * In many cases, it will be useful to extend this class and override
 * sharedResourceName(Map) to provide a unique name for resources created by the
 * factory using map values. If you don't provide an override, every value in
 * the map will be used to produce the unique name, which may create either
 * longer names or more unique shared resources than you really wanted.
 * 
 * @author Jonathan Locke
 */
// TODO Should weak-ref regenerable resources like this in SharedResources!
public class TextTemplateSharedResourceFactory
{
	private static final long serialVersionUID = 1L;

	/**
	 * Shared resource scope
	 */
	private final Class scope;

	/**
	 * Template to use to create resources
	 */
	private final TextTemplate template;

	/**
	 * Creates shared text template resources.
	 * 
	 * @param template
	 *            The template to interpolate into
	 */
	public TextTemplateSharedResourceFactory(final TextTemplate template)
	{
		this(template, Application.class);
	}

	/**
	 * Creates shared text template resources with the given scope.
	 * 
	 * @param template
	 *            The template to interpolate into
	 * @param scope
	 *            The scope in shared resources to add resources at
	 */
	public TextTemplateSharedResourceFactory(final TextTemplate template, final Class scope)
	{
		this.template = template;
		this.scope = scope;
	}

	/**
	 * @param variables
	 *            The variables to interpolate into the template
	 * @return A resource reference to the template encoded as a resource with
	 *         the given variables interpolated.
	 */
	public ResourceReference resourceReference(final Map variables)
	{
		final String uniqueName = sharedResourceName(variables);
		final String templateValue = template.asString(variables);
		final SharedResources sharedResources = Application.get().getSharedResources();
		final Resource resource = sharedResources.get(uniqueName);
		if (resource == null)
		{
			final Resource newResource = new Resource()
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see wicket.Resource#getResourceStream()
				 */
				public IResourceStream getResourceStream()
				{
					return new AbstractStringResourceStream()
					{
						private static final long serialVersionUID = 1L;

						protected String getString()
						{
							return templateValue;
						}

						public long length()
						{
							return templateValue.length();
						}
					};
				}
			};
			sharedResources.add(uniqueName, newResource);
		}
		return new ResourceReference(scope == null ? Application.class : scope, uniqueName);
	}

	/**
	 * @param variables
	 *            Variables that parameterize the linked-to resource
	 * @return A unique name for the variables to use as a resource key
	 */
	protected String sharedResourceName(final Map variables)
	{
		final StringBuffer buffer = new StringBuffer();
		for (final Iterator iterator = variables.values().iterator(); iterator.hasNext();)
		{
			final String value = iterator.next().toString();
			buffer.append(encodeValue(value));
			if (iterator.hasNext())
			{
				buffer.append('-');
			}
		}
		return buffer.toString();
	}

	/**
	 * Simple encoder for key values. Letters and digits are unchanged. All
	 * others are encoded as %<hexcode>.
	 * 
	 * @param value
	 *            The value
	 * @return The encoded value
	 */
	private String encodeValue(final String value)
	{
		final StringBuffer buffer = new StringBuffer(value.length() + 10);
		for (int i = 0; i < value.length(); i++)
		{
			final char c = value.charAt(i);
			if (Character.isLetterOrDigit(c))
			{
				buffer.append(c);
			}
			else
			{
				buffer.append('%');
				buffer.append(Integer.toHexString(c));
			}
		}
		return buffer.toString();
	}
}
