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

import org.springframework.context.ApplicationContext;

import wicket.PageParameters;
import wicket.markup.html.HtmlPage;

/**
 * Everybody's favorite example.
 * @author Jonathan Locke
 */
public class BasePage extends HtmlPage
{
    /**
     * Constructor
     * @param parameters Page parameters
     */
    public BasePage(final PageParameters parameters)
    {
    }
    
    public final ApplicationContext getSpringApplicationContext()
    {
        return ((SpringApplication)this.getApplication()).getSpringApplicationContext();
    }
    
/*
    public final UserDao getUserDao()
    {
        return ((UserDao) this.getSpringApplicationContext()).getBean("userDao");
    }
 */    
}

///////////////////////////////// End of File /////////////////////////////////
