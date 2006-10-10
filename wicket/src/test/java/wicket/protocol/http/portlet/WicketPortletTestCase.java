/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http.portlet;

import junit.framework.TestCase;
import wicket.Component;
import wicket.Page;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.util.diff.DiffUtil;

/**
 * 
 * Base class for wicket portlet tests.
 * 
 * @author Janne Hietam&auml;ki
 */
public abstract class WicketPortletTestCase extends TestCase
{
	/** */
	public MockPortletApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public WicketPortletTestCase(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		application = new MockPortletApplication(null);
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

		application.processRenderRequestCycle(application.createRenderRequestCycle());

		assertEquals(pageClass, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getPortletResponse().getDocument();
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

		application.createActionRequest();
		PortletActionRequestCycle cycle = application.createActionRequestCycle();
		application.getPortletRequest().setRequestToComponent(component);
		application.processActionRequestCycle(cycle);


		application.createRenderRequest();
		application.processRenderRequestCycle(application.createRenderRequestCycle());
		
		String document = application.getPortletResponse().getDocument();
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

		application.createActionRequest();
		PortletActionRequestCycle cycle = application.createActionRequestCycle();
		application.getPortletRequest().setRequestToRedirectString(
				behavior.getCallbackUrl(false).toString());
		application.processActionRequestCycle(cycle);

		application.createRenderRequest();
		application.processRenderRequestCycle(application.createRenderRequestCycle());

		String document = application.getPortletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, pageClass, filename));
	}	
}