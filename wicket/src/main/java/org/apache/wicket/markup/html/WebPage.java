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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebRequestProcessor;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressor;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.JavascriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for HTML pages. This subclass of Page simply returns HTML when asked for its markup
 * type. It also has a method which subclasses can use to retrieve a bookmarkable link to the
 * application's home page.
 * <p>
 * WebPages can be constructed with any constructor when they are being used in a Wicket session,
 * but if you wish to link to a Page using a URL that is "bookmarkable" (which implies that the URL
 * will not have any session information encoded in it, and that you can call this page directly
 * without having a session first directly from your browser), you need to implement your Page with
 * a no-arg constructor or with a constructor that accepts a PageParameters argument (which wraps
 * any query string parameters for a request). In case the page has both constructors, the
 * constructor with PageParameters will be used.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Gwyn Evans
 */
public class WebPage extends Page implements INewBrowserWindowListener
{
	/**
	 * Tries to determine whether this page was opened in a new window or tab. If it is (and this
	 * checker were able to recognize that), a new page map is created for this page instance, so
	 * that it will start using it's own history in sync with the browser window or tab.
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
		 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(IHeaderResponse)
		 */
		@Override
		public final void renderHead(final IHeaderResponse headResponse)
		{
			Response response = headResponse.getResponse();
			final WebRequestCycle cycle = (WebRequestCycle)RequestCycle.get();
			final IRequestTarget target = cycle.getRequestTarget();

			// we don't want to render this for stateless pages
			if (webPage.isPageStateless())
			{
				return;
			}

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

			Session.PageMapAccessMetaData meta = session.getMetaData(Session.PAGEMAP_ACCESS_MDK);
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
				response.write("if (window.name=='' || window.name.indexOf('wicket') > -1) { window.name=\"");
				response.write("wicket-" + name);
				response.write("\"; }");
				JavascriptUtils.writeCloseTag(response);
			}
			else
			{
				// Here is our trickery to detect whether the current request
				// was made in a new window/ tab, in which case it should go in
				// a different page map so that we don't intermingle the history
				// of those windows
				CharSequence url = null;
				if (target instanceof IBookmarkablePageRequestTarget)
				{
					IBookmarkablePageRequestTarget current = (IBookmarkablePageRequestTarget)target;
					BookmarkablePageRequestTarget redirect = new BookmarkablePageRequestTarget(
						session.createAutoPageMapName(), current.getPageClass(),
						current.getPageParameters());
					url = cycle.urlFor(redirect);
				}
				else
				{
					url = webPage.urlFor(INewBrowserWindowListener.INTERFACE);
				}
				JavascriptUtils.writeOpenTag(response);
				response.write("if (window.name=='' || (window.name.indexOf('wicket') > -1 && window.name!='" +
					"wicket-" + name + "')) { window.location=\"");
				response.write(url);
				response.write("\" + (window.location.hash != null ? window.location.hash : \"\"); }");
				JavascriptUtils.writeCloseTag(response);
			}
		}
	}

	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WebPage.class);

	/** The resource references used for new window/tab support */
	private static ResourceReference cookiesResource = new ResourceReference(WebPage.class,
		"cookies.js");

	private static final long serialVersionUID = 1L;

	/**
	 * The url compressor that will compress the urls by collapsing the component path and listener
	 * interface
	 */
	private UrlCompressor compressor;

	/**
	 * Constructor. Having this constructor public means that your page is 'bookmarkable' and hence
	 * can be called/ created from anywhere.
	 */
	protected WebPage()
	{
		commonInit();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel<?> model)
	{
		super(model);
		commonInit();
	}

	/**
	 * @see Page#Page(org.apache.wicket.IPageMap)
	 */
	protected WebPage(final IPageMap pageMap)
	{
		super(pageMap);
		commonInit();
	}

	/**
	 * @see Page#Page(org.apache.wicket.IPageMap, org.apache.wicket.model.IModel)
	 */
	protected WebPage(final IPageMap pageMap, final IModel<?> model)
	{
		super(pageMap, model);
		commonInit();
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request. Having this
	 * constructor public means that your page is 'bookmarkable' and hence can be called/ created
	 * from anywhere. For bookmarkable pages (as opposed to when you construct page instances
	 * yourself, this constructor will be used in preference to a no-arg constructor, if both exist.
	 * Note that nothing is done with the page parameters argument. This constructor is provided so
	 * that tools such as IDEs will include it their list of suggested constructors for derived
	 * classes.
	 * 
	 * Please call this constructor (or the one with the pagemap) if you want to remember the
	 * pageparameters {@link #getPageParameters()}. So that they are reused for stateless links.
	 * 
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected WebPage(final PageParameters parameters)
	{
		super(parameters);
		commonInit();
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request. Having this
	 * constructor public means that your page is 'bookmarkable' and hence can be called/ created
	 * from anywhere. For bookmarkable pages (as opposed to when you construct page instances
	 * yourself, this constructor will be used in preference to a no-arg constructor, if both exist.
	 * Note that nothing is done with the page parameters argument. This constructor is provided so
	 * that tools such as IDEs will include it their list of suggested constructors for derived
	 * classes.
	 * 
	 * Please call this constructor (or the one without the pagemap) if you want to remember the
	 * pageparameters {@link #getPageParameters()}. So that they are reused for stateless links.
	 * 
	 * @param pageMap
	 *            The pagemap where the webpage needs to be constructed in.
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected WebPage(final IPageMap pageMap, final PageParameters parameters)
	{
		super(pageMap, parameters);
		commonInit();
	}

	/**
	 * Gets the markup type for a WebPage, which is "html" by default. Support for pages in another
	 * markup language, such as VXML, would require the creation of a different Page subclass in an
	 * appropriate package under org.apache.wicket.markup. To support VXML (voice markup), one might
	 * create the package org.apache.wicket.markup.vxml and a subclass of Page called VoicePage.
	 * 
	 * @return Markup type for HTML
	 */
	@Override
	public String getMarkupType()
	{
		return "html";
	}

	/**
	 * This method is called when the compressing coding and response strategies are configured in
	 * your Application object like this:
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
			log.error("Page " + clonedPage + " couldn't be cloned to move to another pagemap", e);
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
	@Override
	protected void configureResponse()
	{
		super.configureResponse();

		if (getWebRequestCycle().getResponse() instanceof WebResponse)
		{
			final WebResponse response = getWebRequestCycle().getWebResponse();
			setHeaders(response);
		}
	}

	/**
	 * Subclasses can override this to set there headers when the Page is being served. By default 2
	 * headers will be set
	 * 
	 * <pre>
	 * response.setHeader(&quot;Pragma&quot;, &quot;no-cache&quot;);
	 * response.setHeader(&quot;Cache-Control&quot;, &quot;no-cache, max-age=0, must-revalidate&quot;);
	 * </pre>
	 * 
	 * So if a Page wants to control this or doesn't want to set this info it should override this
	 * method and don't call super.
	 * 
	 * @param response
	 *            The WebResponse where set(Date)Header can be called on.
	 */
	protected void setHeaders(WebResponse response)
	{
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

	@Override
	protected void onAfterRender()
	{
		super.onAfterRender();
		if (Application.DEVELOPMENT.equals(getApplication().getConfigurationType()))
		{
			HtmlHeaderContainer header = (HtmlHeaderContainer)visitChildren(new IVisitor<Component>()
			{
				public Object component(Component component)
				{
					if (component instanceof HtmlHeaderContainer)
					{
						return component;
					}
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});
			if (header == null)
			{
				// the markup must at least contain a <body> tag for wicket to automatically
				// create a HtmlHeaderContainer. Log an error if no header container
				// was created but any of the components or behavior want to contribute
				// something to the header.
				header = new HtmlHeaderContainer(HtmlHeaderSectionHandler.HEADER_ID);
				add(header);

				Response orgResponse = getRequestCycle().getResponse();
				try
				{
					final StringResponse response = new StringResponse();
					getRequestCycle().setResponse(response);

					// Render all header sections of all components on the page
					renderHead(header);

					// Make sure all Components interested in contributing to the header
					// and there attached behaviors are asked.
					final HtmlHeaderContainer finalHeader = header;
					visitChildren(new IVisitor<Component>()
					{
						/**
						 * @see org.apache.wicket.Component.IVisitor#component(org.apache.wicket.Component)
						 */
						public Object component(Component component)
						{
							component.renderHead(finalHeader);
							return CONTINUE_TRAVERSAL;
						}
					});
					response.close();

					if (response.getBuffer().length() > 0)
					{
						// @TODO it is not yet working properly. JDo to fix it
						log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
						log.error("You probably forgot to add a <body> or <header> tag to your markup since no Header Container was \n" +
							"found but components where found which want to write to the <head> section.\n" +
							response.getBuffer());
						log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
					}
				}
				catch (Exception e)
				{
					// just swallow this exception, there isn't much we can do about.
					log.error("header/body check throws exception", e);
				}
				finally
				{
					this.remove(header);
					getRequestCycle().setResponse(orgResponse);
				}
			}
		}
	}
}
