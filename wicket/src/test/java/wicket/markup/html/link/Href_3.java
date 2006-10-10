/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:52:19 +0200 (vr, 26 mei 2006) $
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
package wicket.markup.html.link;

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class Href_3 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 */
	public Href_3()
	{
		new WebMarkupContainer(this, "link1");
		new WebMarkupContainer(this, "link2").setVisible(false);
	}
}
