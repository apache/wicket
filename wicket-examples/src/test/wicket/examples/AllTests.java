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
package wicket.examples;

import junit.framework.Test;
import junit.framework.TestSuite;
import nl.openedge.util.jetty.JettyDecorator;
import wicket.examples.displaytag.DisplaytagTest;
import wicket.examples.forminput.FormInputTest;
import wicket.examples.groovy.GroovyTest;
import wicket.examples.guestbook.GuestbookTest;
import wicket.examples.helloworld.HelloWorldTest;
import wicket.examples.springframework.SpringFrameworkTest;
import wicket.examples.springframework2.SpringFramework2Test;

/**
 * All tests in the project; used by Maven.
 */
public final class AllTests extends TestSuite
{

    /**
     * Construct.
     */
    public AllTests()
    {
        super();
    }

    /**
     * Construct.
     * @param arg0
     * @param arg1
     */
    public AllTests(Class arg0, String arg1)
    {
        super(arg0, arg1);
    }

    /**
     * Construct.
     * @param arg0
     */
    public AllTests(Class arg0)
    {
        super(arg0);
    }

    /**
     * Construct.
     * @param arg0
     */
    public AllTests(String arg0)
    {
        super(arg0);
    }

    /**
     * Suite method.
     * @return test suite
     */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new HelloWorldTest("testHelloWorld"));
		suite.addTest(new GuestbookTest("testHomePage"));
		suite.addTest(new DisplaytagTest("testHomePage"));
		suite.addTest(new SpringFrameworkTest("testHomePage"));
		suite.addTest(new SpringFramework2Test("testHomePage"));
		suite.addTest(new GroovyTest("testHomePage"));
		suite.addTest(new FormInputTest("testHelloWorld"));
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}
}
