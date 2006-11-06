/*
 * $Id: InterceptTest.java 4689 2006-03-01 15:22:39 +0000 (Wed, 01 Mar 2006)
 * joco01 $ $Revision$ $Date: 2006-03-01 15:22:39 +0000 (Wed, 01 Mar
 * 2006) $
 * 
 * ====================================================================
 * Copyright (c) 2005, Topicus B.V. All rights reserved.
 */
package wicket.markup.html.form.login;

import junit.framework.TestCase;
import wicket.Component;
import wicket.ISessionFactory;
import wicket.Page;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebSession;
import wicket.util.string.Strings;
import wicket.util.tester.WicketTester;


/**
 * 
 * @author marrink
 */
public class InterceptTest extends TestCase
{
	private WicketTester tester;

	private MyWebApplication application;
	
	/**
	 * Constructor for InterceptTest.
	 * 
	 * @param arg0
	 */
	public InterceptTest(String arg0)
	{
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		application = new MyWebApplication();
		tester = new WicketTester(application, "src/test/"
				+ getClass().getPackage().getName().replace('.', '/'));
	}

	/**
	 * 
	 */
	public void testClickLink()
	{
		tester.setupRequestAndResponse();
		tester.processRequestCycle();
		MockLoginPage loginPage = (MockLoginPage)tester.getLastRenderedPage();
		assertEquals(application.getLoginPage(), loginPage.getClass());

		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(loginPage.getForm());
		tester.getServletRequest().setParameter(loginPage.getTextField().getInputName(),
				"admin");
		tester.processRequestCycle();

		// continueToInterceptPage seems to return the same call, causing it to
		// login twice as a result the lastrendered page is null
		assertEquals(application.getHomePage(), tester.getLastRenderedPage().getClass());

		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(
				tester.getLastRenderedPage().get("link"));
		tester.processRequestCycle();
		assertEquals(PageA.class, tester.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 */
	public void testClickLink2()
	{
		// same as above but uses different technique to login
		tester.setupRequestAndResponse();
		tester.processRequestCycle();
		MockLoginPage loginPage = (MockLoginPage)tester.getLastRenderedPage();
		assertEquals(application.getLoginPage(), loginPage.getClass());

		// bypass form completely to login but continue to intercept page
		tester.setupRequestAndResponse();
		WebRequestCycle requestCycle = tester.createRequestCycle();
		assertTrue(((MockLoginPage)tester.getLastRenderedPage()).login("admin"));
		tester.processRequestCycle(requestCycle);
		assertEquals(application.getHomePage(), tester.getLastRenderedPage().getClass());

		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(
				tester.getLastRenderedPage().get("link"));
		tester.processRequestCycle();
		assertEquals(PageA.class, tester.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 */
	private static class MyWebApplication extends WebApplication implements ISessionFactory
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param path
		 */
		public MyWebApplication()
		{
		}

		/**
		 * @see wicket.Application#getHomePage()
		 */
		@Override
		public Class<? extends Page> getHomePage()
		{
			return MockHomePage.class;
		}

		@Override
		protected void init()
		{
			super.init();
			
			getSecuritySettings().setAuthorizationStrategy(new MyAuthorizationStrategy());
		}
		
		/**
		 * 
		 * @return Class
		 */
		public Class getLoginPage()
		{
			return MockLoginPage.class;
		}

		/**
		 * 
		 * @see wicket.ISessionFactory#newSession()
		 */
		@Override
		public Session newSession()
		{
			return new MySession(this);
		}

		/**
		 * 
		 * @see wicket.Application#getSessionFactory()
		 */
		@Override
		protected ISessionFactory getSessionFactory()
		{
			return this;
		}
	}

	/**
	 * 
	 */
	public static class MySession extends WebSession
	{
		private static final long serialVersionUID = 1L;

		private String username;

		/**
		 * @param application
		 */
		protected MySession(WebApplication application)
		{
			super(application);
		}

		protected final String getUsername()
		{
			return username;
		}

		protected final void setUsername(String username)
		{
			this.username = username;
		}

		/**
		 * 
		 * @return boolean
		 */
		public boolean isLoggedIn()
		{
			return !Strings.isEmpty(username);
		}
	}

	/**
	 * 
	 */
	private static class MyAuthorizationStrategy implements IAuthorizationStrategy
	{
		/**
		 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
		 */
		public boolean isInstantiationAuthorized(Class componentClass)
		{
			if (MockHomePage.class.equals(componentClass)
					&& !((MySession)Session.get()).isLoggedIn())
			{
				throw new RestartResponseAtInterceptPageException(MockLoginPage.class);
			}
			return true;
		}

		/**
		 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
		 *      wicket.authorization.Action)
		 */
		public boolean isActionAuthorized(Component component, Action action)
		{
			return true;
		}
	}
}
