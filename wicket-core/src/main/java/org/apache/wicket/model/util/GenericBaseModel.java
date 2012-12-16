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

package org.apache.wicket.model.util;

import java.io.Serializable;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWriteableModel;
import org.apache.wicket.util.lang.Objects;

/**
 * Base class for models that contain instances that do not appear to be serializable and cannot
 * thus use <code>Model</code> directly. Makes sure that the object set into the model can be
 * serialized.
 * 
 * @author Timo Rantalaiho
 * @param <T>
 *            type of model object
 */
public abstract class GenericBaseModel<T> implements IModel<T>, IWriteableModel<T>
{
	private static final long serialVersionUID = 1L;
	/** model object */
	private T object;

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public T getObject()
	{
		return object;
	}

	/**
	 * Set the model object. The contents must be serializable, as they are stored in the session
	 * 
	 * @param object
	 *            the model object
	 * @see org.apache.wicket.model.IModel#setObject(Object)
	 */
	@Override
	public void setObject(T object)
	{
		if (!(object instanceof Serializable))
		{
			object = createSerializableVersionOf(object);
		}
		this.object = object;
	}

	/**
	 * Creates a serializable version of the object. The object is usually a collection.
	 * 
	 * @param object
	 * @return serializable version of <code>object</code>
	 */
	protected abstract T createSerializableVersionOf(T object);

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
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		sb.append(":object=[").append(object).append("]");
		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		return Objects.hashCode(object);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;

		}
		if (!(obj instanceof GenericBaseModel))
		{
			return false;
		}
		GenericBaseModel<?> that = (GenericBaseModel<?>)obj;
		return Objects.equal(object, that.object);
	}
}
