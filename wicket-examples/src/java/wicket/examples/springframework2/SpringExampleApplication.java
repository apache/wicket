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

import wicket.examples.springframework2.SpringApplication;
import wicket.util.time.Duration;

/**
 * Here are some short explanations:
 * I used your approach of a SpringApplicationController and a
 * SpringApplication. But you can also use the nromal deployment model of
 * Wicket, if you want to use the SpringBeanModel.
 *
 * I differ between the ApplicationContext of the DispatcherServlet and
 * the ApplicationContext for the middle tier components (this is what
 * SpringBeanModel wants to access): The first is configured in web.xml as
 * you did, the later is configured by setting up a
 * org.springframework.web.context.ContextLoaderListener and the context
 * parameter "contextConfigLocation" in web.xml
 *
 * The SpringContextLocator encapsulates how to get a
 * ApplicationContext. It therefor provides two static methods.
 *
 * SpringBeanModel is the base for Spring bean access. It tries to
 * access the ApplicationContext of the middle tier by calling
 * SpringContextLocator.getApplicationContext(Session) (in the request we
 * would find the DispatcherServlet context). The middle tier context is
 * independent of the DispatcherServlet, because it's set by the
 * ContextLoaderListener, so the locator will also work, if you don't use
 * your SpringApplicationController approach. Instead you can configure the
 * Wicket application framework directly in web.xml !
 *
 * SpringBeanPropertyModel provides OGNL support
 *
 * There are so many possible ways of configuration with Spring. I like
 * your approach of integration with the mvc framework of Spring, because
 * it give me the possibility to configure the objects and the wiring of
 * ApplicationSettings and HttpApplication and I can also use some advanced
 * features of the Spring framework (e.g. interception). But if one wants
 * to set up the Wicket servlet in web.xml, he could also use the
 * SpringBeanModel when he also sets up the ContextLoaderListener.
 *
 * I tried to configure the DispatcherServlet to use the values set with
 * the context parameter contextConfigLocation, but I failed. Without the
 * Listener the spring.xml context is not available. Perhaps there is a way
 * I cannot see.
 *
 * After I had a look at the PersistentObjectModel and its subclass, I'm
 * sure that IModel and IDetachableModel is a good integration point for
 * Wicket and Spring.
 *
 * @author Martin Frey
 */
public class SpringExampleApplication extends SpringApplication
{
    /**
     * 
     * @see wicket.examples.springframework.SpringApplication#initSettings()
     */
    public void initSettings()
    {
        getPages().setHomePage(SpringHtmlPage.class);
        getSettings().setResourcePollFrequency(Duration.ONE_SECOND);
    }
}
