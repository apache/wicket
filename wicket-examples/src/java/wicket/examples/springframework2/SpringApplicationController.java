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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import wicket.WebApplication;
import wicket.protocol.http.WicketServlet;

/**
 * I'm not sure this is the best approach to integrate Spring and Wicket.
 * But beside that you now may use all of Spring's ApplicationContext capabilities,  
 * you may simply add Wicket functionality to existing Spring applications.
 * 
 * The apprach taken is to create a Spring controller derived from 
 * AbstractController and forward handleRequestInternal() to Wicket's
 * Servlet object and it's doGet() method.
 * 
 * The Wicket application to use must be configured with Spring's web application
 * context (<servlet-name>-servlet.xml). 
 * 
 * @author Juergen Donnerstag
 */
public class SpringApplicationController extends AbstractController 
{
    /** Logging */
    private static Log log = LogFactory.getLog(SpringApplicationController.class);

    /** The Wicket application object */
    private SpringApplication application;

    /**
     * JavaBean method to provide Wicket's application object. Will be set
     * through Spring BeanFactory and WebApplicationContext.
     * <servlet-name>-servlet.xml
     * 
     * @param application Wicket application object
     */
    public void setApplication(final SpringApplication application)
    {
        this.application = application;
        this.application.setWicketServlet(new WicketSpringServlet(application));
        //this.application.setSpringApplicationContext(this.getApplicationContext());
    }
    
    /**
     * Handle the request. Simply forward it to Wicket.
     * 
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) throws Exception
    {
        if (application != null)
        {
             application.getWicketServlet().doGet(servletRequest, servletResponse);
        }
        else
        {
            log.error("Wickets application object is not available. Probably the bean named 'wicketApplication' was not found in Spring's web application context: <servlet-name>-servlet.xml");
        }
        
        return null;
    }
    
    public final class WicketSpringServlet extends WicketServlet
    {
        public WicketSpringServlet(final WebApplication application)
        {
            this.webApplication = application;
        }
        
        public void init()
        {
            ; // replace super implementation with nothing. Apllication class
              // will be defined through Spring xml. 
        }
    }
}