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
package wicket.examples.compref;

import java.util.Calendar;

import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.beanedit.BeanFormPanel;
import wicket.extensions.markup.html.beanedit.BeanModel;

/**
 * Example Page for editing JavaBeans.
 * 
 * @author Eelco Hillenius
 */
public class BeanEditPage extends WicketExamplePage
{
    /**
     * Constructor
     */
    public BeanEditPage()
    {
    	Person p = new Person();
    	p.setName("Fritz");
    	p.setLastName("Fritzel");
    	Calendar cal = Calendar.getInstance();
    	cal.set(1940, 12, 12);
    	p.setDateOfBirth(cal.getTime());

    	add(new BeanFormPanel("beanEditPanel", new BeanModel(p)));
    }
}