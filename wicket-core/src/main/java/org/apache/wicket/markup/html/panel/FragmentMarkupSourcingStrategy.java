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
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.util.lang.Args;

/**
 * A markup sourcing strategy suitable for Fragment components.
 * 
 * @author Juergen Donnerstag
 */
public class FragmentMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy
{
	/** The wicket:id of the associated markup fragment */
	private String markupId;

	/** The container providing the inline markup */
	private final MarkupContainer markupProvider;

	/**
	 * Constructor.
	 * 
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup
	 */
	public FragmentMarkupSourcingStrategy(final String markupId,
		final MarkupContainer markupProvider)
	{
		Args.notNull(markupId, "markupId");

		this.markupId = markupId;
		this.markupProvider = markupProvider;
	}

	/**
	 * Skip the body markup associated with the 'component'. The body markup is expected to be raw
	 * markup only, not containing an wicket component. The body markup may serve documentary
	 * purposes for the developer / designer.
	 * <p>
	 * Than search for the markup of the fragment, effectively replacing the original markup.
	 */
	@Override
	public void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		// Skip the body markup making sure it contains only raw markup
		super.onComponentTagBody(component, markupStream, openTag);

		// Get the fragments open tag
		MarkupStream stream = new MarkupStream(getMarkup((MarkupContainer)component, null));
		ComponentTag fragmentOpenTag = stream.getTag();

		// if it is an open close tag, skip this fragment.
		if (!fragmentOpenTag.isOpenClose())
		{
			// We'll completely ignore the fragments open tag. It'll not be rendered
			stream.next();

			// Render the body of the fragment
			component.onComponentTagBody(stream, fragmentOpenTag);
		}
	}

	/**
	 * Returns markup provider associated with this fragment
	 * 
	 * @param component
	 * @return markup provider
	 */
	protected final MarkupContainer getMarkupProvider(final Component component)
	{
		return (markupProvider != null ? markupProvider : component.getParent());
	}

	/**
	 * Get the markup stream which shall be used to search for the fragment
	 * 
	 * @param component
	 * @return The markup stream to be used to find the fragment markup
	 */
	public IMarkupFragment chooseMarkup(final Component component)
	{
		return getMarkupProvider(component).getMarkup(null);
	}

	/**
	 * Search for the child's markup in the fragment markup.
	 */
	@Override
	public IMarkupFragment getMarkup(final MarkupContainer container, final Component child)
	{
		// Get the markup to search for the fragment markup
		IMarkupFragment markup = chooseMarkup(container);
		if (markup == null)
		{
			throw new MarkupException("The fragments markup provider has no associated markup. " +
				"No markup to search for fragment markup with id: " + markupId);
		}

		// Search for the fragment markup
		IMarkupFragment childMarkup = markup.find(markupId);
		if (childMarkup == null)
		{
			// There is one more option if the markup provider has associated markup
			MarkupContainer markupProvider = getMarkupProvider(container);
			Markup associatedMarkup = markupProvider.getAssociatedMarkup();
			if (associatedMarkup != null)
			{
				markup = associatedMarkup;
				if (markup != null)
				{
					childMarkup = markup.find(markupId);
				}
			}
		}

		if (childMarkup == null)
		{
			throw new MarkupNotFoundException("No Markup found for Fragment " + markupId +
				" in providing markup container " + getMarkupProvider(container));
		}
		else
		{
			MarkupElement fragmentTag = childMarkup.get(0);
			if ((fragmentTag instanceof WicketTag && ((WicketTag)fragmentTag).isFragementTag()) == false)
			{
				throw new MarkupNotFoundException("Markup found for Fragment '" + markupId
					+ "' in providing markup container " + getMarkupProvider(container)
					+ " is not a <wicket:fragment> tag");
			}
		}

		if (child == null)
		{
			return childMarkup;
		}

		// search for the child inside the fragment markup
		return childMarkup.find(child.getId());
	}
}
