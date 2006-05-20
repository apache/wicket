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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.Component;

/**
 * Model is the basic implementation of an AbstractModel. It just wraps a simple
 * model object. The model object must be serializable, as it is stored in the
 * session. If you have large objects to store, consider using
 * {@link AbstractDetachableModel}instead of this class.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class Model<V /* extends Serializable*/> extends AbstractModel<V>
{
	private static final long serialVersionUID = 1L;

	/** Backing object. */
	private V object;

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
	public Model(final V object)
	{
		setObject(object);
	}

	/**
	 * @param map
	 *            The Map, which may or may not be Serializable
	 * @return A Model object wrapping the Map
	 */
	public static Model<Map> valueOf(final Map map)
	{
		return new Model<Map>(map instanceof Serializable ? map : new HashMap(map));
	}

	/**
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @return A Model object wrapping the List
	 */
	public static Model<Serializable> valueOf(final List list)
	{
		return new Model<Serializable>(list instanceof Serializable ? (Serializable)list : new ArrayList(list));
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	@Override
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public V getObject(final Component component)
	{
		return object;
	}

	/**
	 * Set the model object; calls setObject(java.io.Serializable). The model
	 * object must be serializable, as it is stored in the session
	 * 
	 * @param object
	 *            the model object
	 * @see wicket.model.IModel#setObject(Component, Object)
	 */
	public void setObject(final Component component, final V object)
	{
		setObject(object);
	}

	/**
	 * Sets the model object. The model object must be serializable, as it is
	 * stored in the session
	 * 
	 * @param object
	 *            The serializable model object
	 * @see wicket.model.IModel#setObject(Component, Object)
	 */
	public void setObject(final V object)
	{
		this.object = object;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":nestedModel=[").append(getNestedModel()).append("]");
		sb.append(":object=[").append(this.object).append("]");
		return sb.toString();
	}
}
