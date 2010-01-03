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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Checks;

/**
 * WebMarkupContainer with it's own markup and possibly <wicket:head> tag.
 * 
 * @author Juergen Donnerstag
 */
public abstract class WebMarkupContainerWithAssociatedMarkup extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** A utility class which implements the internals */
	private ContainerWithAssociatedMarkupHelper markupHelper;

	/**
	 * @see Component#Component(String)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id)
	{
		this(id, null);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	@Override
	public void renderHead(HtmlHeaderContainer container)
	{
		renderHeadFromAssociatedMarkupFile(container);
		super.renderHead(container);
	}

	/**
	 * Called by components like Panel and Border which have associated Markup and which may have a
	 * &lt;wicket:head&gt; tag.
	 * <p>
	 * Whereas 'this' might be a Panel or Border, the HtmlHeaderContainer parameter has been added
	 * to the Page as a container for all headers any of its components might wish to contribute.
	 * <p>
	 * The headers contributed are rendered in the standard way.
	 * 
	 * @param container
	 *            The HtmlHeaderContainer added to the Page
	 */
	protected final void renderHeadFromAssociatedMarkupFile(final HtmlHeaderContainer container)
	{
		if (markupHelper == null)
		{
			markupHelper = new ContainerWithAssociatedMarkupHelper(this);
		}

		markupHelper.renderHeadFromAssociatedMarkupFile(container);
	}

	/**
	 * Search the child's markup in the header section of the markup
	 * 
	 * @param markup
	 * @param child
	 * @return Null, if not found
	 */
	public IMarkupFragment findMarkupInAssociatedFileHeader(final IMarkupFragment markup,
		final Component child)
	{
		IMarkupFragment childMarkup = null;
		MarkupStream stream = new MarkupStream(markup);
		while (stream.skipUntil(ComponentTag.class) && (childMarkup == null))
		{
			ComponentTag tag = stream.getTag();
			if (tag instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)tag;
				if (wtag.isHeadTag())
				{
					if (tag.getMarkupClass() == null)
					{
						childMarkup = stream.getMarkupFragment().find(child.getId());
					}
				}
			}
			else if (TagUtils.isHeadTag(tag))
			{
				childMarkup = stream.getMarkupFragment().find(child.getId());
			}

			if (tag.isOpen() && !tag.hasNoCloseTag())
			{
				stream.skipToMatchingCloseTag(tag);
			}
			stream.next();
		}

		return childMarkup;
	}

	/**
	 * Convenience method for subclasses to call. It is exactly like you would implement
	 * getMarkup(child) in your subclass. The only difference is, you need to provide the name of
	 * tag, such as 'panel' in &lt;wicket:panel&gt;
	 * 
	 * @param tagName
	 * @param child
	 * @return the markup fragment for the child
	 */
	public final IMarkupFragment getMarkup(final String tagName, final Component child)
	{
		Checks.argumentNotEmpty(tagName, "tagName");

		// get the associated markup resource file
		IMarkupFragment markup = getAssociatedMarkup();
		if (markup == null)
		{
			throw new MarkupException("Unable to find associated markup file for: " +
				this.toString());
		}

		// Find <wicket:'name'>
		IMarkupFragment panelMarkup = findTag(markup, tagName);
		if (panelMarkup == null)
		{
			throw new MarkupNotFoundException("Expected to find <wicket:" + tagName +
				"> in associated markup file. Markup: " + markup.toString());
		}

		// If child == null, return the markup fragment starting with the <wicket:border> tag
		if (child == null)
		{
			return panelMarkup;
		}

		// Find the markup for the child component
		panelMarkup = panelMarkup.find(child.getId());
		if (panelMarkup != null)
		{
			return panelMarkup;
		}

		return findMarkupInAssociatedFileHeader(markup, child);
	}

	/**
	 * Search for &lt;wicket:panel ...&gt; on the same level.
	 * 
	 * @param markup
	 * @param name
	 * @return null, if not found
	 */
	private final IMarkupFragment findTag(final IMarkupFragment markup, final String name)
	{
		MarkupStream stream = new MarkupStream(markup);

		while (stream.skipUntil(ComponentTag.class))
		{
			ComponentTag tag = stream.getTag();
			if (tag.isOpen() || tag.isOpenClose())
			{
				if (tag instanceof WicketTag)
				{
					if (tag.getName().equalsIgnoreCase(name))
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