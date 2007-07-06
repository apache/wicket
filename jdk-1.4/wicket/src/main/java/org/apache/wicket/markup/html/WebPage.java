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
import org.apache.wicket.IPageMap;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.html.internal.HtmlBodyContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebRequestProcessor;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressor;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.JavascriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	/**
	 * Tries to determine whether this page was opened in a new window or tab.
	 * If it is (and this checker were able to recognize that), a new page map
	 * is created for this page instance, so that it will start using it's own
	 * history in sync with the browser window or tab.
	 */
	private static final class PageMapChecker extends AbstractBehavior
			implements
				IHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		private final WebPage webPage;

		/**
		 * Construct.
		 * 
		 * @param webPage
		 */
		PageMapChecker(WebPage webPage)
		{
			this.webPage = webPage;
		}

		/**
		 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.Response)
		 */
		public final void renderHead(final IHeaderResponse headResponse)
		{
			Response response = headResponse.getResponse();
			final WebRequestCycle cycle = (WebRequestCycle)RequestCycle.get();
			final IRequestTarget target = cycle.getRequestTarget();

			IPageMap pageMap = webPage.getPageMap();
			String name = pageMap.getName();
			if (name == null)
			{
				name = "wicket:default";
			}
			else
			{
				name = name.replace('"', '_');
			}

			Session session = Session.get();

			Session.PageMapAccessMetaData meta = (Session.PageMapAccessMetaData)session
					.getMetaData(Session.PAGEMAP_ACCESS_MDK);
			if (meta == null)
			{
				meta = new Session.PageMapAccessMetaData();
				session.setMetaData(Session.PAGEMAP_ACCESS_MDK, meta);
			}
			boolean firstAccess = meta.add(pageMap);

			if (firstAccess)
			{
				// this is the first access to the pagemap, set window.name
				JavascriptUtils.writeOpenTag(response);
				response
						.write("if (window.name=='' || window.name.indexOf('wicket') > -1) { window.name=\"");
				response.write(name);
				response.write("\"; }");
				JavascriptUtils.writeCloseTag(response);
			}
			else
			{
				// Here is our trickery to detect whether the current request
				// was
				// made in a new window/ tab, in which case it should go in a
				// different page map so that we don't intermangle the history
				// of
				// those windows
				CharSequence url = null;
				if (target instanceof IBookmarkablePageRequestTarget)
				{
					IBookmarkablePageRequestTarget current = (IBookmarkablePageRequestTarget)target;
					BookmarkablePageRequestTarget redirect = new BookmarkablePageRequestTarget(
							session.createAutoPageMapName(), current.getPageClass(), current
									.getPageParameters());
					url = cycle.urlFor(redirect);
				}
				else
				{
					url = webPage.urlFor(INewBrowserWindowListener.INTERFACE);
				}
				JavascriptUtils.writeOpenTag(response);
				response
						.write("if (window.name=='' || (window.name.indexOf('wicket') > -1 && window.name!='" +
								name + "')) { window.location=\"");
				response.write(url);
				response.write("\"; }");
				JavascriptUtils.writeCloseTag(response);
			}
		}
	}

	/** log. */
	private static final Logger _log = LoggerFactory.getLogger(WebPage.class);

	/** The resource references used for new window/tab support */
	private static ResourceReference cookiesResource = new ResourceReference(WebPage.class,
			"cookies.js");

	private static final long serialVersionUID = 1L;

	/**
	 * Boolean flag that represents whether or not we have already added a
	 * {@link HtmlBodyContainer} to this page or not
	 */
	private boolean bodyContainerAdded = false;

	/**
	 * The url compressor that will compress the urls by collapsing the
	 * component path and listener interface
	 */
	private UrlCompressor compressor;

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
	protected WebPage(final IPageMap pageMap)
	{
		super(pageMap);
		commonInit();
	}

	/**
	 * @see Page#Page(PageMap, IModel)
	 */
	protected WebPage(final IPageMap pageMap, final IModel model)
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
		super(parameters);
	}

	/**
	 * Gets the markup type for a WebPage, which is "html" by default. Support
	 * for pages in another markup language, such as VXML, would require the
	 * creation of a different Page subclass in an appropriate package under
	 * org.apache.wicket.markup. To support VXML (voice markup), one might
	 * create the package org.apache.wicket.markup.vxml and a subclass of Page
	 * called VoicePage.
	 * 
	 * @return Markup type for HTML
	 */
	public String getMarkupType()
	{
		return "html";
	}

	/**
	 * This method is called when the compressing coding and response stategies
	 * are configured in your Application object like this:
	 * 
	 * <pre>
	 * protected IRequestCycleProcessor newRequestCycleProcessor()
	 * {
	 * 	return new UrlCompressingWebRequestProcessor();
	 * }
	 * </pre>
	 * 
	 * @return The URLCompressor for this webpage.
	 * 
	 * @since 1.2
	 * 
	 * @see UrlCompressingWebRequestProcessor
	 * @see UrlCompressor
	 */
	public final UrlCompressor getUrlCompressor()
	{
		if (compressor == null)
		{
			compressor = new UrlCompressor();
		}
		return compressor;
	}

	/**
	 * @see org.apache.wicket.markup.html.INewBrowserWindowListener#onNewBrowserWindow()
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
			_log.error("Page " + clonedPage + " couldn't be cloned to move to another pagemap", e);
		}
		final IPageMap map = getSession().createAutoPageMap();
		clonedPage.moveToPageMap(map);
		setResponsePage(clonedPage);
	}

	/**
	 * Common code executed by constructors.
	 */
	private void commonInit()
	{
		// if automatic multi window support is on, add a page checker instance
		if (getApplication().getPageSettings().getAutomaticMultiWindowSupport())
		{
			add(new PageMapChecker(this));
		}
	}

	/**
	 * @see org.apache.wicket.Page#configureResponse()
	 */
	protected void configureResponse()
	{
		super.configureResponse();

		if (getWebRequestCycle().getResponse() instanceof WebResponse) {
			final WebResponse response = getWebRequestCycle().getWebResponse();
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate"); // no-store
		}
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

	protected void onBeforeRender()
	{
		super.onBeforeRender();

		if (!bodyContainerAdded)
		{
			// Add a Body container if the associated markup contains a <body>
			// tag get markup stream gracefully
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
							// Add a default container if the tag has the
							// default name
							if (HtmlBodyContainer.BODY_ID.equals(tag.getId()))
							{
								add(new HtmlBodyContainer(tag.getId()));
							}
							bodyContainerAdded = true;
							break;
						}
					}
				}
			}
		}

	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onDetach()
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
}
