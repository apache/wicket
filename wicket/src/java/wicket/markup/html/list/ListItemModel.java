/*
 * $Id: ListItemModel.java 5871 2006-05-25 22:41:52 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 22:41:52 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.model.AbstractDetachableModel;
import wicket.model.IModel;

/**
 * Model for list items.
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 */
public class ListItemModel<T> extends AbstractDetachableModel<T>
{
	private static final long serialVersionUID = 1L;

	// It is easy and cheap to re-build it if necessary.
	// Avoid synchronising it in a cluster
	private transient T object;

	/** The ListView's list model */
	private final ListView<T> listView;

	/* The list item's index */
	private final int index;

	/**
	 * Construct
	 * 
	 * @param listView
	 *            The ListView
	 * @param index
	 *            The index of this model
	 */
	public ListItemModel(final ListView<T> listView, final int index)
	{
		this.listView = listView;
		this.index = index;
		attach();
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	@Override
	public IModel<T> getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	@Override
	protected void onAttach()
	{
		// Re-attach the model object based on index and ListView model object
		this.object = listView.getModelObject().get(index);
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		this.object = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject()
	 */
	@Override
	protected T onGetObject()
	{
		return object;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onSetObject(java.lang.Object)
	 */
	@Override
	protected void onSetObject(final T object)
	{
		this.object = object;
	}
}
