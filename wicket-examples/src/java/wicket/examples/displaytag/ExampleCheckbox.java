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

import java.util.List;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.TextField;
import wicket.markup.html.list.ListItem;
import wicket.model.Model;

/**
 * A table with checkboxes and input fields. I'm not sure
 * wicket.examples.wicket.examples.displaytag can do!
 * 
 * @author Juergen Donnerstag
 */
public class ExampleCheckbox extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleCheckbox(final PageParameters parameters)
	{
		// test data
		List data = new TestList(6, false);

		// Add table of existing comments
		add(new SimpleListView("rows", data)
		{
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				listItem.add(new CheckBox("activ", new Model(new Boolean(value.isActive()))));
				listItem.add(new TextField("comment", new Model(value.getStatus())));
			}
		});
	}
}