/*
 * $Id: ExampleDecorator.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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

import java.text.DecimalFormat;
import java.util.List;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.util.time.Time;

/**
 * Examples on how to format table data
 * 
 * @author Juergen Donnerstag
 */
public class ExampleDecorator extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleDecorator(final PageParameters parameters)
	{
		// Test data
		List<ListObject> data = new TestList(10, false);

		// Add table
		new SimpleListView<ListObject>(this, "rows", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				new Label(listItem, "date", Time.valueOf(value.getDate()).toString("yyyy-MM-dd"));

				final DecimalFormat format = new DecimalFormat("$ #,##0.00");
				new Label(listItem, "money", format.format(value.getMoney()));
			}
		};

		// Dropdown for selecting locale
		new LocaleSelector(this, "localeSelector");

		// Add table
		new SimpleListView<ListObject>(this, "rows2", data);
	}
}