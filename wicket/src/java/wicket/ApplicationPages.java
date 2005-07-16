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
 * 
 * @author Jonathan Locke
 */
public class ApplicationPages
{
	/**
	 * Use this homepage strategy if you don't want to redirect so the url just stays / 
	 */
	public static final HomePageStrategy NO_REDIRECT = new HomePageStrategy("no-redirect");
	
	/**
	 * Use this homepage strategy if you want to redirect the homepage to a bookmarkable url like: page=mybookmarkablepage
	 * This is the same as calling: setResponsePage(MyPage.class); 
	 */
	public static final HomePageStrategy BOOKMARK_REDIRECT = new HomePageStrategy("bookmark-redirect");
	
	/**
	 * Use this homepage strategy if you want to redirect the homepage just as a normal page would be in 
	 * wicket (when you submit a form on the page or when you do in the code: setResponsePage(new MyPage());
	 * If you have set the overall Redirect Strategy to ONE_PASS_RENDER then the homepage response will honor that. 
	 * Then it is the same as setting the homepage strategy to NO_REDIRECT.
	 */
	public static final HomePageStrategy PAGE_REDIRECT = new HomePageStrategy("page-redirect");
	
	/** Home page class */
	private Class homePage;
	
	/**
	 * What homepage strategy should be used (no redirect/redirect to bookmarkable/redirect to page)
	 * The default is redirect to page.
	 */
	private HomePageStrategy homePageStrategy = PAGE_REDIRECT;

	/** Class of internal error page */
	private Class internalErrorPage;

	/** The error page displayed when an expired page is accessed */
	private Class pageExpiredErrorPage;

	/** A map where nice names for bookmarkable page classes are stored*/
	private HashMap bookmarkableNames = new HashMap();
	
	/**
	 * Enumerated type for different ways of handling the homepage.
	 */	
	public static final class HomePageStrategy extends EnumeratedType
	{
		HomePageStrategy(final String name)
		{
			super(name);
		}
	}

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
	 * @see ApplicationPages#setHomePageStrategy(HomePageStrategy)
	 */
	public final HomePageStrategy getHomePageStrategy()
	{
		return homePageStrategy;
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
	public final ApplicationPages setHomePageStrategy(final HomePageStrategy homePageStrategy)
	{
		this.homePageStrategy = homePageStrategy;
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
		checkPageClass(pageExpiredErrorPage);
		this.pageExpiredErrorPage = pageExpiredErrorPage;
		return this;
	}

	/**
	 * Throws an IllegalArgumentException if the given class is not a subclass
	 * of Page.
	 * 
	 * @param pageClass
	 *            The page class to check
	 */
	private final void checkPageClass(final Class pageClass)
	{
		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Class must be a subclass of Page");
		}
	}

	/**
	 * Checks and returns if for the given class was set a nice bookmarkable name
	 * if nothing was set for this class then the class name is returned.
	 * 
	 * @param pageClass The class to be checked
	 * 
	 * @return A name that was added to the bookmarkable map or the given name as nothing was found.
	 */
	public final String getBookmarkablePageName(final Class pageClass)
	{
		String niceName = (String)bookmarkableNames.get(pageClass.getName());
		if(niceName == null) niceName = pageClass.getName();
		return niceName;
	}
	
	
	/**
	 * @param bookmarkableName String to check for
	 * @return The page classname if the bookmarkable name if found or else the bookmarkablename itself
	 */
	public final String getBookmarkablePageClassname(final String bookmarkableName)
	{
		if(bookmarkableName == null) return null;
		
		Iterator it = bookmarkableNames.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry = (Entry)it.next();
			if(entry.getValue().equals(bookmarkableName))
			{
				return (String)entry.getKey();
			}
		}
		return bookmarkableName;
	}
	
	/**
	 * Use this method to add nice names to youre bookmarkable pages.
	 * So that "org.wicket.pages.WicketPage" can be just "wicketpage" 
	 * @param page
	 * @param name
	 */
	public final void addBookmarkablePage(Class page, String name)
	{
		bookmarkableNames.put(page.getName(), name);
	}

}
