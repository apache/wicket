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

import java.util.Iterator;

import wicket.ApplicationPages;
import wicket.Component;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.WicketRuntimeException;
import wicket.markup.html.ajax.IAjaxListener;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.model.IModel;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;

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
public class WebPage extends Page implements IHeaderRenderer
{
	private static final long serialVersionUID = 1L;

	/** Components contribution to <body onload="..." */
	private String bodyOnLoad;

	/**
	 * Constructor. Having this constructor public means that you page is
	 * 'bookmarkable' and hence can be called/ created from anywhere.
	 */
	protected WebPage()
	{
		super();
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
		this();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel model)
	{
		super(model);
	}

	/**
	 * Returns a bookmarkable URL that references a given page class using a
	 * given set of page parameters. Since the URL which is returned contains
	 * all information necessary to instantiate and render the page, it can be
	 * stored in a user's browser as a stable bookmark.
	 * 
	 * @param pageMapName
	 *            Name of pagemap to use
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public String urlFor(final String pageMapName, final Class pageClass,
			final PageParameters parameters)
	{
		if (pageClass == null)
		{
			throw new NullPointerException("argument pageClass may not be null");
		}

		final WebRequestCycle cycle = getWebRequestCycle();
		final StringBuffer buffer = urlPrefix(cycle);
		if (pageMapName == null)
		{
			appendPageMapName(buffer);
		}
		else
		{
			buffer.append("?pagemap=");
			buffer.append(pageMapName);
			buffer.append('&');
		}
		ApplicationPages pages = getApplicationPages();

		// "bookmarkablePage=xxx" is required if PageParameters exist,
		// some sort of redirection takes place, or if we're not dealing
		// with the homepage.
		if ((parameters != null && !parameters.isEmpty())
				|| pages.getHomePageRenderStrategy() != ApplicationPages.NO_REDIRECT
				|| !pages.getHomePage().equals(pageClass))
		{
			buffer.append("bookmarkablePage=");
			String pageReference = cycle.getApplication().getPages().aliasForClass(pageClass);
			if (pageReference == null)
			{
				pageReference = pageClass.getName();
			}
			buffer.append(pageReference);
		}
		if (parameters != null)
		{
			for (final Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
			{
				final String key = (String)iterator.next();
				final String value = parameters.getString(key);
				if (value != null)
				{
					final String escapedValue = Strings.escapeMarkup(value);
					buffer.append('&');
					buffer.append(key);
					buffer.append('=');
					buffer.append(escapedValue);
				}
			}
		}
		if (buffer.charAt(buffer.length() - 1) == '?')
		{
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return cycle.getResponse().encodeURL(buffer.toString());
	}

	/**
	 * Returns a URL that references a given interface on a component. When the
	 * URL is requested from the server at a later time, the interface will be
	 * called. A URL returned by this method will not be stable across sessions
	 * and cannot be bookmarked by a user.
	 * 
	 * @param component
	 *            The component to reference
	 * @param listenerInterface
	 *            The listener interface on the component
	 * @return A URL that encodes a page, component and interface to call
	 */
	public String urlFor(final Component component, final Class listenerInterface)
	{
		// Ensure that component instanceof listenerInterface
		if (!listenerInterface.isAssignableFrom(component.getClass()))
		{
			throw new WicketRuntimeException("The component " + component + " of class "
					+ component.getClass() + " does not implement " + listenerInterface);
		}

		// Buffer for composing URL
		final WebRequestCycle cycle = getWebRequestCycle();
		final StringBuffer buffer = urlPrefix(cycle);
		appendPageMapName(buffer);
		buffer.append("path=");
		buffer.append(component.getPath());
		int versionNumber = component.getPage().getCurrentVersionNumber();
		if (versionNumber > 0)
		{
			buffer.append("&version=");
			buffer.append(versionNumber);
		}
		String listenerName = Classes.name(listenerInterface);
		if (!"IRedirectListener".equals(listenerName))
		{
			buffer.append("&interface=");
			buffer.append(listenerName);
		}

		// add an extra parameter for regconition in case we are targetting a
		// dispatched handler
		if (IAjaxListener.class.isAssignableFrom(listenerInterface))
		{
			buffer.append("&dispatched=true");
		}

		return cycle.getResponse().encodeURL(buffer.toString());
	}

