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
package wicket.examples.displaytag;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTestCase;
import nl.openedge.util.jetty.JettyDecorator;

/**
 * jWebUnit test for Hello World.
 */
public class DisplaytagTest extends WebTestCase
{
    /**
     * Construct.
     * @param name name of test
     */
    public DisplaytagTest(String name)
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
     * Simply test that all pages get loaded 
     */
    public void testHomePage() 
    { 
        beginAt("/displaytag");
        assertTitleEquals("How to do tables like displaytag with Wicket");

        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleNoColumns");
        assertTitleEquals("Example No Columns");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.BasicColumns");
        assertTitleEquals("Basic Columns");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleStyles");
        assertTitleEquals("Example Styles");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleImpObjects");
        assertTitleEquals("Example Imp Objects");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleSubsets");
        assertTitleEquals("Example Subsets");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleAutolink");
        assertTitleEquals("Example Autolink");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleDecorator");
        assertTitleEquals("Example Decorator");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleDecoratorLink");
        assertTitleEquals("Example Decorator Link");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExamplePaging");
        assertTitleEquals("Example Paging");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleSorting");
        assertTitleEquals("Example Sorting");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleGrouping");
        assertTitleEquals("Example Grouping");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleSubtotals");
        assertTitleEquals("Example Subtotals");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleExport");
        assertTitleEquals("Example Export");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExamplePse");
        assertTitleEquals("Example PSE");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleTwoTables");
        assertTitleEquals("Example Two Tables");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleNestedTables");
        assertTitleEquals("Example Nested Table");
        
        beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleCheckbox");
        assertTitleEquals("Example Checkbox");
    }

    /**
     * Suite method.
     * @return test suite
     */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new DisplaytagTest("testHomePage"));
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}    
}
