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
package wicket.examples.helloworld;

import junit.framework.Test;
import junit.framework.TestSuite;
import nl.openedge.util.jetty.JettyDecorator;
import wicket.examples.WicketWebTestCase;

/**
 * jWebUnit test for Hello World.
 */
public class HelloWorldTest extends WicketWebTestCase
{
    /**
     * Construct.
     * @param name name of test
     */
    public HelloWorldTest(String name)
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
     * @throws Exception
     */
    public void testHelloWorld() throws Exception 
    {
        beginAt("/helloworld");
        this.dumpResponse(System.out);
        assertTitleEquals("Wicket Examples - helloworld");
        //assertXPath("//SPAN", "Hello World!");
        //assertWicketIdTagText("message", "Hello World");
        
        assertTextInElement("message", "Hello World!");
    }

	/**
	 * Suite method.
	 * 
	 * @return Test suite
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new HelloWorldTest("testHelloWorld"));
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}    
}
