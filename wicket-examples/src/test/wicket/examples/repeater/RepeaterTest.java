/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.repeater;

import junit.framework.Test;
import wicket.examples.WicketWebTestCase;

/**
 * jWebUnit test for Hello World.
 */
public class RepeaterTest extends WicketWebTestCase
{
	/**
	 * 
	 * @return Test
	 */
	public static Test suite()
	{
		return suite(RepeaterTest.class);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public RepeaterTest(String name)
	{
		super(name);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testHelloWorld() throws Exception
	{
		beginAt("/repeater");
		this.dumpResponse(System.out);
		assertTitleEquals("Wicket Examples - repeater views");

		this.clickLinkWithText("OrderedRepeatingView Example - basic example of a repeater view");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this
				.clickLinkWithText("RefreshingView Example - basic view that recreates its items every request");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this.clickLinkWithText("Simple DataView Example - simple example of a dataview");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this
				.clickLinkWithText("Paging DataView Example - builds on previous to demonstrate paging and page navigation");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this
				.clickLinkWithText("Sorting DataView Example - builds on previous to demonstrate sorting");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this
				.clickLinkWithText("DataView and optimized item removal - demonstrates a dataview with a different IItemReuseStrategy implementation");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this
				.clickLinkWithText("DataGridView Example - a view that generates grids where rows are representing by items of the data provider and columns are represented by an array of ICellPopulators objects");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this
				.clickLinkWithText("DataTable Example - demonstrates data table component that wraps dataview to offer easy paging and sorting");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");

		this.clickLinkWithText("[go back]");
		this.clickLinkWithText("GridView Example - demonstrates a grid view");
		assertTitleEquals("Wicket Examples - repeater views");
		assertTextPresent("Selected Contact: ");
		assertTextPresent("No Contact Selected");
	}
}
