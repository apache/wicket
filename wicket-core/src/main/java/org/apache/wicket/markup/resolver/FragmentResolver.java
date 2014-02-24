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
import org.apache.wicket.markup.html.WebComponent;

/**
 * Usually you either have a markup file or a xml tag with wicket:id="myComponent" to associate
 * markup with a component. However in some rare cases, especially when working with small panels it
 * is a bit awkward to maintain tiny pieces of markup in plenty of panel markup files. Use cases are
 * for example list views where list items are different depending on a state.
 * <p>
 * Inline panels provide a means to maintain the panels tiny piece of markup in the parents markup
 * file. During the render process, when Wicket iterates over the markup file, the markup fragment
 * must be ignored. It is only indirectly referenced by component.
 * 
 * @author Juergen Donnerstag
 */
public class FragmentResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final String FRAGMENT = "fragment";

	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		// If <wicket:...>
		if (tag instanceof WicketTag)
		{
			final WicketTag wTag = (WicketTag)tag;

			// If <wicket:fragment ...>
			if (wTag.isFragmentTag())
			{
				return new WebComponent(wTag.getId()).setVisible(false);
			}
		}

		// We were not able to handle the tag
		return null;
	}
}