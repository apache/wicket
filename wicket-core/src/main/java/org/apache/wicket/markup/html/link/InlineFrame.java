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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Implementation of an <a href="http://www.w3.org/TR/REC-html40/present/frames.html#h-16.5">inline
 * frame</a> component. Must be used with an iframe (&lt;iframe src...) element. The src attribute
 * will be generated.
 * 
 * @author Sven Meier
 * @author Ralf Ebert
 * 
 */

public class InlineFrame extends WebMarkupContainer implements ILinkListener
{
	private static final long serialVersionUID = 1L;

	/** The provider of the page. */
	private final IPageProvider pageProvider;

	/**
	 * Constructs an inline frame that instantiates the given Page class when the content of the
	 * inline frame is requested. The instantiated Page is used to render a response to the user.
	 * 
	 * @param <C>
	 * 
	 * @param id
	 *            See Component
	 * @param c
	 *            Page class
	 */
	public <C extends Page> InlineFrame(final String id, final Class<C> c)
	{
		this(id, c, null);
	}

	/**
	 * Constructs an inline frame that instantiates the given Page class when the content of the
	 * inline frame is requested. The instantiated Page is used to render a response to the user.
	 * 
	 * @param <C>
	 * 
	 * @param id
	 *            See Component
	 * @param c
	 *            Page class
	 * @param params
	 *            Page parameters
	 */
	public <C extends Page> InlineFrame(final String id, final Class<C> c,
		final PageParameters params)
	{
		this(id, new PageProvider(c, params));

		// Ensure that c is a subclass of Page
		if (!Page.class.isAssignableFrom(c))
		{
			throw new IllegalArgumentException("Class " + c + " is not a subclass of Page");
		}
	}

	/**
	 * This constructor is ideal if a Page object was passed in from a previous Page. Construct an
	 * inline frame containing the given Page.
	 * 
	 * @param id
	 *            See component
	 * @param page
	 *            The page
	 */
	public InlineFrame(final String id, final Page page)
	{
		this(id, new PageProvider(page.getPageId(), page.getClass(), page.getRenderCount()));
	}

	/**
	 * This constructor is ideal for constructing pages lazily.
	 * 
	 * Constructs an inline frame which invokes the getPage() method of the IPageLink interface when
	 * the content of the inline frame is requested. Whatever Page objects is returned by this
	 * method will be rendered back to the user.
	 * 
	 * @param id
	 *            See Component
	 * @param pageProvider
	 *            the provider of the page to be contained in the inline frame if and when the
	 *            content is requested
	 */
	public InlineFrame(final String id, IPageProvider pageProvider)
	{
		super(id);

		this.pageProvider = pageProvider;
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 */
	protected CharSequence getURL()
	{
		return urlFor(ILinkListener.INTERFACE, new PageParameters());
	}

	/**
	 * Handles this frame's tag.
	 * 
	 * @param tag
	 *            the component tag
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "iframe");

		// Set href to link to this frame's frameRequested method
		CharSequence url = getURL();

		// generate the src attribute
		tag.put("src", url);

		super.onComponentTag(tag);
	}

	/**
	 * @see org.apache.wicket.markup.html.link.ILinkListener#onLinkClicked()
	 */
	@Override
	public final void onLinkClicked()
	{
		setResponsePage(pageProvider.getPageInstance());
	}


	@Override
	protected boolean getStatelessHint()
	{
		/*
		 * TODO optimization: the inlineframe component does not always have to be stateless.
		 * 
		 * unfortunately due to current implementation always using ipagelink and a ilinklistener
		 * callback it has to always be stateful because it can be put inside a listview item which
		 * will not be built upon a stateless callback causing a "component at path
		 * listview:0:iframe not found" error.
		 * 
		 * eventually variant such as (string, ipagemap, class<? extends Page>) can be made
		 * stateless because they can generate a bookmarkable url. another advantage of a
		 * bookmarkable url is that multiple iframes will not block.
		 */
		return false;
	}
}
