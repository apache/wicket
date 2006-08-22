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
package wicket;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.markup.ComponentTag;
import wicket.markup.IMarkup;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.html.border.Border;
import wicket.markup.html.panel.Fragment;
import wicket.markup.resolver.IMarkupFinder;
import wicket.util.string.PrependingStringBuffer;

/**
 * Responding to an AJAX request requires that we position the markup stream at
 * the component associated with the AJAX request. That is straight forward in
 * most cases except for "transparent" components and for components which
 * implement their own IComponentResolver.
 * <p>
 * As users may implement and register there own resolvers, they also have a
 * need to implement and register there own IMarkupFinder. IMarkupFinder can be
 * registered with MarkupFragmentFinder which is accessible through
 * IMarkupSettings.getMarkupFragmentFinder().
 * 
 * @author Juergen Donnerstag
 */
public final class MarkupFragmentFinder
{
	private static final Pattern LIST_VIEW_NUMBER = Pattern.compile(IMarkup.TAG_PATH_SEPARATOR
			+ "\\d+");

	/** List of registered markup finders */ 
	private List<IMarkupFinder> markupFinders = new ArrayList<IMarkupFinder>();

	/**
	 * Construct
	 */
	public MarkupFragmentFinder()
	{
		registerMarkupFilter(new DefaultMarkupFinder());
	}

	/**
	 * Register an additional IMarkupFinder for find the markup associated with
	 * a Component. All finders registered are executed in sequence until the
	 * markup has been found.
	 * 
	 * @param finder
	 */
	public void registerMarkupFilter(final IMarkupFinder finder)
	{
		this.markupFinders.add(finder);
	}

	/**
	 * Gets the markup stream and positions it at the component.
	 * 
	 * @param component
	 * @param <T>
	 *            Component type
	 * @return A MarkupStream which is positioned at the component
	 */
	public final <T> MarkupStream find(final Component<T> component)
	{
		MarkupStream markupStream = null;
		for (IMarkupFinder finder : this.markupFinders)
		{
			markupStream = finder.find(component);
			if (markupStream != null)
			{
				break;
			}
		}
		
		return markupStream;
	}

	/**
	 * Default markup finder. Will allways be registered.
	 * 
	 */
	private static class DefaultMarkupFinder implements IMarkupFinder
	{
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
		public final <T> MarkupStream find(final Component<T> component)
		{
			// Get the parent's associated markup stream.
			MarkupContainer parentWithAssociatedMarkup = component.findParentWithAssociatedMarkup();
			MarkupStream markupStream = null;
	
			// Might be that we have to walk up the component hierarchy
			while (true)
			{
				markupStream = parentWithAssociatedMarkup.getAssociatedMarkupStream(true);
	
				// In case the component has already been rendered, this is a
				// performance short cut. But actually this was necessary because
				// transparent containers and components which implement
				// IComponentResolver destroy the 1:1 match between component path
				// and markup path.
				if (component.markupIndex != -1)
				{
					// Might be that the markup has been reloaded and that the
					// position has changed. Make sure the component is still
					// available
					try
					{
						markupStream.setCurrentIndex(component.markupIndex);
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
				relativePath = joinPath(relativePath, component.getId());
	
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
				final Matcher matcher = LIST_VIEW_NUMBER.matcher(relativePath);
				relativePath = matcher.replaceAll("");
	
				// If the component is defined in the markup
				int index = markupStream.positionAt(relativePath, false);
				if (index != -1)
				{
					return markupStream;
				}
	
				// Yet another exception for Border in the code base.
				// However if the container with the markup is a Border, than
				// ...
				if (parentWithAssociatedMarkup instanceof Border)
				{
					parentWithAssociatedMarkup = parentWithAssociatedMarkup
							.findParentWithAssociatedMarkup();
				}
				else
				{
					// if it is a child of a fragement. First find the fragement
					MarkupContainer mc = component.findParent(Fragment.class);
					if (mc != null)
					{
						final Fragment fragment = (Fragment)mc;
						final MarkupContainer markupProvider = fragment.getMarkupProvider();
						if (markupProvider != null)
						{
							markupStream = markupProvider.getMarkupStream();
							if (markupStream == null)
							{
								markupStream = markupProvider.getAssociatedMarkupStream(true);
							}
						}
	
						String fragmentId = fragment.getFragmentMarkupId();
						String componentId = getComponentRelativePath(mc, parentWithAssociatedMarkup);
						if ((componentId == null) || (componentId.length() == 0))
						{
							componentId = mc.getId();
						}
						else
						{
							componentId += Component.PATH_SEPARATOR + mc.getId();
						}
						relativePath = relativePath.replace(componentId, fragmentId);
						relativePath = joinPath(relativePath, component.getId());
	
						// If the component is defined in the markup
						index = markupStream.positionAt(relativePath, false);
						if (index != -1)
						{
							return markupStream;
						}
					}
					throw new WicketRuntimeException("Unable to find the markup for the component: "
							+ component.getId());
				}
	
				// Not found, reset the stream
				markupStream = null;
			}
		}
	
		private static String joinPath(final String path, final String id)
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
		private static String getComponentRelativePath(final Component component,
				final MarkupContainer parentWithAssociatedMarkup)
		{
			final MarkupContainer parent = component.getParent();
			if ((parent == null) || (parent == parentWithAssociatedMarkup))
			{
				return "";
			}
	
			final PrependingStringBuffer buffer = new PrependingStringBuffer(32);
			for (Component c = parent; (c != parentWithAssociatedMarkup) && (c != null); c = c
					.getParent())
			{
				if (buffer.length() > 0)
				{
					buffer.prepend(IMarkup.TAG_PATH_SEPARATOR);
				}
				buffer.prepend(c.getId());
			}
			return buffer.toString();
		}
	}
}