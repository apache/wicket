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

import wicket.Page;
import wicket.markup.html.WebPage;

/**
 * A page that shows interesting attributes of the Wicket environment, including
 * the current session and the component tree for the current page.
 * 
 * @author Jonathan Locke
 */
public final class WicketInspector extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param page
	 *            The page to be analyzed
	 */
	public WicketInspector(final Page page)
	{
		add(new WicketSessionView("session", page.getSession()));
		add(new WicketPageView("page", page));
	}
}
