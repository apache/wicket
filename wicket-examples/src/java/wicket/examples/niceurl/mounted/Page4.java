/*
 * $Id$ $Revision$ $Date:
 * 2005-12-11 00:38:17 +0100 (So, 11 Dez 2005) $
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
package wicket.examples.niceurl.mounted;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.examples.niceurl.Home;
import wicket.markup.html.link.BookmarkablePageLink;


/**
 * Simple bookmarkable page.
 * 
 * @author Eelco Hillenius
 */
public class Page4 extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Page4(PageParameters parameters)
	{
		add(new BookmarkablePageLink("homeLink", Home.class));
	}
}
