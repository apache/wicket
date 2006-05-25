/*
 * $Id: Signin2Test.java 5395 2006-04-16 13:42:28 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:42:28 +0000 (Sun, 16 Apr
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
package wicket.examples.signin2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import wicket.examples.WicketWebTestCase;

/**
 * jWebUnit test for Hello World.
 */
public class Signin2Test extends WicketWebTestCase
{
	private final static Log log = LogFactory.getLog(Signin2Test.class);

	/**
	 * 
	 * @return Test
	 */
	public static Test suite()
	{
		return suite(Signin2Test.class);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public Signin2Test(String name)
	{
		super(name);
	}

	/**
	 * Test page.
	 */
	public void testSignIn2()
	{
		beginAt("/signin2");
		assertTitleEquals("Wicket Examples - signin2");

		this.setFormElement("username", "wicket");
		this.setFormElement("password", "wicket");
		this.checkCheckbox("rememberMeRow:rememberMe");
		log.debug("Submit Login screen");
		this.submit("submit");

		// this.dumpResponse(System.err);
		assertTitleEquals("Wicket Examples - signin2");
		// a) With wicket submitting a form will result in a temporary redirect,
		// with the redirect setting the Cookie.
		// b) jWebUnits Cookie test methods are all using the http response
		// object only
		// c) Like a browser, jwebunit will automatically handle the redirect
		// request
		// Hence dumpCookie will not print an Cookie and assertCookiePresent
		// will
		// fail.
		// The only mean available is to indirectly test the cookies. Indirectly
		// because
		// the screen flow depends on the cookies.
		// this.dumpCookies(System.err);
		// this.assertCookiePresent("signInPanel.signInForm.username");
		// this.assertCookiePresent("signInPanel.signInForm.password");
		this.dumpResponse(System.out);
		log.debug("Click 'Sign Out'");
		this.clickLinkWithText("Sign Out");

		assertTitleEquals("Wicket Examples - signin2");
		this.dumpResponse(System.out);
		log.debug("Click 'Home'");
		this.clickLinkWithText("Home");
		/*
		 * jWebUnit is missing assertCookieNotPresent() try { // jWebUnit does
		 * not offer an assertCookieNotPresent
		 * this.assertCookiePresent("signInPanel.signInForm.username");
		 * assertTrue("Should have thrown an excpetion", false); } catch
		 * (AssertionFailedError ex) { ; // ok }
		 * 
		 * try { // jWebUnit does not offer an assertCookieNotPresent
		 * this.assertCookiePresent("signInPanel.signInForm.password");
		 * assertTrue("Should have thrown an excpetion", false); } catch
		 * (AssertionFailedError ex) { ; // ok }
		 */
		assertTitleEquals("Wicket Examples - signin2");
	}
}
