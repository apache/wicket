/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.model;

import wicket.Component;

/**
 * This provide a base class to work with detachable {@link wicket.model.IModel}
 * 's. It encapsulates the logic for attaching and detaching models. The
 * onAttach() abstract method will be called at the first access to the model
 * within a request and - if the model was attached earlier, onDetach() will be
 * called at the end of the request. In effect, attachment and detachment is
 * only done when it is actually needed.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class AbstractDetachableModel extends AbstractModel
{
	/**
	 * Transient flag to prevent multiple detach/attach scenario. We need to
	 * maintain this flag as we allow 'null' model values.
	 */
	private transient boolean attached = false;

	/**
	 * Attaches to the current session
	 * 
	 * @see IModel#attach()
	 */
	public final void attach()
	{
		if (!attached)
		{
			onAttach();
			attached = true;
		}
	}

	/**
	 * Detaches from the current session.
	 * 
	 * @see IModel#detach()
	 */
	public final void detach()
	{
		if (attached)
		{
			onDetach();
			attached = false;
		}
	}

	/**
	 * @see wicket.model.IModel#getObject(Component)
	 */
	public final Object getObject(final Component component)
	{
		attach();
		return onGetObject(component);
	}

	/**
	 * @see wicket.model.IModel#setObject(Component, Object)
	 */
	public final void setObject(final Component component, final Object object)
	{
		attach();
		onSetObject(component, object);
	}

	/**
	 * Gets whether this model has been attached to the current session.
	 * 
	 * @return whether this model has been attached to the current session
	 */
	public boolean isAttached()
	{
		return attached;
	}

	/**
	 * Attaches to the given session. Implement this method with custom
	 * behaviour, such as loading the model object.
	 */
	protected abstract void onAttach();

	/**
	 * Detaches from the given session. Implement this method with custom
	 * behaviour, such as setting the model object to null.
	 */
	protected abstract void onDetach();

	/**
	 * Called when getObject() is called in order to retrieve the detachable
	 * object. Before this method is called, getObject() always calls attach()
	 * to ensure that the object is attached.
	 * 
	 * @param component
	 *            The component asking for the object
	 * @return The object
	 */
	protected abstract Object onGetObject(final Component component);

	/**
	 * This default implementation of onSetObject throws an
	 * UnsupportedOperationException to indicate that the subclass has not
	 * implemented onSetObject() and therefore does not implement setObject().
	 * If the subclass does not override this method, the model is effectively
	 * read-only.
	 * 
	 * @param component
	 *            The component wanting to set the object
	 * @param object
	 *            The object to set into the model
	 */
	protected abstract void onSetObject(final Component component, final Object object);
}
