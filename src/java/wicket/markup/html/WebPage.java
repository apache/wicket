/*
 * $Id$
 * $Revision$ $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.INewBrowserWindowListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Response;
import wicket.behavior.AbstractBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.TagUtils;
import wicket.markup.html.internal.HtmlBodyContainer;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.parser.filter.BodyOnLoadHandler;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.model.IModel;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.protocol.http.request.urlcompressing.URLCompressor;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingCodingStrategy;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingTargetResolverStrategy;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.settings.IRequestCycleSettings;
import wicket.util.collections.ArrayListStack;
import wicket.util.lang.Objects;

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
public class WebPage extends Page implements INewBrowserWindowListener
{
	/** log. */
	private static Log log = LogFactory.getLog(WebPage.class);
	
	/** Log. */
	// private static final Log log = LogFactory.getLog(WebPage.class);
	private static final long serialVersionUID = 1L;

	/** The body container */
	private BodyContainer bodyContainer;

	private URLCompressor compressor;

	/**
	 * Constructor. Having this constructor public means that your page is
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
	 * Having this constructor public means that your page is 'bookmarkable' and
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
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate"); // no-store
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
		MarkupStream markupStream = getAssociatedMarkupStream(false);
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
					if (tag.isOpen() && TagUtils.isBodyTag(tag))
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

		add(new PageMapChecker());

		// TODO Post 1.2: If the concept proofs valuable we could add the header
		// container the same way instead of using a resolver. The advantages
		// would be that the header container be available at build time already
		// and not only at render time.
	}

	/**
	 * This method is called when the compressing coding and response stategies are 
	 * configured in your Application object like this:
	 * 
	 *  <pre>
	 * protected IRequestCycleProcessor newRequestCycleProcessor()
	 * {
	 *   return new CompoundRequestCycleProcessor(new WebURLCompressingCodingStrategy(),new WebURLCompressingTargetResolverStrategy(),null,null,null);
	 * }
	 *  </pre>
	 * @return The URLCompressor for this webpage. 
	 * 
	 * @since 1.2
	 * 
	 * @see WebURLCompressingCodingStrategy
	 * @see WebURLCompressingTargetResolverStrategy
	 * @see URLCompressor
	 */
	public final URLCompressor getUrlCompressor()
	{
		if (compressor == null) 
		{
			compressor = new URLCompressor();
		}
		return compressor;
	}	
	/**
	 * 
	 * @see wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		// This code can not go into HtmlHeaderContainer as
		// header.onEndRequest() is executed inside an iterator 
		// and you can only call container.remove() which 
		// is != iter.remove(). And the iterator is not available 
		// inside onEndRequest(). Obviously WebPage.onEndRequest() 
		// is invoked outside the iterator loop.
		final Component header = get(HtmlHeaderSectionHandler.HEADER_ID);
		if (header != null)
		{
			this.remove(header);
		}
		super.onDetach();
	}

	/**
	 * @see wicket.INewBrowserWindowListener#onNewBrowserWindow()
	 */
	public void onNewBrowserWindow()
	{
		// if the browser reports a history of 0 then make a new webpage
		WebPage clonedPage = this;
		try
		{
			clonedPage = (WebPage)Objects.cloneObject(this);
			final PageMap map = getSession().createAutoPageMap();
			clonedPage.moveToPageMap(map);
		} 
		catch (Exception e)
		{
			log.error("Page couldn't be cloned to move to another pagemap: " + clonedPage.getClass(), e);
		}
		setResponsePage(clonedPage);
	}

	/**
	 * 
	 */
	private class PageMapChecker extends AbstractBehavior implements IHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public void renderHead(final Response response)
		{
			final RequestCycle cycle = getRequestCycle();
			final IRequestTarget target = cycle.getRequestTarget();
			
			int initialAccessStackSize = 0;
			if (getApplication().getRequestCycleSettings().getRenderStrategy() == IRequestCycleSettings.REDIRECT_TO_RENDER
					&& target instanceof RedirectPageRequestTarget)
			{
				initialAccessStackSize = 1;
			}
			
			// Javascript: for the most part it is getting the same functionality
			// for multiple browsers. What the end result is, is that a call back
			// to the server is made at the moment it detects that there is  
			// no history. because that could mean that a page is opened in 
			// a new tab/window without that page being in its own pagemap, 
			// that redirect will put that page in its own pagemap.
			final ArrayListStack accessStack = getPageMap().getAccessStack();
			if (accessStack.size() > initialAccessStackSize)
			{
				response.write("<script language=\"JavaScript\">if((history.length == 0 && document.all) || (history.length == 1 && !document.all)){ if (!document.all) window.location.hash='some-random-hash!'; document.location.href = '");
				if (target instanceof IBookmarkablePageRequestTarget)
				{
					IBookmarkablePageRequestTarget current = (IBookmarkablePageRequestTarget)target; 
					BookmarkablePageRequestTarget redirect = new BookmarkablePageRequestTarget(getSession().createAutoPageMapName(),
							current.getPageClass(), current.getPageParameters());
					response.write(cycle.urlFor(redirect));
				}
				else
				{
					response.write(urlFor(INewBrowserWindowListener.INTERFACE));
				}
				response.write("'}</script>");
			}
		}
	}
}
