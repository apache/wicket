/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

import wicket.util.string.Strings;

/**
 * Holder for Wicket pages with special meanings. 
 *
 * @author Jonathan Locke
 */
public class ApplicationPages
{    
    /** Home page class */
    private Class homePageClass;

    /** Home page class name */
    private String homePageClassName;

    /** Class of internal error page */
    private Class internalErrorPageClass;

    /** The error page displayed when an expired page is accessed */
    private Class pageExpiredErrorPageClass;

    /** Error page to show when stale markup renderings are encountered */
    private Class staleDataErrorPageClass;
    
    /**
     * Gets home page class.
     * @return Returns the homePage.
     * @see ApplicationPages#setHomePage(Class)
     */
    public final Class getHomePage()
    {
        // If no home page and one was specified
        if (homePageClass == null && !Strings.isEmpty(homePageClassName))
        {
            // try resolving it
            homePageClass = Session.get().getClassResolver().resolveClass(homePageClassName);
        }

        // If no home page is available
        if (homePageClass == null)
        {
            // give up
            throw new IllegalStateException("No home page was specified in application settings");
        }
        
        return homePageClass;
    }
    
    /**
     * Gets internal error page class.
     * 
     * @return Returns the internalErrorPage.
     * @see ApplicationPages#setInternalErrorPage(Class)
     */
    public final Class getInternalErrorPage()
    {
        return internalErrorPageClass;
    }

    /**
     * Gets the page expired page class.
     * 
     * @return Returns the pageExpiredErrorPage.
     * @see ApplicationPages#setPageExpiredErrorPage(Class)
     */
    public final Class getPageExpiredErrorPage()
    {
        return pageExpiredErrorPageClass;
    }

    /**
     * Gets the stale data error page class.
     * @return Returns the staleDataErrorPage.
     * @see ApplicationPages#setStaleDataErrorPage(Class)
     */
    public final Class getStaleDataErrorPage()
    {
        return staleDataErrorPageClass;
    }

    /**
     * Sets home page class. The class must be external / bookmarkable and therefore must
     * extend Page.
     * @param homePage The home page class
     * @return This
     */
    public final ApplicationPages setHomePage(final Class homePage)
    {
        this.homePageClass = homePage;
        return this;
    }

    /**
     * Sets home page class as a string. The class must be external / bookmarkable
     * and therefore must extend Page.
     * @param homePage The name of the home page class (full class name)
     * @return This
     */
    public final ApplicationPages setHomePage(final String homePage)
    {
        this.homePageClassName = homePage;
        return this;
    }
    

    /**
     * Sets internal error page class. The class must be external / bookmarkable and
     * therefore must extend Page and must be able to construct from PageParameters.
     * @param internalErrorPage The internalErrorPage to set.
     * @return This
     */
    public final ApplicationPages setInternalErrorPage(final Class internalErrorPage)
    {
        this.internalErrorPageClass = internalErrorPage;
        return this;
    }
    
    /**
     * Sets the page expired page class. The class must be external /
     * bookmarkable and therefore must extend Page and must be able to construct
     * from PageParameters.
     * 
     * @param pageExpiredErrorPage
     *            The pageExpiredErrorPage to set.
     * @return This
     */
    public final ApplicationPages setPageExpiredErrorPage(
            final Class pageExpiredErrorPage)
    {
        this.pageExpiredErrorPageClass = pageExpiredErrorPage;
        return this;
    }

    /**
     * Sets the stale data error page class. The class must be external / bookmarkable and
     * therefore must extend Page and must be able to construct from PageParameters.
     * @param staleDataErrorPage The staleDataErrorPage to set
     * @return This
     */
    public final ApplicationPages setStaleDataErrorPage(final Class staleDataErrorPage)
    {
        this.staleDataErrorPageClass = staleDataErrorPage;
        return this;
    }
}
