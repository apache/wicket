/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

/**
 * A factory class that creates Pages. A Page can be created by Class, with or
 * without a PageParameters argument to pass to the Page's constructor. A Page
 * can also be created with a Page argument, but this form is intended for
 * internal use only.
 * 
 * @see ApplicationSettings#setDefaultPageFactory(IPageFactory)
 * @see Session#setPageFactory(IPageFactory)
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public interface IPageFactory
{
    /**
     * Creates a new page using a page class.
     * 
     * @param pageClass
     *            The page class to instantiate
     * @return The page
     * @throws WicketRuntimeException
     *             Thrown if the page cannot be constructed
     */
    public Page newPage(final Class pageClass);

    /**
     * Creates a new Page, passing PageParameters to the Page constructor if
     * such a constructor exists. If no such constructor exists and the
     * parameters argument is null or empty, then any available default
     * constructor will be used.
     * 
     * @param pageClass
     *            The class of Page to create
     * @param parameters
     *            Any parameters to pass to the Page's constructor
     * @return The new page
     * @throws WicketRuntimeException
     *             Thrown if the page cannot be constructed
     */
    public Page newPage(final Class pageClass, final PageParameters parameters);

    /**
     * Creates a new Page, passing the given Page to the Page constructor if
     * such a constructor exists. This method is intended for internal use only.
     * 
     * @param pageClass
     *            The page class to create
     * @param page
     *            The page
     * @return The new page
     * @throws WicketRuntimeException
     *             Thrown if the page cannot be constructed
     */
    public Page newPage(final Class pageClass, final Page page);
}