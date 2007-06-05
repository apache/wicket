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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.util.lang.PropertyResolver;
import org.apache.wicket.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.string.Strings;

/**
 * Serves as a base class for different kinds of property models.
 * 
 * @see org.apache.wicket.model.AbstractDetachableModel
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class AbstractPropertyModel
		implements
			IChainingModel,
			IObjectClassAwareModel,
			IPropertyReflectionAwareModel
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
	 * @see org.apache.wicket.model.IChainingModel#getChainedModel()
	 */
	public IModel getChainedModel()
	{
		if (target instanceof IModel)
			return (IModel)target;
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
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
	 * Gets the property expression for this model
	 * 
	 * @return The property expression
	 */
	public final String getPropertyExpression()
	{
		return propertyExpression();
	}

	/**
	 * @see org.apache.wicket.model.IChainingModel#setChainedModel(org.apache.wicket.model.IModel)
	 */
	public void setChainedModel(IModel model)
	{
		target = model;
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
			// TODO check, really do this?
			// why not just set the target to the object?
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
			prc = new PropertyResolverConverter(Application.get().getConverterLocator(), Session
					.get().getLocale());
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

	/**
	 * @return The target object
	 */
	public final Object getTarget()
	{
		Object object = target;
		while (object instanceof IModel)
		{
			object = ((IModel)object).getObject();
		}
		return object;
	}

	/**
	 * @return model object class
	 */
	public Class getObjectClass()
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			Object target = getTarget();
			return target != null ? target.getClass() : null;
		}

		final Object target = getTarget();
		if (target != null)
		{
			return PropertyResolver.getPropertyClass(expression, target);
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IPropertyReflectionAwareModel#getPropertyField()
	 */
	public Field getPropertyField()
	{
		String expression = propertyExpression();
		if (Strings.isEmpty(expression) == false) 
		{
			Object target = getTarget();
			if (target != null) 
			{
				return PropertyResolver.getPropertyField(expression, target);
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IPropertyReflectionAwareModel#getPropertyGetter()
	 */
	public Method getPropertyGetter()
	{
		String expression = propertyExpression();
		if (Strings.isEmpty(expression) == false) 
		{
			Object target = getTarget();
			if (target != null) 
			{
				return PropertyResolver.getPropertyGetter(expression, target);
			}
		}
		return null;
	}
	
	/**
	 * @see org.apache.wicket.model.IPropertyReflectionAwareModel#getPropertySetter()
	 */
	public Method getPropertySetter()
	{
		String expression = propertyExpression();
		if (Strings.isEmpty(expression) == false) 
		{
			Object target = getTarget();
			if (target != null) 
			{
				return PropertyResolver.getPropertySetter(expression, target);
			}
		}
		return null;
	}
	
	/**
	 * @return The property expression for the component
	 */
	protected abstract String propertyExpression();

	/**
	 * @param component
	 * @return nothing
	 * @deprecated use {@link #getObject()} instead
	 */
	protected final Object onGetObject(Component component)
	{
		throw new UnsupportedOperationException();
	}

	// TODO remove these methods after a deprecation release

	/**
	 * @param component
	 * @param object
	 * @deprecated use {@link #setObject(object)} instead
	 */
	protected final void onSetObject(Component component, Object object)
	{
		throw new UnsupportedOperationException();
	}

}
