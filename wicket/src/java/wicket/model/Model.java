/*
 * $Id$ $Revision:
 * 1.11 $ $Date$
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

import java.io.Serializable;

import wicket.WicketRuntimeException;

/**
 * Model is the basic implementation of an AbstractModel. It just wraps a simple
 * model object. The model object must be serializable, as it is stored in the
 * session. If you have large objects to store, consider using
 * {@link AbstractDetachableModel} instead of this class.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class Model extends AbstractModel
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 3031804965001519439L;

	/** Backing object. */
	private Serializable object;

	/**
	 * Construct the model without providing an object.
	 */
	public Model()
	{
	}

	/**
	 * Construct the model, setting the given object as the wrapped object.
	 * 
	 * @param object
	 *            The model object proper
	 */
	public Model(final Serializable object)
	{
		this.object = object;
	}

	/**
	 * Get the model object proper.
	 * 
	 * @return The model object proper
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Set the model object; calls setObject(java.io.Serializable). The model
	 * object must be serializable, as it is stored in the session
	 * 
	 * @param object
	 *            the model object
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
		if (object != null)
		{
			if (!(object instanceof Serializable))
			{
				throw new WicketRuntimeException("Model object must be Serializable");
			}
		}

		setObject((Serializable)object);
	}

	/**
	 * Sets the model object. The model object must be serializable, as it is
	 * stored in the session
	 * 
	 * @param object
	 *            the serializable model object
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Serializable object)
	{
		this.object = object;
	}
}