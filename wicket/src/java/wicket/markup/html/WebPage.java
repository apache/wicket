/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.markup.html;

import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.html.internal.HtmlBodyContainer;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.parser.filter.BodyOnLoadHandler;
import wicket.model.IModel;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;

/**
 * Base class for HTML pages. This subclass of Page simply returns HTML when
 * asked for its markup type. It also has a method which subclasses can use to
 * retrieve a bookmarkable link to the application's home page.
 * <p>
 * WebPages can be constructed with any constructor when they are being used in
 * a Wicket session, but if you wish to link to a Page using a URL that is
 * "bookmarkable" (which implies that the URL will not have any session
 * information encoded in it, and that you can call this page directly without
 * having a session first directly from your browser), you need to implement
 * your Page with a no-arg constructor or with a constructor that accepts a
 * PageParameters argument (which wraps any query string parameters for a
 * request). In case the page has both constructors, the constructor with
 * PageParameters will be used.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Gwyn Evans
 */
public class WebPage extends Page
{
	/** Log. */
	// private static final Log log = LogFactory.getLog(WebPage.class);

	private static final long serialVersionUID = 1L;

	/** The body container */
	private BodyContainer bodyContainer;

	/**
	 * Constructor. Having this constructor public means that you page is
	 * 'bookmarkable' and hence can be called/ created from anywhere.
	 */
	protected WebPage()
	{
		commonInit();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel model)
	{
		super(model);
		commonInit();
	}

	/**
	 * @see Page#Page(PageMap)
	 */
	protected WebPage(final PageMap pageMap)
	{
		super(pageMap);
		commonInit();
	}

	/**
	 * @see Page#Page(PageMap, IModel)
	 */
	protected WebPage(final PageMap pageMap, final IModel model)
	{
		super(pageMap, model);
		commonInit();
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request.
	 * Having this constructor public means that you page is 'bookmarkable' and
	 * hence can be called/ created from anywhere. For bookmarkable pages (as
	 * opposed to when you construct page instances yourself, this constructor
	 * will be used in preference to a no-arg constructor, if both exist. Note
	 * that nothing is done with the page parameters argument. This constructor
	 * is provided so that tools such as IDEs will include it their list of
	 * suggested constructors for derived classes.
	 * 
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected WebPage(final PageParameters parameters)
	{
		this((IModel)null);
	}

	/**
	 * Get the body container for adding onLoad javascript to the body tag.
	 * 
	 * @return The body container
	 */
	public BodyContainer getBodyContainer()
	{
		return bodyContainer;
	}

	/**
	 * Gets the markup type for a WebPage, which is "html" by default. Support
	 * for pages in another markup language, such as VXML, would require the
	 * creation of a different Page subclass in an appropriate package under
	 * wicket.markup. To support VXML (voice markup), one might create the
	 * package wicket.markup.vxml and a subclass of Page called VoicePage.
	 * <p>
	 * Note: The markup type must be equal to the extension of the markup file.
	 * In the case of WebPages, it must always be "html".
	 * 
	 * @return Markup type for HTML
	 */
	public String getMarkupType()
	{
		return "html";
	}

	/**
	 * @see wicket.Page#configureResponse()
	 */
	protected void configureResponse()
	{
		super.configureResponse();

		final WebResponse response = getWebRequestCycle().getWebResponse();
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
	}

	/**
	 * @return The WebRequestCycle for this WebPage.
	 */
	protected final WebRequestCycle getWebRequestCycle()
	{
		return (WebRequestCycle)getRequestCycle();
	}

	/**
	 * Creates and returns a bookmarkable link to this application's home page.
	 * 
	 * @param id
	 *            Name of link
	 * @return Link to home page for this application
	 */
	protected final BookmarkablePageLink homePageLink(final String id)
	{
		return new BookmarkablePageLink(id, getApplication().getHomePage());
	}

	/**
	 * Common code executed by constructors
	 */
	private void commonInit()
	{
		// Add a Body container if the associated markup contains a <body> tag
		// get markup stream gracefully
		MarkupStream markupStream = getApplication().getMarkupCache().getMarkupStream(this, false);
		if (markupStream != null)
		{
			// The default <body> container. It can be accessed, replaced
			// and attribute modifiers can be attached. <body> tags without
			// wicket:id get automatically a wicket:id="body" assigned.
			// find the body tag
			while (markupStream.hasMore())
			{
				final MarkupElement element = markupStream.next();
				if (element instanceof ComponentTag)
				{
					final ComponentTag tag = (ComponentTag)element;
					if (tag.isOpen() && "body".equals(tag.getName())
							&& (tag.getNamespace() == null))
					{
						// Add a default container if the tag has the default
						// _body name
						if (BodyOnLoadHandler.BODY_ID.equals(tag.getId()))
						{
							add(new HtmlBodyContainer());
						}
						// remember the id of the tag
						bodyContainer = new BodyContainer(this, tag.getId());
						break;
					}
				}
			}
		}

		// TODO Post 1.2: If the concept proofs valuable we could add the header
		// container the same way instead of using a resolver. The advantages
		// would be that the header container be available at build time already
		// and not only at render time.
	}
}
