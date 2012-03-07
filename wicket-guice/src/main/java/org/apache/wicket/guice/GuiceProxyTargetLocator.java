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
import java.lang.reflect.Type;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.core.util.lang.WicketObjects;

class GuiceProxyTargetLocator implements IProxyTargetLocator
{
	private static final long serialVersionUID = 1L;

	private final Annotation bindingAnnotation;

	private final boolean optional;

	private final String className;

	private final String fieldName;

	public GuiceProxyTargetLocator(final Field field, final Annotation bindingAnnotation,
		final boolean optional)
	{
		this.bindingAnnotation = bindingAnnotation;
		this.optional = optional;
		className = field.getDeclaringClass().getName();
		fieldName = field.getName();
	}

	public Object locateProxyTarget()
	{
		final GuiceInjectorHolder holder = Application.get().getMetaData(
			GuiceInjectorHolder.INJECTOR_KEY);

		final Type type;
		try
		{
			Class<?> clazz = WicketObjects.resolveClass(className);
			final Field field = clazz.getDeclaredField(fieldName);
			type = field.getGenericType();
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Error accessing member: " + fieldName +
				" of class: " + className, e);
		}

		// using TypeLiteral to retrieve the key gives us automatic support for
		// Providers and other injectable TypeLiterals
		final Key<?> key;

		if (bindingAnnotation == null)
		{
			key = Key.get(TypeLiteral.get(type));
		}
		else
		{
			key = Key.get(TypeLiteral.get(type), bindingAnnotation);
		}

		Injector injector = holder.getInjector();

		// if the Inject annotation is marked optional and no binding is found
		// then skip this injection (WICKET-2241)
		if (optional)
		{
			// Guice 2.0 throws a ConfigurationException if no binding is find while 1.0 simply
			// returns null.
			try
			{
				if (injector.getBinding(key) == null)
				{
					return null;
				}
			}
			catch (RuntimeException e)
			{
				return null;
			}
		}

		return injector.getInstance(key);
	}
}
