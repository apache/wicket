/*
 * $Id: MockWebApplicationTest.java 5135 2006-03-26 10:21:04 +0000 (Sun, 26 Mar
 * 2006) jdonnerstag $ $Revision$ $Date: 2006-03-26 10:21:04 +0000 (Sun,
 * 26 Mar 2006) $
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
package wicket.protocol.http;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.markup.html.link.Link;
import wicket.util.diff.DiffUtil;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class MockWebApplicationTest extends TestCase
{

	private MockWebApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public MockWebApplicationTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		application = new MockWebApplication(null);
		application.setHomePage(MockPage.class);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "MockPage_expectedResult.html"));

		// Inspect the page & model
		MockPage p = (MockPage)application.getLastRenderedPage();
		Assert.assertEquals("Link should have been clicked 0 times", 0, p.getLinkClickCount());
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink() throws Exception
	{
		// Need to call the home page first
		testRenderHomePage();

		// Now request that we click the link
		application.setupRequestAndResponse();
		MockPage p = (MockPage)application.getLastRenderedPage();
		Link link = (Link)p.get("actionLink");
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();

		// Check that redirect was set as expected and invoke it
		/*
		 * Assert.assertTrue("Response should be a redirect",
		 * application.getServletResponse().isRedirect()); String redirect =
		 * application.getServletResponse().getRedirectLocation();
		 * application.setupRequestAndResponse();
		 * application.getServletRequest().setRequestToRedirectString(redirect);
		 * application.processRequestCycle();
		 */
		// Validate the document
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil
				.validatePage(document, this.getClass(), "MockPage_expectedResult2.html"));

		// Inspect the page & model
		p = (MockPage)application.getLastRenderedPage();
		Assert.assertEquals("Link should have been clicked 1 time", 1, p.getLinkClickCount());
	}
}
