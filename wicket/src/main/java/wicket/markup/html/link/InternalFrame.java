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
package wicket.markup.html.link;

import wicket.IPageMap;
import wicket.Page;
import wicket.PageMap;
import wicket.RequestCycle;
import wicket.Session;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.util.string.Strings;

/**
 * Implementation of an <a
 * href="http://www.w3.org/TR/REC-html40/present/frames.html#h-16.5">inline
 * frame</a> component. Must be used with an iframe (&lt;iframe src...)
 * element. The src attribute will be generated.
 * 
 * @author Sven Meier
 * @author Ralf Ebert
 * 
 * @deprecated will be replaced by {@link InlineFrame} in Wicket 2.0 as that's a
 *             better name for it.
 */
public class InternalFrame extends WebMarkupContainer implements ILinkListener
{
	private static final long serialVersionUID = 1L;

	/** The link. */
	private final IPageLink pageLink;

	/**
	 * The pagemap name where the page that will be created by this inline frame
	 * will be created in.
	 */
	private final String pageMapName;

	/**
	 * Constructs an inline frame that instantiates the given Page class when
	 * the content of the inline frame is requested. The instantiated Page is
	 * used to render a response to the user.
	 * 
	 * @param id
	 *            See Component
	 * @param pageMap
	 *            the pagemap where the page of the inline frame must be in
	 * @param c
	 *            Page class
	 */
	public InternalFrame(final String id, final IPageMap pageMap, final Class c)
	{
		this(id, pageMap, new IPageLink()
		{
			private static final long serialVersionUID = 1L;

			public Page getPage()
			{
				// Create page using page factory
				return Session.get().getPageFactory().newPage(c);
			}

			public Class getPageIdentity()
			{
				return c;
			}
		});

		// Ensure that c is a subclass of Page
		if (!Page.class.isAssignableFrom(c))
		{
			throw new IllegalArgumentException("Class " + c + " is not a subclass of Page");
		}
	}

	/**
	 * This constructor is ideal if a Page object was passed in from a previous
	 * Page. Construct an inline frame containing the given Page.
	 * 
	 * @param id
	 *            See component
	 * @param pageMap
	 *            the pagemap where the page of the inline frame must be in
	 * @param page
	 *            The page
	 */
	public InternalFrame(final String id, final IPageMap pageMap, final Page page)
	{
		this(id, pageMap, new IPageLink()
		{
			private static final long serialVersionUID = 1L;

			public Page getPage()
			{
				// use given page
				return page;
			}

			public Class getPageIdentity()
			{
				return page.getClass();
			}
		});
	}

	/**
	 * This constructor is ideal for constructing pages lazily.
	 * 
	 * Constructs an inline frame which invokes the getPage() method of the
	 * IPageLink interface when the content of the inline frame is requested.
	 * Whatever Page objects is returned by this method will be rendered back to
	 * the user.
	 * 
	 * @param id
	 *            See Component
	 * @param pageMap
	 *            the pagemap where the page of the inline frame must be in
	 * @param pageLink
	 *            An implementation of IPageLink which will create the page to
	 *            be contained in the inline frame if and when the content is
	 *            requested
	 */
	public InternalFrame(final String id, final IPageMap pageMap, IPageLink pageLink)
	{
		super(id);

		this.pageMapName = pageMap.getName();

		this.pageLink = pageLink;
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 */
	protected CharSequence getURL()
	{
		return urlFor(ILinkListener.INTERFACE);
	}

	/**
	 * Handles this frame's tag.
	 * 
	 * @param tag
	 *            the component tag
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "iframe");

		// Set href to link to this frame's frameRequested method
		CharSequence url = getURL();

		// generate the src attribute
		tag.put("src", Strings.replaceAll(url, "&", "&amp;"));

		super.onComponentTag(tag);
	}

	/**
	 * @see wicket.markup.html.link.ILinkListener#onLinkClicked()
	 */
	public final void onLinkClicked()
	{
		RequestCycle.get().getRequest().getRequestParameters().setPageMapName(pageMapName);

		setResponsePage(pageLink.getPage());
	}

	/**
	 * Returns the pageMap.
	 * 
	 * @return pageMap
	 */
	public final IPageMap getPageMap()
	{
		return PageMap.forName(this.pageMapName);
	}
}