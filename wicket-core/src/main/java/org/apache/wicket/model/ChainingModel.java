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

import org.apache.wicket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of IChainingModel
 *
 * @param <T>
 *            The Model object type
 *
 * @see CompoundPropertyModel
 * @see AbstractPropertyModel
 *
 * @since 6.0.0
 */
public class ChainingModel<T> implements IChainingModel<T>
{
	private static final Logger LOG = LoggerFactory.getLogger(ChainingModel.class);

	/** Any model object (which may or may not implement IModel) */
	private Object target;

	public ChainingModel(final Object modelObject)
	{
		if (modelObject instanceof Session)
		{
			LOG.warn("It is not a good idea to reference the Session instance "
					+ "in models directly as it may lead to serialization problems. "
					+ "If you need to access a property of the session via the model use the "
					+ "page instance as the model object and 'session.attribute' as the path.");
		}

		target = modelObject;
	}

	/**
	 * Unsets this property model's instance variables and detaches the model.
	 *
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		// Detach nested object if it's a detachable
		if (target instanceof IDetachable)
		{
			((IDetachable)target).detach();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setObject(T object)
	{
		if (target instanceof IModel)
		{
			((IModel<T>)target).setObject(object);
		}
		else
		{
			target = object;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getObject()
	{
		if (target instanceof IModel)
		{
			return ((IModel<T>)target).getObject();
		}
		return (T)target;
	}

	@Override
	public IModel<?> getChainedModel()
	{
		if (target instanceof IModel)
		{
			return (IModel<?>)target;
		}
		return null;
	}

	@Override
	public void setChainedModel(IModel<?> model)
	{
		target = model;
	}

	/**
	 * @return The target - object or model
	 */
	protected final Object getTarget()
	{
		return target;
	}

	/**
	 * Sets a new target - object or model
	 * @return this object
	 */
	protected final ChainingModel<T> setTarget(final Object modelObject)
	{
		this.target = modelObject;
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Model:classname=[");
		sb.append(getClass().getName()).append(']');
		sb.append(":nestedModel=[").append(target).append(']');
		return sb.toString();
	}
}
