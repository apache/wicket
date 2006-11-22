/*
 * $Id$ $Revision:
 * 5389 $ $Date$
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
import wicket.examples.displaytag.utils.ReportList;
import wicket.examples.displaytag.utils.ReportableListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;

/**
 * A table supporting grouping
 * 
 * @author Juergen Donnerstag
 */
public class ExampleGrouping extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleGrouping(final PageParameters parameters)
	{
		// Test data
		ReportList data = new ReportList();

		// Add table of existing comments
		add(new SimpleListView("rows", data)
		{
			// Remember the value from the previous row
			private ReportableListObject previousValue = null;

			public void populateItem(final ListItem listItem)
			{
				final ReportableListObject value = (ReportableListObject)listItem.getModelObject();

				// If not the first row. Remember: components not explicitly
				// added are automatically added as Label components.
				if (previousValue != null)
				{
					boolean equal = value.getCity().equals(previousValue.getCity());
					listItem.add(new Label("city", equal ? "" : value.getCity()));

					equal &= value.getProject().equals(previousValue.getProject());
					listItem.add(new Label("project", equal ? "" : value.getProject()));
				}

				// These values are not grouped
				listItem.add(new Label("hours", Double.toString(value.getAmount())));

				// Remember current value
				previousValue = value;
			}
		});
	}
}
