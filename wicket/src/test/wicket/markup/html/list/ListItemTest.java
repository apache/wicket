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

import java.util.ArrayList;

import junit.framework.TestCase;
import wicket.model.Model;
import wicket.protocol.http.MockWebApplication;


/**
 * Test for list items.
 * @author Juergen Donnerstag
 */
public class ListItemTest extends TestCase
{
	private static final long serialVersionUID = 1L;

	/** Use a mock application to handle Link-clicked event */
	private static MockWebApplication application; // = new MockWebApplication(null);

	protected void setUp() throws Exception
	{
		super.setUp();

		if (application == null)
		{
			application = new MockWebApplication(null);
		}
	}

	/**
	 * @param modelListSize
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
			private static final long serialVersionUID = 1L;

			protected void populateItem(final ListItem listItem)
			{
				// do nothing
			}
		};
	}

	/**
	 *
	 */
	public void testListItem()
	{
/*	TODO tests have to be reviewed based on new ListItem and ListView implementations    
		ListItem li;

		try
		{
			li = new ListItem(createListView(0), 0);
		}
		catch (IndexOutOfBoundsException ex)
		{
			li = null;
			// Though empty ListView model object lists are allowed,
			// it'll never happen that ListView will create a ListItem
			// with an empty ListView model object list.
		}

		ListView lv = createListView(1);
		li = new ListItem(lv, 0);
		assertEquals("", li.getModelObject(), new Integer(0));
		assertEquals("", li.isEvenIndex(), true);
		assertEquals("", li.isFirst(), true);
		assertEquals("", li.isLast(), true);
		assertEquals("", li.getIndex(), 0);
		assertEquals(lv, li.getListView());

		li = new ListItem(createListView(2), 0);
		assertEquals("", li.getModelObject(), new Integer(0));
		assertEquals("", li.isEvenIndex(), true);
		assertEquals("", li.isFirst(), true);
		assertEquals("", li.isLast(), false);
		assertEquals("", li.getIndex(), 0);

		li = new ListItem(createListView(2), 1);
		assertEquals("", li.getModelObject(), new Integer(1));
		assertEquals("", li.isEvenIndex(), false);
		assertEquals("", li.isFirst(), false);
		assertEquals("", li.isLast(), true);
		assertEquals("", li.getIndex(), 1);

		li = new ListItem(createListView(3), 1);
		assertEquals("", li.getModelObject(), new Integer(1));
		assertEquals("", li.isEvenIndex(), false);
		assertEquals("", li.isFirst(), false);
		assertEquals("", li.isLast(), false);
		assertEquals("", li.getIndex(), 1);
*/		
	}

