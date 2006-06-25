/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
package wicket;

import wicket.markup.html.WebPage;

/**
 * Use this mock when testing you wan't to test a link on a page.
 * <p>
 * Example:
 * 
 * <pre>
 * 	Page page = new MockPageWithLink();
 * 	new Link(page, MockPageWithLink.LINK_ID) {
 * 		public void onClick() {
 * 			// ...
 * 		}
 * 	}
 * </pre>
 * 
 * @author Frank Bille
 */
public class MockPageWithLink extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Use this constant for links you add to the page.
	 */
	public static final String LINK_ID = "link"; 
}
