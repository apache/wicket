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
package org.apache.wicket.core.util.lang;

import static org.apache.wicket.core.util.lang.IPropertyExpressionResolver.CREATE_NEW_VALUE;
import static org.apache.wicket.core.util.lang.IPropertyExpressionResolver.RESOLVE_CLASS;
import static org.apache.wicket.core.util.lang.IPropertyExpressionResolver.RETURN_NULL;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;
import org.apache.wicket.util.string.Strings;

/**
 * Facade with common usages of the {@link ObjectWithGetAndSet} resolved by {@link Application}'s
 * {@link IPropertyExpressionResolver}
 */
public final class PropertyResolver
{
	/**
	 * Looks up the value from the object with the given expression. If the expression, the object
	 * itself or one property evaluates to null then a null will be returned.
	 *
	 * @param expression
	 *            The expression string with the property to be lookup.
	 * @param object
	 *            The object which is evaluated.
	 * @return The value that is evaluated. Null something in the expression evaluated to null.
	 */
	public static Object getValue(String expression, Object object)
	{
		if (expression == null || expression.equals("") || object == null)
		{
			return object;
		}
		IPropertyExpressionResolver propertyExpressionResolver = Application.get()
			.getApplicationSettings().getPropertyExpressionResolver();
		ObjectWithGetAndSet property = propertyExpressionResolver.resolve(expression, object,
			object.getClass(), RETURN_NULL);
		return property == null ? null : property.getValue(false);
	}

	/**
	 * @param expression
	 * @param object
	 * @param targetClass
	 * @return class of the target property object
	 * @throws WicketRuntimeException
	 *             if the cannot be resolved
	 */
	public static Class<?> getPropertyClass(String expression, Object object, Class<?> targetClass)
	{
		IPropertyExpressionResolver propertyExpressionResolver = Application.get()
			.getApplicationSettings().getPropertyExpressionResolver();
		ObjectWithGetAndSet objectWithGetAndSet = propertyExpressionResolver.resolve(expression,
			object, targetClass, RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("No Class returned for expression: " + expression
				+ " for getting the target class of: " + targetClass);
		}
		return objectWithGetAndSet.getTargetClass();
	}

	/**
	 * Set the value on the object with the given expression. If the expression can't be evaluated
	 * then a WicketRuntimeException will be thrown. If a null object is encountered then it will
	 * try to generate it by calling the default constructor and set it on the object.
	 *
	 * The value will be tried to convert to the right type with the given converter.
	 *
	 * @param expression
	 *            The expression string with the property to be set.
	 * @param object
	 *            The object which is evaluated to set the value on.
	 * @param value
	 *            The value to set.
	 * @param converter
	 *            The converter to convert the value if needed to the right type.
	 * @throws WicketRuntimeException
	 */
	public static void setValue(String expression, Object object, Object value,
		PropertyResolverConverter prc)
	{
		IPropertyExpressionResolver propertyExpressionResolver = Application.get()
			.getApplicationSettings().getPropertyExpressionResolver();

		if (Strings.isEmpty(expression))
		{
			throw new WicketRuntimeException(
				"Empty expression setting value: " + value + " on object: " + object);
		}
		if (object == null)
		{
			throw new WicketRuntimeException(
				"Attempted to set property value on a null object. Property expression: "
					+ expression + " Value: " + value);
		}

		ObjectWithGetAndSet objectWithGetAndSet = propertyExpressionResolver.resolve(expression,
			object, object.getClass(), CREATE_NEW_VALUE);

		objectWithGetAndSet.setValue(value,
			prc == null
				? new PropertyResolverConverter(Application.get().getConverterLocator(),
					Session.get().getLocale())
				: prc);
	}

}