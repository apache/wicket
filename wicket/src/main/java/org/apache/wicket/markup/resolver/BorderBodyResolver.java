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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.border.Border;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In case of Border where the associated markup has a container in between the wicket:border and
 * the wicket:body tag, the wicket:body container can not be resolved easily. Lets assume the
 * container in the middle is a Form container, than this Form container does not know how to
 * resolve the wicket:body. Only the Border component does know how to do it.
 * <p>
 * Until 1.3, the original Border implementation, resolved these markup vs component hierarchy
 * mismatches, but the new implementation by purpose no longer does. We want to minimize such
 * mismatches for simplicity and clarity reasons. No magic.
 * 
 * NOTE: This resolver will be removed again in Wicket 1.4(!)
 * 
 * @author Juergen Donnerstag
 */
public class BorderBodyResolver implements IComponentResolver
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(BorderBodyResolver.class);

	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
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
		// Must be wicket:body
		if (tag instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)tag;
			if (wtag.isBodyTag())
			{
				// Find the border parent. There must not be a body container in
				// between
				MarkupContainer parent = container.getParent();
				while (parent != null)
				{
					if (parent instanceof Border.BorderBodyContainer)
					{
						break;
					}
					else if (parent instanceof Border)
					{
						Component component = parent.get(tag.getId());
						if (component != null)
						{
							component.render(markupStream);

							log.warn("Please consider to change your java code to " +
								"something like: " + container.getId() +
								".add(getBodyContainer()); for the component hierarchy to " +
								"better reflect the markup hierarchy. For example, say that " +
								"you have a border class in which you do: " +
								"\'WebMarkupContainer div = new " +
								"WebMarkupContainer(\"roundDiv\"); add(div);\' you should " +
								"now do \'add(div); div.add(getBodyContainer());\'. " +
								"Please fix this before Wicket 1.4");
						}

						return true;
					}
					parent = parent.getParent();
				}
			}
		}

		// We were not able to handle the tag
		return false;
	}
}