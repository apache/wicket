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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.reflection.CachingPropertyLocator;
import org.apache.wicket.core.util.reflection.IGetAndSet;
import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;
import org.apache.wicket.util.string.Strings;

/**
 * This class parses expressions to lookup or set a value on the object that is given. <br/>
 * The supported expressions are:
 * <dl>
 * <dt>"property"</dt>
 * <dd>This could be a bean property with getter and setter. Or if a map is given as an object it
 * will be lookup with the expression as a key when there is not getter for that property.</dd>
 * <dt>"property1.property2"</dt>
 * <dd>Both properties are looked up as described above. If property1 evaluates to null then if
 * there is a setter (or if it is a map) and the Class of the property has a default constructor
 * then the object will be constructed and set on the object.</dd>
 * <dt>"method()"</dt>
 * <dd>The corresponding method is invoked.</dd>
 * <dt>"property.index" or "property[index]"</dt>
 * <dd>If the property is a List or Array then the following expression can be a index on that list
 * like: 'mylist.0'. The list will grow automatically if the index is greater than the size.
 * <p>
 * This expression will also map on indexed properties, i.e. {@code getProperty(index)} and
 * {@code setProperty(index,value)} methods.</dd>
 * <dt>"property.key" or "property[key]"</dt>
 * <dd>If the property is a Map then the following expression can be a key in that map like:
 * 'myMap.key'.</dd>
 * </dl>
 * <strong>Note that the {@link DefaultPropertyLocator} by default provides access to private
 * members and methods. If guaranteeing encapsulation of the target objects is a big concern, you
 * should consider using an alternative implementation.</strong>
 * <p>
 * <strong>Note: If a property evaluates to an instance of {@link org.apache.wicket.model.IModel}
 * then the expression should use '.object' to work with its value.</strong>
 *
 * @author jcompagner
 * @author svenmeier
 */

public class OGNLPropertyExpressionResolver implements IPropertyExpressionResolver
{
	private final static int RETURN_NULL = 0;
	private final static int CREATE_NEW_VALUE = 1;
	private final static int RESOLVE_CLASS = 2;

	private IPropertyResolver locator = new CachingPropertyLocator( new DefaultPropertyLocator());


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
	//TODO remove, only being used in tests
	public Object getValue(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			RETURN_NULL);
		if (objectWithGetAndSet == null)
		{
			return null;
		}

