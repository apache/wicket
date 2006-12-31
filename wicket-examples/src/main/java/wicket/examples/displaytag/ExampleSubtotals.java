/*
 * $Id: ExampleSubtotals.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24 May
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.examples.displaytag.utils.ReportList;
import wicket.examples.displaytag.utils.ReportableListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.PropertyModel;

/**
 * Table with subtotals calculated and printed into the table on the fly
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSubtotals extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleSubtotals(final PageParameters parameters)
	{
		// Test data
		final ReportList data = new ReportList();
		final Map<String, Integer> groups = new LinkedHashMap<String, Integer>(); // Keep the insertion order

		// Fill the 'groups' map
		ReportableListObject previousValue = data.get(0);
		groups.put(previousValue.getCity(), new Integer(0));
		int startIdx = 0;
		for (int i = 1; i < data.size(); i++)
		{
			final ReportableListObject value = data.get(i);

			if (!value.getCity().equals(previousValue.getCity()))
			{
				groups.put(previousValue.getCity(), new Integer(i - startIdx));
				groups.put(value.getCity(), new Integer(0));
				previousValue = value;
				startIdx = i;
			}
		}
		groups.put(previousValue.getCity(), new Integer(data.size() - startIdx));

		// add the table
		List<String> groupList = new ArrayList<String>();
		groupList.addAll(groups.keySet());
		new ListView<String>(this, "border", groupList)
		{
			private int startIndex = 0;

			@Override
			public void populateItem(final ListItem listItem)
			{
				SubtotalTable subtable = new SubtotalTable<ReportableListObject>(listItem, "rows", data);
				subtable.setStartIndex(startIndex);

				String group = listItem.getModelObjectAsString();
				int size = (groups.get(group)).intValue();
				subtable.setViewSize(size);
				startIndex += size;

				new Label(listItem, "name", new PropertyModel(subtable, "group1"));
				new Label(listItem, "value", new PropertyModel(subtable, "subtotal"));
			}
		};
	}

	/**
	 * A subtotal + grouping table prints the tables rows and adds a bar and the
	 * subtotal at the bottom.
	 */
	private class SubtotalTable<T> extends SimpleListView<T>
	{
		private ReportableListObject previousValue = null;
		private double subtotal = 0;
		private final String city;

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param id
		 * @param data
		 */
		public SubtotalTable(final MarkupContainer parent, final String id, final List<T> data)
		{
			super(parent, id, data);
		}

		/**
		 * @return Subtotal
		 */
		public double getSubtotal()
		{
			return subtotal;
		}

		/**
		 * @return Group 1
		 */
		public String getGroup1()
		{
			return city;
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		@Override
		public void populateItem(final ListItem listItem)
		{
			final ReportableListObject value = (ReportableListObject)listItem.getModelObject();

			if (previousValue != null)
			{
				new Label(listItem, "city", "");

				boolean equal = value.getProject().equals(previousValue.getProject());
				new Label(listItem, "project", equal ? "" : value.getProject());
			}

			new Label(listItem, "hours", Double.toString(value.getAmount()));

			subtotal += value.getAmount();
			previousValue = value;
		}
	}
}
