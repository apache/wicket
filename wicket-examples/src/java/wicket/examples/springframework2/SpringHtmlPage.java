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

import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;

/**
 * Simple example for transparent access of a Spring managed bean.l
 * 
 * @author Martin Fey
 */
public class SpringHtmlPage extends HtmlPage
{

    /** Creates a new instance of SpringHtmlPage */
    public SpringHtmlPage()
    {
        // call super() so that the page gets bound to the session
        super();
        add(new NavigationPanel("mainNavigation", "Spring integration example with Spring models"));
        
        // set the applicationcontext for further use
        add(new Label("message", new SpringBeanModel("message")));
        add(new Label("user", new SpringBeanPropertyModel(UserModel.class, "forename")));
        add(new Label("user2", new SpringBeanPropertyModel("user", "surname")));
    }
}