		return objectWithGetAndSet.getValue();
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
	@Override
	public void setValue(final String expression, final Object object, final Object value,
		final PropertyResolverConverter converter)
	{
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

		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			CREATE_NEW_VALUE);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
				+ " for setting value: " + value + " on: " + object);
		}
		objectWithGetAndSet.setValue(value,
			converter == null
				? new PropertyResolverConverter(Application.get().getConverterLocator(),
					Session.get().getLocale())
				: converter);
	}

	/**
	/**
	 * @param expression
	 * @param object
	 * @return Field for the property expression
	 * @throws WicketRuntimeException
	 *             if there is no such field
	 */
	public Field getPropertyField(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
				+ " for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getField();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Getter method for the property expression
	 * @throws WicketRuntimeException
	 *             if there is no getter method
	 */
	public Method getPropertyGetter(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
				+ " for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getGetter();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Setter method for the property expression
	 * @throws WicketRuntimeException
	 *             if there is no setter method
	 */
	public Method getPropertySetter(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
				+ " for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getSetter();
	}

	@Override
	public ObjectWithGetAndSet resolve(String expression, Object object, Class<? extends Object> clz)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			RESOLVE_CLASS, clz);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
				+ " for getting the target class of: " + clz);
		}
		return objectWithGetAndSet;
	}

	/**
	 * Just delegating the call to the original getObjectAndGetSetter passing the object type as
	 * parameter.
	 *
	 * @param expression
	 * @param object
	 * @param tryToCreateNull
	 * @return {@link ObjectWithGetAndSet}
	 */
	private ObjectWithGetAndSet getObjectWithGetAndSet(final String expression,
		final Object object, int tryToCreateNull)
	{
		return getObjectWithGetAndSet(expression, object, tryToCreateNull, object.getClass());
	}

	/**
	 * Receives the class parameter also, since this method can resolve the type for some
	 * expression, only knowing the target class.
	 *
	 * @param expression
	 *            property expression
	 * @param object
	 *            root object
	 * @param tryToCreateNull
	 *            how should null values be handled
	 * @param clz
	 *            owning clazz
	 * @return final getAndSet and the target to apply it on, or {@code null} if expression results
	 *         in an intermediate null
	 */
	private ObjectWithGetAndSet getObjectWithGetAndSet(final String expression,
		final Object object, final int tryToCreateNull, Class<?> clz)
	{
		String expressionBracketsSeperated = Strings.replaceAll(expression, "[", ".[").toString();
		int index = getNextDotIndex(expressionBracketsSeperated, 0);
		while (index == 0 && expressionBracketsSeperated.startsWith("."))
		{
			// eat dots at the beginning of the expression since they will confuse
			// later steps
			expressionBracketsSeperated = expressionBracketsSeperated.substring(1);
			index = getNextDotIndex(expressionBracketsSeperated, 0);
		}
		int lastIndex = 0;
		Object value = object;
		String exp = expressionBracketsSeperated;
		while (index != -1)
		{
			exp = expressionBracketsSeperated.substring(lastIndex, index);
			if (exp.length() == 0)
			{
				exp = expressionBracketsSeperated.substring(index + 1);
				break;
			}

			IGetAndSet getAndSet = null;
			try
			{
				getAndSet = getGetAndSet(exp, clz);
			}
			catch (WicketRuntimeException ex)
			{
				// expression by itself can't be found. try combined with the following
				// expression (e.g. for a indexed property);
				int temp = getNextDotIndex(expressionBracketsSeperated, index + 1);
				if (temp == -1)
				{
					exp = expressionBracketsSeperated.substring(lastIndex);
					break;
				}
				else
				{
					index = temp;
					continue;
				}
			}
			Object nextValue = null;
			if (value != null)
			{
				nextValue = getAndSet.getValue(value);
			}
			if (nextValue == null)
			{
				if (tryToCreateNull == CREATE_NEW_VALUE)
				{
					nextValue = getAndSet.newValue(value);
					if (nextValue == null)
					{
						return null;
					}
				}
				else if (tryToCreateNull == RESOLVE_CLASS)
				{
					clz = getAndSet.getTargetClass();
				}
				else
				{
					return null;
				}
			}
			value = nextValue;
			if (value != null)
			{
				// value can be null if we are in the RESOLVE_CLASS
				clz = value.getClass();
			}

			lastIndex = index + 1;
			index = getNextDotIndex(expressionBracketsSeperated, lastIndex);
			if (index == -1)
			{
				exp = expressionBracketsSeperated.substring(lastIndex);
				break;
			}
		}
		IGetAndSet getAndSet = getGetAndSet(exp, clz);
		return new ObjectWithGetAndSet(getAndSet, value);
	}

	/**
	 *
	 * @param expression
	 * @param start
	 * @return next dot index
	 */
	private static int getNextDotIndex(final String expression, final int start)
	{
		boolean insideBracket = false;
		for (int i = start; i < expression.length(); i++)
		{
			char ch = expression.charAt(i);
			if (ch == '.' && !insideBracket)
			{
				return i;
			}
			else if (ch == '[')
			{
				insideBracket = true;
			}
			else if (ch == ']')
			{
				insideBracket = false;
			}
		}
		return -1;
	}

	private IGetAndSet getGetAndSet(String exp, final Class<?> clz)
	{

		IGetAndSet getAndSet = locator.get(clz, exp);
		if (getAndSet == null)
		{
			throw new WicketRuntimeException(
				"Property could not be resolved for class: " + clz + " expression: " + exp);
		}

		return getAndSet;
	}


}
