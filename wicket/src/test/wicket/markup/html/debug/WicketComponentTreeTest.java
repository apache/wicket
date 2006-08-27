/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ======================================================================== 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at
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

import wicket.WicketTestCase;

/**
 * Test the component: PageView
 * 
 * @author Juergen Donnerstag
 */
public class WicketComponentTreeTest extends WicketTestCase
{
	// private static final Log log = LogFactory.getLog(WicketComponentTreeTest.class);

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public WicketComponentTreeTest(String name)
	{
		super(name);
	}

	/**
	 * Test a simply page containing the debug component
	 * @throws Exception
	 */
	public void test1() throws Exception
	{
		this.executeTest(WicketComponentTreeTestPage.class, "WicketComponentTreeTestPage_ExpectedResult.html");
	}
}