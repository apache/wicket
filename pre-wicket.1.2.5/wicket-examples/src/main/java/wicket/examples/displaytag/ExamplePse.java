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
package wicket.examples.displaytag;

import wicket.PageParameters;
import wicket.examples.displaytag.export.CsvView;
import wicket.examples.displaytag.export.ExcelView;
import wicket.examples.displaytag.export.ExportLink;
import wicket.examples.displaytag.export.XmlView;
import wicket.examples.displaytag.list.SortableListViewHeader;
import wicket.examples.displaytag.list.SortableListViewHeaders;
import wicket.examples.displaytag.utils.MyPageableListViewNavigator;
import wicket.examples.displaytag.utils.ReportList;
import wicket.examples.displaytag.utils.ReportableListObject;
import wicket.examples.displaytag.utils.SimplePageableListView;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;

/**
 * Pageable + sortable + exportable + grouping table
 * 
 * @author Juergen Donnerstag
 */
public class ExamplePse extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExamplePse(final PageParameters parameters)
	{
		// Test data
		final ReportList data = new ReportList();

		// Add the table
		final SimplePageableListView table = new SimplePageableListView("rows", data, 10)
		{
			// Groups: value must be equal
			private ReportableListObject previousValue = null;

			/**
			 * 
			 */
			public void populateItem(final ListItem listItem)
			{
				super.populateItem(listItem);

				final ReportableListObject value = (ReportableListObject)listItem.getModelObject();

				// If first row of table, print anyway
				if (previousValue != null)
				{
					boolean equal = value.getCity().equals(previousValue.getCity());
					listItem.add(new Label("city", equal ? "" : value.getCity()));

					equal &= value.getProject().equals(previousValue.getProject());
					listItem.add(new Label("project", equal ? "" : value.getProject()));
				}

				// Not included in grouping
				listItem.add(new Label("hours", Double.toString(value.getAmount())));

				// remember the current value for the next row
				previousValue = value;
			}
		};

		add(table);

		// Add the sortable header and define how to sort the different columns
		add(new SortableListViewHeaders("header", table)
		{
			protected Comparable getObjectToCompare(final SortableListViewHeader header,
					final Object object)
			{
				final String name = header.getId();
				if (name.equals("city"))
				{
					return ((ReportableListObject)object).getCity();
				}
				if (name.equals("project"))
				{
					return ((ReportableListObject)object).getProject();
				}

				return "";
			}
		});

		// Add a table navigator
		add(new MyPageableListViewNavigator("pageTableNav", table));

		// Add export links
		add(new ExportLink("exportCsv", data, new CsvView(data, true, false, false)));
		add(new ExportLink("exportExcel", data, new ExcelView(data, true, false, false)));
		add(new ExportLink("exportXml", data, new XmlView(data, true, false, false)));
	}
}
