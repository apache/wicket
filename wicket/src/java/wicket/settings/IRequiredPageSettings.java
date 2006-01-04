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
package wicket.settings;

/**
 * Settings interface for specifying Wicket page classes that have special
 * meaning to an application, such as an application's home page and any error
 * display pages.
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
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IRequiredPageSettings
{
	/**
	 * Gets home page class.
	 * 
	 * @return Returns the homePage.
	 * @see IRequiredPageSettings#setHomePage(Class)
	 */
	Class getHomePage();

	/**
	 * Gets internal error page class.
	 * 
	 * @return Returns the internalErrorPage.
	 * @see IRequiredPageSettings#setInternalErrorPage(Class)
	 */
	Class getInternalErrorPage();

	/**
	 * Gets the page expired page class.
	 * 
	 * @return Returns the pageExpiredErrorPage.
	 * @see IRequiredPageSettings#setPageExpiredErrorPage(Class)
	 */
	Class getPageExpiredErrorPage();

	/**
	 * Sets home page class. The class must be bookmarkable and must extend
	 * Page.
	 * 
	 * @param homePage
	 *            The home page class
	 */
	void setHomePage(final Class homePage);

	/**
	 * Sets internal error page class. The class must be bookmarkable and must
	 * extend Page.
	 * 
	 * @param internalErrorPage
	 *            The internalErrorPage to set.
	 */
	void setInternalErrorPage(final Class internalErrorPage);

	/**
	 * Sets the page expired page class. The class must be bookmarkable and must
	 * extend Page.
	 * 
	 * @param pageExpiredErrorPage
	 *            The pageExpiredErrorPage to set.
	 */
	void setPageExpiredErrorPage(final Class pageExpiredErrorPage);
}
