/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.markup.html;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;

/**
 * This is a tag resolver which handles &lt;wicket:head&gt; tags. It must be
 * registered (with the application) and assumes that a WicketTag has already
 * been created (see WicketTagIdentifier).
 * <p>
 * Provided the current tag is a &lt;wicket:head&gt;, a HtmlHeaderContainer
 * component is created, (auto) added to the component hierarchie and
 * immediately rendered. Please see the javadoc for HtmlHeaderContainer on how
 * it treats the tag.
 * 
 * @author Juergen Donnerstag
 */
public class HtmlHeaderResolver implements IComponentResolver
{
	/** Logging */
	private static Log log = LogFactory.getLog(HtmlHeaderResolver.class);

	/**
	 * Try to resolve the tag, then create a component, add it to the container
	 * and render it.
	 * 
	 * @see wicket.IComponentResolver#resolve(MarkupContainer, MarkupStream,
	 *      ComponentTag)
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return true, if componentId was handle by the resolver. False, otherwise
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// It must be <wicket:...>
		if (tag instanceof WicketTag)
		{
			final WicketTag wicketTag = (WicketTag)tag;

			// It must be <wicket:head...>
			if (wicketTag.isHeadTag())
			{
				// Create, add and render the component
				HtmlHeaderContainer header = new HtmlHeaderContainer();
				
				try
				{
				    container.autoAdd(header);
				}
				catch (IllegalArgumentException ex)
				{
				    throw new WicketRuntimeException(
				            "If the root exception says something like " +
				            "\"A child with id '_header' already exists\" " +
				            "then you most likely forgot to override autoAdd() " + 
				            "in your bordered page component.", ex);
				}

				// Yes, we handled the tag
				return true;
			}
		}

		// We were not able to handle the tag
		return false;
	}
}