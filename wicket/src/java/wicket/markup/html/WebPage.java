/*
 * $Id: WebPage.java 5399 2006-04-17 10:23:38Z joco01 $ $Revision$ $Date:
 * 2006-04-17 12:23:38 +0200 (Mo, 17 Apr 2006) $
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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.MetaDataKey;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.ResourceReference;
import wicket.Response;
import wicket.Session;
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
import wicket.model.Model;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.protocol.http.request.urlcompressing.URLCompressor;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingCodingStrategy;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingTargetResolverStrategy;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.util.lang.Objects;
import wicket.util.string.JavascriptUtils;

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
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(WebPage.class);

	/** meta data key for missing body tags logging. */
	private static final MetaDataKey PAGEMAP_ACCESS_MDK = new MetaDataKey(
			PageMapAccessMetaData.class)
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * meta data for recording map map access.
	 */
	private static final class PageMapAccessMetaData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		Set pageMapNames = new HashSet(1);
	}

	/** The resource references used for new window/tab support */
	private static ResourceReference cookiesResource = new ResourceReference(WebPage.class,
			"cookies.js");

	/** The body container */
	private BodyContainer bodyContainer;

	/**
	 * The url compressor that will compress the urls by collapsing the
	 * component path and listener interface
	 */
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
	 * Get a facade to the body container for adding onLoad javascript to the
	 * body tag.
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
	 * Common code executed by constructors.
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
						// name
						if (BodyOnLoadHandler.BODY_ID.equals(tag.getId()))
						{
							add(new HtmlBodyContainer(tag.getId()));
						}
						// remember the id of the tag
						bodyContainer = new BodyContainer(this, tag.getId());
						break;
					}
				}
			}
		}

		// if automatic multi window support is on, add a page checker instance
		if (getApplication().getPageSettings().getAutomaticMultiWindowSupport())
		{
			add(new PageMapChecker());
		}

		// TODO Post 1.2: If the concept proofs valuable we could add the header
		// container the same way instead of using a resolver. The advantages
		// would be that the header container be available at build time already
		// and not only at render time.
	}

	/**
	 * This method is called when the compressing coding and response stategies
	 * are configured in your Application object like this:
	 * 
	 * <pre>
	 * protected IRequestCycleProcessor newRequestCycleProcessor()
	 * {
	 * 	return new CompoundRequestCycleProcessor(new WebURLCompressingCodingStrategy(),
	 * 			new WebURLCompressingTargetResolverStrategy(), null, null, null);
	 * }
	 * </pre>
	 * 
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
	 * @see wicket.markup.html.INewBrowserWindowListener#onNewBrowserWindow()
	 */
	public void onNewBrowserWindow()
	{
		// if the browser reports a history of 0 then make a new webpage
		WebPage clonedPage = this;
		try
		{
			clonedPage = (WebPage)Objects.cloneObject(this);
		}
		catch (Exception e)
		{
			log.error("Page " + clonedPage + " couldn't be cloned to move to another pagemap", e);
		}
		final PageMap map = getSession().createAutoPageMap();
		clonedPage.moveToPageMap(map);
		setResponsePage(clonedPage);
	}

	/**
	 * Tries to determine whether this page was opened in a new window or tab.
	 * If it is (and this checker were able to recognize that), a new page map
	 * is created for this page instance, so that it will start using it's own
	 * history in sync with the browser window or tab.
	 */
	private final class PageMapChecker extends AbstractBehavior implements IHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/** The unload model for deleting the pagemap cookie */
		private Model onUnLoadModel;

		/**
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public final void renderHead(final Response response)
		{
			final WebRequestCycle cycle = (WebRequestCycle)getRequestCycle();
			final IRequestTarget target = cycle.getRequestTarget();

			String name = getPageMap().getName();
			if (name == null)
			{
				name = "wicket:default";
			}
			else
			{
				name = name.replace('"', '_');
			}

			Session session = getSession();

			PageMapAccessMetaData meta = (PageMapAccessMetaData)session
					.getMetaData(PAGEMAP_ACCESS_MDK);
			if (meta == null)
			{
				meta = new PageMapAccessMetaData();
				session.setMetaData(PAGEMAP_ACCESS_MDK, meta);
			}
			boolean firstAccess = false;
			if (!meta.pageMapNames.contains(name))
			{
				firstAccess = true;
				meta.pageMapNames.add(name);
			}

			// Here is our trickery to detect whether the current request was
			// made in a new window/ tab, in which case it should go in a
			// different page map so that we don't intermangle the history of
			// those windows
			CharSequence url = null;
			if (target instanceof IBookmarkablePageRequestTarget)
			{
				IBookmarkablePageRequestTarget current = (IBookmarkablePageRequestTarget)target;
				BookmarkablePageRequestTarget redirect = new BookmarkablePageRequestTarget(
						getSession().createAutoPageMapName(), current.getPageClass(), current
								.getPageParameters());
				url = cycle.urlFor(redirect);
			}
			else
			{
				url = urlFor(INewBrowserWindowListener.INTERFACE);
			}
			if (firstAccess)
			{
				// this is the first access to the pagemap, set window.name
				JavascriptUtils.writeOpenTag(response);
				response.write("window.name=\"");
				response.write(name);
				response.write("\";");
				JavascriptUtils.writeCloseTag(response);
			}
			else
			{
				JavascriptUtils.writeOpenTag(response);
				response.write("if (window.name=='') { window.location=\"");
				response.write(url);
				response.write("\"; }");
				JavascriptUtils.writeCloseTag(response);
			}
		}
	}
}
