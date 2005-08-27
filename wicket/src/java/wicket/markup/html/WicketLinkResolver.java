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

import wicket.Component;
import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;

/**
 * This is a tag resolver which handles &lt;wicket:link&gt; tags. Because
 * autolinks are already detected and handled, the only task of this
 * resolver will be to add a "transparent" WebMarkupContainer to 
 * transparently handling child components. 
 * 
 * @author Juergen Donnerstag
 */
public class WicketLinkResolver implements IComponentResolver
{
	/** Logging */
	private static Log log = LogFactory.getLog(WicketLinkResolver.class);

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
		// It must be <body onLoad>
		if (tag instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag) tag;
			if (wtag.isLinkTag() && (wtag.getNamespace() != null))
			{
				Component component = new TransparentWebMarkupContainer(
						"_link_" + container.getPage().getAutoIndex());
				container.autoAdd(component);
	
				// Yes, we handled the tag
				return true;
			}
		}

		// We were not able to handle the tag
		return false;
	}

	/**
	 * This is a WebMarkupContainer, except that it is transparent for
	 * it child components.
	 */
	public class TransparentWebMarkupContainer extends WebMarkupContainer
		implements
			IComponentResolver
	{
		/**
		 * @param id
		 */
		public TransparentWebMarkupContainer(final String id)
		{
			super(id);
		}

		/**
		 * Because the component is not able to resolve any inner
		 * component, it'll passed it down to its parent.
		 * 
		 * @param container
		 *            The container parsing its markup
		 * @param markupStream
		 *            The current markupStream
		 * @param tag
		 *            The current component tag while parsing the markup
		 * @return True if componentId was handled by the resolver, false
		 *         otherwise.
		 */
		public final boolean resolve(final MarkupContainer container,
				final MarkupStream markupStream, final ComponentTag tag)
		{
			// Delegate the request to the parent component
			final Component component = this.getParent().get(tag.getId());
			if (component == null)
			{
				return false;
			}

			component.render();
			return true;
		}
	}
	
}