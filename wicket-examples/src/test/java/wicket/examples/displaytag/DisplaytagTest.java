/*
 * $Id: DisplaytagTest.java 3905 2006-01-19 20:34:20 +0000 (Thu, 19 Jan 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-01-19 20:34:20 +0000 (Thu, 19 Jan
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.displaytag;

import junit.framework.Test;
import wicket.examples.WicketWebTestCase;

/**
 * jWebUnit test for Hello World.
 */
public class DisplaytagTest extends WicketWebTestCase
{
	/**
	 * 
	 * @return Test
	 */
	public static Test suite()
	{
		return suite(DisplaytagTest.class);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public DisplaytagTest(final String name)
	{
		super(name);
	}

	/**
	 * Simply test that all pages get loaded
	 */
	public void test_1()
	{
		beginAt("/displaytag");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleNoColumns");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.BasicColumns");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleStyles");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleImpObjects");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleSubsets");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleAutolink");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleDecorator");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleDecoratorLink");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExamplePaging");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleSorting");
		dumpResponse(System.out);
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleGrouping");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleSubtotals");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleExport");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExamplePse");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleTwoTables");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleNestedTables");
		assertTitleEquals("Wicket Examples - displaytag");

		beginAt("/displaytag?bookmarkablePage=wicket.examples.displaytag.ExampleCheckbox");
		assertTitleEquals("Wicket Examples - displaytag");
	}
}
