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

import wicket.Session;
import wicket.util.lang.PropertyResolver;
import wicket.util.lang.PropertyResolverConverter;
import wicket.util.string.Strings;

/**
 * Serves as a base class for different kinds of property models.
 * 
 * @see wicket.model.AbstractDetachableModel
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractPropertyModel<T> implements IModel<T>
{
	/** Any target object (which may or may not implement IModel) */
	private Object target;

	/**
	 * Constructor
	 * 
	 * @param target
	 *            The target of the property expression
	 */
	public AbstractPropertyModel(final Object target)
	{
		this.target = target;
	}

	/**
	 * @return The target for this property
	 */
	protected Object getTarget()
	{
		if (target instanceof IModel)
		{
			return ((IModel)target).getObject();
		}
		return target;
	}

	/**
	 * @return The property expression
	 */
	protected abstract String propertyExpression();

	/**
	 * @see wicket.model.AbstractDetachableModel#detach()
	 */
	public void detach()
	{
		// Detach nested object if it's an IModel
		if (target instanceof IModel)
		{
			((IModel)target).detach();
		}
	}

	/**
	 * Retrieves the value of the property expression against the target object
	 * 
	 * @see wicket.model.IModel#getObject()
	 */
	@SuppressWarnings("unchecked")
	public T getObject()
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			return (T)getTarget();
		}

		final Object target = getTarget();
		if (target != null)
		{
			return (T)PropertyResolver.getValue(expression, target);
		}
		return null;
	}


	/**
	 * Applies the property expression on the target object using the given
	 * object argument.
	 * 
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void setObject(T object)
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			if (target instanceof IModel)
			{
				((IModel)target).setObject(object);
			}
			else
			{
				target = object;
			}
		}
		else
		{
			PropertyResolverConverter prc = null;
			prc = new PropertyResolverConverter(Session.get(), Session.get().getLocale());
			PropertyResolver.setValue(expression, getTarget(), object, prc);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":nestedModel=[").append(target).append("]");
		return sb.toString();
	}
}
