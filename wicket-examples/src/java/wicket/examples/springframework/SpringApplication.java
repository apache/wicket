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
package wicket.examples.springframework;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import wicket.ApplicationSettings;
import wicket.markup.html.InternalErrorPage;
import wicket.markup.html.PageExpiredErrorPage;
import wicket.markup.html.StaleDataErrorPage;
import wicket.protocol.http.HttpApplication;

/**
 * HttpApplication class for hello world example.
 * @author Juergen Donnerstag
 */
public class SpringApplication extends HttpApplication implements InitializingBean  
{
    /** Logging */
    private static final Log log = LogFactory.getLog(SpringApplication.class);
    
	/** Settings for the application */
    private ApplicationSettings settings;
    
    /**
     * Constructor.
     */
    public SpringApplication()
    {
    }

    /**
     * JavaBean method to set the applicationSettings object. The 
     * ApplicationSettings object will be determined through Springs web application
     * context. See <servlet-name>-servlet.xml
     * 
     * @param settings Wicket's AppicationSettingsObject
     */
    public void setSettings(final ApplicationSettings settings)
    {
        this.settings = settings;
    }

    /**
     * @return Returns the settings for this web application
     */
    public ApplicationSettings getSettings()
    {
        if (settings == null)
        {
            log.error("Applications settings not yet defined: Check with Spring's web application context!");
        }
        return settings;
    }

    /**
     * THIS IS NOT PART IF WICKET'S PUBLIC API. IT IS ONLY MEANT TO BE USED
     * BY SPRING TO SET DEFAULT VALUES FOR THE SETTINGS. 
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
    {
        // Provide some default, if not explicitly set through Springs
        // web application context
        if (getSettings() == null)
        {
            setSettings(new ApplicationSettings(this));
        }
        
        initSettings();
    }
    
    /**
     * Subclasses may override it to provide there own settings
     */
    public void initSettings()
    {
        // Set default error pages for HTML markup
        getSettings().setPageExpiredErrorPage(PageExpiredErrorPage.class)
        	.setInternalErrorPage(InternalErrorPage.class)
            .setStaleDataErrorPage(StaleDataErrorPage.class);
    }

    /**
     * DO NOT CALL THIS METHOD YOURSELF. IT IS NOT PART OF THE PUBLIC API
     * OF WICKET. IT MAY BE REMOVED IN THE FUTURE.
     * 
     * HttpApplication's doGet() is protected and not directly accessible from 
     * the controller. doService() simply delegates to super.doGet().
     *  
     * @param servletRequest The http servlet request
     * @param servletResponse The http servlet response
     * @throws IOException
     * @throws ServletException
     */
    public void doService(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) throws IOException, ServletException
    {
        doGet(servletRequest, servletResponse);
    }
}

///////////////////////////////// End of File /////////////////////////////////
