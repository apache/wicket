/*
 * $Id: OutputTransformerContainerTest.java 4507 2006-02-16 22:51:20 +0000 (Thu,
 * 16 Feb 2006) jonathanlocke $ $Revision$ $Date: 2006-02-16 22:51:20
 * +0000 (Thu, 16 Feb 2006) $
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
package wicket.markup.outputTransformer;

import wicket.WicketTestCase;

/**
 * 
 * @author Juergen Donnerstag
 */
public class OutputTransformerContainerTest extends WicketTestCase
{
	// private static Log log =
	// LogFactory.getLog(OutputTransformerContainerTest.class);

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public OutputTransformerContainerTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
		executeTest(Page_1.class, "PageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
		executeTest(Page_2.class, "PageExpectedResult_2.html");
	}
}
