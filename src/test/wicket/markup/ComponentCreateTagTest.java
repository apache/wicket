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
import wicket.markup.html.list.DiffUtil;
import wicket.protocol.http.MockWebApplication;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class ComponentCreateTagTest extends TestCase
{

	private MockWebApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public ComponentCreateTagTest(String name)
	{
		super(name);
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
	    executeTest(ComponentCreateTag_1.class, "ComponentCreateTagExpectedResult_1.html");
	}

	/**
	 * A Table with X rows and a label inside
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
	    executeTest(ComponentCreateTag_2.class, "ComponentCreateTagExpectedResult_2.html");
	}

	/**
	 * A Border
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_3() throws Exception
	{
	    executeTest(ComponentCreateTag_3.class, "ComponentCreateTagExpectedResult_3.html");
	}

	/**
	 * A Border inside another Border (nested <wicket:components>)
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_4() throws Exception
	{
	    executeTest(ComponentCreateTag_4.class, "ComponentCreateTagExpectedResult_4.html");
	}

	/**
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	public void executeTest(final Class pageClass, final String filename) throws Exception
	{
		application = new MockWebApplication(null);
		application.getPages().setHomePage(pageClass);
		application.getSettings().setStripWicketTags(true);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		String document = application.getServletResponse().getDocument();
		System.out.println(document);

		DiffUtil.validatePage(document, this.getClass(), filename);
	}
}