	/**
	 * @param path
	 *            The path
	 * @return The url for the path
	 */
	public String urlFor(final String path)
	{
		if ((path == null) || (path.trim().length() == 0))
		{
			return urlPrefix(getWebRequestCycle()).toString();
		}
		else
		{
			return urlPrefix(getWebRequestCycle()) + "/" + path;
		}
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
		return new BookmarkablePageLink(id, getApplicationPages().getHomePage());
	}

	/**
	 * Appends any pagemap name to the buffer
	 * 
	 * @param buffer
	 *            The string buffer to append to
	 */
	private void appendPageMapName(final StringBuffer buffer)
	{
		final PageMap pageMap = getPageMap();
		if (!pageMap.isDefault())
		{
			buffer.append("?pagemap=");
			buffer.append(pageMap.getName());
			buffer.append('&');
		}
		else
		{
			buffer.append('?');
		}
	}

	/**
	 * Creates a prefix for a url.
	 * 
	 * @param cycle
	 *            The web request cycle
	 * @return Prefix for URLs including the context path and servlet path.
	 */
	private StringBuffer urlPrefix(final WebRequestCycle cycle)
	{
		final StringBuffer buffer = new StringBuffer();
		final WebRequest request = cycle.getWebRequest();
		if (request != null)
		{
			final String contextPath = request.getContextPath();
			buffer.append(contextPath);
			String path = request.getServletPath();
			if (path == null || "".equals(path))
			{
				path = "/";
			}
			buffer.append(path);
		}

		return buffer;
	}

	/**
	 * THIS IS NOT PART OF WICKETS PUBLIC API. DO NOT USE IT YOURSELF.
	 * <p>
	 * Invoked by HtmlHeaderContainer it'll ask all child components of the Page
	 * if they have something to contribute to the &lt;head&gt; section of the
	 * HTML output. Every component interested must implement
	 * IHeaderContributor.
	 * <p>
	 * Note: HtmlHeaderContainer will be removed from the component hierachie at
	 * the end of the request (@see #onEndRequest()) and thus can not transport
	 * status from one request to the next. This is true for all components
	 * added to the header.
	 * 
	 * @see IHeaderContributor
	 * @param container
	 *            The header component container
	 */
	public final void renderHeaderSections(final HtmlHeaderContainer container)
	{
		// A components interested in contributing to the header must
		// implement IHeaderContributor.
		visitChildren(IHeaderContributor.class, new IVisitor()
		{
			/**
			 * @see wicket.Component.IVisitor#component(wicket.Component)
			 */
			public Object component(Component component)
			{
				if (component.isVisible())
				{
					if (component instanceof IHeaderContributor)
					{
						((IHeaderContributor)component).renderHead(container);
					}
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * THIS IS NOT PART OF THE PUBLIC API.
	 * 
	 * Get what will be appended to the page markup's body onload attribute
	 * 
	 * @return The onload attribute
	 */
	public String getBodyOnLoad()
	{
		return this.bodyOnLoad;
	}

	/**
	 * THIS IS NOT PART OF THE PUBLIC API.
	 * 
	 * Append string to body onload attribute
	 * 
	 * @param onLoad
	 *            Attribute value to be appended
	 */
	public final void appendToBodyOnLoad(final String onLoad)
	{
		if (onLoad != null)
		{
			// Tell the page to change the Page's
			// body tags.
			if (this.bodyOnLoad == null)
			{
				this.bodyOnLoad = onLoad;
			}
			else
			{
				this.bodyOnLoad = this.bodyOnLoad + onLoad;
			}
		}
	}

	/**
	 * Remove the header component and all its children from the component
	 * hierachie. Be aware, thus you can not transfer state from one request to
	 * another.
	 * 
	 * @see wicket.Component#onEndRequest()
	 */
	protected void onEndRequest()
	{
		final Component header = get(HtmlHeaderSectionHandler.HEADER_ID);
		if (header != null)
		{
			this.remove(header);
		}

		super.onEndRequest();
	}
}