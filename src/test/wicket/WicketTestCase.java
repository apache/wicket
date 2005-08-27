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
package wicket;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.list.DiffUtil;
import wicket.protocol.http.MockWebApplication;

/**
 * Base class for tests which require comparing wicket response
 * with a file
 */
public class WicketTestCase extends TestCase
{
	private static Log log = LogFactory.getLog(WicketTestCase.class);

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
		
		application.getPages().setHomePage(pageClass);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		assertEquals(pageClass, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), filename));
	}
}
