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
package wicket.markup.html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.internal.HeaderContainer;
import wicket.markup.html.internal.WicketHeadContainer;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.model.IModel;

/**
 * A WebMarkupContainer, such as Panel or Border, with an associated markup
 * file.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Juergen Donnerstag
 */
public class WebMarkupContainerWithAssociatedMarkup<T> extends WebMarkupContainer<T>
{
	private static final Logger log = LoggerFactory.getLogger(WebMarkupContainerWithAssociatedMarkup.class);

	private static final long serialVersionUID = 1L;

	/** True if body onLoad attribute modifiers have been attached */
	private boolean checkedBody = false;

	/** <wicket:head> is only allowed before <body>, </head>, <wicket:panel> etc. */
	private boolean noMoreWicketHeadTagsAllowed = false;

	/** True, if headers have been added */
	private transient boolean headersInitialized;

	/**
	 * @see Component#Component(MarkupContainer,String)
	 */
	public WebMarkupContainerWithAssociatedMarkup(MarkupContainer parent, final String id)
	{
		this(parent, id, null);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(MarkupContainer parent, final String id,
			IModel<T> model)
	{
		super(parent, id, model);

		getAssociatedMarkup(true);
	}

	/**
	 * Get the child markup fragment with the 'id'
	 * 
	 * @param id
	 * @return MarkupFragment
	 */
	@Override
	public MarkupFragment getMarkupFragment(final String id)
	{
		MarkupFragment fragment = getMarkupFragment().getChildFragment(id, false);
		if (fragment != null)
		{
			return fragment;
		}

		return getAssociatedMarkup(true).getChildFragment(id, true);
	}

	/**
	 * @see wicket.Component#renderHead(wicket.markup.html.IHeaderResponse)
	 */
	@Override
	public void renderHead(final IHeaderResponse response)
	{
		if (isVisible())
		{
			renderHead(response, getAssociatedMarkup(true));

			super.renderHead(response);
		}
	}

	/**
	 * 
	 * @param response
	 * @param fragment
	 */
	private void renderHead(final IHeaderResponse response, final MarkupFragment fragment)
	{
		// Get the header container <head> from the page
		final Page page = getPage();
		final HeaderContainer headerContainer = (HeaderContainer)page
				.get(HtmlHeaderSectionHandler.HEADER_ID);

		// Search for wicket:head in the associated markup, create container for
		// these tags and copy the body onload and onunload attributes
		fragment.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
		{
			private boolean foundBody = false;

			/**
			 * @see wicket.markup.MarkupFragment.IVisitor#visit(wicket.markup.MarkupElement,
			 *      wicket.markup.MarkupFragment)
			 */
			public Object visit(final MarkupElement element, final MarkupFragment parent)
			{
				final MarkupFragment frag = (MarkupFragment)element;
				final ComponentTag tag = frag.getTag();

				// if <wicket:head>, than
				if (tag.isWicketHeadTag())
				{
					if (foundBody == true)
					{
						throwMarkupException(fragment, tag,
								"<wicket:head> must be before the <body>, <wicket:panel> ... tag");
					}

					// Create a new wicket header container
					WicketHeadContainer header = newWicketHeaderContainer(frag);

					// Determine if the wicket:head markup should be printed or
					// not.
					header.setEnable(headerContainer.okToRender(header));
					header.render();

					return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
				}
				else if (tag.isBodyTag())
				{
					// Found <body>. "Copy" the attributes to the page's body
					// tag, if the container loading the markup is not a Page
					foundBody = true;
					if (page instanceof WebPage)
					{
						addBodyModifier(BodyContainer.ONLOAD, tag);
						addBodyModifier(BodyContainer.ONUNLOAD, tag);
					}
				}
				else if (tag.isMajorWicketComponentTag())
				{
					// Allow for improved error messages
					foundBody = true;
				}

				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Create a new WicketHeadContainer. Users may wish subclass the method to
	 * implemented more sophisticated header scoping stragegies.
	 * 
	 * @param fragment
	 *            The markup fragment associated with the wicket:head
	 * @return The new header part container s
	 */
	public WicketHeadContainer newWicketHeaderContainer(final MarkupFragment fragment)
	{
		return new WicketHeadContainer(this, fragment.getTag().getId() + getPage().getAutoIndex(), fragment);
	}

	/**
	 * Throw a MarkupException
	 * 
	 * @param parent
	 *            The associated markup file
	 * @param tag
	 *            The element causing the error
	 * @param message
	 *            The error message
	 */
	private void throwMarkupException(final MarkupFragment parent, final MarkupElement tag,
			final String message)
	{
		// Create a MarkupStream and position it at the error location
		MarkupStream markupStream = new MarkupStream(parent);
		while (markupStream.hasMore())
		{
			if (markupStream.next() == tag)
			{
				break;
			}
		}
		throw new MarkupException(markupStream,
				"<wicket:head> must be before the <body>, <wicket:panel> ... tag");
	}

	/**
	 * Attach an AttributeModifier to the body container which appends the new
	 * value to the onLoad attribute
	 * 
	 * @param attribute
	 *            The body tags attribute name
	 * @param tag
	 *            The tag to "copy" the attributes from
	 */
	private void addBodyModifier(final String attribute, final ComponentTag tag)
	{
		final CharSequence value = tag.getString(attribute);
		if (value != null)
		{
			// Attach an AttributeModifier to the body container
			// which appends the new value to the onLoad attribute
			((WebPage)this.getPage()).getBodyContainer().addModifier(attribute, value, this);
		}
	}
}