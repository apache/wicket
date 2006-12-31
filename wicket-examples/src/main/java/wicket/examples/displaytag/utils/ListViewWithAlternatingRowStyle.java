/*
 * $Id: ListViewWithAlternatingRowStyle.java 5244 2006-04-03 06:17:05Z
 * jdonnerstag $ $Revision$ $Date: 2006-04-03 08:17:05 +0200 (Mo, 03 Apr
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
package wicket.examples.displaytag.utils;

import java.util.List;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.IModel;


/**
 * This is a simple ListView extension providing alternate row styles (colours).
 * The styles are named "even" and "odd".
 * 
 * @author Juergen Donnerstag
 * 
 * @param <T>
 */
public abstract class ListViewWithAlternatingRowStyle<T> extends ListView<T>
{
	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param data
	 */
	public ListViewWithAlternatingRowStyle(final MarkupContainer parent, final String id, final List<T> data)
	{
		super(parent, id, data);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public ListViewWithAlternatingRowStyle(final MarkupContainer parent, final String id,
			final IModel<List<T>> model)
	{
		super(parent, id, model);
	}

	/**
	 * Subclass Table's newCell() and return a ListItem which will add/modify
	 * its class attribute and thus provide ListItems with alternating row
	 * colours.
	 * 
	 * See wicket.markup.html.table.Table#newItem(int)
	 * 
	 * @param index
	 *            Index of item
	 * @return List item
	 */
	@Override
	protected ListItem<T> newItem(final int index)
	{
		return new ListItem<T>(this, index, getListItemModel(getModel(), index))
		{
			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				// add/modify the attribute controlling the CSS style
				tag.put("class", (getIndex() % 2) == 0 ? "even" : "odd");

				// continue with default behavior
				super.onComponentTag(tag);
			}
		};
	}
}
