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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;

/**
 * Detect &lt;wicket:extend&gt; and &lt;wicket:child&gt; tags, which are silently ignored, because
 * they have already been processed.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupInheritanceResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:extend" and "wicket:child"
		WicketTagIdentifier.registerWellKnownTagName("extend");
		WicketTagIdentifier.registerWellKnownTagName("child");
	}

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return true, if componentId was handle by the resolver. False, otherwise
	 */
	public boolean resolve(final MarkupContainer< ? > container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		// It must be <wicket:...>
		if (tag instanceof WicketTag)
		{
			final WicketTag wicketTag = (WicketTag)tag;
			final String id = wicketTag.getId() + container.getPage().getAutoIndex();

			// It must be <wicket:extend...>
			if (wicketTag.isExtendTag())
			{
				container.autoAdd(new TransparentWebMarkupContainer(id), markupStream);
				return true;
			}

			// It must be <wicket:child...>
			if (wicketTag.isChildTag())
			{
				container.autoAdd(new TransparentWebMarkupContainer(id), markupStream);
				return true;
			}
		}
		// We were not able to handle the componentId
		return false;
	}

	/**
	 * This is a WebMarkupContainer, except that it is transparent for it child components.
	 */
	private static class TransparentWebMarkupContainer extends WebMarkupContainer<Object>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param id
		 */
		public TransparentWebMarkupContainer(final String id)
		{
			super(id);
		}

		/**
		 * @see org.apache.wicket.MarkupContainer#isTransparentResolver()
		 */
		@Override
		public boolean isTransparentResolver()
		{
			return true;
		}
	}
}