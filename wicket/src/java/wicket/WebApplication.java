/*
 * $Id$ $Revision:
 * 1.3 $ $Date$
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

import wicket.markup.html.InternalErrorPage;
import wicket.markup.html.PageExpiredErrorPage;
import wicket.markup.html.StaleDataErrorPage;
import wicket.protocol.http.HttpApplication;

/**
 * A web application is an HttpApplication that serves HTML pages. This class is
 * intended to be subclassed by framework clients to define settings relevant to
 * a given web application.
 * <p>
 * Application settings are given defaults by the WebApplication() constructor,
 * such as error page classes appropriate for HTML. WebApplication subclasses
 * can override these values and/or modify other application settings in their
 * respective constructors by calling getSettings() to retrieve a mutable
 * ApplicationSettings object.
 * </p>
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.ServletConfig}object, you should override the
 * init() method of {@link javax.servlet.GenericServlet}. For example:
 * 
 * <pre>
 * 
 *    public void init() throws ServletException
 *    {
 *      ServletConfig config = getServletConfig();
 *      String webXMLParameter = config.getInitParameter(&quot;myWebXMLParameter&quot;);
 *      ...
 *  
 * </pre>
 * 
 * </p>
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @see ApplicationSettings
 * @see ApplicationPages
 */
public class WebApplication extends HttpApplication
{
    /** Serial Version ID. */
    private static final long serialVersionUID = 1152456333052646498L;

    /** Settings for application. */
    private final ApplicationSettings settings;
    
    /** Pages for application */
    private final ApplicationPages pages;

    /**
     * Constructor.
     */
    public WebApplication()
    {
        this.settings = new ApplicationSettings(this);
        this.pages = new ApplicationPages();

        // Set default error pages for HTML markup
        pages.setPageExpiredErrorPage(PageExpiredErrorPage.class)
             .setInternalErrorPage(InternalErrorPage.class)
             .setStaleDataErrorPage(StaleDataErrorPage.class);
    }

    /**
     * @see wicket.IApplication#getSettings()
     */
    public ApplicationSettings getSettings()
    {
        return settings;
    }
    
    /**
     * @see wicket.IApplication#getPages()
     */
    public ApplicationPages getPages()
    {
        return pages;
    }
}
