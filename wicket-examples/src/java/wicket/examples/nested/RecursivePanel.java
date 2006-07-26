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
package wicket.examples.nested;

import java.util.List;

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;

/**
 * This example list knows how to display sublists. It expects a list where each
 * element is either a string or another list.
 * 
 * @author Eelco Hillenius
 */
public final class RecursivePanel extends Panel
{
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The id of this component
	 * @param list
	 *            a list where each element is either a string or another list
	 */
	public RecursivePanel(final String id, List list)
	{
		super(id);
		add(new Rows("rows", list));
	}

	/**
	 * The list class.
	 */
	private static class Rows extends ListView
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            name of the component
		 * @param list
		 *            a list where each element is either a string or another
		 *            list
		 */
		public Rows(String name, List list)
		{
			super(name, list);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			Object modelObject = listItem.getModelObject();

			if (modelObject instanceof List)
			{
				// create a panel that renders the sub lis
				RecursivePanel nested = new RecursivePanel("nested", (List)modelObject);
				listItem.add(nested);
				// if the current element is a list, we create a dummy row/
				// label element
				// as we have to confirm to our HTML definition, and set it's
				// visibility
				// property to false as we do not want LI tags to be rendered.
				WebMarkupContainer row = new WebMarkupContainer("row");
				row.setVisible(false);
				row.add(new WebMarkupContainer("label"));
				listItem.add(row);
			}
			else
			{
				// if the current element is not a list, we create a dummy panel
				// element
				// to confirm to our HTML definition, and set this panel's
				// visibility
				// property to false to avoid having the UL tag rendered
				RecursivePanel nested = new RecursivePanel("nested", null);
				nested.setVisible(false);
				listItem.add(nested);
				// add the row (with the LI element attached, and the label with
				// the
				// row's actual value to display
				WebMarkupContainer row = new WebMarkupContainer("row");
				row.add(new Label("label", modelObject.toString()));
				listItem.add(row);
			}
		}
	}
}
