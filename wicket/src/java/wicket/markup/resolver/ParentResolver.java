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

/**
 * Some containers are transparent to the user (e.g. HtmlHeaderContainer or
 * BodyOnLoadContainer) and delegate component resolution to there parent.
 * 
 * @author Juergen Donnerstag
 */
public class ParentResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * Try to resolve the tag, then create a component, add it to the container
	 * and render it.
	 * <p>
	 * Note: Special tags like &ltwicket:...&gt> and tags which id start with
	 * "_" are not resolved.
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// Ignore special tags like _panel, _border, _extend etc.
		if (tag instanceof WicketTag)
		{
			return false;
		}

		MarkupContainer parent = container;
		while ((parent != null) && (parent.isTransparentResolver()))
		{
			// Try to find the component with the parent component.
			parent = parent.getParent();
			if (parent != null)
			{
				Component component = parent.get(tag.getId());
				if (component != null)
				{
					component.render(markupStream);
					return true;
				}
			}
		}

		// If not yet found, restore the original parent and test if it
		// implement IComponentResolver
		parent = container.getParent();
		if (parent instanceof IComponentResolver)
		{
			return ((IComponentResolver)parent).resolve(container, markupStream, tag);
		}
		return false;
	}
}