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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.model.util.WildcardCollectionModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.model.util.WildcardSetModel;
import org.apache.wicket.util.lang.Objects;


/**
 * <code>Model</code> is the basic implementation of an <code>IModel</code>. It just wraps a simple
 * model object. The model object must be serializable, as it is stored in the session. If you have
 * large objects to store, consider using {@link org.apache.wicket.model.LoadableDetachableModel}
 * instead of this class.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * 
 * @param <T>
 *            The type of the Model Object
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
	 * Factory method for models that contain lists. This factory method will automatically rebuild
	 * a nonserializable <code>list</code> into a serializable one.
	 * 
	 * @param <C>
	 *            model type
	 * @param list
	 *            The List, which may or may not be Serializable
	 * @return A Model object wrapping the List
	 */
	public static <C> IModel<List<? extends C>> ofList(final List<? extends C> list)
	{
		return new WildcardListModel<>(list);
	}

	/**
	 * Factory method for models that contain maps. This factory method will automatically rebuild a
	 * nonserializable <code>map</code> into a serializable one.
	 * 
	 * @param <K>
	 *            key type in map
	 * @param <V>
	 *            value type in map
	 * @param map
	 *            The Map, which may or may not be Serializable
	 * @return A Model object wrapping the Map
	 */
	public static <K, V> IModel<Map<K, V>> ofMap(final Map<K, V> map)
	{
		return new MapModel<>(map);
	}

	/**
	 * Factory method for models that contain sets. This factory method will automatically rebuild a
	 * nonserializable <code>set</code> into a serializable one.
	 * 
	 * @param <C>
	 *            model type
	 * @param set
	 *            The Set, which may or may not be Serializable
	 * @return A Model object wrapping the Set
	 */
	public static <C> IModel<Set<? extends C>> ofSet(final Set<? extends C> set)
	{
		return new WildcardSetModel<>(set);
	}

	/**
	 * Factory method for models that contain collections. This factory method will automatically
	 * rebuild a nonserializable <code>collection</code> into a serializable {@link ArrayList}.
	 * 
	 * @param <C>
	 *            model type
	 * @param collection
	 *            The Collection, which may or may not be Serializable
	 * @return A Model object wrapping the Set
	 */
	public static <C> IModel<Collection<? extends C>> of(final Collection<? extends C> collection)
	{
		return new WildcardCollectionModel<>(collection);
	}


	/**
	 * Factory methods for Model which uses type inference to make code shorter. Equivalent to
	 * <code>new Model<TypeOfObject>(object)</code>.
	 * 
	 * @param <T>
	 * @param object
	 * @return Model that contains <code>object</code>
	 */
	public static <T extends Serializable> Model<T> of(T object)
	{
		return new Model<>(object);
	}

	/**
	 * Supresses generics warning when converting model types
	 * 
	 * @param <T>
	 * @param model
	 * @return <code>model</code>
	 */
	@SuppressWarnings("unchecked")
	public static <T> IModel<T> of(IModel<?> model)
	{
		return (IModel<T>)model;
	}

	/**
	 * Factory methods for Model which uses type inference to make code shorter. Equivalent to
	 * <code>new Model<TypeOfObject>()</code>.
	 * 
	 * @param <T>
	 * @return Model that contains <code>object</code>
	 */
	public static <T extends Serializable> Model<T> of()
	{
		return new Model<>();
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
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
	@Override
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
	@Override
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
		StringBuilder sb = new StringBuilder("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		sb.append(":object=[").append(object).append("]");
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(object);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof Model<?>))
		{
			return false;
		}
		Model<?> that = (Model<?>)obj;
		return Objects.equal(object, that.object);
	}
}
