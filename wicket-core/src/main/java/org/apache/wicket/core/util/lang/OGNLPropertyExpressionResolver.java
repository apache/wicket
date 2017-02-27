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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.reflection.AbstractGetAndSet;
import org.apache.wicket.core.util.reflection.ArrayGetAndSet;
import org.apache.wicket.core.util.reflection.ArrayLengthGetAndSet;
import org.apache.wicket.core.util.reflection.FieldGetAndSet;
import org.apache.wicket.core.util.reflection.IGetAndSet;
import org.apache.wicket.core.util.reflection.IndexedPropertyGetAndSet;
import org.apache.wicket.core.util.reflection.ListGetAndSet;
import org.apache.wicket.core.util.reflection.MapGetAndSet;
import org.apache.wicket.core.util.reflection.MethodGetAndSet;
import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;
import org.apache.wicket.core.util.reflection.ReflectionUtility;
import org.apache.wicket.util.lang.Generics;
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

	private IPropertyLocator locator;
	
	public OGNLPropertyExpressionResolver()
	{
		this(new CachingPropertyLocator(new DefaultPropertyLocator()));
	}

	public OGNLPropertyExpressionResolver(IPropertyLocator locator)
	{
		this.locator = locator;
	}

	@Override
	public ObjectWithGetAndSet resolve(String expression, Object object, Class<? extends Object> clz, int tryToCreateNull)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object,
			tryToCreateNull, clz);
		if (objectWithGetAndSet == null && tryToCreateNull != RETURN_NULL)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
				+ " for getting the target class of: " + clz);
		}
		return objectWithGetAndSet;
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
				if (tryToCreateNull == IPropertyExpressionResolver.CREATE_NEW_VALUE)
				{
					nextValue = getAndSet.newValue(value);
					if (nextValue == null)
					{
						return null;
					}
				}
				else if (tryToCreateNull == IPropertyExpressionResolver.RESOLVE_CLASS)
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

	/**
	 * A locator of properties.
	 * 
	 * @see https://issues.apache.org/jira/browse/WICKET-5623
	 */
	public static interface IPropertyLocator
	{
		/**
		 * Get {@link IGetAndSet} for a property.
		 * 
		 * @param clz owning class
		 * @param exp identifying the property
		 * @return getAndSet or {@code null} if non located
		 */
		IGetAndSet get(Class<?> clz, String exp);
	}

	/**
	 * A wrapper for another {@link IPropertyLocator} that caches results of
	 * {@link #get(Class, String)}.
	 */
	public static class CachingPropertyLocator implements IPropertyLocator
	{
		private final ConcurrentHashMap<String, IGetAndSet> map = Generics.newConcurrentHashMap(16);

		/**
		 * Special token to put into the cache representing no located {@link IGetAndSet}.
		 */
		private IGetAndSet NONE = new AbstractGetAndSet()
		{

			@Override
			public Object getValue(Object object)
			{
				return null;
			}

			@Override
			public Object newValue(Object object)
			{
				return null;
			}

			@Override
			public void setValue(Object object, Object value, PropertyResolverConverter converter)
			{
			}
		};

		private IPropertyLocator resolver;

		public CachingPropertyLocator(IPropertyLocator locator)
		{
			this.resolver = locator;
		}

		public IGetAndSet get(Class<?> clz, String exp)
		{
			String key = clz.getName() + "#" + exp;

			IGetAndSet located = map.get(key);
			if (located == null)
			{
				located = resolver.get(clz, exp);
				if (located == null)
				{
					located = NONE;
				}
				map.put(key, located);
			}

			if (located == NONE)
			{
				located = null;
			}

			return located;
		}
	}

	/**
	 * Default locator supporting <em>Java Beans</em> properties, maps, lists and method invocations.
	 */
	public static class DefaultPropertyLocator implements IPropertyLocator
	{
		
		public IGetAndSet get(Class<?> clz, String exp)
		{
			IGetAndSet getAndSet = null;

			Method method = null;
			Field field;
			if (exp.startsWith("["))
			{
				// if expression begins with [ skip method finding and use it as
				// a key/index lookup on a map.
				exp = exp.substring(1, exp.length() - 1);
			}
			else if (exp.endsWith("()"))
			{
				// if expression ends with (), don't test for setters just skip
				// directly to method finding.
				method = ReflectionUtility.findMethod(clz, exp);
			}
			else
			{
				method = ReflectionUtility.findGetter(clz, exp);
			}
			if (method == null)
			{
				if (List.class.isAssignableFrom(clz))
				{
					try
					{
						int index = Integer.parseInt(exp);
						getAndSet = new ListGetAndSet(index);
					}
					catch (NumberFormatException ex)
					{
						// can't parse the exp as an index, maybe the exp was a
						// method.
						method = ReflectionUtility.findMethod(clz, exp);
						if (method != null)
						{
							getAndSet = new MethodGetAndSet(method,
								ReflectionUtility.findSetter(method, clz), null);
						}
						else
						{
							field = ReflectionUtility.findField(clz, exp);
							if (field != null)
							{
								getAndSet = new FieldGetAndSet(field);
							}
							else
							{
								throw new WicketRuntimeException("The expression '" + exp
									+ "' is neither an index nor is it a method or field for the list "
									+ clz);
							}
						}
					}
				}
				else if (Map.class.isAssignableFrom(clz))
				{
					getAndSet = new MapGetAndSet(exp);
				}
				else if (clz.isArray())
				{
					try
					{
						int index = Integer.parseInt(exp);
						getAndSet = new ArrayGetAndSet(clz.getComponentType(), index);
					}
					catch (NumberFormatException ex)
					{
						if (exp.equals("length") || exp.equals("size"))
						{
							getAndSet = new ArrayLengthGetAndSet();
						}
						else
						{
							throw new WicketRuntimeException("Can't parse the expression '" + exp
								+ "' as an index for an array lookup");
						}
					}
				}
				else
				{
					field = ReflectionUtility.findField(clz, exp);
					if (field == null)
					{
						method = ReflectionUtility.findMethod(clz, exp);
						if (method == null)
						{
							int index = exp.indexOf('.');
							if (index != -1)
							{
								String propertyName = exp.substring(0, index);
								String propertyIndex = exp.substring(index + 1);
								try
								{
									int parsedIndex = Integer.parseInt(propertyIndex);
									// if so then it could be a getPropertyIndex(int)
									// and setPropertyIndex(int, object)
									String name = Character.toUpperCase(propertyName.charAt(0))
										+ propertyName.substring(1);
									method = clz.getMethod(ReflectionUtility.GET + name,
										new Class[] { int.class });
									getAndSet = new IndexedPropertyGetAndSet(method, parsedIndex);
								}
								catch (Exception e)
								{
									throw new WicketRuntimeException("No get method defined for class: "
										+ clz + " expression: " + propertyName);
								}
							}
						}
						else
						{
							getAndSet = new MethodGetAndSet(method,
								ReflectionUtility.findSetter(method, clz), null);
						}
					}
					else
					{
						getAndSet = new FieldGetAndSet(field);
					}
				}
			}
			else
			{
				field = ReflectionUtility.findField(clz, exp);
				getAndSet = new MethodGetAndSet(method, ReflectionUtility.findSetter(method, clz), field);
			}

			return getAndSet;
		}
	}
}
