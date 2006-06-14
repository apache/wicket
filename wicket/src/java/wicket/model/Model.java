/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:43:46 +0200 (vr, 26 mei 2006) $
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
public class Model<T /* extends Serializable */> extends AbstractModel<T>
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
	 * @param map
	 *            The Map, which may or may not be Serializable
	 * @return A Model object wrapping the Map
	 */
	public static Model<Map<String, Serializable>> valueOf(final Map<String, Serializable> map)
	{
		return new Model<Map<String, Serializable>>(map instanceof Serializable
				? map
				: new HashMap<String, Serializable>(map));
	}

	/**
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @return A Model object wrapping the List
	 */
	public static Model<List<Serializable>> valueOf(final List<Serializable> list)
	{
		return new Model<List<Serializable>>(list instanceof Serializable
				? list
				: new ArrayList<Serializable>(list));
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
		this.object=object;
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
