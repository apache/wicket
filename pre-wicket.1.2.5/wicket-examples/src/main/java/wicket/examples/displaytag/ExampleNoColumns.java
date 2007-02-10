/*
 * $Id$ $Revision:
 * 1547 $ $Date$
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
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.ListViewWithAlternatingRowStyle;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;

/**
 * A very simple example. It is based on {@link ListView} which is a Wicket core
 * component for all sorts of lists, table and grids.
 * 
 * @author Juergen Donnerstag
 */
public class ExampleNoColumns extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleNoColumns(final PageParameters parameters)
	{
		// Test data
		List data = new TestList(10, false);

		// ListVieweWithAlternatingRowStyle is a very simple extension
		// to ListView implementing alternating row styles by changing
		// the tags class attribute accordingly.
		// Note: This is not the simplest way Wicket offers to render
		// lists, but it shows how ListViews work in general.
		add(new ListViewWithAlternatingRowStyle("entries", data)
		{
			/**
			 * populateItem() is called for each item of the list. ListItem
			 * provides, beside the IModel, the current index while iterating
			 * over the data provided.
			 */
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();
				listItem.add(new Label("entry", value.getName()));
			}
		});
	}
}
