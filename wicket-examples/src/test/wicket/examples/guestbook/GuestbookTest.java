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
package wicket.examples.guestbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTestCase;
import nl.openedge.util.jetty.JettyDecorator;

/**
 * jWebUnit test for Hello World.
 */
public class GuestbookTest extends WebTestCase
{
	private static Log log = LogFactory.getLog(GuestbookTest.class);

    /**
     * Construct.
     * @param name name of test
     */
    public GuestbookTest(String name)
    {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        getTestContext().setBaseUrl("http://localhost:8098/wicket-examples");
    }

    /**
     * Test page.
     * @throws InterruptedException
     */
    public void testHomePage() throws InterruptedException {
        try
        {
            beginAt("/guestbook");
        } 
        catch (Throwable ex)
        {
            log.error(ex.getMessage());
        }
        
        this.dumpResponse(System.out);
        assertTitleEquals("Wicket Examples - guestbook");
        this.assertElementNotPresent("comments");
        
        assertFormPresent("wicket-commentForm");
        this.assertFormElementPresent("0.commentForm.text");
        this.setFormElement("0.commentForm.text", "test-1");
        this.submit();

        this.dumpResponse(System.err);
        assertTitleEquals("Wicket Examples - guestbook");
        assertFormPresent("wicket-commentForm");
        this.assertFormElementPresent("0.commentForm.text");
        this.assertElementPresent("wicket-comments");
        // assertTextInElement() seems to be buggy
        //this.assertTextInElement("text", "test-1");
        this.assertTextPresent("test-1");
        this.setFormElement("0.commentForm.text", "test-2");
        this.submit();

        assertTitleEquals("Wicket Examples - guestbook");
        this.assertElementPresent("wicket-comments");
        // assertTextInElement() seems to be buggy
        //this.assertTextInElement("text", "test-1");
        this.assertTextPresent("test-1");
        //this.assertTextInElement("text", "test-2");
        this.assertTextPresent("test-2");
    }

	/**
	 * Suite method.
	 * 
	 * @return Test suite
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new GuestbookTest("testHomePage"));
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}    
}
