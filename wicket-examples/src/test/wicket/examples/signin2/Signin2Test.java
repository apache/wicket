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
package wicket.examples.signin2;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTestCase;
import nl.openedge.util.jetty.JettyDecorator;

/**
 * jWebUnit test for Hello World.
 */
public class Signin2Test extends WebTestCase
{

    /**
     * Construct.
     * @param name name of test
     */
    public Signin2Test(String name)
    {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        getTestContext().setBaseUrl("http://localhost:8098/wicket-examples");
    }

    /**
     * Test page.
     */
    public void testSignIn2() {
        beginAt("/signin2");
        this.dumpResponse(System.err);
        assertTitleEquals("Wicket Examples - signin2");
        
        this.setFormElement("1:signInPanel:signInForm:username", "wicket");
        this.setFormElement("1:signInPanel:signInForm:password", "wicket");
        this.checkCheckbox("1:signInPanel:signInForm:rememberMeRow:rememberMe");
        this.submit("submit");
        
        //this.dumpResponse(System.err);
        assertTitleEquals("Wicket Examples - signin2");
        // a) With wicket submitting a form will result in a temporary redirect, with 
        // the redirect setting the Cookie.
        // b) jWebUnits Cookie test methods are all using the http response object only
        // c) Like a browser, jwebunit will automatically handle the redirect request
        // Thus dumpCookie will not print an Cookie and assertCookiePresent will fail.
        // The only mean available is to indirectly test the cookies. Indirectly because
        // the screen flow depends on the cookies.
        //this.dumpCookies(System.err);
        //this.assertCookiePresent("signInPanel.signInForm.username");
        //this.assertCookiePresent("signInPanel.signInForm.password");
        this.clickLinkWithText("Sign Out");
        
        assertTitleEquals("Wicket Examples - signin2");
        this.clickLinkWithText("Home");
/* jWebUnit is missing assertCookieNotPresent()
        try
        {
            // jWebUnit does not offer an assertCookieNotPresent
            this.assertCookiePresent("signInPanel.signInForm.username");
            assertTrue("Should have thrown an excpetion", false);
        }
        catch (AssertionFailedError ex)
        {
            ; // ok
        }

        try
        {
            // jWebUnit does not offer an assertCookieNotPresent
            this.assertCookiePresent("signInPanel.signInForm.password");
            assertTrue("Should have thrown an excpetion", false);
        }
        catch (AssertionFailedError ex)
        {
            ; // ok
        }
*/
        assertTitleEquals("Wicket Examples - signin2");
    }

	/**
	 * Suite method.
	 * 
	 * @return Test suite
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new Signin2Test("testSignIn2"));
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}    
}
