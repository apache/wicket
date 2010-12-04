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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Objects;

/**
 * Usually you either have a markup file or a xml tag with wicket:id="myComponent" to associate
 * markup with a component. However in some use cases, especially when working with small panels it
 * is a bit awkward to maintain tiny pieces of markup in plenty of panel markup files. Use cases are
 * for example list views where list items are different depending on a state.
 * <p>
 * Fragments provide a means to maintain the panels tiny piece of markup. Since it can be anywhere,
 * the component whose markup contains the fragment's markup must be provided (markup provider).
 * <p>
 * 
 * <pre>
 *  &lt;span wicket:id=&quot;myPanel&quot;&gt;Example input (will be removed)&lt;/span&gt;
 * 
 *  &lt;wicket:fragment wicket:id=&quot;frag1&quot;&gt;panel 1&lt;/wicket:fragment&gt;
 *  &lt;wicket:fragment wicket:id=&quot;frag2&quot;&gt;panel 2&lt;/wicket:fragment&gt;
 * </pre>
 * 
 * <pre>
 *  add(new Fragment(&quot;myPanel1&quot;, &quot;frag1&quot;, myPage);
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public class Fragment extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** The wicket:id of the associated markup fragment */
	private String markupId;

	/** The container providing the inline markup */
	private final MarkupContainer markupProvider;

	/**
	 * Constructor.
	 * 
	 * @see org.apache.wicket.Component#Component(String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup
	 */
	public Fragment(final String id, final String markupId, final MarkupContainer markupProvider)
	{
		this(id, markupId, markupProvider, null);
	}

	/**
	 * Constructor.
	 * 
	 * @see org.apache.wicket.Component#Component(String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup
	 * @param model
	 *            The model for this fragment
	 */
	public Fragment(final String id, final String markupId, final MarkupContainer markupProvider,
		final IModel<?> model)
	{
		super(id, model);

		if (markupId == null)
		{
			throw new IllegalArgumentException("markupId cannot be null");
		}

		this.markupId = markupId;
		this.markupProvider = markupProvider;
	}

	/**
	 * The associated markup fragment can be modified
	 * 
	 * @param markupId
	 */
	public final void setMarkupTagReferenceId(final String markupId)
	{
		if (markupId == null)
		{
			throw new IllegalArgumentException("markupId cannot be null");
		}
		if (!Objects.equal(this.markupId, markupId))
		{
			addStateChange();
		}
		this.markupId = markupId;
	}

	/**
	 * Make sure we open up open-close tags to open-body-close
	 * 
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			tag.setType(TagType.OPEN);
		}
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
		// Skip the components body. It will be replaced by the fragment
		if (((ComponentTag)markupStream.get(markupStream.getCurrentIndex() - 1)).isOpen())
		{
			markupStream.skipRawMarkup();
		}

		renderFragment(openTag);
	}

	/**
	 * Render the markup starting at the current position of the markup strean
	 * 
	 * @see #onComponentTagBody(MarkupStream, ComponentTag)
	 * 
	 * @param openTag
	 */
	private void renderFragment(final ComponentTag openTag)
	{
		MarkupStream stream = new MarkupStream(getMarkup(null));

		// Get the fragments open tag
		ComponentTag fragmentOpenTag = stream.getTag();

		// if it is an open close tag, skip this fragment.
		if (!fragmentOpenTag.isOpenClose())
		{
			// We'll completely ignore the fragments open tag. It'll not be
			// rendered
			stream.next();

			// Render the body of the fragment
			super.onComponentTagBody(stream, fragmentOpenTag);
		}
	}

	/**
	 * Returns markup provider associated with this fragment
	 * 
	 * @return markup provider
	 */
	protected final MarkupContainer getMarkupProvider()
	{
		return (markupProvider != null ? markupProvider : getParent());
	}

	/**
	 * Get the markup stream which shall be used to search for the fragment
	 * 
	 * @param markupStream
	 *            The markup stream is associated with the component (not the fragment)
	 * @return The markup stream to be used to find the fragment markup
	 */
	protected IMarkupFragment chooseMarkup()
	{
		return getMarkupProvider().getMarkup(null);
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		// Get the markup to search for the fragment markup
		IMarkupFragment markup = chooseMarkup();
		if (markup == null)
		{
			throw new MarkupException(
				"chooseMarkup() returned null. No markup to search for fragment markup with id: " +
					markupId);
		}

		// Search for the fragment markup
		IMarkupFragment childMarkup = markup.find(markupId);
		if (childMarkup == null)
		{
			// There is one more option if the markup provider has associated markup
			MarkupContainer markupProvider = getMarkupProvider();
			if (markupProvider.hasAssociatedMarkup())
			{
				markup = markupProvider.getAssociatedMarkup();
				if (markup != null)
				{
					childMarkup = markup.find(markupId);
				}
			}
		}

		if (childMarkup == null)
		{
			throw new MarkupNotFoundException("No Markup found for Fragment " + markupId +
				" in providing markup container " + markupProvider.toString());
		}

		if (child == null)
		{
			return childMarkup;
		}

		// search for the child insight the fragment markup
		return childMarkup.find(child.getId());
	}
}
