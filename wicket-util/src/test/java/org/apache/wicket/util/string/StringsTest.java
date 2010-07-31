/**
 * 
 */
package org.apache.wicket.util.string;

import junit.framework.TestCase;


/**
 * 
 */
public class StringsTest extends TestCase
{
	public void testStripJSessionId()
	{
		String url = "http://localhost/abc";
		assertEquals(url, Strings.stripJSessionId(url));
		assertEquals(url + "/", Strings.stripJSessionId(url + "/"));
		assertEquals(url + "?param", Strings.stripJSessionId(url + "?param"));
		assertEquals(url + "?param=a:b", Strings.stripJSessionId(url + "?param=a:b"));
		assertEquals(url + "/?param", Strings.stripJSessionId(url + "/?param"));
		assertEquals(url, Strings.stripJSessionId(url + ";jsessionid=12345"));
		assertEquals(url + "?param", Strings.stripJSessionId(url + ";jsessionid=12345?param"));
		assertEquals(url + "?param=a:b", Strings.stripJSessionId(url +
			";jsessionid=12345?param=a:b"));
	}
}
