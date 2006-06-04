/*
 * $Id: TableWithAlternatingRowStyle.java 3764 2006-01-14 17:38:33Z
 * jonathanlocke $ $Revision$ $Date: 2006-01-14 18:38:33 +0100 (Sa, 14
 * Jan 2006) $
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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.resolver.IComponentResolver;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.IModel;


/**
 * This is a simple ListView extension automatically creating Labels for each
 * ListItem. In case Label is not the right one, you can still add any other
 * component you want. Alternating row styles are provided as well.
 * 
 * @author Juergen Donnerstag
 * 
 * @param <T> The type of the list element
 */
public class SimpleListView<T> extends ListView<T> implements IComponentResolver
{
	/** The tags "class" attribute for odd index rows */
	public static String ODD = "odd";

	/** The tags "class" attribute for even index rows */
	public static String EVEN = "even";

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param data
	 */
	public SimpleListView(MarkupContainer parent, final String id, final List<T> data)
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
	public SimpleListView(MarkupContainer parent, final String id, final IModel<List<T>> model)
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
		return new SimpleListListItem(this, index, getListItemModel(getModel(), index));
	}

	/**
	 * Get the tags "class" attribute
	 * 
	 * @param id
	 *            The wicket:id of the tag
	 * @param index
	 *            The row index
	 * @return The class value to be used
	 */
	protected String getClassAttribute(final String id, final int index)
	{
		// add/modify the attribute controlling the CSS style
		return ((index % 2) == 0 ? EVEN : ODD);
	}

	/**
	 * 
	 * @param model
	 * @param index
	 * @return IModel
	 */
	@Override
	protected IModel<T> getListItemModel(final IModel<List<T>> model, final int index)
	{
		return new BoundCompoundPropertyModel<T>(super.getListItemModel(model, index));
	}

	/**
	 * Automatically add Labels if the user didn't provide any component himself
	 * 
	 * @param container
	 * @param markupStream
	 * @param tag
	 * @return true, if component has been added
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		String componentId = tag.getId();
		if ((componentId != null) && !componentId.startsWith(Component.AUTO_COMPONENT_PREFIX))
		{
			componentId = Component.AUTO_COMPONENT_PREFIX + componentId;
			tag.setId(componentId);
		}

		newLabel(container, componentId).autoAdded();
		return true;
	}

	/**
	 * Create a new default component in case it is not explicitly defined.
	 * 
	 * @param parent
	 * @param id
	 * @return Usually a Label like component
	 */
	protected Component newLabel(MarkupContainer parent, final String id)
	{
		return new Label(parent, id);
	}

	/**
	 * May be overriden, but doesn't have to.
	 * 
	 * @param item
	 */
	@Override
	protected void populateItem(final ListItem item)
	{
	}

	/**
	 * 
	 */
	public class SimpleListListItem extends ListItem<T>
	{
		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param index
		 *            The row index
		 * @param model
		 *            The associated model
		 */
		public SimpleListListItem(MarkupContainer parent, final int index, final IModel<T> model)
		{
			super(parent, index, model);
		}

		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			// add/modify the attribute controlling the CSS style
			final String classAttr = getClassAttribute(tag.getId(), getIndex());
			if (classAttr != null)
			{
				tag.put("class", classAttr);
			}

			// continue with default behavior
			super.onComponentTag(tag);
		}
	};
}
