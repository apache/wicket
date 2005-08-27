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
package wicket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.markup.html.WebMarkupContainer;

/**
 * Detect &lt;wicket:extend&gt; and &lt;wicket:child&gt; tags,
 * which are silently ignored, because they have already been processed.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupInheritanceResolver implements IComponentResolver
{
	/** Logging */
	private static Log log = LogFactory.getLog(MarkupInheritanceResolver.class);

	/**
	 * @see wicket.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
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
			
			// It must be <wicket:extend...>
			if (wicketTag.isExtendTag())
			{
				container.autoAdd(new TransparentWebMarkupContainer("_extend"));
			    return true;
			}
			
			// It must be <wicket:child...>
			if (wicketTag.isChildTag())
			{
				container.autoAdd(new TransparentWebMarkupContainer("_child"));
			    return true;
			}
		}
		// We were not able to handle the componentId
		return false;
	}

	/**
	 * This is a WebMarkupException, except that it is transparent for
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
		 * Because the autolink component is not able to resolve any inner
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