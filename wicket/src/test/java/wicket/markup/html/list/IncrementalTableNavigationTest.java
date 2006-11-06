/*
 * $Id: IncrementalTableNavigationTest.java 5131 2006-03-26 10:12:04 +0000 (Sun,
 * 26 Mar 2006) jdonnerstag $ $Revision$ $Date: 2006-03-26 10:12:04 +0000
 * (Sun, 26 Mar 2006) $
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
package wicket.markup.html.list;

import wicket.Page;
import wicket.WicketTestCase;
import wicket.markup.html.link.Link;


/**
 * Test for simple table behavior.
 */
public class IncrementalTableNavigationTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public IncrementalTableNavigationTest(String name)
	{
		super(name);
	}

	/**
	 * Test simple table behavior.
	 * 
	 * @throws Exception
	 */
	public void testPagedTable() throws Exception
	{
		executeTest(IncrementalTableNavigationPage.class,
				"IncrementalTableNavigationPage_ExpectedResult_1.html");

		Page page = tester.getLastRenderedPage();
		Link link = (Link)page.get("nextNext");
		executedListener(IncrementalTableNavigationPage.class, link,
				"IncrementalTableNavigationPage_ExpectedResult_1-1.html");

		link = (Link)page.get("prev");
		executedListener(IncrementalTableNavigationPage.class, link,
				"IncrementalTableNavigationPage_ExpectedResult_1-2.html");
	}
}
