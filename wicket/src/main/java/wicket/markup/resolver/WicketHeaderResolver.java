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
import wicket.markup.html.internal.TransparentWebMarkupContainer;
import wicket.markup.parser.filter.WicketTagIdentifier;

/**
 * Detect &lt;wicket:extend&gt; and &lt;wicket:child&gt; tags, which are
 * silently ignored, because they have already been processed.
 * 
 * @author Juergen Donnerstag
 */
public class WicketHeaderResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:head"
		WicketTagIdentifier.registerWellKnownTagName("head");
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
		// If <head>...<wicket:head...> ...</head> 
		if (tag.isWicketHeadTag())
		{
			Component component = new TransparentWebMarkupContainer(container, Component.AUTO_COMPONENT_PREFIX
					+ tag.getId());

			component.setRenderBodyOnly(true);
			component.render(markupStream);
			
			return true;
		}

		// We were not able to handle the id
		return false;
	}
}