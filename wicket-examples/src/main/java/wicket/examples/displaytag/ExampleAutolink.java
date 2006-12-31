/*
 * $Id: ExampleAutolink.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.extensions.markup.html.basic.SmartLinkLabel;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;

/**
 * A table with autolink cells.
 * 
 * @author Juergen Donnerstag
 */
public class ExampleAutolink extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleAutolink(final PageParameters parameters)
	{
		// test data
		List<ListObject> data = new TestList(10, false);

		// Add table
		new SimpleListView<ListObject>(this, "rows", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				new Label(listItem, "id", Integer.toString(value.getId()));
				new SmartLinkLabel(listItem, "email", value.getEmail());
				new SmartLinkLabel(listItem, "url", value.getUrl());
			}
		};

		// Add table
		new SimpleListView<ListObject>(this, "rows2", data)
		{
			@Override
			protected Component newLabel(final MarkupContainer parent, final String id)
			{
				return new SmartLinkLabel(parent, id);
			}
		};
	}
}