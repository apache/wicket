/*
 * $Id: BasicColumns.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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
		List<ListObject> data = new TestList(6, false);

		// ==========================================================================
		// Add a simple table
		new ListViewWithAlternatingRowStyle<ListObject>(this, "rows", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				new Label(listItem, "id", Integer.toString(value.getId()));
				new Label(listItem, "name", value.getName());
				new Label(listItem, "email", value.getEmail());
				new Label(listItem, "status", value.getStatus());
				new Label(listItem, "comments", value.getDescription());
			}
		};

		// ==========================================================================
		// Add a simple table but get the model data from a
		// CompoundPropertyModel
		// avoiding redundant naming of the label id and the property name.
		new ListViewWithAlternatingRowStyle<ListObject>(this, "rows2", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				// you see. No more model required
				new Label(listItem, "id");
				new Label(listItem, "name");
				new Label(listItem, "email");
				new Label(listItem, "status");
				new Label(listItem, "description");
			}

			// This makes the trick
			@Override
			protected IModel<ListObject> getListItemModel(final IModel<List<ListObject>> model, final int index)
			{
				return new CompoundPropertyModel<ListObject>(super.getListItemModel(model, index));
			}
		};

		// ==========================================================================
		// SimpleListView implements what has been shown in the previous example
		// plus that if a list item component is missing, it will automatically
		// be
		// created for you. Hence, it is even easier
		new SimpleListView<ListObject>(this, "rows3", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				// You only need to manually add the components where
				// a) the tag id is NOT equal to the wicket id
				// b) you need something else than a Label
				// c) you need to attach AttributeModifier
				// d) any other fancy stuff
				new Label(listItem, "comments", value.getDescription());
			}
		};

		// ==========================================================================
		// It can be that simple to create a list view with multiple column
		new SimpleListView<ListObject>(this, "rows4", data);
	}
}
