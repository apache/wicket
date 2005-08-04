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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.util.lang.EnumeratedType;

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
 * <p>
 * You can set property homePageRenderStrategy to choose from different ways the home page
 * url shows up in your browser.
 * <p>
 * You can register aliases for bookmarkable pages by calling putPageAlias; this way you can
 * point to bookmarkable pages with logical names instead of full class names.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public class ApplicationPages
{
	/**
	 * Enumerated type for different ways of handling the rendering/ redirecting of the homepage.
	 */	
	public static final class HomePageRenderStrategy extends EnumeratedType
	{
		HomePageRenderStrategy(final String name)
		{
			super(name);
		}
	}

	/**
	 * Use this homepage strategy if you don't want to redirect so the url just stays '/' .
	 */
	public static final HomePageRenderStrategy NO_REDIRECT = new HomePageRenderStrategy("no-redirect");
	
	/**
	 * Use this homepage strategy if you want to redirect the homepage to a bookmarkable url like: bookmarkablePage=mybookmarkablepage
	 * This is the same as calling: setResponsePage(MyPage.class);. 
	 */
	public static final HomePageRenderStrategy BOOKMARK_REDIRECT = new HomePageRenderStrategy("bookmark-redirect");
	
	/**
	 * Use this homepage strategy if you want to redirect the homepage just as a normal page would be in 
	 * wicket (when you submit a form on the page or when you do in the code: setResponsePage(new MyPage());
	 * If you have set the overall Redirect Strategy to ONE_PASS_RENDER then the homepage response will honor that. 
	 * Then it is the same as setting the homepage strategy to NO_REDIRECT.
	 * This one is the default used by wicket. 
	 */
	public static final HomePageRenderStrategy PAGE_REDIRECT = new HomePageRenderStrategy("page-redirect");
	
	/** Home page class */
	private Class homePage;

	/** Class of internal error page. */
	private Class internalErrorPage;

	/** The error page displayed when an expired page is accessed. */
	private Class pageExpiredErrorPage;

	/**
	 * What homepage strategy should be used (no redirect/redirect to bookmarkable/redirect to page)
	 * The default is redirect to page.
	 */
	private HomePageRenderStrategy homePageRenderStrategy = PAGE_REDIRECT;

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
			throw new IllegalStateException("No home page class was specified in ApplicationSettings");
		}

		return homePage;
	}

	/**
	 * Gets home page redirect strategy.
	 * 
	 * @return Returns the homePage.
	 * @see ApplicationPages#setHomePageRenderStrategy(HomePageRenderStrategy)
	 */
	public final HomePageRenderStrategy getHomePageRenderStrategy()
	{
		return homePageRenderStrategy;
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
	 * Sets home page strategy.
	 * Set one of the ApplicationPages.NO_REDIRECT, ApplicationPages.BOOKMARK_REDIRECT or ApplicationPages.PAGE_REDIRECT
	 *  
	 * @param homePageStrategy The homepage redirect strategy that has to be used  
	 *
	 * @return This
	 */
	public final ApplicationPages setHomePageRenderStrategy(final HomePageRenderStrategy homePageStrategy)
	{
		if (homePageStrategy == null)
		{
			throw new NullPointerException("argument homePageStrategy may not be null");
		}

		this.homePageRenderStrategy = homePageStrategy;
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
	 * Returns the alias for the given page class or null if no alias was found.
	 * Returns null when argument pageClass is null.
	 * @param pageClass The class to get the alias for
	 * @return the alias of the page class
	 */
	public final String aliasForClass(final Class pageClass)
	{
		if (pageClass == null) return null;

		String alias = (String)classAliases.get(pageClass);
		if(alias == null) alias = pageClass.getName();
		return alias;
	}
	
	
	/**
	 * Returns the page class for the given alias or null if the alias is not mapped.
	 * Returns null if argument alias is null.
	 * @param alias the alias to look up
	 * @return The page class for the given alias or null if no mapping was found
	 */
	public final Class classForAlias(final String alias)
	{
		if(alias == null) return null;
		
		for(Iterator i = classAliases.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry entry = (Entry)i.next();
			if(entry.getValue().equals(alias))
			{
				return (Class)entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Use this method to add logical names to your bookmarkable pages.
	 * E.g. "test" could map to "com.mycomp.MyPage".
	 * @param pageClass class of the page to map
	 * @param alias the alias or logical name of the bookmarkable page
	 */
	public final void putClassAlias(Class pageClass, String alias)
	{
		if (pageClass == null)
		{
			throw new NullPointerException("argument pageClass may not be null");
		}

		if (alias == null)
		{
			throw new NullPointerException("argument alias may not be null");
		}

		if(classAliases.containsValue(alias))
		{
			throw new WicketRuntimeException("can't set the same alias name twice");
		}
		else
		{
			classAliases.put(pageClass, alias);
		}
	}

	/**
	 * Throws an IllegalArgumentException if the given class is not a subclass of Page.
	 * @param pageClass the page class to check
	 */
	private final void checkPageClass(final Class pageClass)
	{
		// NOTE: we can't really check on whether it is a bookmarkable page here, as - though
		// the default is that a bookmarkable page must either have a default constructor and/ or
		// a constructor with a PageParameters object, this could be different for another
		// IPageFactory implementation

		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("argument " + pageClass +
					" must be a subclass of Page");
		}
	}
}
