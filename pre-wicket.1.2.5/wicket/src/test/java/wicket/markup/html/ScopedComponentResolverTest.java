/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.markup.html;

import wicket.WicketTestCase;
import wicket.markup.resolver.ScopedComponentResolver;

/**
 * 
 * @author Juergen Donnerstag
 */
public class ScopedComponentResolverTest extends WicketTestCase
{
	// private static final Log log = LogFactory.getLog(ScopedComponentResolverTest.class);

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public ScopedComponentResolverTest(String name)
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
		application.getPageSettings().addComponentResolver(new ScopedComponentResolver());
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
	    executeTest(ScopedPage.class, "ScopedPageExpectedResult.html");
	}
}
