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
package wicket.examples.springframework2;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

import wicket.RequestCycle;
import wicket.Session;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebSession;

/**
 * 
 * @author martin
 */
public class SpringContextLocator
{
    /* this is transient, because the spring context is not serializable */
    private transient ApplicationContext applicationContext = null;

    /*
     * this is a reference to the http servlet session to obtain a reference to
     * spring context
     */
    private javax.servlet.http.HttpSession httpSession = null;

    /** 
     * Creates a new SpringContextLocator 
     * @param session
     */
    protected SpringContextLocator(WebSession session)
    {
        super();
        
        httpSession = session.getHttpSession();
        this.applicationContext = SpringContextLocator
                .getApplicationContext(session);
    }

    public SpringContextLocator(WebRequestCycle cycle)
    {
        super();
        
        this.applicationContext = SpringContextLocator
                .getApplicationContext(cycle);
    }

    /** 
     * Creates a new SpringContextLocator 
     * @param cycle
     */
    protected SpringContextLocator(RequestCycle cycle)
    {
        super();
        
        httpSession = ((WebSession) cycle.getSession())
                .getHttpSession();
        
        this.applicationContext = SpringContextLocator
                .getApplicationContext(cycle);
    }

    public ApplicationContext getApplicationContext()
    {
        if (applicationContext == null)
        {
            applicationContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(httpSession
                            .getServletContext());
        }
        return applicationContext;
    }

    public static SpringContextLocator getInstance(Session session)
    {
        WebSession httpSession = (WebSession) session;
        return new SpringContextLocator(httpSession);
    }

    public static SpringContextLocator getInstance(RequestCycle cycle)
    {
        WebRequestCycle httpCycle = (WebRequestCycle) cycle;
        return new SpringContextLocator(httpCycle);
    }

    /**
     * This method always trie to get the WebApplicationContext set by the
     * ContextLoaderListener of Spring otherwise fails.
     * @param session
     * @return
     * 
     * @see org.springframework.web.context.support.WebApplicationContextUtils
     */
    public static ApplicationContext getApplicationContext(Session session)
    {
        javax.servlet.http.HttpSession httpSession = ((WebSession) session)
                .getHttpSession();
        
        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(httpSession
                        .getServletContext());
    }

    /**
     * This method first tries to get the WebApplicationContext set by the
     * Spring {@link org.springframework.web.servlet.DispatcherServlet} from 
     * the HttpServletRequest. If this fails, the context is searched in the 
     * ServletContext probably set by Spring's ContextLoaderListener
     * @param cycle
     * @return
     * 
     * @see org.springframework.web.servlet.support.RequestContextUtils
     */
    public static ApplicationContext getApplicationContext(RequestCycle cycle)
    {
        javax.servlet.http.HttpServletRequest httpRequest = ((WebRequest) cycle
                .getRequest()).getHttpServletRequest();
        
        javax.servlet.ServletContext servletContext = httpRequest.getSession()
                .getServletContext();
        
        ApplicationContext context = RequestContextUtils
                .getWebApplicationContext(httpRequest, servletContext);
        
        if (context == null)
        {
            throw new IllegalStateException(
                    "No WebApplicationContext found: no DispatcherServlet/ContextLoaderListener registered?");
        }
        
        return context;
    }

}
