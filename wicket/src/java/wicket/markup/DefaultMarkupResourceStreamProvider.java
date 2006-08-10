/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
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
package wicket.markup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.locator.IResourceStreamLocator;

/**
 * Wicket default implementation for loading the markup resource stream
 * associated with a MarkupContainer.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class DefaultMarkupResourceStreamProvider implements IMarkupResourceStreamProvider
{
	/** Log for reporting. */
	private static final Log log = LogFactory.getLog(DefaultMarkupResourceStreamProvider.class);

	/**
	 * Constructor.
	 */
	public DefaultMarkupResourceStreamProvider()
	{
	}

	/**
	 * Create a new markup resource stream for the container.
	 * <p>
	 * Note: it will only called once, the IResourceStream will be cached by
	 * MarkupCache.
	 * <p>
	 * Note: IResourceStreamLocators should be used in case the strategy to find
	 * a markup resource should be extended for ALL components of your
	 * application.
	 * 
	 * @see wicket.util.resource.locator.IResourceStreamLocator
	 * @see wicket.markup.DefaultMarkupResourceStreamProvider
	 * 
	 * @param container
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @return A IResourceStream if the resource was found
	 */
	@SuppressWarnings("unchecked")
	public IResourceStream getMarkupResourceStream(final MarkupContainer container,
			Class<? extends MarkupContainer> containerClass)
	{
		// Get locator to search for the resource
		final IResourceStreamLocator locator = Application.get().getResourceSettings()
				.getResourceStreamLocator();

		// Markup is associated with the containers class. Walk up the class
		// hierarchy up to MarkupContainer to find the containers markup
		// resource.
		while (containerClass != MarkupContainer.class)
		{
			final IResourceStream resourceStream = locator.locate(containerClass, containerClass
					.getName().replace('.', '/'), container.getStyle(), container.getLocale(),
					container.getMarkupType());

			// Did we find it already?
			if (resourceStream != null)
			{
				return new MarkupResourceStream(resourceStream, new ContainerInfo(container),
						containerClass);
			}

			// Walk up the class hierarchy one level, if markup has not
			// yet been found
			containerClass = (Class<? extends MarkupContainer>)containerClass.getSuperclass();
		}

		return null;
	}
}
