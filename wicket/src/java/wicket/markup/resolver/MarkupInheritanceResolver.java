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
package wicket.markup.resolver;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.filter.WicketTagIdentifier;

/**
 * Detect &lt;wicket:extend&gt; and &lt;wicket:child&gt; tags, which are
 * silently ignored, because they have already been processed.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupInheritanceResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName("extend");
		WicketTagIdentifier.registerWellKnownTagName("child");
	}

	/**
	 * @see wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
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
				// TODO we just have to prefix it i with a AUTO prefix.. Can
				// this be done? Shoult the wicketTag id first be altered?
				new TransparentWebMarkupContainer(container, Component.AUTO_COMPONENT_PREFIX
						+ wicketTag.getId()).autoAdded();
				return true;
			}

			// It must be <wicket:child...>
			if (wicketTag.isChildTag())
			{
				// TODO we just have to prefix it i with a AUTO prefix.. Can
				// this be done? Shoult the wicketTag id first be altered?
				new TransparentWebMarkupContainer(container, Component.AUTO_COMPONENT_PREFIX
						+ wicketTag.getId()).autoAdded();
				return true;
			}
		}
		// We were not able to handle the componentId
		return false;
	}

	/**
	 * This is a WebMarkupContainer, except that it is transparent for it child
	 * components.
	 */
	private static class TransparentWebMarkupContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param parent
		 * @param id
		 */
		public TransparentWebMarkupContainer(MarkupContainer parent, final String id)
		{
			super(parent, id);
		}

		/**
		 * @see wicket.MarkupContainer#isTransparentResolver()
		 */
		@Override
		public boolean isTransparentResolver()
		{
			return true;
		}
	}
}