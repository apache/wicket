package org.apache.wicket.protocol.http;

import junit.framework.TestCase;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

import javax.servlet.http.Cookie;
import java.util.List;

public class ModifyCookiePageTest extends TestCase
{
	private WicketTester tester;

	@Override
	public void setUp()
	{
		tester = new WicketTester();
	}

	@Test
	public void testSetCookieWithinLinkListener()
	{
		// render page
		tester.startPage(ModifyCookiePage.class);
		tester.assertRenderedPage(ModifyCookiePage.class);

		// click link that creates a cookie with in the link listener
		tester.clickLink(ModifyCookiePage.CREATE_COOKIE_ID);

		// check page is rendered
		tester.assertRenderedPage(ModifyCookiePage.class);

		// get response
		MockHttpServletResponse response = tester.getLastResponse();
		assertNotNull(response);

		// check that one cookie was set
		List<Cookie> cookies = response.getCookies();
		assertEquals(1, cookies.size());

		// check that cookie contains proper values
		Cookie cookie = cookies.get(0);
		assertEquals(ModifyCookiePage.COOKIE_NAME, cookie.getName());
		assertEquals(ModifyCookiePage.COOKIE_VALUE, cookie.getValue());
	}
}