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
package wicket.markup.html.form.persistence;

import wicket.PageParameters;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.FeedbackPanel;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class CookieValuePersisterTestPage extends WebPage
{
	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public CookieValuePersisterTestPage(final PageParameters parameters)
	{
	    
	    // Create and add feedback panel to page
	    final FeedbackPanel feedback = new FeedbackPanel("feedback");
	 
	    add(new TestForm("form", feedback));
	}
	
	/**
	 * 
	 * @author Juergen Donnerstag
	 */
	public final class TestForm extends Form
	{
	    /**
	     * Constructor
	     * @param componentName Name of form
	     * @param feedback Feedback component that shows errors
	     */
	    public TestForm(final String componentName, final FeedbackPanel feedback)
	    {
	        super(componentName, feedback);
	        
	        add(new TextField("input", "test"));
	    }
	    
	    /**
	     * Dummy
	     */
	    public final void onSubmit()
	    {
	    }
	}
}