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
import wicket.examples.guestbook.GuestbookTest;
import wicket.examples.hangman.HangManTest;
import wicket.examples.hangman.WordGeneratorTest;
import wicket.examples.helloworld.HelloWorldTest;
import wicket.examples.linkomatic.LinkomaticTest;
import wicket.examples.signin2.Signin2Test;

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
		suite.addTestSuite(HangManTest.class);
		suite.addTestSuite(WordGeneratorTest.class);
		suite.addTestSuite(HelloWorldTest.class);
		suite.addTestSuite(GuestbookTest.class);
		suite.addTestSuite(DisplaytagTest.class);
		suite.addTestSuite(FormInputTest.class);
		suite.addTestSuite(LinkomaticTest.class);
		suite.addTestSuite(Signin2Test.class);
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}
}
