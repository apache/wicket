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
package com.voicetribe.wicket.markup.html.form;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.panel.SignInPanel;
import com.voicetribe.wicket.protocol.http.HttpRequestCycle;
import com.voicetribe.wicket.protocol.http.MockHttpApplication;
import com.voicetribe.wicket.protocol.http.MockPage;

/**
 * Test cases for Cookie handling
 *
 * @author Juergen Donnerstag
 */
public class CookieTest extends TestCase 
{
    private MockHttpApplication application;
    private SignInPanel panel;
    private Form form;
    
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
        
        application = new MockHttpApplication(null);
        application.getSettings().setHomePage(MockPage.class);
        application.setupRequestAndResponse();

        this.panel = new SignInPanel("panel")
		{
            protected String signIn(final RequestCycle cycle, final String username, final String password)
            {
            	return null;
            }
		};
        
		this.form = (Form)panel.get("signInForm");
    }
    
    public void testSetCookieOnForm() throws IOException, ServletException
    {
    	// initialize 
        final Cookie cookieUsername = new Cookie("signInForm.username", "juergen");
        final Cookie cookiePassword = new Cookie("signInForm.password", "test");
        final Cookie[] cookies = new Cookie[] {cookieUsername, cookiePassword};
        
        application.getServletRequest().setCookies(cookies);

        HttpRequestCycle cycle = new HttpRequestCycle(
        		application, 
				application.getWicketSession(),
				application.getWicketRequest(),
				application.getWicketResponse());
        
        // test
		this.form.setFormComponentValuesFromPersister(cycle);
		
		// validate
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        Assert.assertEquals(cookieUsername.getValue(), username.getModelObjectAsString());
        Assert.assertEquals(cookiePassword.getValue(), password.getModelObjectAsString());
    }
    
    public void testSetCookieOnPage() throws IOException, ServletException
    {
    	// initialize
        final Cookie cookieUsername = new Cookie("panel.signInForm.username", "juergen");
        final Cookie cookiePassword = new Cookie("panel.signInForm.password", "test");
        final Cookie[] cookies = new Cookie[] {cookieUsername, cookiePassword};
        
        application.getServletRequest().setCookies(cookies);
        
        HtmlPage page = new MockPage(null);
		page.add(this.panel);

		application.getServletRequest().setRequestToComponent(this.form);
        
		// test
        application.processRequestCycle();
        
        // validate
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        Assert.assertEquals(cookieUsername.getValue(), username.getModelObjectAsString());
        Assert.assertEquals(cookiePassword.getValue(), password.getModelObjectAsString());
    }
    
    public void testPersistCookieWithPersistenceDisabled() throws IOException, ServletException
    {
    	// initialize 
        HtmlPage page = new MockPage(null);
		page.add(this.panel);
		
        HttpRequestCycle cycle = new HttpRequestCycle(
        		application, 
				application.getWicketSession(),
				application.getWicketRequest(),
				application.getWicketResponse());
        
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        //username.setPersistent(true);
        //password.setPersistent(true);
        
        // test
        // will call persistFromComponentData(), which is private
		this.form.formSubmitted(cycle);
		
		// validate
        Collection cookies = application.getServletResponse().getCookies();
        Assert.assertEquals(cookies.size(), 0);
    }
    
    public void testPersistCookie() throws IOException, ServletException
    {
    	// initialize 
        HtmlPage page = new MockPage(null);
		page.add(this.panel);
		
        HttpRequestCycle cycle = new HttpRequestCycle(
        		application, 
				application.getWicketSession(),
				application.getWicketRequest(),
				application.getWicketResponse());
        
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        username.setPersistent(true);
        password.setPersistent(true);
        
        // test
        // will call persistFromComponentData(), which is private
		this.form.formSubmitted(cycle);
		
		// validate
        Collection cookies = application.getServletResponse().getCookies();
        Assert.assertEquals(cookies.size(), 2);
        Iterator iter = cookies.iterator();
        while (iter.hasNext()) 
        {
        	Cookie cookie = (Cookie)iter.next();
        	Assert.assertNotNull(page.get(cookie.getName()));
        	Assert.assertEquals(cookie.getValue(), page.get(cookie.getName()).getModelObjectAsString());
        }
    }
    
    public void testRemoveFromPage() throws IOException, ServletException
    {
    	// initialize 
        HtmlPage page = new MockPage(null);
		page.add(this.panel);
		
        HttpRequestCycle cycle = new HttpRequestCycle(
        		application, 
				application.getWicketSession(),
				application.getWicketRequest(),
				application.getWicketResponse());
        
        FormComponent username = (FormComponent)panel.get("signInForm.username");
        FormComponent password = (FormComponent)panel.get("signInForm.password");
        
        username.setPersistent(true);
        password.setPersistent(true);
        
        // test
		page.removePersistedFormData(cycle, SignInPanel.SignInForm.class, true);
		
		// validate
        Collection cookieCollection = application.getServletResponse().getCookies();
        Assert.assertEquals(0, cookieCollection.size());

        // initialize
        final Cookie cookieUsername = new Cookie("panel.signInForm.username", "juergen");
        final Cookie cookiePassword = new Cookie("panel.signInForm.password", "test");
        final Cookie[] cookies = new Cookie[] {cookieUsername, cookiePassword};
        
        application.getServletRequest().setCookies(cookies);
        
        // test
		page.removePersistedFormData(cycle, SignInPanel.SignInForm.class, true);
		
		// validate
        cookieCollection = application.getServletResponse().getCookies();
        Assert.assertEquals(2, cookieCollection.size());
        Iterator iter = cookieCollection.iterator();
        while (iter.hasNext()) 
        {
        	Cookie cookie = (Cookie)iter.next();
        	Assert.assertNotNull(page.get(cookie.getName()));
        	Assert.assertEquals(cookie.getMaxAge(), 0);
        }
    }
}