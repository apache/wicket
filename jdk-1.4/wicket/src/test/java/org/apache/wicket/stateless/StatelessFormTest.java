package org.apache.wicket.stateless;

import junit.framework.TestCase;

import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.stateless.pages.HomePage;
import org.apache.wicket.stateless.pages.LoginPage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author marrink
 */
public class StatelessFormTest extends TestCase
{
	private static final Logger log = LoggerFactory.getLogger(StatelessFormTest.class);

	private WicketTester mock = null;

	private WebApplication application;

	private Class homePage = HomePage.class;
	private Class loginPage = LoginPage.class;


	protected void setUp() throws Exception
	{
		mock = new WicketTester(application = new WebApplication()
		{


			public Class getHomePage()
			{
				return StatelessFormTest.this.getHomePage();
			}
		}, "src/test/java/" + getClass().getPackage().getName().replace('.', '/'));
	}

	protected void tearDown() throws Exception
	{
		mock.setupRequestAndResponse();
		mock.getWicketSession().invalidate();
		mock.processRequestCycle();
		mock.destroy();
		mock = null;
		application = null;
		setHomePage(HomePage.class);
		setLoginPage(LoginPage.class);
	}

	/**
	 * @return Returns the homePage.
	 */
	public Class getHomePage()
	{
		return homePage;
	}

	/**
	 * @param homePage The homePage to set.
	 */
	public void setHomePage(Class homePage)
	{
		this.homePage = homePage;
	}

	/**
	 * @return Returns the loginPage.
	 */
	public Class getLoginPage()
	{
		return loginPage;
	}

	/**
	 * @param loginPage The loginPage to set.
	 */
	public void setLoginPage(Class loginPage)
	{
		this.loginPage = loginPage;
	}

	/**
	 * Login through the login page.
	 */
	public void testLogin()
	{
		mock.startPage(getLoginPage());
		mock.assertRenderedPage(getLoginPage());
		FormTester form = mock.newFormTester("signInPanel:signInForm");
		form.setValue("username", "test");
		form.setValue("password", "test");
		form.submit();
		mock.assertRenderedPage(getHomePage());
	}

}
