/**
 * 
 */
package wicket.markup.html.basic;

import wicket.markup.html.form.Form;
import wicket.protocol.http.MockHttpServletRequest;
import wicket.protocol.http.MockWebApplication;
import junit.framework.TestCase;

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
		MockWebApplication mockWebApp = new MockWebApplication(null);
		
        mockWebApp.getPages().setHomePage(SimpleResponsePageClass.class);
        mockWebApp.setupRequestAndResponse();
        mockWebApp.processRequestCycle();
        SimpleResponsePageClass manageBook = (SimpleResponsePageClass) mockWebApp.getLastRenderedPage();;

        Form form = (Form) manageBook.get("form");
        mockWebApp.setupRequestAndResponse();

        MockHttpServletRequest mockRequest = mockWebApp.getServletRequest();
        mockRequest.setRequestToComponent(form);
        mockWebApp.processRequestCycle();      
       
        //assertion failed,  getLastRenderedPage() return null.
        assertTrue( mockWebApp.getLastRenderedPage() instanceof SimplePage);

    }
}
