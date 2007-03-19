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
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class AbstractPropertyModel implements IModel
{
	/** Any model object (which may or may not implement IModel) */
	private Object target;

	/**
	 * Constructor
	 * 
	 * @param modelObject
	 *            The nested model object
	 */
	public AbstractPropertyModel(final Object modelObject)
	{
		if (modelObject == null)
		{
			throw new IllegalArgumentException("Parameter modelObject cannot be null");
		}

		this.target = modelObject;
	}

	protected Object getTarget()
	{
		if (target instanceof IModel)
		{
			return ((IModel)target).getObject();
		}
		return target;
	}

	/**
	 * @param component
	 *            The component to get a property expression for
	 * @return The property expression for the component
	 */
	protected abstract String propertyExpression();

	/**
	 * Unsets this property model's instance variables and detaches the model.
	 * 
	 * @see AbstractDetachableModel#onDetach()
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
	 * @see wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			return getTarget();
		}

		final Object target = getTarget();
		if (target != null)
		{
			return PropertyResolver.getValue(expression, target);
		}
		return null;
	}

	/**
	 * Applies the property expression on the model object using the given
	 * object argument.
	 * 
	 * @param object
	 *            The object that will be used when setting a value on the model
	 *            object
	 * @see IModel#setObject(Object)
	 */
	public void setObject(Object object)
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
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		sb.append(":nestedModel=[").append(target).append("]");
		return sb.toString();
	}
}
