/*
 * $Id$ $Revision$ $Date:
 * 2006-07-19 01:15:49 -0700 (Wed, 19 Jul 2006) $
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

import wicket.WicketRuntimeException;


/**
 * Model is the basic implementation of an AbstractModel. It just wraps a simple
 * model object. The model object must be serializable, as it is stored in the
 * session. If you have large objects to store, consider using
 * {@link AbstractDetachableModel}instead of this class.
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class Model<T> extends AbstractModel<T>
{
	private static final long serialVersionUID = 1L;

	/** Backing object. */
	private T object;

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
	public Model(final T object)
	{
		setObject(object);
	}

	/**
	 * Return a map, possibly a new serializable map in case the provided one
	 * was not serializable.
	 * 
	 * @param <K>
	 *            The key type
	 * @param <V>
	 *            The value type
	 * @param map
	 *            The Map, which may or may not be Serializable
	 * @return A Model object wrapping the Map
	 */
	public static <K, V> Model<Map<K, V>> valueOf(final Map<K, V> map)
	{
		return new Model<Map<K, V>>(map instanceof Serializable ? map : new HashMap<K, V>(map));
	}

	/**
	 * Return a list, possibly a new serializable list in case the provided one
	 * was not serializable.
	 * 
	 * @param <K>
	 *            The type
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @return A Model object wrapping the List
	 */
	public static <K> Model<List<K>> valueOf(final List<K> list)
	{
		return new Model<List<K>>(list instanceof Serializable ? list : new ArrayList<K>(list));
	}

	/**
	 * Shortcut utility method so you dont have to use the long generics syntax
	 * 
	 * @param <K>
	 * @param object
	 * @return model that wraps the provided object
	 */
	public static <K> Model<K> valueOf(final K object)
	{
		return new Model<K>(object);
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
	 * @see wicket.model.IModel#getObject()
	 */
	public T getObject()
	{
		return object;
	}

	/**
	 * Set the model object; calls setObject(java.io.Serializable). The model
	 * object must be serializable, as it is stored in the session
	 * 
	 * @param object
	 *            the model object
	 * 
	 * @see wicket.model.IModel#setObject(Object)
	 */
	public void setObject(final T object)
	{
		if (object != null)
		{
			if (!(object instanceof Serializable))
			{
				throw new WicketRuntimeException("Model object must be Serializable");
			}
		}
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
