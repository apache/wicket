/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.panels.signin;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.protocol.http.WebRequestCycle;
import wicket.settings.ISecuritySettings;
import wicket.util.crypt.ICrypt;
import wicket.util.crypt.NoCryptFactory;
import wicket.util.tester.WicketTester;


/**
 * Test cases for Cookie handling
 * 
 * @author Juergen Donnerstag
 */
public class CookieTest extends TestCase
{
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(CookieTest.class);

	private WicketTester application;
	private SignInPanel panel;
	private Form form;
	private Cookie cookieUsername;
	private Cookie cookiePassword;
	private Cookie[] cookies;
	private WebPage page;

	/**
	 * Create the test case.
	 * 
	 * @param name
	 *            The test name
	 */
	public CookieTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		application = new WicketTester(null);
		application.setHomePage(MockPage.class);
		application.setupRequestAndResponse();
		
		final ISecuritySettings settings = application.getSecuritySettings();
		settings.setCryptFactory(new NoCryptFactory());

		this.page = new MockPage(null);
		this.panel = new SignInPanel(page, "panel")
		{
			@Override
			public boolean signIn(final String username, final String password)
			{
				return true;
			}
		};

		this.panel.setPersistent(true);
		this.form = (Form)panel.get("signInForm");

		final ICrypt crypt = application.getSecuritySettings().getCryptFactory().newCrypt();
		final String encryptedPassword = crypt.encryptUrlSafe("test");
		assertNotNull(encryptedPassword);
		this.cookieUsername = new Cookie("panel:signInForm:username", "juergen");
		this.cookiePassword = new Cookie("panel:signInForm:password", encryptedPassword);
		this.cookies = new Cookie[] { cookieUsername, cookiePassword };

		application.getServletRequest().setCookies(cookies);

		new WebRequestCycle(application.getWicketSession(), application.getWicketRequest(),
				application.getWicketResponse());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	public void testSetCookieOnForm() throws IOException, ServletException
	{
		// initialize
		this.form.loadPersistentFormComponentValues();

		// validate
		FormComponent username = (FormComponent)panel.get("signInForm:username");
		FormComponent password = (FormComponent)panel.get("signInForm:password");

		Assert.assertNotNull(username);
		Assert.assertNotNull(password);

		Assert.assertNotNull(cookieUsername);

		Assert.assertEquals(cookieUsername.getValue(), username.getModelObjectAsString());
		Assert.assertEquals("test", password.getModelObjectAsString());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	public void testPersistCookieWithPersistenceDisabled() throws IOException, ServletException
	{
		// test will call persistFromComponentData(), which is private
		this.panel.setPersistent(false);
		this.form.onFormSubmitted();

		// validate
		Collection cookies = application.getServletResponse().getCookies();
		Iterator iter = cookies.iterator();
		while (iter.hasNext())
		{
			Assert.assertEquals(0, ((Cookie)iter.next()).getMaxAge());
		}
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	public void testPersistCookie() throws IOException, ServletException
	{
		panel.setPersistent(true);

		// test will call persistFromComponentData(), which is private
		this.form.onFormSubmitted();

		// validate
		Collection cookies = application.getServletResponse().getCookies();
		Assert.assertEquals(2, cookies.size());
		Iterator iter = cookies.iterator();
		while (iter.hasNext())
		{
			Cookie cookie = (Cookie)iter.next();
			Assert.assertNotNull(page.get(cookie.getName()));
			// Skip "deleted" cookies
			if (page.get(cookie.getName()).getModelObjectAsString() != "")
			{
				Assert.assertEquals(cookie.getValue(), page.get(cookie.getName())
						.getModelObjectAsString());
			}
		}
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	public void testRemoveFromPage() throws IOException, ServletException
	{
		panel.setPersistent(true);

		// test
		page.removePersistedFormData(SignInPanel.SignInForm.class, true);

		// validate
		Collection cookieCollection = application.getServletResponse().getCookies();
		// Cookies are remove by setting maxAge == 0
		Assert.assertEquals(2, cookieCollection.size());

		// initialize
		final Cookie cookieUsername = new Cookie("panel:signInForm:username", "juergen");
		final Cookie cookiePassword = new Cookie("panel:signInForm:password", "test");
		final Cookie[] cookies = new Cookie[] { cookieUsername, cookiePassword };

		application.getServletRequest().setCookies(cookies);

		// test
		page.removePersistedFormData(SignInPanel.SignInForm.class, true);

		// validate
		cookieCollection = application.getServletResponse().getCookies();
		Assert.assertEquals(4, cookieCollection.size());
		Iterator iter = cookieCollection.iterator();
		while (iter.hasNext())
		{
			Cookie cookie = (Cookie)iter.next();
			Assert.assertNotNull(page.get(cookie.getName()));
			Assert.assertEquals(cookie.getMaxAge(), 0);
		}
	}
}