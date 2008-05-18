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
package org.apache.wicket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;


/**
 * <code>Model</code> is the basic implementation of an <code>IModel</code>. It just wraps a
 * simple model object. The model object must be serializable, as it is stored in the session. If
 * you have large objects to store, consider using
 * {@link org.apache.wicket.model.LoadableDetachableModel} instead of this class.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * 
 * @param <T>
 *            The Model Object
 */
public class Model<T extends Serializable> implements IModel<T>
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
	 * @deprecated see {@link Model#ofMap(Map)}
	 * @return A Model object wrapping the Map
	 */
	@Deprecated
	public static Model valueOf(final Map map)
	{
		return new Model(map instanceof Serializable ? (Serializable)map : new HashMap(map));
	}

	/**
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @deprecated see {@link Model#of(List)}
	 * @return A Model object wrapping the List
	 */
	@Deprecated
	public static Model valueOf(final List list)
	{
		return new Model(list instanceof Serializable ? (Serializable)list : new ArrayList(list));
	}

	/**
	 * Factory method for models that contain maps. This factory method will automatically rebuild a
	 * nonserializable <code>map</code> into a serializable one.
	 * 
	 * @param <K>
	 *            key type in map
	 * @param <V>
	 *            value type in map
	 * @param <T>
	 *            model type
	 * @param map
	 *            The Map, which may or may not be Serializable
	 * @return A Model object wrapping the Map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, T extends Map<K, V> & Serializable> Model<T> of(final Map<K, V> map)
	{
		return new Model<T>((T)(map instanceof Serializable ? map : new HashMap<K, V>(map)));
	}

	/**
	 * Factory method for models that contain lists. This factory method will automatically rebuild
	 * a nonserializable <code>list</code> into a serializable one.
	 * 
	 * @param <V>
	 *            value type in list
	 * @param <T>
	 *            model type
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @return A Model object wrapping the List
	 */
	@SuppressWarnings("unchecked")
	public static <V, T extends List<V> & Serializable> Model<T> of(final List<V> list)
	{
		return new Model<T>((T)(list instanceof Serializable ? list : new ArrayList<V>(list)));
	}

	/**
	 * Factory method for models that contain sets. This factory method will automatically rebuild a
	 * nonserializable <code>set</code> into a serializable one.
	 * 
	 * @param <V>
	 *            value type in set
	 * @param <T>
	 *            model type
	 * @param set
	 *            The Set, which may or may not be Serializable
	 * @return A Model object wrapping the Set
	 */
	@SuppressWarnings("unchecked")
	public static <V, T extends Set<V> & Serializable> Model<T> of(final Set<V> set)
	{
		return new Model<T>((T)(set instanceof Serializable ? set : new HashSet<V>(set)));
	}


	/**
	 * Factory methods for Model which uses type inference to make code shorter. Eqivalent to
	 * <code>new Model<TypeOfObject>(object)</code>.
	 * 
	 * @param <T>
	 * @param object
	 * @return Model that contains <code>object</code>
	 */
	public static <T extends Serializable> Model<T> of(T object)
	{
		return new Model<T>(object);
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	public T getObject()
	{
		return object;
	}

	/**
	 * Set the model object; calls setObject(java.io.Serializable). The model object must be
	 * serializable, as it is stored in the session
	 * 
	 * @param object
	 *            the model object
	 * @see org.apache.wicket.model.IModel#setObject(Object)
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
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
		if (object instanceof IDetachable)
		{
			((IDetachable)object).detach();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		sb.append(":object=[").append(object).append("]");
		return sb.toString();
	}

	// TODO These methods are for helping people upgrade. Remove after
	// deprecation release.
	/**
	 * @param component
	 * @return
	 * @deprecated replace by {@link IModel#getObject()}.
	 */
	@Deprecated
	public final Object getObject(Component component)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @param component
	 * @param object
	 * @deprecated replace by {@link IModel#setObject(Object)}.
	 */
	@Deprecated
	public final void setObject(Component component, Object object)
	{
		throw new UnsupportedOperationException();
	}
}