	/**
	 * @return request cycle
	 * @throws IOException
	 */
/*	private WebRequestCycle createRequestCycle() throws IOException
	{
		// Prepare the mock application to test the Link
		application.setupRequestAndResponse();
		WebRequestCycle cycle = new WebRequestCycle(application, application.getWicketSession(),
				application.getWicketRequest(), application.getWicketResponse());

		MockPage page = new MockPage(null);
		cycle.setPage(page);

		return cycle;
	}
*/
	/**
	 * @throws IOException
	 */
/*	public void testMoveUpLink() throws IOException
	{
		// Prepare the mock application to test the Link
		WebRequestCycle cycle = createRequestCycle();

		// Create a ListView with a model object which contains 4 items
		ListView lv = createListView(4);

		// add the ListView to the page
		cycle.getPage().add(lv);

		// Create a ListItem for list object at index 1.
		ListItem li = new ListItem(lv, 1);

		// Create a move-up Link for the listItem and simulate a user
		// clicking it
		Link link = li.moveUpLink("1");
		link.onClick();
		assertEquals(new Integer(1), lv.getListObject(0));
		assertEquals(new Integer(0), lv.getListObject(1));
		assertEquals(new Integer(2), lv.getListObject(2));
		assertEquals(new Integer(3), lv.getListObject(3));

		// Repeat it for some critical entries
		li = new ListItem(lv, 0);
		link = li.moveUpLink("1");
		try
		{
			link.onClick();
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			link = null;
		}
		assertNull(link);

		li = new ListItem(lv, 3);
		link = li.moveUpLink("1");
		link.onClick();
		assertEquals(new Integer(1), lv.getListObject(0));
		assertEquals(new Integer(0), lv.getListObject(1));
		assertEquals(new Integer(3), lv.getListObject(2));
		assertEquals(new Integer(2), lv.getListObject(3));

		// Be aware: listItem is linked to the index, not the component. Thus
		// repeating the click-event will again swap item 3 and 4.
		link.onClick();
		assertEquals(new Integer(1), lv.getListObject(0));
		assertEquals(new Integer(0), lv.getListObject(1));
		assertEquals(new Integer(2), lv.getListObject(2));
		assertEquals(new Integer(3), lv.getListObject(3));
	}
*/
	/**
	 * @throws IOException
	 */
/*	public void testMoveDownLink() throws IOException
	{
		// Prepare the mock application to test the Link
		WebRequestCycle cycle = createRequestCycle();

		// Create a ListView with a model object which contains 4 items
		ListView lv = createListView(4);

		// add the ListView to the page
		cycle.getPage().add(lv);

		// Create a ListItem for list object at index 1.
		ListItem li = new ListItem(lv, 1);

		// Create a move-up Link for the listItem and simulate a user
		// clicking it
		Link link = li.moveDownLink("1");
		link.onClick();
		assertEquals(new Integer(0), lv.getListObject(0));
		assertEquals(new Integer(2), lv.getListObject(1));
		assertEquals(new Integer(1), lv.getListObject(2));
		assertEquals(new Integer(3), lv.getListObject(3));

		// Repeat it for some critical entries
		li = new ListItem(lv, 3);
		link = li.moveDownLink("1");
		try
		{
			link.onClick();
		}
		catch (IndexOutOfBoundsException ex)
		{
			link = null;
		}
		assertNull(link);

		li = new ListItem(lv, 0);
		link = li.moveDownLink("1");
		link.onClick();
		assertEquals(new Integer(2), lv.getListObject(0));
		assertEquals(new Integer(0), lv.getListObject(1));
		assertEquals(new Integer(1), lv.getListObject(2));
		assertEquals(new Integer(3), lv.getListObject(3));

		// Be aware: listItem is linked to the index, not the component. Thus
		// repeating the click-event will again swap item 0 and 1.
		link.onClick();
		assertEquals(new Integer(0), lv.getListObject(0));
		assertEquals(new Integer(2), lv.getListObject(1));
		assertEquals(new Integer(1), lv.getListObject(2));
		assertEquals(new Integer(3), lv.getListObject(3));
	}
*/
	/**
	 * @throws IOException
	 */
/*	public void testRemoveLink() throws IOException
	{
		// Prepare the mock application to test the Link
		WebRequestCycle cycle = createRequestCycle();

		// Create a ListView with a model object which contains 4 items
		ListView lv = createListView(4);

		// add the ListView to the page
		cycle.getPage().add(lv);

		// Create a ListItem for list object at index 1.
		ListItem li = new ListItem(lv, 1);

		// Create a remove-Link for the listItem and simulate a user
		// clicking it
		Link removeLink = li.removeLink("1");
		removeLink.onClick();
		assertEquals(new Integer(0), lv.getListObject(0));
		assertEquals(new Integer(2), lv.getListObject(1));
		assertEquals(new Integer(3), lv.getListObject(2));

		// Repeat the procedure for "critical" entries
		li = new ListItem(lv, 0);
		removeLink = li.removeLink("0");
		removeLink.onClick();
		assertEquals(new Integer(2), lv.getListObject(0));
		assertEquals(new Integer(3), lv.getListObject(1));

		li = new ListItem(lv, 1);
		removeLink = li.removeLink("3");
		removeLink.onClick();
		assertEquals(new Integer(2), lv.getListObject(0));

		li = new ListItem(lv, 0);
		removeLink = li.removeLink("xxx");
		removeLink.onClick();
		assertEquals(0, lv.getList().size());
	}
*/	
}
