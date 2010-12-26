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
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.PropertyResolver;
import org.apache.wicket.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as a base class for different kinds of property models. By default, this class uses
 * {@link PropertyResolver} to resolve expressions on the target model object. Note that the
 * property resolver by default provides access to private members and methods. If guaranteeing
 * encapsulation of the target objects is a big concern, you should consider using an alternative
 * implementation.
 * 
 * @see PropertyResolver
 * @see org.apache.wicket.model.IDetachable
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * 
 * @param <T>
 *            The Model object type
 */
public abstract class AbstractPropertyModel<T>
	implements
		IChainingModel<T>,
		IObjectClassAwareModel<T>,
		IPropertyReflectionAwareModel<T>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractPropertyModel.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

		if (modelObject instanceof Session)
		{
			logger.warn("It is not a good idea to reference the Session instance "
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
	public void detach()
	{
		// Detach nested object if it's a detachable
		if (target instanceof IDetachable)
		{
			((IDetachable)target).detach();
		}
	}

	/**
	 * @see org.apache.wicket.model.IChainingModel#getChainedModel()
	 */
	public IModel<?> getChainedModel()
	{
		if (target instanceof IModel)
		{
			return (IModel<?>)target;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
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
		else if (expression.startsWith("."))
		{
			throw new IllegalArgumentException(
				"Property expressions cannot start with a '.' character");
		}

		final Object target = getTarget();
		if (target != null)
		{
			return (T)PropertyResolver.getValue(expression, target);
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
	public void setChainedModel(IModel<?> model)
	{
		target = model;
	}

	/**
	 * Applies the property expression on the model object using the given object argument.
	 * 
	 * @param object
	 *            The object that will be used when setting a value on the model object
	 * @see IModel#setObject(Object)
	 */
	@SuppressWarnings("unchecked")
	public void setObject(T object)
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			// TODO check, really do this?
			// why not just set the target to the object?
			if (target instanceof IModel)
			{
				((IModel<T>)target).setObject(object);
			}
			else
			{
				target = object;
			}
		}
		else
		{
			PropertyResolverConverter prc = null;
			prc = new PropertyResolverConverter(Application.get().getConverterLocator(),
				Session.get().getLocale());
			PropertyResolver.setValue(expression, getTarget(), object, prc);
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
			Object tmp = ((IModel<?>)object).getObject();
			if (tmp == object)
			{
				break;
			}
			object = tmp;
		}
		return object;
	}

	/**
	 * @return model object class
	 */
	@SuppressWarnings("unchecked")
	public Class<T> getObjectClass()
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			Object target = getTarget();
			return (Class<T>)(target != null ? target.getClass() : null);
		}

		final Object target = getTarget();
		if (target != null)
		{
			try
			{
				return (Class<T>)PropertyResolver.getPropertyClass(expression, target);
			}
			catch (Exception e)
			{
				// ignore.
			}
		}
		else if (this.target instanceof IObjectClassAwareModel)
		{
			try
			{
				Class<?> targetClass = ((IObjectClassAwareModel<?>)this.target).getObjectClass();
				if (targetClass != null)
				{
					return PropertyResolver.getPropertyClass(expression, targetClass);
				}
			}
			catch (WicketRuntimeException e)
			{
				// it was just a try.
			}

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
				try
				{
					return PropertyResolver.getPropertyField(expression, target);
				}
				catch (Exception ignore)
				{
					// ignore.
				}
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
				try
				{
					return PropertyResolver.getPropertyGetter(expression, target);
				}
				catch (Exception ignore)
				{
				}
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
				try
				{
					return PropertyResolver.getPropertySetter(expression, target);
				}
				catch (Exception ignore)
				{
				}
			}
		}
		return null;
	}

	/**
	 * @return The property expression for the component
	 */
	protected abstract String propertyExpression();
}
