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

import wicket.protocol.http.HttpRequest;

/**
 * A factory class to load Pages. 
 * 
 * @author Juergen Donnerstag
 */
public interface IPageFactory
{ // TODO finalize javadoc
    /**
     * Creates a new page. If Page with PageParameter argument constructor
     * exists, PageParameter will be null.
     * 
     * @param pageClass The page class to instantiate
     * @return The page
     * @throws RenderException
     */
    public abstract Page newPage(final Class pageClass);

    /**
     * Creates a new page. If Page with PageParameter argument constructor
     * exists, PageParameter will be null.
     * 
     * @param className The name of the page class to instantiate
     * @return The page
     * @throws RenderException
     */
    public abstract Page newPage(final String className);

    /**
     * Creates a new page. Take the PageParameters from the request.
     * 
     * @param pageClass The page class to instantiate
     * @param request The HTTP request to get the page parameters from
     * @return The page
     * @throws RenderException
     */
    public abstract Page newPage(final Class pageClass, 
            final HttpRequest request);

    /**
     * Creates a new page. Take the PageParameters from the request.
     * 
     * @param pageClassName The name of the page class to instantiate
     * @param request The HTTP request to get the page parameters from
     * @return The page
     * @throws RenderException
     */
    public abstract Page newPage(final String pageClassName, 
            final HttpRequest request);

    /**
     * Creates a new Page and apply the PageParameters to the Page constructor
     * if a proper constructor exists. Else use the default constructor.
     * 
     * @param pageClass The page class to create
     * @param parameters The page parameters
     * @return The new page
     * @throws RenderException
     */
    public abstract Page newPage(final Class pageClass,
            final PageParameters parameters);

    /**
     * Creates a new Page and apply the PageParameters to the Page constructor
     * if a proper constructor exists. Else use the default constructor.
     * 
     * @param pageClassName The name of the page class to create
     * @param parameters The page parameters
     * @return The new page
     * @throws RenderException
     */
    public abstract Page newPage(final String pageClassName,
            final PageParameters parameters);

    /**
     * Creates a new instance of a page using the given class name.
     * If pageClass implements a constructor with Page argument, page
     * will be forwarded to that constructor. Else, the default
     * constructor will be used and page be neglected.
     * 
     * @param pageClass The class of page to create
     * @param page Parameter to page constructor
     * @return The new page
     * @throws RenderException
     */
    public abstract Page newPage(final Class pageClass, final Page page);

    /**
     * Creates a new instance of a page using the given class name.
     * If pageClass implements a constructor with Page argument, page
     * will be forwarded to that constructor. Else, the default
     * constructor will be used and page be neglected.
     * 
     * @param pageClassName The name of the class of page to create
     * @param page Parameter to page constructor
     * @return The new page
     * @throws RenderException
     */
    public abstract Page newPage(final String pageClassName, final Page page);

    /**
     * Object Factory: Simply load the Class with name. Subclasses may overwrite
     * it to load Groovy classes e.g..
     * 
     * @param classname fully qualified classname
     * @return Class
     */
    public abstract Class getClassInstance(final String classname);
    
    /**
     * Get next (child-) factory from the chain of factories.
     * 
     * @return null, if no child factory defined.
     */
    public abstract IPageFactory getChildFactory();
    
    /**
     * Set the next factory in the chain
     * 
     * @param childFactory The child-factory.
     */
    public abstract void setChildFactory(final IPageFactory childFactory);
}