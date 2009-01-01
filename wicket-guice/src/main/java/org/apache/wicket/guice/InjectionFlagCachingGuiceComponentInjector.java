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
package org.apache.wicket.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.proxy.LazyInitProxyFactory;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Finding the guice annotations via reflection for each Component instantiation is a rather
 * heavyweight operation (@link {@link GuiceComponentInjector}.
 * 
 * This causes considerable overhead with large lists that have complicated component structure per
 * row, which is totally unnecessary for stock wicket components that do not contain any Guice
 * annotations.
 * 
 * @author Teppo Kurki (Wicket-1763)
 */
public class InjectionFlagCachingGuiceComponentInjector extends GuiceComponentInjector
{
	/** */
	private final Map<Class< ? >, Boolean> classToDoInject = new ConcurrentHashMap<Class< ? >, Boolean>();

	/**
	 * Construct.
	 * 
	 * @param app
	 * @param injector
	 */
	public InjectionFlagCachingGuiceComponentInjector(final Application app, final Injector injector)
	{
		super(app, injector);
	}

	/**
	 * @see org.apache.wicket.guice.GuiceComponentInjector#inject(java.lang.Object)
	 */
	@Override
	public void inject(final Object object)
	{
		Class< ? > current = object.getClass();
		Boolean doInject = classToDoInject.get(current);
		if (doInject != null && !doInject.booleanValue())
		{
			return;
		}

		boolean actualDoInject = false;

		do
		{
			Field[] currentFields = current.getDeclaredFields();
			for (final Field field : currentFields)
			{
				if (field.getAnnotation(Inject.class) != null)
				{
					actualDoInject = true;
					try
					{
						Annotation bindingAnnotation = findBindingAnnotation(field.getAnnotations());
						Object proxy = LazyInitProxyFactory.createProxy(field.getType(),
								new GuiceProxyTargetLocator(field.getGenericType(),
										bindingAnnotation));
						if (!field.isAccessible())
						{
							field.setAccessible(true);
						}
						field.set(object, proxy);
					}
					catch (IllegalAccessException e)
					{
						throw new WicketRuntimeException("Error Guice-injecting field " +
								field.getName() + " in " + object, e);
					}
					catch (MoreThanOneBindingException e)
					{
						throw new RuntimeException(
								"Can't have more than one BindingAnnotation on field " +
										field.getName() + " of class " +
										object.getClass().getName());
					}
				}
			}

			Method[] currentMethods = current.getDeclaredMethods();
			for (final Method method : currentMethods)
			{
				if (method.getAnnotation(Inject.class) != null)
				{
					actualDoInject = true;
					Annotation[][] paramAnnotations = method.getParameterAnnotations();
					Class< ? >[] paramTypes = method.getParameterTypes();
					Type[] genericParamTypes = method.getGenericParameterTypes();
					Object[] args = new Object[paramTypes.length];
					for (int i = 0; i < paramTypes.length; i++)
					{
						Type paramType;
						if (genericParamTypes[i] instanceof ParameterizedType)
						{
							paramType = ((ParameterizedType)genericParamTypes[i]).getRawType();
						}
						else
						{
							paramType = paramTypes[i];
						}
						try
						{
							Annotation bindingAnnotation = findBindingAnnotation(paramAnnotations[i]);
							args[i] = LazyInitProxyFactory.createProxy(paramTypes[i],
									new GuiceProxyTargetLocator(genericParamTypes[i],
											bindingAnnotation));
						}
						catch (MoreThanOneBindingException e)
						{
							throw new RuntimeException(
									"Can't have more than one BindingAnnotation on parameter " + i +
											"(" + paramType + ") of method " + method.getName() +
											" of class " + object.getClass().getName());
						}
					}
					try
					{
						method.invoke(object, args);
					}
					catch (IllegalAccessException e)
					{
						throw new WicketRuntimeException(e);
					}
					catch (InvocationTargetException e)
					{
						throw new WicketRuntimeException(e);
					}
				}
			}
			current = current.getSuperclass();
		}

		// Do a null check in case Object isn't in the current classloader.
		while (current != null && current != Object.class);
		if (doInject == null)
		{
			classToDoInject.put(object.getClass(), new Boolean(actualDoInject));
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.guice.GuiceComponentInjector#onInstantiation(org.apache.wicket.Component)
	 */
	@Override
	public void onInstantiation(final Component component)
	{
		inject(component);
	}

	/**
	 * 
	 * @param annotations
	 * @return
	 * @throws MoreThanOneBindingException
	 */
	private Annotation findBindingAnnotation(final Annotation[] annotations)
			throws MoreThanOneBindingException
	{
		Annotation bindingAnnotation = null;

		// Work out if we have a BindingAnnotation on this parameter.
		for (Annotation annotation : annotations)
		{
			if (annotation.annotationType().getAnnotation(BindingAnnotation.class) != null)
			{
				if (bindingAnnotation != null)
				{
					throw new MoreThanOneBindingException();
				}
				bindingAnnotation = annotation;
			}
		}
		return bindingAnnotation;
	}

	/**
	 * 
	 */
	private static class MoreThanOneBindingException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
}
