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
package wicket.markup.html.form;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import wicket.ApplicationSettings;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.encryption.ICrypt;
import wicket.markup.html.form.encryption.NoCrypt;
import wicket.markup.html.panel.SignInPanel;
import wicket.protocol.http.HttpRequestCycle;
import wicket.protocol.http.MockWebApplication;
import wicket.protocol.http.MockPage;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Test cases for Cookie handling
 *
 * @author Juergen Donnerstag
 */
public class CookieTest extends TestCase 
{
    private MockWebApplication application;
    private SignInPanel panel;
    private Form form;
    private Cookie cookieUsername;
    private Cookie cookiePassword;
    private Cookie[] cookies;
    private HtmlPage page;
    private HttpRequestCycle cycle;
    
    /**
     * Create the test case.
     *
     * @param name The test name
     */
    public CookieTest(String name) 
    {
        super(name);
    }

    protected void setUp() throws Exception 
	{
        super.setUp();
        
        application = new MockWebApplication(null);
        application.getPages().setHomePage(MockPage.class);
        application.setupRequestAndResponse();

        final ApplicationSettings settings = application.getSettings();
        settings.setCryptClass(NoCrypt.class);

        this.panel = new SignInPanel("panel")
		{
            public boolean signIn(final String username, final String password)
            {
            	return true;
            }
		};
        
		this.panel.setPersistent(true);
		this.form = (Form)panel.get("signInForm");

        final ICrypt crypt = application.getCrypt();
        final String encryptedPassword = crypt.encryptString("test");
        assertNotNull(encryptedPassword);
        this.cookieUsername = new Cookie("panel.signInForm.username", "juergen");
        this.cookiePassword = new Cookie("panel.signInForm.password", encryptedPassword);
        this.cookies = new Cookie[] {cookieUsername, cookiePassword};
        
        application.getServletRequest().setCookies(cookies);

        cycle = new HttpRequestCycle(
        		application, 
				application.getWicketSession(),
				application.getWicketRequest(),
				application.getWicketResponse());
        
        this.page = new MockPage(null);
		page.add(this.panel);
		
		HttpRequestCycle cycle = new HttpRequestCycle(
		        application, 
		        application.getWicketSession(), 
		        application.getWicketRequest(),
		        application.getWicketResponse());

        application.getWicketSession().setRequestCycle(cycle);
    }

    /**
     * 
     * @throws IOException
     * @throws ServletException
     */
    public void testSetCookieOnForm() throws IOException, ServletException
    {
    	// initialize 
		this.form.setFormComponentValuesFromPersister();
		
		// validate
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        Assert.assertEquals(cookieUsername.getValue(), username.getModelObjectAsString());
        Assert.assertEquals("test", password.getModelObjectAsString());
    }

    /**
     * 
     * @throws IOException
     * @throws ServletException
     */
    public void testSetCookieOnPage() throws IOException, ServletException
    {
		application.getServletRequest().setRequestToComponent(this.form);
        application.processRequestCycle();
        
        // validate
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        System.out.println("!!!!!!!!!!!!! cookieUsername.getValue() = " + cookieUsername.getValue());
        System.out.println("!!!!!!!!!!!!! username.getModelObjectAsString() = " + username.getModelObjectAsString());
        
        // TODO I don't understand what these tests are supposed to do, so I don't know why they are failing...
        
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
		this.form.formSubmitted();
		
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
		this.form.formSubmitted();
		
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
        	    Assert.assertEquals(cookie.getValue(), page.get(cookie.getName()).getModelObjectAsString());
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
        final Cookie cookieUsername = new Cookie("panel.signInForm.username", "juergen");
        final Cookie cookiePassword = new Cookie("panel.signInForm.password", "test");
        final Cookie[] cookies = new Cookie[] {cookieUsername, cookiePassword};
        
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