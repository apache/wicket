/*
 * $Id$ $Revision:
 * 1.9 $ $Date$
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
package wicket.protocol.http;

import wicket.Application;
import wicket.ISessionFactory;
import wicket.Session;
import wicket.markup.html.link.AutolinkComponentResolver;
import wicket.markup.html.pages.InternalErrorPage;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.markup.html.pages.StaleDataErrorPage;
import wicket.protocol.http.WicketServlet;

/**
 * A web application is a subclass of Application which associates with an
 * instance of WicketServlet to serve pages over the HTTP protocol. This class
 * is intended to be subclassed by framework clients to define a web
 * application.
 * <p>
 * Application settings are given defaults by the WebApplication() constructor,
 * such as error page classes appropriate for HTML. WebApplication subclasses
 * can override these values and/or modify other application settings in their
 * respective constructors by calling getSettings() to retrieve a mutable
 * ApplicationSettings object.
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.ServletConfig}object, you should override the
 * init() method. For example:
 * 
 * <pre>
 *           public void init()
 *           {
 *             String webXMLParameter = getWicketServlet()
 *             			.getInitParameter(&quot;myWebXMLParameter&quot;);
 *             URL schedulersConfig = getWicketServlet().getServletContext()
 *             			.getResource(&quot;/WEB-INF/schedulers.xml&quot;);
 *             ...
 * </pre>
 * 
 * @see WicketServlet
 * @see wicket.ApplicationSettings
 * @see wicket.ApplicationPages
 * @author Jonathan Locke
 * @author Chris Turner
 */
public abstract class WebApplication extends Application
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 1152456333052646498L;

	/** The WicketServlet that this application is attached to */
	private WicketServlet wicketServlet;

	/**
	 * Constructor.
	 */
	public WebApplication()
	{
        // Set default error pages for HTML markup
        getPages().setPageExpiredErrorPage(PageExpiredErrorPage.class).setInternalErrorPage(
                InternalErrorPage.class).setStaleDataErrorPage(StaleDataErrorPage.class);
        
        // Add resolver for automatically resolving HTML links
        getComponentResolvers().add(new AutolinkComponentResolver());
	}
   
    /**
	 * @see wicket.Application#getSessionFactory()
	 */
	public ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			public Session newSession()
			{
				return new HttpSession(WebApplication.this);
			}
		};
	}
    
	/**
	 * Initialize; if you need the wicket servlet for initialization, e.g.
	 * because you want to read an initParameter from web.xml or you want to
	 * read a resource from the servlet's context path, you can override this
	 * method and provide custom initialization. This method is called right
	 * after this application class is constructed, and the wicket servlet is
	 * set.
	 */
	public void init()
	{
	}

	/**
	 * @return The Wicket servlet for this application
	 */
	public WicketServlet getWicketServlet()
	{
		return wicketServlet;
	}

	/**
	 * THIS METHOD IS ONLY FOR INTERNAL USE.
	 * 
	 * @param wicketServlet
	 *            The wicket servlet instance for this application
	 * @throws IllegalStateException
	 *             If an attempt is made to call this method once the wicket
	 *             servlet has been set for the application.
	 */
	public void setWicketServlet(WicketServlet wicketServlet)
	{
		if (this.wicketServlet == null)
		{
			this.wicketServlet = wicketServlet;
		}
		else
		{
			throw new IllegalStateException("WicketServlet cannot be changed once it is set");
		}
	}
}