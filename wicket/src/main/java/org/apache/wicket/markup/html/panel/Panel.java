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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.model.IModel;

/**
 * A panel is a reusable component that holds markup and other components.
 * <p>
 * Whereas WebMarkupContainer is an inline container like
 * 
 * <pre>
 *  ...
 *  &lt;span wicket:id=&quot;xxx&quot;&gt;
 *    &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/span&gt;
 *  ...
 * </pre>
 * 
 * a Panel has its own associated markup file and the container content is taken from that file,
 * like:
 * 
 * <pre>
 *  &lt;span wicket:id=&quot;mypanel&quot;/&gt;
 * 
 *  TestPanel.html
 *  &lt;wicket:panel&gt;
 *    &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/wicket:panel&gt;
 * </pre>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class Panel extends WebMarkupContainerWithAssociatedMarkup
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final String PANEL = "panel";

	static
	{
		// register "wicket:panel"
		WicketTagIdentifier.registerWellKnownTagName(PANEL);
	}

	/** If if tag was an open-close tag */
	private transient boolean wasOpenCloseTag = false;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Panel(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Panel(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			wasOpenCloseTag = true;

			// Convert <span wicket:id="myPanel" /> into
			// <span wicket:id="myPanel">...</span>
			tag.setType(XmlTag.OPEN);
		}

// IMarkupFragment markup = getMarkup(null);
// ComponentTag panelTag = (ComponentTag)markup.get(0);
// for (String key : panelTag.getAttributes().keySet())
// {
// tag.append(key, panelTag.getAttribute(key), ", ");
// }
		super.onComponentTag(tag);
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Render the associated markup
		renderAssociatedMarkup(PANEL,
			"Markup for a panel component has to contain part '<wicket:panel>'");

		if (wasOpenCloseTag == false)
		{
			// Skip any raw markup in the body
			markupStream.skipRawMarkup();
			if (markupStream.get().closes(openTag) == false)
			{
				throw new MarkupException(markupStream, "close tag not found for tag: " +
					openTag.toString() + ". Component: " + this.toString());
			}
		}
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		IMarkupFragment markup = PanelMarkupHelper.getMarkup(this, child);
		if ((child == null) || (markup != null))
		{
			return markup;
		}

		return findMarkupInAssociatedFileHeader(child);
	}

	/**
	 * Re-useable helper
	 * 
	 */
	public static class PanelMarkupHelper
	{
		/**
		 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
		 * 
		 * @param parent
		 * @param child
		 * @return The markup associated with the child
		 */
		public static IMarkupFragment getMarkup(final MarkupContainer parent, final Component child)
		{
			IMarkupFragment markup = parent.getAssociatedMarkup();
			if (markup == null)
			{
				throw new MarkupNotFoundException("Failed to find markup file associated. " +
					parent.getClass().getSimpleName() + ": " + parent.toString());
			}

			// Find <wicket:panel>
			IMarkupFragment panelMarkup = findPanelTag(markup);
			if (panelMarkup == null)
			{
				throw new MarkupNotFoundException(
					"Expected to find <wicket:panel> in associated markup file. Markup: " +
						markup.toString());
			}

			// If child == null, than return the markup fragment starting with <wicket:panel>
			if (child == null)
			{
				return panelMarkup;
			}

			// Find the markup for the child component
			return panelMarkup.find(child.getId());
		}

		/**
		 * Search for &lt;wicket:panel ...&gt; on the same level.
		 * 
		 * @param markup
		 * @param name
		 * @return null, if not found
		 */
		private final static IMarkupFragment findPanelTag(final IMarkupFragment markup)
		{
			MarkupStream stream = new MarkupStream(markup);

			while (stream.skipUntil(ComponentTag.class))
			{
				ComponentTag tag = stream.getTag();
				if (tag.isOpen() || tag.isOpenClose())
				{
					if (tag instanceof WicketTag)
					{
						WicketTag wtag = (WicketTag)tag;
						if (wtag.isPanelTag())
						{
							return stream.getMarkupFragment();
						}
					}
					stream.skipToMatchingCloseTag(tag);
				}

				stream.next();
			}

			return null;
		}
	}
}
