/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.jonathan.stylesheet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.ComponentWicketTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;

/**
 * Holder for dynamically generated stylesheet links.
 * 
 * @author Jonathan Locke
 */
public abstract class StylesheetLinks extends WebComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 209001445308790198L;

	/** The set of links */
	private Set links = new HashSet();

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Name of component
	 */
	public StylesheetLinks(String name)
	{
		super(name);

		// Visit all siblings and children which edit stylesheet links
		getParent().visitChildren(IStylesheetLinkEditor.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Let the component contribute to the link list
				((IStylesheetLinkEditor)component).edit(StylesheetLinks.this);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Override this method if you need to rearrange or otherwise edit the links
	 * contributed by components to resolve conflicts in ordering.
	 */
	protected void edit()
	{
	}

	/**
	 * @return Returns the links.
	 */
	protected Set getLinks()
	{
		return links;
	}

	/**
	 * @see wicket.markup.html.WebComponent#onRender()
	 */
	protected void onRender()
	{
		// Allow subclass an opportunity to reorder things
		edit();

		// Get markup stream
		final MarkupStream markupStream = findMarkupStream();

		// Get next tag
		final ComponentTag tag = markupStream.getTag();

		// Must be <wicket:stylesheet/>
		if (tag instanceof ComponentWicketTag && tag.isOpenClose("wicket:stylesheet"))
		{
			// Skip tag entirely, removing it from output
			markupStream.next();

			// Write out the links
			for (final Iterator iterator = links.iterator(); iterator.hasNext();)
			{
				// Get next link
				final String link = iterator.next().toString();

				// Write to response
				getResponse().write("<link rel='stylesheet' type='text/css' href='" + link + "'/>");
			}
		}
		else
		{
			throw new WicketRuntimeException("Must be attached to a <wicket:stylesheet/> tag");
		}
	}
}
