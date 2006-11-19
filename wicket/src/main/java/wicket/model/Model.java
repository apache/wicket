/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.Component;
import wicket.WicketRuntimeException;

/**
 * Model is the basic implementation of an AbstractModel. It just wraps a simple
 * model object. The model object must be serializable, as it is stored in the
 * session. If you have large objects to store, consider using
 * {@link AbstractDetachableModel}instead of this class.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class Model extends AbstractModel
{
	private static final long serialVersionUID = 1L;

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
		setObject(object);
	}

	/**
	 * @param map
	 *            The Map, which may or may not be Serializable
	 * @return A Model object wrapping the Map
	 */
	public static Model valueOf(final Map map)
	{
		return new Model(map instanceof Serializable ? (Serializable)map : new HashMap(map));
	}

	/**
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @return A Model object wrapping the List
	 */
	public static Model valueOf(final List list)
	{
		return new Model(list instanceof Serializable ? (Serializable)list : new ArrayList(list));
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(final Component component)
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
	public void setObject(final Component component, final Object object)
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
	 *            The serializable model object
	 * @see wicket.model.IModel#setObject(Component, Object)
	 */
	public void setObject(final Serializable object)
	{
		this.object = object;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":nestedModel=[").append(getNestedModel()).append("]");
		sb.append(":object=[").append(this.object).append("]");
		return sb.toString();
	}
}
