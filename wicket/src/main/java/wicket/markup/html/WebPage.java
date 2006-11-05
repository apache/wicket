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

import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.MetaDataKey;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.ResourceReference;
import wicket.Response;
import wicket.Session;
import wicket.behavior.AbstractBehavior;
import wicket.markup.MarkupFragment;
import wicket.markup.html.internal.HeaderContainer;
import wicket.markup.html.internal.HtmlBodyContainer;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.parser.filter.BodyOnLoadHandler;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.protocol.http.WebSession;
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
 * @param <T>
 *            The type
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Gwyn Evans
 */
public class WebPage<T> extends Page<T> implements INewBrowserWindowListener
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(WebPage.class);

	static
	{
		// register "wicket:head"
		WicketTagIdentifier.registerWellKnownTagName("head");
	}

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

		Set<String> pageMapNames = new HashSet<String>(1);
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
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel<T> model)
	{
		super(model);
	}

	/**
	 * @see Page#Page(PageMap)
	 */
	protected WebPage(final PageMap pageMap)
	{
		super(pageMap);
	}

	/**
	 * @see Page#Page(PageMap, IModel)
	 */
	protected WebPage(final PageMap pageMap, final IModel<T> model)
	{
		super(pageMap, model);
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
		this((IModel<T>)null);
	}

	/**
	 * Get a facade to the body container for adding onLoad javascript to the
	 * body tag.
	 * 
	 * @return The body container
	 */
	public final BodyContainer getBodyContainer()
	{
		return bodyContainer;
	}

	/**
	 * Gets the container for the &lt;head&gt; tag, which will also render the
	 * &lt;wicket:head&gt; tags.
	 * 
	 * @return The header container
	 */
	public final HeaderContainer getHeaderContainer()
	{
		return (HeaderContainer)get(HtmlHeaderSectionHandler.HEADER_ID);
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
	@Override
	public String getMarkupType()
	{
		return "html";
	}

	/**
	 * @see wicket.Page#configureResponse()
	 */
	@Override
	protected void configureResponse()
	{
		super.configureResponse();

		final WebResponse response = getResponse();
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate"); // no-store
	}

	/**
	 * @see wicket.RequestCycle#getRequest()
	 */
	@Override
	public WebRequest getRequest()
	{
		return (WebRequest)super.getRequest();
	}

	/**
	 * @see wicket.RequestCycle#getResponse()
	 */
	@Override
	public WebResponse getResponse()
	{
		return (WebResponse)super.getResponse();
	}

	/**
	 * @return Session as a WebSession
	 */
	@Override
	public WebSession getSession()
	{
		return (WebSession)super.getSession();
	}

	/**
	 * @return The WebRequestCycle for this WebPage.
	 */
	@Override
	public WebRequestCycle getRequestCycle()
	{
		return (WebRequestCycle)super.getRequestCycle();
	}

	/**
	 * Creates and returns a bookmarkable link to this application's home page.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            Name of link
	 * @return Link to home page for this application
	 */
	protected final BookmarkablePageLink homePageLink(MarkupContainer parent, final String id)
	{
		return new BookmarkablePageLink(parent, id, getApplication().getHomePage());
	}

	/**
	 * Common code executed by constructors.
	 */
	protected void onAssociatedMarkupLoaded(final MarkupFragment markup)
	{
		// Create a body container, assuming that all HTML pages require a
		// <body> tag
		new HtmlBodyContainer(this, BodyOnLoadHandler.BODY_ID);
		
		// Add this little helper to the page
		this.bodyContainer = new BodyContainer(this, BodyOnLoadHandler.BODY_ID);

		// The <head> container. It can be accessed, replaced
		// and attribute modifiers can be attached.
		// HtmlHeaderSectionHandler guarantees the <head> tag does exist.
		new HtmlHeaderContainer(this, HtmlHeaderSectionHandler.HEADER_ID);

		// if automatic multi window support is on, add a page checker instance
		if (getApplication().getPageSettings().getAutomaticMultiWindowSupport())
		{
			add(new PageMapChecker());
		}

		// Do all the default staff
		super.onAssociatedMarkupLoaded(markup);
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
	 * @see wicket.markup.html.INewBrowserWindowListener#onNewBrowserWindow()
	 */
	public void onNewBrowserWindow()
	{
		// if the browser reports a history of 0 then make a new webpage
		WebPage clonedPage = this;
		try
		{
			clonedPage = Objects.cloneObject(this);
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
		public final void renderHead(final IHeaderResponse headResponse)
		{
			Response response = headResponse.getResponse();

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
				response.write("if (window.name=='') { window.name=\"");
				response.write(name);
				response.write("\"; }");
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
