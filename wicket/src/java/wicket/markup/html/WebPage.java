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

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AccessStackPageMap;
import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.MetaDataKey;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.ResourceReference;
import wicket.Response;
import wicket.WicketRuntimeException;
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
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.settings.IRequestCycleSettings.RenderStrategy;
import wicket.util.collections.ArrayListStack;
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

	/** meta data key for missing body tags logging. */
	private static final MetaDataKey MISSING_BODY_TAG_LOGGED_MDK = new MetaDataKey(
			MissingBodyTagLoggedMetaData.class)
	{
		private static final long serialVersionUID = 1L;
	};

	/** meta data for missing body tags logging. */
	private static final class MissingBodyTagLoggedMetaData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		Set/* <Class> */<Class<? extends WebPage>> missingBodyTagsLogged = new HashSet<Class<? extends WebPage>>(
				1);
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
	protected WebPage(final IModel<T> model)
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
	protected WebPage(final PageMap pageMap, final IModel<T> model)
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
		this((IModel<T>)null);
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
							new HtmlBodyContainer(this, tag.getId());
						}
						// remember the id of the tag
						bodyContainer = new BodyContainer(this, tag.getId());
						break;
					}
				}
			}
		}
		else
		{
			throw new WicketRuntimeException(
					"Each Page must have associated markup. Unable to find the markup file for Page: " 
					+ this.toString());
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
	@Override
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
		private Model<String> onUnLoadModel;

		/**
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public final void renderHead(final Response response)
		{
			if (WebPage.this.isStateless() == true)
				return;
			
			final WebRequestCycle cycle = (WebRequestCycle)getRequestCycle();
			final IRequestTarget target = cycle.getRequestTarget();

			int initialAccessStackSize = 0;
			if (getApplication().getRequestCycleSettings().getRenderStrategy() == RenderStrategy.REDIRECT_TO_RENDER
					&& target instanceof RedirectPageRequestTarget)
			{
				initialAccessStackSize = 1;
			}

			// Here is our trickery to detect whether the current request was
			// made in a new window/ tab, in which case it should go in a
			// different page map so that we don't intermangle the history of
			// those windows
			final ArrayListStack accessStack;
			if (getPageMap() instanceof AccessStackPageMap)
			{
				accessStack = ((AccessStackPageMap)getPageMap()).getAccessStack();
			}
			else
			{
				accessStack = new ArrayListStack();
			}
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
			final BodyContainer body = getBodyContainer();
			final Cookie[] cookies = cycle.getWebRequest().getCookies();
			if ((cookies == null && accessStack.size() > initialAccessStackSize) || body == null)
			{
				// If the browser does not support cookies, we try to work
				// with the history
				if (cookies != null && body == null && log.isWarnEnabled())
				{
					// issue a warning; the cookies based alternative would
					// have worked, but unfortunately, it doesn't now
					// because it misses the body tag
					Application app = getApplication();
					MissingBodyTagLoggedMetaData meta = (MissingBodyTagLoggedMetaData)app
							.getMetaData(MISSING_BODY_TAG_LOGGED_MDK);
					if (meta == null)
					{
						meta = new MissingBodyTagLoggedMetaData();
						app.setMetaData(MISSING_BODY_TAG_LOGGED_MDK, meta);
					}
					Class<? extends WebPage> pageClass = WebPage.this.getClass();
					if (!meta.missingBodyTagsLogged.contains(pageClass))
					{
						log
								.warn("Page with class "
										+ pageClass.getName()
										+ " does not have a body tag. It is advisable to have"
										+ " a body tag pair, as multi window support might be problematic without.");
						meta.missingBodyTagsLogged.add(pageClass);
					}
				}

				if (accessStack.size() > initialAccessStackSize)
				{
					JavascriptUtils.writeOpenTag(response);
					response
							.write("if((history.length == 0 && document.all) || (history.length == 1 && !document.all)){ if (!document.all) window.location.hash='some-random-hash!'; document.location.href = '");
					response.write(url);
					response.write("'}");
					JavascriptUtils.writeCloseTag(response);
				}
			}
			else
			{
				// We seem to have cookie support. Write out a script that
				// adds a cookie on page load, and removes it on page unload.
				// Whenever the cookie is not unloaded (it's there on load),
				// we know that we have a new window/ tab instance
				if (onUnLoadModel == null)
				{
					onUnLoadModel = new Model<String>()
					{
						private static final long serialVersionUID = 1L;

						/**
						 * @see wicket.model.Model#getObject()
						 */
						@Override
						public String getObject()
						{
							Application application = Application.get();
							return "deleteWicketCookie('pm-" + getPageMap().getName()
									+ application.getApplicationSettings().getContextPath()
									+ application.getApplicationKey() + "');";
						}
					};
					body.addOnUnLoadModifier(onUnLoadModel, null);
				}
				Application application = Application.get();
				final String pageMapName = getPageMap().getName();
				JavascriptUtils.writeJavascriptUrl(response, urlFor(cookiesResource));
				JavascriptUtils.writeOpenTag(response);
				response.write("var pagemapcookie = getWicketCookie('pm-");
				response.write(pageMapName);
				response.write(application.getApplicationSettings().getContextPath());
				response.write(application.getApplicationKey());
				response.println("');");
				response.write("if(!pagemapcookie && pagemapcookie != '1'){setWicketCookie('pm-");
				response.write(pageMapName);
				response.write(application.getApplicationSettings().getContextPath());
				response.write(application.getApplicationKey());
				response.println("',1);}");
				response.write("else {document.location.href = '");
				response.write(url);
				response.println("';}");
				JavascriptUtils.writeCloseTag(response);
			}
		}
	}
}
