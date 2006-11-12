/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.resolver;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;

/**
 * This is a tag resolver which handles &lt;wicket:link&gt; tags. Because
 * autolinks are already detected and handled, the only task of this resolver
 * will be to add a "transparent" WebMarkupContainer to transparently handling
 * child components.
 * 
 * @author Juergen Donnerstag
 */
public class WicketLinkResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * Try to resolve the tag, then create a component, add it to the container
	 * and render it.
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
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
		// It must be <wicket:link>
		if (tag.isLinkTag())
		{
			final String id = Component.AUTO_COMPONENT_PREFIX + "_link_"
					+ container.getPage().getAutoIndex();
			final Component component = new WebMarkupContainer(container, id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see wicket.MarkupContainer#isTransparentResolver()
				 */
				@Override
				public boolean isTransparentResolver()
				{
					return true;
				}
			};

			component.render(markupStream);

			// Yes, we handled the tag
			return true;
		}

		// We were not able to handle the tag
		return false;
	}
}