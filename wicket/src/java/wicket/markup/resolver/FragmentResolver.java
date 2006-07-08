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

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.parser.filter.WicketTagIdentifier;

/**
 * Usually you either have a markup file or a xml tag with
 * wicket:id="myComponent" to associate markup with a component. However in some
 * rare cases, especially when working with small panels it is a bit awkward to
 * maintain tiny pieces of markup in plenty of panel markup files. Use cases are
 * for example list views where list items are different depending on a state.
 * <p>
 * Inline panels provide a means to maintain the panels tiny piece of markup in
 * the parents markup file. During the render process, when Wicket iterates over
 * the markup file, the markup fragment must be ignored. It is only indirectly
 * referenced by component.
 * 
 * @author Juergen Donnerstag
 */
public class FragmentResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName("fragment");
	}

	/**
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
		// If <wicket:fragment ...>
		if (tag.isFragementTag())
		{
			// skip the markup associated with the tag
			markupStream.skipComponent();
			return true;
		}

		// We were not able to handle the tag
		return false;
	}
}