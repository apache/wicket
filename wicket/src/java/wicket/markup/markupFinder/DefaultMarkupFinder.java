/*
 * $Id: MarkupStream.java 5090 2006-03-22 21:52:18Z jdonnerstag $ $Revision:
 * 5090 $ $Date: 2006-03-22 22:52:18 +0100 (Mi, 22 Mrz 2006) $
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
package wicket.markup.markupFinder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.IMarkup;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;

/**
 * Default markup finder. Will allways be registered.
 * 
 * @author Juergen Donnerstag
 */
public class DefaultMarkupFinder implements IMarkupFinder
{
	static private final Pattern LIST_VIEW_NUMBER = Pattern.compile(IMarkup.TAG_PATH_SEPARATOR
			+ "\\d+");

	/**
	 * Construct.
	 */
	public DefaultMarkupFinder()
	{
	}

	/**
	 * Gets the markup stream and positions it at the component.
	 * 
	 * @param component
	 * @param <T>
	 *            Component type
	 * @return A MarkupStream which is positioned at the component
	 */
	public <T> MarkupStream find(final Component<T> component)
	{
		// Get the parent's associated markup stream.
		MarkupContainer parentWithAssociatedMarkup = component.findParentWithAssociatedMarkup();
		MarkupStream markupStream = null;

		// Might be that we have to walk up the component hierarchy
		markupStream = parentWithAssociatedMarkup.getAssociatedMarkupStream(true);
		markupStream = find(component, markupStream, parentWithAssociatedMarkup);

		return markupStream;
	}

	/**
	 * 
	 * @see wicket.markup.markupFinder.IMarkupFinder#find(wicket.Component,
	 *      wicket.markup.MarkupStream, wicket.MarkupContainer)
	 */
	public <T> MarkupStream find(final Component<T> component, MarkupStream markupStream,
			final MarkupContainer parentWithAssociatedMarkup)
	{
		// In case the component has already been rendered, this is a
		// performance short cut. But actually this was necessary because
		// transparent containers and components which implement
		// IComponentResolver destroy the 1:1 match between component path
		// and markup path.
		if (component.getMarkupIndex() != -1)
		{
			// Might be that the markup has been reloaded and that the
			// position has changed. Make sure the component is still
			// available
			try
			{
				markupStream.setCurrentIndex(component.getMarkupIndex());
				MarkupElement elem = markupStream.get();
				if (elem instanceof ComponentTag)
				{
					ComponentTag tag = (ComponentTag)elem;
					if (tag.getId().equals(component.getId()))
					{
						// Ok, found it
						return markupStream;
					}
				}
			}
			catch (IndexOutOfBoundsException ex)
			{
				// fall through. Don't do anything
			}
		}

		// Make sure the markup stream is positioned at the correct element
		String relativePath = getComponentRelativePath(component, parentWithAssociatedMarkup);

		// If the component is defined in the markup
		int index = positionAt(markupStream, relativePath);
		if (index != -1)
		{
			return markupStream;
		}

		return null;
	}

	protected int positionAt(final MarkupStream markupStream, final String relativePath)
	{
		return markupStream.positionAt(relativePath, false);
	}

	protected String joinPath(final String path, final String id)
	{
		if ((path != null) && (path.length() > 0))
		{
			return path + IMarkup.TAG_PATH_SEPARATOR + id;
		}

		return id;
	}

	/**
	 * Gets component path relative to the parent container with associated
	 * markup.
	 * 
	 * @param component
	 * @param parentWithAssociatedMarkup
	 *            A parent container of 'component', which has associated markup
	 *            that contains the markup fragment
	 * @return the relative path
	 */
	protected String getComponentRelativePath(final Component component,
			final MarkupContainer parentWithAssociatedMarkup)
	{
		MarkupContainer parent = component.getParent();
		if ((parent == null) || (parent == parentWithAssociatedMarkup))
		{
			return component.getId();
		}

		final StringBuffer buffer = new StringBuffer(32);
		for (; (parent != parentWithAssociatedMarkup) && (parent != null); parent = parent
				.getParent())
		{
			if (buffer.length() > 0)
			{
				buffer.insert(0, IMarkup.TAG_PATH_SEPARATOR);
			}
			buffer.insert(0, parent.getId());
		}

		buffer.append(IMarkup.TAG_PATH_SEPARATOR);
		buffer.append(component.getId());

		// TODO Post 1.2: A component path e.g. "panel:label" does not match
		// 1:1 with the markup in case of ListView, where the path contains
		// a number for each list item. E.g. list:0:label. What we currently
		// do is simply remove the number from the path and hope that no
		// user uses an integer for a component id. This is a hack only. A
		// much better solution would delegate to the various components
		// recursivly to search within there realm only for the components
		// markup. ListItems could then simply do nothing and delegate to
		// their parents.

		// s/:\d+//g
		final Matcher matcher = LIST_VIEW_NUMBER.matcher(buffer);
		return matcher.replaceAll("");
	}
}