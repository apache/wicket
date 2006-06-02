/*
 * $Id: ImageMap.java 5231 2006-04-01 15:34:49 -0800 (Sat, 01 Apr 2006) joco01 $
 * $Revision: 1.9 $ $Date: 2006-04-01 15:34:49 -0800 (Sat, 01 Apr 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.link;

import wicket.MarkupContainer;
import wicket.Page;
import wicket.PageMap;
import wicket.RequestCycle;
import wicket.Session;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.util.string.Strings;

/**
 * Implementation of an internal frame component. Must be used with an iframe
 * (&lt;iframe src...) element. The src attribute will be generated.
 * 
 * @author Sven Meier
 * @author Ralf Ebert
 */
public class InternalFrame extends WebMarkupContainer implements ILinkListener
{
	private static final long serialVersionUID = 1L;

	/** The link. */
	private final IPageLink pageLink;

	/**
	 * The pagemap name where the page that will be created by this internal
	 * frame will be created in.
	 */
	private final String pageMapName;

	/**
	 * Constructs an internal frame that instantiates the given Page class when
	 * the content of the internal frame is requested. The instantiated Page is
	 * used to render a response to the user.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param pageMap
	 *            the pagemap where the page of the internal frame must be in
	 * @param c
	 *            Page class
	 */
	public InternalFrame(MarkupContainer parent, final String id, final PageMap pageMap,
			final Class<? extends Page> c)
	{
		this(parent, id, pageMap, new IPageLink()
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
	 * Page. Construct an internal frame containing the given Page.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See component
	 * @param pageMap
	 *            the pagemap where the page of the internal frame must be in
	 * @param page
	 *            The page
	 */
	public InternalFrame(MarkupContainer parent, final String id, final PageMap pageMap,
			final Page page)
	{
		this(parent, id, pageMap, new IPageLink()
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
	 * Constructs an internal frame which invokes the getPage() method of the
	 * IPageLink interface when the content of the internal frame is requested.
	 * Whatever Page objects is returned by this method will be rendered back to
	 * the user.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param pageMap
	 *            the pagemap where the page of the internal frame must be in
	 * @param pageLink
	 *            An implementation of IPageLink which will create the page to
	 *            be contained in the internal frame if and when the content is
	 *            requested
	 */
	public InternalFrame(MarkupContainer parent, final String id, final PageMap pageMap,
			IPageLink pageLink)
	{
		super(parent, id);

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
	@Override
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
	public final PageMap getPageMap()
	{
		return PageMap.forName(this.pageMapName);
	}
}