/*
 * $Id: TestPage_1.java 4126 2006-02-04 09:51:46Z jdonnerstag $
 * $Revision: 4126 $ $Date: 2006-02-04 10:51:46 +0100 (za, 04 feb 2006) $
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
package wicket;

import wicket.markup.html.WebPage;
import wicket.markup.html.link.Link;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class DisabledComponentPage1 extends WebPage
{
	private static final long serialVersionUID = 1L;
	Link link;

	/**
	 * Construct.
	 */
	public DisabledComponentPage1()
	{
		link = new Link("link")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				setResponsePage(new DisabledComponentPage2());
			}
			
		};
		add(link);
	}
}
