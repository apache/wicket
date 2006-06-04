/*
 * $Id: ExampleGrouping.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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

import java.util.List;

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
		List<ReportableListObject> data = new ReportList();

		// Add table of existing comments
		new SimpleListView<ReportableListObject>(this, "rows", data)
		{
			// Remember the value from the previous row
			private ReportableListObject previousValue = null;

			@Override
			public void populateItem(final ListItem listItem)
			{
				final ReportableListObject value = (ReportableListObject)listItem.getModelObject();

				// If not the first row. Remember: components not explicitly
				// added are automatically added as Label components.
				if (previousValue != null)
				{
					boolean equal = value.getCity().equals(previousValue.getCity());
					new Label(listItem, "city", equal ? "" : value.getCity());

					equal &= value.getProject().equals(previousValue.getProject());
					new Label(listItem, "project", equal ? "" : value.getProject());
				}

				// These values are not grouped
				new Label(listItem, "hours", Double.toString(value.getAmount()));

				// Remember current value
				previousValue = value;
			}
		};
	}
}
