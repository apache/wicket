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
package wicket;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for specifying Wicket page classes that have special meaning to an
 * application, such as an application's home page and any error display pages.
 * <p>
 * <i>homePage </i> (required, no default) - You must set this property to the
 * bookmarkable page that you want the framework to respond with when no path
 * information is specified.
 * <p>
 * <i>internalErrorPage </i>- You can override this with your own page class to
 * display internal errors in a different way.
 * <p>
 * <i>pageExpiredErrorPage </i>- You can override this with your own
 * bookmarkable page class to display expired page errors in a different way.
 * You can set property homePageRenderStrategy to choose from different ways the
 * home page url shows up in your browser.
 * <p>
 * You can register aliases for bookmarkable pages by calling putClassAlias;
 * this way you can point to bookmarkable pages with logical names instead of
 * full class names.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public class ApplicationPages
{
	/** Home page class */
	private Class homePage;

	/** Class of internal error page. */
	private Class internalErrorPage;

	/** The error page displayed when an expired page is accessed. */
	private Class pageExpiredErrorPage;

	/** A map where aliases for bookmarkable page classes are stored. */
	private final Map classAliases = new HashMap();

	/**
	 * Gets home page class.
	 * 
	 * @return Returns the homePage.
	 * @see ApplicationPages#setHomePage(Class)
	 */
	public final Class getHomePage()
	{
		// If no home page is available
		if (homePage == null)
		{
			// give up with an exception
			throw new IllegalStateException(
					"No home page class was specified in ApplicationSettings");
		}

		return homePage;
	}

	/**
	 * Gets internal error page class.
	 * 
	 * @return Returns the internalErrorPage.
	 * @see ApplicationPages#setInternalErrorPage(Class)
	 */
	public final Class getInternalErrorPage()
	{
		return internalErrorPage;
	}

	/**
	 * Gets the page expired page class.
	 * 
	 * @return Returns the pageExpiredErrorPage.
	 * @see ApplicationPages#setPageExpiredErrorPage(Class)
	 */
	public final Class getPageExpiredErrorPage()
	{
		return pageExpiredErrorPage;
	}

	/**
	 * Sets home page class. The class must be bookmarkable and must extend
	 * Page.
	 * 
	 * @param homePage
	 *            The home page class
	 * @return This
	 */
	public final ApplicationPages setHomePage(final Class homePage)
	{
		checkPageClass(homePage);
		this.homePage = homePage;
		return this;
	}

	/**
	 * Sets internal error page class. The class must be bookmarkable and must
	 * extend Page.
	 * 
	 * @param internalErrorPage
	 *            The internalErrorPage to set.
	 * @return This
	 */
	public final ApplicationPages setInternalErrorPage(final Class internalErrorPage)
	{
		if (internalErrorPage == null)
		{
			throw new NullPointerException("argument internalErrorPage may not be null");
		}
		checkPageClass(internalErrorPage);

		this.internalErrorPage = internalErrorPage;
		return this;
	}

	/**
	 * Sets the page expired page class. The class must be bookmarkable and must
	 * extend Page.
	 * 
	 * @param pageExpiredErrorPage
	 *            The pageExpiredErrorPage to set.
	 * @return This
	 */
	public final ApplicationPages setPageExpiredErrorPage(final Class pageExpiredErrorPage)
	{
		if (pageExpiredErrorPage == null)
		{
			throw new NullPointerException("argument pageExpiredErrorPage may not be null");
		}
		checkPageClass(pageExpiredErrorPage);

		this.pageExpiredErrorPage = pageExpiredErrorPage;
		return this;
	}

	/**
	 * Throws an IllegalArgumentException if the given class is not a subclass
	 * of Page.
	 * 
	 * @param pageClass
	 *            the page class to check
	 */
	private final void checkPageClass(final Class pageClass)
	{
		// NOTE: we can't really check on whether it is a bookmarkable page
		// here, as - though
		// the default is that a bookmarkable page must either have a default
		// constructor and/ or
		// a constructor with a PageParameters object, this could be different
		// for another
		// IPageFactory implementation

		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("argument " + pageClass
					+ " must be a subclass of Page");
		}
	}
}
