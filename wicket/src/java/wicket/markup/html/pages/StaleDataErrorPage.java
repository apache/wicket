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

import wicket.Page;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.PageLink;

/**
 * Stale data error page.
 * 
 * @author Jonathan Locke
 */
public class StaleDataErrorPage extends HtmlPage
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -3703752578058679886L;

	/**
	 * Constructor.
	 * 
	 * @param page
	 *            The page to send the user to
	 */
	public StaleDataErrorPage(final Page page)
	{
		add(new PageLink("tryAgain", new IPageLink()
		{
			/** Serial Version ID */
			private static final long serialVersionUID = -2940024780787095228L;

			public Page getPage()
			{
				return page;
			}

			public Class getPageIdentity()
			{
				return page.getClass();
			}
		}));
	}
}