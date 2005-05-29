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

import wicket.Component;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.WicketRuntimeException;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.model.IModel;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.lang.Classes;

/**
 * Base class for HTML pages. This subclass of Page simply returns HTML when
 * asked for its markup type. It also has a method which subclasses can use to
 * retrieve a bookmarkable link to the application's home page.
 * <p>
 * WebPages can be constructed with any
 * constructor when they are being used in a Wicket session, but if you wish to
 * link to a Page using a URL that is "bookmarkable" (which implies that the URL
 * will not have any session information encoded in it), you need to implement
 * your Page with a no-arg constructor or with a constructor that accepts a
 * PageParameters argument (which wraps any query string parameters for a
 * request).
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class WebPage extends Page
{
	/**
	 * Constructor.
	 */
	protected WebPage()
	{
		super();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel model)
	{
		super(model);
	}

	/**
	 * Returns a bookmarkable URL that references a given page class using a given set of
	 * page parameters. Since the URL which is returned contains all information necessary
	 * to instantiate and render the page, it can be stored in a user's browser as a
	 * stable bookmark.
	 * @param pageMapName Name of pagemap to use
	 * @param pageClass Class of page
	 * @param parameters Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public String urlFor(final String pageMapName, final Class pageClass,
			final PageParameters parameters)
	{
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
		buffer.append("bookmarkablePage=");
		buffer.append(pageClass.getName());
		if (parameters != null)
		{
			for (final Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
			{
				final String key = (String)iterator.next();
				buffer.append('&');
				buffer.append(key);
				buffer.append('=');
				buffer.append(parameters.getString(key));
			}
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
		buffer.append("component=");
		buffer.append(component.getPath());
		buffer.append("&version=");
		buffer.append(component.getPage().getCurrentVersionNumber());
		buffer.append("&interface=");
		buffer.append(Classes.name(listenerInterface));
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param cycle
	 *            The web request cycle
	 * @return Prefix for URLs including the context path, servlet path and
	 *         application name (if servlet path is empty).
	 */
	private StringBuffer urlPrefix(final WebRequestCycle cycle)
	{
		final StringBuffer buffer = new StringBuffer();
		final WebRequest request = cycle.getWebRequest();
		if (request != null)
		{
			buffer.append(request.getContextPath());
			final String servletPath = ((WebRequest)request).getServletPath();
			if (servletPath.equals(""))
			{
				buffer.append('/');
				buffer.append(cycle.getApplication().getName());
			}
			else
			{
				buffer.append(servletPath);
			}
		}

		return buffer;
	}
}