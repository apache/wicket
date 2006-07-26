/*
 * $Id: NestedList.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
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
package wicket.examples.nested;

import java.util.List;

import wicket.MarkupContainer;
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
	 * @param parent
	 * @param id
	 *            The id of this component
	 * @param list
	 *            a list where each element is either a string or another list
	 */
	public RecursivePanel(MarkupContainer parent, final String id, List list)
	{
		super(parent, id);
		new Rows<String>(this, "rows", list);
	}

	/**
	 * The list class.
	 */
	private static class Rows<T> extends ListView<T>
	{
		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param name
		 *            name of the component
		 * @param list
		 *            a list where each element is either a string or another
		 *            list
		 */
		@SuppressWarnings("unchecked")
		public Rows(MarkupContainer parent, String name, List list)
		{
			super(parent, name, list);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		@Override
		protected void populateItem(ListItem<T> listItem)
		{
			T modelObject = listItem.getModelObject();

			if (modelObject instanceof List)
			{
				// create a panel that renders the sub lis
				new RecursivePanel(listItem, "nested", (List)modelObject);
				// if the current element is a list, we create a dummy row/
				// label element
				// as we have to confirm to our HTML definition, and set it's
				// visibility
				// property to false as we do not want LI tags to be rendered.
				WebMarkupContainer row = new WebMarkupContainer(listItem, "row");
				row.setVisible(false);
				new WebMarkupContainer(row, "label");
			}
			else
			{
				// if the current element is not a list, we create a dummy panel
				// element
				// to confirm to our HTML definition, and set this panel's
				// visibility
				// property to false to avoid having the UL tag rendered
				RecursivePanel nested = new RecursivePanel(listItem, "nested", null);
				nested.setVisible(false);
				// add the row (with the LI element attached, and the label with
				// the
				// row's actual value to display
				WebMarkupContainer row = new WebMarkupContainer(listItem, "row");
				new Label(row, "label", modelObject.toString());
			}
		}
	}
}
