/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
package wicket.ajax.markup.html.componentMap;

import wicket.Page;
import wicket.WicketTestCase;
import wicket.markup.html.list.DiffUtil;

/**
 * Test for ajax handler.
 *
 * @author Juergen Donnerstag
 */
public class SimpleTestPanelTest extends WicketTestCase
{
	/**
	 * Construct.
	 * @param name
	 */
	public SimpleTestPanelTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
	    executeTest(SimpleTestPage.class, "SimpleTestPageExpectedResult.html");
	    
		Page page = application.getLastRenderedPage();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString("?path=0:testPanel:baseSpan:linja1&interface=IBehaviorListener&behaviorId=0");
		application.processRequestCycle();
		String document = application.getServletResponse().getDocument();

		assertTrue(DiffUtil.validatePage(document, this.getClass(), "SimpleTestPageExpectedResult-1.html"));
	}
}
