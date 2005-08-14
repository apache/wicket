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
package wicket.markup.html.link;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.list.DiffUtil;
import wicket.protocol.http.MockWebApplication;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class HrefTest extends TestCase
{
	private static Log log = LogFactory.getLog(HrefTest.class);

	private MockWebApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public HrefTest(String name)
	{
		super(name);
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
	    executeTest(Href_1.class, "HrefExpectedResult_1.html", false);
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
	    executeTest(Href_1.class, "HrefExpectedResult_1-1.html", true);
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_3() throws Exception
	{
	    executeTest(Href_2.class, "HrefExpectedResult_2.html", true);
	}

	/**
	 * @param pageClass
	 * @param filename
	 * @param stripWicketTag
	 * @throws Exception
	 */
	public void executeTest(final Class pageClass, final String filename, 
	        final boolean stripWicketTag) throws Exception
	{
		application = new MockWebApplication(null);
		application.getPages().setHomePage(pageClass);
		application.getSettings().setStripWicketTags(stripWicketTag);
		
		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		String document = application.getServletResponse().getDocument();
		log.info(document);

		assertTrue(DiffUtil.validatePage(document, this.getClass(), filename));
	}
}
