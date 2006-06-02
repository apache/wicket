/*
 * $Id: WicketTestCase.java 5366 2006-04-13 20:38:37 +0000 (Thu, 13 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-13 20:38:37 +0000 (Thu, 13 Apr
 * 2006) $
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

import junit.framework.TestCase;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.list.DiffUtil;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.tester.WicketTester;

/**
 * Base class for tests which require comparing wicket response with a file.
 * <p>
 * To create/replace the expected result file with the new content, define the
 * system property like -Dwicket.replace.expected.results=true
 * 
 */
public abstract class WicketTestCase extends TestCase
{
	/** */
	public WicketTester application;

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

	@Override
	protected void setUp() throws Exception
	{
		application = new WicketTester(null);
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to
	 * automatically replace the expected output file.
	 * 
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	protected void executeTest(final Class<? extends Page> pageClass, final String filename) throws Exception
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

	/**
	 * 
	 * @param pageClass
	 * @param component
	 * @param filename
	 * @throws Exception
	 */
	protected void executedListener(final Class pageClass, final Component component,
			final String filename) throws Exception
	{
		assertNotNull(component);

		System.out.println("=== " + pageClass.getName() + " : " + component.getPageRelativePath()
				+ " ===");

		application.setupRequestAndResponse();
		WebRequestCycle cycle = application.createRequestCycle();
		application.getServletRequest().setRequestToComponent(component);
		application.processRequestCycle(cycle);

		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, pageClass, filename));
	}

	/**
	 * 
	 * @param pageClass
	 * @param behavior
	 * @param filename
	 * @throws Exception
	 */
	protected void executedBehavior(final Class pageClass, final AbstractAjaxBehavior behavior,
			final String filename) throws Exception
	{
		assertNotNull(behavior);

		System.out.println("=== " + pageClass.getName() + " : " + behavior.toString() + " ===");

		application.setupRequestAndResponse();
		WebRequestCycle cycle = application.createRequestCycle();
		application.getServletRequest().setRequestToRedirectString(
				behavior.getCallbackUrl(false).toString());
		application.processRequestCycle(cycle);

		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, pageClass, filename));
	}
}
