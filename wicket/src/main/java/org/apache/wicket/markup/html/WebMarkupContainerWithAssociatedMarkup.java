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
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.IModel;

/**
 * WebMarkupContainer with it's own markup and possibly &lt;wicket:head&gt; tag.
 * 
 * @author Juergen Donnerstag
 */
public class WebMarkupContainerWithAssociatedMarkup extends WebMarkupContainer
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
	 * @see Component#Component(String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Copy attributes from <wicket:panel> to the "calling" tag
		IMarkupFragment markup = getMarkup(null);
		String namespace = markup.getMarkupResourceStream().getWicketNamespace() + ":";

		MarkupElement elem = markup.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag panelTag = (ComponentTag)elem;
			for (String key : panelTag.getAttributes().keySet())
			{
				// exclude "wicket:XX" attributes
				if (key.startsWith(namespace) == false)
				{
					tag.append(key, panelTag.getAttribute(key), ", ");
				}
			}
		}
		else
		{
			throw new MarkupException(markup.getMarkupResourceStream(),
				"Expected a Tag but found raw markup: " + elem.toString());
		}

		super.onComponentTag(tag);
	}

	/**
	 * Render the header from the associated markup file
	 */
	@Override
	public void renderHead(final HtmlHeaderContainer container)
	{
		if (markupHelper == null)
		{
			markupHelper = new ContainerWithAssociatedMarkupHelper(this);
		}

		markupHelper.renderHeadFromAssociatedMarkupFile(container);

		super.renderHead(container);
	}

	/**
	 * Search the child's markup in the header section of the markup
	 * 
	 * @param child
	 * @return Null, if not found
	 */
	public IMarkupFragment findMarkupInAssociatedFileHeader(final Component child)
	{
		// Get the associated markup
		IMarkupFragment markup = getAssociatedMarkup();
		IMarkupFragment childMarkup = null;

		// MarkupStream is good at searching markup
		MarkupStream stream = new MarkupStream(markup);
		while (stream.skipUntil(ComponentTag.class) && (childMarkup == null))
		{
			ComponentTag tag = stream.getTag();
			if (TagUtils.isWicketHeadTag(tag))
			{
				if (tag.getMarkupClass() == null)
				{
					// find() can still fail an return null => continue the search
					childMarkup = stream.getMarkupFragment().find(child.getId());
				}
			}
			else if (TagUtils.isHeadTag(tag))
			{
				// find() can still fail an return null => continue the search
				childMarkup = stream.getMarkupFragment().find(child.getId());
			}

			// Must be a direct child. We are not interested in grand children
			if (tag.isOpen() && !tag.hasNoCloseTag())
			{
				stream.skipToMatchingCloseTag(tag);
			}
			stream.next();
		}

		return childMarkup;
	}
}