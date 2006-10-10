/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.list;

import wicket.MarkupContainer;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

/**
 * Container that holds components in a ListView.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 */
public class ListItem<T> extends WebMarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	/** The index of the ListItem in the parent ListView */
	private final int index;

	/**
	 * A constructor which uses the index and the list provided to create a
	 * ListItem. This constructor is the default one.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param index
	 *            The index of the item
	 * @param model
	 *            The model object of the item
	 */
	public ListItem(MarkupContainer parent, final int index, final IModel<T> model)
	{
		super(parent, Integer.toString(index), model);
		this.index = index;
	}

	/**
	 * Gets the index of the listItem in the parent listView.
	 * 
	 * @return The index of this listItem in the parent listView
	 */
	public final int getIndex()
	{
		return index;
	}
}
