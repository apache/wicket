/*
 * $Id: SimplePage_10.java 3749 2006-01-14 00:54:30Z ivaynberg $ $Revision$
 * $Date: 2006-01-14 01:54:30 +0100 (Sa, 14 Jan 2006) $
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
package wicket.markup.html.basic;

import wicket.markup.html.WebPage;
import wicket.markup.html.link.BookmarkablePageLink;


/**
 * Conditional comments
 * 
 * @author Juergen Donnerstag
 */
public class SimplePage_12 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public SimplePage_12()
	{
		add(new BookmarkablePageLink("link", SimplePage_3.class));
	}
}
