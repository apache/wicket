/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.model;

import wicket.Component;

/**
 * Model that makes working with detachable models a breeze.
 * LoadableDetachableModel holds a temporary, transient model object, that is set
 * on 'onAttach' by calling abstract method 'load', and that will be reset/ set
 * to null on 'onDetach'.
 *
 * A usage example:
 * <pre>
 * LoadableDetachableModel venueListModel = new LoadableDetachableModel()
 * {
 *   protected Object load()
 *   {
 *      return getVenueDao().findVenues();
 *   }	
 * };
 * </pre>
 *
 * @author Eelco Hillenius
 */
public abstract class LoadableDetachableModel extends AbstractDetachableModel
{
	/** temporary, transient object. */
	private transient Object tempModelObject;

	/**
	 * Construct.
	 */
	public LoadableDetachableModel()
	{
		super();
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected final void onAttach()
	{
		this.setObject(load());
	}

	/**
	 * Loads and returns the (temporary) model object.
	 * @return the (temporary) model object
	 */
	protected abstract Object load();

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected final void onDetach()
	{
		tempModelObject = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	protected final Object onGetObject(Component component)
	{
		return tempModelObject;
	}

	/**
	 * Sets the object.
	 * @param object the object
	 */
	protected final void setObject(Object object)
	{
		setObject(null, object);
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onSetObject(wicket.Component, java.lang.Object)
	 */
	protected final void onSetObject(Component component, Object object)
	{
		this.tempModelObject = object;
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public final Object getNestedModel()
	{
		return tempModelObject;
	}
}
