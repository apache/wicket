package org.apache.wicket.protocol.http.mock;

import javax.servlet.http.Cookie;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for Cookies helper class
 */
public class CookiesTest extends Assert
{
	@Test
	public void testIsEqual() throws Exception
	{
		Cookie c1 = new Cookie("Name", "Value");
		Cookie c2 = new Cookie("Name", "Value");

		assertTrue(Cookies.isEqual(c1, c2));

		c2.setPath("Path");
		assertFalse(Cookies.isEqual(c1, c2));

		c1.setPath("Path");
		assertTrue(Cookies.isEqual(c1, c2));

		c2.setDomain("Domain");
		assertFalse(Cookies.isEqual(c1, c2));

		c1.setDomain("Domain");
		assertTrue(Cookies.isEqual(c1, c2));
	}
}
