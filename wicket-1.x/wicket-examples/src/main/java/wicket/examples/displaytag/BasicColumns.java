/*
 * $Id$ $Revision:
 * 5244 $ $Date$
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
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;

/**
 * Simple table with a few columns
 * 
 * @author Juergen Donnerstag
 */
public class BasicColumns extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public BasicColumns(final PageParameters parameters)
	{
		// test data
		List data = new TestList(6, false);

		// ==========================================================================
		// Add a simple table
		add(new ListViewWithAlternatingRowStyle("rows", data)
		{
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				listItem.add(new Label("id", Integer.toString(value.getId())));
				listItem.add(new Label("name", value.getName()));
				listItem.add(new Label("email", value.getEmail()));
				listItem.add(new Label("status", value.getStatus()));
				listItem.add(new Label("comments", value.getDescription()));
			}
		});

		// ==========================================================================
		// Add a simple table but get the model data from a
		// CompoundPropertyModel
		// avoiding redundant naming of the label id and the property name.
		add(new ListViewWithAlternatingRowStyle("rows2", data)
		{
			public void populateItem(final ListItem listItem)
			{
				// you see. No more model required
				listItem.add(new Label("id"));
				listItem.add(new Label("name"));
				listItem.add(new Label("email"));
				listItem.add(new Label("status"));
				listItem.add(new Label("description"));
			}

			// This makes the trick
			protected IModel getListItemModel(final IModel model, final int index)
			{
				return new CompoundPropertyModel(super.getListItemModel(model, index));
			}
		});

		// ==========================================================================
		// SimpleListView implements what has been shown in the previous example
		// plus that if a list item component is missing, it will automatically
		// be
		// created for you. Hence, it is even easier
		add(new SimpleListView("rows3", data)
		{
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				// You only need to manually add the components where
				// a) the tag id is NOT equal to the wicket id
				// b) you need something else than a Label
				// c) you need to attach AttributeModifier
				// d) any other fancy stuff
				listItem.add(new Label("comments", value.getDescription()));
			}
		});

		// ==========================================================================
		// It can be that simple to create a list view with multiple column
		add(new SimpleListView("rows4", data));
	}
}
