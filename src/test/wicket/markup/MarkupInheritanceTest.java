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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.link.Link;
import wicket.markup.html.list.DiffUtil;
import wicket.protocol.http.MockWebApplication;

/**
 */
public class MarkupInheritanceTest extends TestCase
{
	private static Log log = LogFactory.getLog(MarkupInheritanceTest.class);

	private MockWebApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public MarkupInheritanceTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		application = new MockWebApplication(null);
	}
	
	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
	    executeTest(MarkupInheritanceExtension_1.class, "MarkupInheritanceExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
	    executeTest(MarkupInheritanceExtension_2.class, "MarkupInheritanceExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_3() throws Exception
	{
	    // Inherit from a inherited component is currently not supported
	    executeTest(MarkupInheritanceExtension_3.class, "MarkupInheritanceExpectedResult_3.html");
	}

	/**
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	public void executeTest(final Class pageClass, final String filename) throws Exception
	{
		System.out.println("=== " + pageClass.getName() + " ===");
		
		application.getPages().setHomePage(pageClass);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		assertEquals(pageClass, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), filename));
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_4() throws Exception
	{
		System.out.println("=== " + MarkupInheritanceExtension_4.class.getName() + " ===");
		
		application.getPages().setHomePage(MarkupInheritanceExtension_4.class);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		assertEquals(MarkupInheritanceExtension_4.class, application.getLastRenderedPage().getClass());
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "MarkupInheritanceExpectedResult_4.html"));

		MarkupInheritanceExtension_4 page = (MarkupInheritanceExtension_4)application.getLastRenderedPage();

		Link link = (Link)page.get("link");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();

		assertEquals(MarkupInheritanceExtension_4.class, application.getLastRenderedPage().getClass());

		document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "MarkupInheritanceExpectedResult_4-1.html"));
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_5() throws Exception
	{
	    executeTest(MarkupInheritanceExtension_5.class, "MarkupInheritanceExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_6() throws Exception
	{
	    executeTest(MarkupInheritancePage_6.class, "MarkupInheritanceExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_7() throws Exception
	{
	    executeTest(MarkupInheritanceExtension_7.class, "MarkupInheritanceExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_8() throws Exception
	{
		application.getSettings().setStripWicketTags(true);
	    executeTest(MarkupInheritanceExtension_8.class, "MarkupInheritanceExpectedResult_8.html");
	}
}
