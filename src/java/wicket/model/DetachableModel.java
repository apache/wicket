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

/**
 * A detachable model which wraps a given model object.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public abstract class DetachableModel extends AbstractDetachableModel
{
	/**
	 * The wrapped model object. Note that this object is transient to ensure we
	 * never serialize it even if the user forgets to set the object to null in
	 * their detach() method.
	 */
	private transient Object object;

	/**
	 * Construct.
	 */
	public DetachableModel()
	{
	}

	/**
	 * Constructs the detachable model with the given model object.
	 * 
	 * @param object
	 *            the model object
	 */
	public DetachableModel(final Object object)
	{
		this.object = object;
	}

	/**
	 * Gets the model object.
	 * 
	 * @return the model object
	 * @see wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		// TODO Commenting this out seems right, but breaks a test 
//		attach();
		return object;
	}

	/**
	 * Sets the model object.
	 * 
	 * @param object
	 *            the model object
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(final Object object)
	{
		this.object = object;
	}
	
	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		this.object = null;
	}
}
