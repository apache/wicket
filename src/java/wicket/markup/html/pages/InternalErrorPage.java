/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.pages;

import wicket.PageParameters;
import wicket.markup.html.HtmlPage;

/**
 * Internal error display page.
 * 
 * @author Jonathan Locke
 */
public class InternalErrorPage extends HtmlPage
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -4676797850273383367L;

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            The page parameters
	 */
	public InternalErrorPage(final PageParameters parameters)
	{
		add(homePageLink("homePageLink"));
	}
}