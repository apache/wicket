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
package wicket.markup.html.list;

import java.io.IOException;
import java.util.ArrayList;

import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.MockWebApplication;
import wicket.protocol.http.MockPage;


import junit.framework.TestCase;

/**
 * Test for ListView
 * @author Juergen Donnerstag
 */
public class ListViewTest extends TestCase
{
	/** Use a mock application to handle Link-clicked event */
	private static MockWebApplication application;

	protected void setUp() throws Exception
	{
		super.setUp();

		if (application == null)
		{
			application = new MockWebApplication(null);
		}
	}

	/**
	 * Create a predefined ListView
	 * @param modelListSize # of elements to go into the list
	 * @return list view
	 */
	private ListView createListView(final int modelListSize)
	{
		ArrayList modelList = new ArrayList();
		for (int i = 0; i < modelListSize; i++)
		{
			modelList.add(new Integer(i));
		}

		return new ListView("listView", new Model(modelList))
		{
			protected void populateItem(final ListItem listItem)
			{
				; // do nothing
			}
		};
	}

	/**
	 * Helper: Create a request cycle and set the next page to render
	 * @return request cycle
	 * @throws IOException
	 */
	private WebRequestCycle createRequestCycle() throws IOException
	{
		// Prepare the mock application to test the Link
		application.setupRequestAndResponse();
		WebRequestCycle cycle = new WebRequestCycle(application, application.getWicketSession(),
				application.getWicketRequest(), application.getWicketResponse());

		MockPage page = new MockPage(null);
		cycle.setPage(page);

		return cycle;
	}

	/**
	 * 
	 */
	public void testListView()
	{
		ListView lv = createListView(4);
		assertEquals(4, lv.getList().size());
		assertEquals(4, lv.getViewSize());
		assertEquals(0, lv.getStartIndex());
		assertEquals(new Integer(0), lv.getListObject(0));

		// This is the number of ListViews child-components
		assertEquals(0, lv.size());

		lv.setStartIndex(-1);
		assertEquals(0, lv.getStartIndex());

		lv.setStartIndex(3);
		assertEquals(3, lv.getStartIndex());

		// The upper boundary doesn't get tested, yet.
		lv.setStartIndex(99);
		assertEquals(99, lv.getStartIndex());

		lv.setViewSize(-1);
		assertEquals(0, lv.getViewSize());

		lv.setViewSize(0);
		assertEquals(0, lv.getViewSize());

		// The upper boundary doesn't get tested, yet.
		lv.setViewSize(99);
		assertEquals(0, lv.getViewSize());
		lv.setStartIndex(1);
		assertEquals(3, lv.getViewSize());
	}

	/**
	 * @throws IOException
	 */
	public void testListViewNewItem() throws IOException
	{
		ListView lv = createListView(4);
		ListItem li = lv.newItem(0);
		assertEquals(0, li.getIndex());
		assertEquals(new Integer(0), li.getModelObject());
	}

	/**
	 * @throws IOException
	 */
	public void testEmptyListView() throws IOException
	{
		// Empty tables
		ListView lv = createListView(0);
		assertEquals(0, lv.getStartIndex());
		assertEquals(0, lv.getViewSize());

		// null tables are a special case used for table navigation
		// bar, where there is no underlying model necessary, as
		// listItem.getIndex() is equal to the required listItem.getModelObject()
		lv = new ListView("listView", new Model(null))
		{
			protected void populateItem(final ListItem listItem)
			{
				; // do nothing
			}
		};
		assertEquals(0, lv.getStartIndex());
		assertEquals(0, lv.getViewSize());

		lv.setStartIndex(5);
		lv.setViewSize(10);
		assertEquals(5, lv.getStartIndex());
		assertEquals(10, lv.getViewSize());
	}
}
