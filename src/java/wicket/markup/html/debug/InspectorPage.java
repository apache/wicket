/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.debug;

import wicket.Application;
import wicket.Session;
import wicket.markup.html.WebPage;
import wicket.markup.html.image.Image;

/**
 * A page that shows interesting attributes of the Wicket environment, including
 * the current session and the component tree for the current page.
 * 
 * @author Jonathan Locke
 */
public final class InspectorPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * 
	 * @param page
	 *            The page to be analyzed
	 */
	public InspectorPage(final WebPage page)
	{
		add(new ApplicationView("application", page.getApplication()));
		add(new SessionView("session", page.getSession()));
		add(new PageView("page", page));
		add(new Image("bug"));
	}
	
	/**
	 * Construct.
	 */
	public InspectorPage()
	{
		add(new ApplicationView("application", Application.get()));
		add(new SessionView("session", Session.get()));
		add(new PageView("page", null));
		add(new Image("bug"));
	}
	
	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
	
	/**
	 * @see wicket.Page#isStateless()
	 */
	public boolean isStateless()
	{
		return true;
	}
}
