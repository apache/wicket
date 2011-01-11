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
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;

/**
 * 
 * @author Juergen Donnerstag
 */
public class PanelMarkupSourcingStrategy extends AssociatedMarkupSourcingStrategy
{
	/** */
	public static final String PANEL = "panel";

	static
	{
		// register "wicket:panel"
		WicketTagIdentifier.registerWellKnownTagName(PANEL);
	}

	/**
	 * Constructor.
	 */
	public PanelMarkupSourcingStrategy()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		super.onComponentTagBody(component, markupStream, openTag);

		// Render the associated markup
		((MarkupContainer)component).renderAssociatedMarkup(PANEL,
			"Markup for a panel component has to contain part '<wicket:panel>'");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMarkupFragment getMarkup(final MarkupContainer parent, final Component child)
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
		markup = panelMarkup.find(child.getId());
		if ((child == null) || (markup != null))
		{
			return markup;
		}

		return findMarkupInAssociatedFileHeader(parent, child);
	}

	/**
	 * Search for &lt;wicket:panel ...&gt; on the same level.
	 * 
	 * @param markup
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
