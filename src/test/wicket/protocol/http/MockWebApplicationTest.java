/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.protocol.http;

import wicket.markup.html.link.Link;
import wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import wicket.protocol.http.documentvalidation.Tag;
import wicket.protocol.http.documentvalidation.TextContent;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Simple application that demonstrates the mock http application
 * code (and checks that it is working)
 *
 * @author Chris Turner
 */
public class MockWebApplicationTest extends TestCase {

	private MockWebApplication application;

	/**
	 * Create the test.
	 *
	 * @param name The test name
	 */
	public MockWebApplicationTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		application = new MockWebApplication(null);
		application.getPages().setHomePage(MockPage.class);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception {
		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		String document = application.getServletResponse().getDocument();
		Assert.assertTrue(validateDocument(document, 0));

		// Inspect the page & model
		MockPage p = (MockPage)application.getLastRenderedPage();
		Assert.assertEquals("Link should have been clicked 0 times", 0, p.getLinkClickCount());
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink() throws Exception {
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
		Assert.assertTrue("Response should be a redirect", application.getServletResponse().isRedirect());
		String redirect = application.getServletResponse().getRedirectLocation();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString(redirect);
		application.processRequestCycle();
*/
		// Validate the document
		String document = application.getServletResponse().getDocument();
		Assert.assertTrue(validateDocument(document, 1));

		// Inspect the page & model
		p = (MockPage)application.getLastRenderedPage();
		Assert.assertEquals("Link should have been clicked 1 time", 1, p.getLinkClickCount());
	}


	/**
	 * Helper method to validate the returned XML document.
	 *
	 * @param document The document
	 * @param expectedLinkClickCount The number of times the link should have been clicked
	 * @return The validation result
	 */
	private boolean validateDocument(String document, int expectedLinkClickCount) {
		HtmlDocumentValidator validator = new HtmlDocumentValidator();
		Tag html = new Tag("html");
		Tag head = new Tag("head");
		html.addExpectedChild(head);
		Tag title = new Tag("title");
		head.addExpectedChild(title);
		title.addExpectedChild(new TextContent("Mock Page"));
		Tag body = new Tag("body");
		html.addExpectedChild(body);
		Tag a = new Tag("a");
		a.addExpectedAttribute("href", "/MockWebApplication/MockWebApplication\\?component=[0-9]+.actionLink(&amp;version=[0-9]+)?&amp;interface=ILinkListener");
		a.addExpectedAttribute("wicket:id", "actionLink");
		body.addExpectedChild(a);
		a.addExpectedChild(new TextContent("Action link clicked "));
		Tag span = new Tag("span");
		span.addExpectedChild(new TextContent("" + expectedLinkClickCount));
		a.addExpectedChild(span);
		a.addExpectedChild(new TextContent(" times"));
		validator.addRootElement(html);

		return validator.isDocumentValid(document);
	}


}
