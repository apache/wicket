/*
 * $Id$ $Revision:
 * 3927 $ $Date$
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
package wicket.examples;

import junit.framework.TestCase;
import wicket.protocol.http.MockWebApplication;
import wicket.util.diff.DiffUtil;

/**
 * Base class for tests which require comparing wicket response with a file
 */
public abstract class WicketTestCase extends TestCase
{
	/** */
	public MockWebApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public WicketTestCase(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		application = new MockWebApplication(null);
	}

	/**
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	protected void executeTest(final Class pageClass, final String filename) throws Exception
	{
		System.out.println("=== " + pageClass.getName() + " ===");

		application.setHomePage(pageClass);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		assertEquals(pageClass, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), filename));
	}
}
