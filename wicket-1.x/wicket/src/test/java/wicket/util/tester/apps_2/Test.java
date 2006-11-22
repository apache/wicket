/*
 * $Id: Book.java 2940 2005-10-06 20:37:53Z jdonnerstag $ $Revision$
 * $Date: 2005-10-06 22:37:53 +0200 (Do, 06 Okt 2005) $
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
package wicket.util.tester.apps_2;

import junit.framework.TestCase;
import wicket.authorization.IAuthorizationStrategy;
import wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import wicket.util.tester.WicketTester;

/**
 * 
 * @author Juergen Donnerstag
 */
public class Test extends TestCase
{
	/**
	 * 
	 */
	public void testRedirect()
	{
		final WicketTester tester = new WicketTester();
		final IAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
				RedirectPage.class, LoginPage.class)
		{
			protected boolean isAuthorized()
			{
				return false;
			}
		};

		tester.getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);

		tester.startPage(RedirectPage.class);
		tester.assertRenderedPage(LoginPage.class);
	}
}
