/*
 * $Id: PageableListViewNavigation.java,v 1.3 2005/02/17 06:13:40 jonathanlocke
 * Exp $ $Revision$ $Date$
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

import java.util.List;

import wicket.Component;
import wicket.model.AbstractDetachableModel;
import wicket.model.IModel;

/**
 * Model for list items.
 */
public class ListItemModel extends AbstractDetachableModel
{
	// It is easy and cheap to re-build it if necessary.
	// Avoid synchronising it in a cluster
	private transient Object object;

	/** The ListView's model */
	private IModel listViewModel;
	
	/* The list items index */
	private int index;
	
	/**
	 * Construct
	 * 
	 * @param listViewModel
	 * @param index
	 */
	public ListItemModel(final IModel listViewModel, final int index)
	{
		this.listViewModel = listViewModel;
		this.index = index;
		attach();
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public Object getNestedModel()
	{
		return null;
	}
	
	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
	    // re-attach the model object based on index and listView model object
		this.object = ((List)listViewModel.getObject(null)).get(index);
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		this.object = null;
		
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	protected Object onGetObject(Component component)
	{
		return object;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onSetObject(wicket.Component, java.lang.Object)
	 */
	protected void onSetObject(Component component, Object object)
	{
		this.object = object;
	}
}