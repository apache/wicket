/**
 * 
 */
package wicket.markup.html.basic;

import junit.framework.TestCase;
import wicket.markup.html.form.Form;
import wicket.protocol.http.MockHttpServletRequest;
import wicket.util.tester.WicketTester;

/**
 * @author jcompagner
 * 
 */
public class SimpleResponsePageClassTest extends TestCase
{
	/**
	 * @throws Exception
	 */
	public void testResponsePageClass() throws Exception
	{
		WicketTester tester = new WicketTester(SimpleResponsePageClass.class);
		tester.setupRequestAndResponse();
		tester.processRequestCycle();
		SimpleResponsePageClass manageBook = (SimpleResponsePageClass)tester.getLastRenderedPage();

		Form form = (Form)manageBook.get("form");
		tester.setupRequestAndResponse();

		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.setRequestToComponent(form);
		tester.processRequestCycle();

		// assertion failed, getLastRenderedPage() return null.
		assertTrue(tester.getLastRenderedPage() instanceof SimplePage);
	}
}
