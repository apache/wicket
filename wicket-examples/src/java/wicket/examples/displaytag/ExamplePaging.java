/*
 * $Id$ $Revision:
 * 4623 $ $Date$
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.MyPageableListViewNavigator;
import wicket.examples.displaytag.utils.SimplePageableListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.list.ListItem;
import wicket.model.Model;

/**
 * Table with paging
 * 
 * @author Juergen Donnerstag
 */
public class ExamplePaging extends Displaytag
{
	private final Random random = new Random();

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExamplePaging(final PageParameters parameters)
	{
		// Test data
		final List data = new TestList(55, false);

		// =======================================================================
		// Add pageable table with alternating row styles. A Label component
		// is automatically created for each list item.
		// - The list contains more items than the page
		final SimplePageableListView table = new SimplePageableListView("rows", data, 10);

		add(table);
		add(new MyPageableListViewNavigator("pageTableNav", table));

		// =======================================================================
		// Add pageable table with alternating row styles
		// - The list contains less items than the page
		// - Explicitly create a Label
		List data2 = new ArrayList();
		data2.addAll(data.subList(0, 10));
		final SimplePageableListView table2 = new SimplePageableListView("rows2", data2, 20)
		{
			public void populateItem(final ListItem listItem)
			{
				super.populateItem(listItem);

				final ListObject value = (ListObject)listItem.getModelObject();
				listItem.add(new Label("comments", value.getDescription()));
			}
		};

		add(table2);
		add(new MyPageableListViewNavigator("pageTableNav2", table2));

		// =======================================================================
		// Empty table
		List data4 = new ArrayList();
		final SimplePageableListView table4 = new SimplePageableListView("rows4", data4, 10);
		add(table4);
		add(new MyPageableListViewNavigator("pageTableNav4", table4));

		// =======================================================================
		add(new Label("info5", ""));

		final List addRemoveOptions = new ArrayList();
		addRemoveOptions.add("10");
		addRemoveOptions.add("5");
		addRemoveOptions.add("3");
		addRemoveOptions.add("2");
		addRemoveOptions.add("1");
		addRemoveOptions.add("-1");
		addRemoveOptions.add("-2");
		addRemoveOptions.add("-3");
		addRemoveOptions.add("-5");
		addRemoveOptions.add("-10");

		add(new DropDownChoice("addRemove", new Model(null), addRemoveOptions)
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}

			protected void onSelectionChanged(Object newSelection)
			{
				int anz = Integer.parseInt((String)newSelection);

				Label info5 = (Label)ExamplePaging.this.get("info5");

				List data5 = (List)ExamplePaging.this.get("rows5").getModelObject();
				if (anz > 0)
				{
					data5.addAll(data.subList(0, anz));
					info5.setModelObject("" + anz + " elements add to the list");
				}
				else if (anz < 0)
				{
					anz = data5.size() + anz;
					if (anz < 0)
					{
						anz = 0;
					}

					info5.setModelObject("" + (data5.size() - anz)
							+ " elements removed from the list");

					while (data5.size() > anz)
					{
						data5.remove(0);
					}
				}
			}
		});

		List data5 = new ArrayList();
		final SimplePageableListView table5 = new SimplePageableListView("rows5", data5, 4);
		add(table5);
		add(new MyPageableListViewNavigator("pageTableNav5", table5));
	}
}