/*
 * $Id$ $Revision$ $Date:
 * 2005-10-09 18:17:12 +0200 (So, 09 Okt 2005) $
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
package wicket.examples.linkomatic;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;


/**
 * Trivial page.
 * 
 * @author Jonathan Locke
 */
public class Page3 extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Page3(PageParameters parameters)
	{
		add(new Label("bookmarkparameter", parameters.getString("bookmarkparameter")));
	}
}
