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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import wicket.ApplicationPages;
import wicket.ApplicationSettings;
import wicket.WebApplication;
import wicket.markup.html.InternalErrorPage;
import wicket.markup.html.PageExpiredErrorPage;
import wicket.markup.html.StaleDataErrorPage;

/**
 * WicketServlet class for hello world example.
 * @author Juergen Donnerstag
 */
public class SpringApplication extends WebApplication implements InitializingBean  
{
    /** Logging */
    private static final Log log = LogFactory.getLog(SpringApplication.class);
    
	/** Settings for the application */
    private ApplicationSettings settings;
    
    /** Common application pages */
    private ApplicationPages pages;
    
    /** Spring application context */
    private ApplicationContext springContext;
    
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
    public final void setSettings(final ApplicationSettings settings)
    {
        this.settings = settings;
    }

    /**
     * @return Returns the settings for this web application
     */
    public final ApplicationSettings getSettings()
    {
        if (settings == null)
        {
            log.error("Applications settings not yet defined: Check with Spring's web application context!");
        }
        return settings;
    }

    /**
     * @return Returns the pages.
     */
    public ApplicationPages getPages()
    {
        return pages;
    }

    /**
     * @param pages The pages to set.
     */
    public void setPages(ApplicationPages pages)
    {
        this.pages = pages;
    }

    /**
     * 
     * @param context
     */
    public final void setSpringApplicationContext(final ApplicationContext context)
    {
        this.springContext = context;
    }

    /**
     * @return The spring application context.
     */
    public final ApplicationContext getSpringApplicationContext()
    {
        return this.springContext;
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

        if (getPages() == null)
        {
            setPages(new ApplicationPages());
        }
        
        initSettings();
    }
    
    /**
     * Subclasses may override it to provide there own settings
     */
    public void initSettings()
    {
        // Set default error pages for HTML markup
        getPages().setPageExpiredErrorPage(PageExpiredErrorPage.class)
        	.setInternalErrorPage(InternalErrorPage.class)
            .setStaleDataErrorPage(StaleDataErrorPage.class);
    }
}
