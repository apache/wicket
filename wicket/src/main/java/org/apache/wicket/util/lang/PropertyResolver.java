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
package org.apache.wicket.util.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE: THIS CLASS IS NOT PART OF THE WICKET PUBLIC API, DO NOT USE IT UNLESS YOU KNOW WHAT YOU ARE
 * DOING.
 * <p>
 * This class parses expressions to lookup or set a value on the object that is given. <br/>
 * The supported expressions are:
 * <p>
 * "property": This can can then be a bean property with get and set method. Or if a map is given as
 * an object it will be lookup with the property as a key when there is not get method for that
 * property.
 * <p/>
 * <p>
 * "property1.property2": Both properties are lookup as written above. If property1 evaluates to
 * null then if there is a setMethod (or if it is a map) and the Class of the property has a default
 * constructor then the object will be constructed and set on the object.
 * <p/>
 * <p>
 * "property.index": If the property is a List or Array then the second property can be a index on
 * that list like: 'mylist.0' this expression will also map on a getProperty(index) or
 * setProperty(index,value) methods. If the object is a List then the list will grow automatically
 * if the index is greater then the size
 * <p/>
 * <p>
 * Index or map properties can also be written as: "property[index]" or "property[key]"
 * <p/>
 * 
 * @author jcompagner
 */
public final class PropertyResolver
{
	private final static int RETURN_NULL = 0;
	private final static int CREATE_NEW_VALUE = 1;
	private final static int RESOLVE_CLASS = 2;

	private final static Map<Object, IClassCache> applicationToClassesToGetAndSetters = Generics.newConcurrentHashMap(2);
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PropertyResolver.class);

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
	public final static Object getValue(final String expression, final Object object)
	{
		if (expression == null || expression.equals("") || object == null)
		{
			return object;
		}

		ObjectAndGetSetter getter = getObjectAndGetSetter(expression, object, RETURN_NULL);
		if (getter == null)
		{
			return null;
		}
		return getter.getValue();

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
	 */
	public final static void setValue(final String expression, final Object object, Object value,
		PropertyResolverConverter converter)
	{
		if (expression == null || expression.equals(""))
		{
			throw new WicketRuntimeException("Empty expression setting value: " + value +
				" on object: " + object);
		}
		if (object == null)
		{
			throw new WicketRuntimeException(
				"Attempted to set property value on a null object. Property expression: " +
					expression + " Value: " + value);
		}

		ObjectAndGetSetter setter = getObjectAndGetSetter(expression, object, CREATE_NEW_VALUE);
		if (setter == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for setting value: " + value + " on: " + object);
		}
		setter.setValue(value, converter == null ? new PropertyResolverConverter(Application.get()
			.getConverterLocator(), Session.get().getLocale()) : converter);
	}

	/**
	 * @param expression
	 * @param object
	 * @return class of the target property object
	 */
	public final static Class<?> getPropertyClass(String expression, Object object)
	{
		ObjectAndGetSetter setter = getObjectAndGetSetter(expression, object, RESOLVE_CLASS);
		if (setter == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target classs of: " + object);
		}
		return setter.getTargetClass();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Field for the property expression or null if such field doesn't exist (only getters
	 *         and setters)
	 */
	public final static Field getPropertyField(String expression, Object object)
	{
		ObjectAndGetSetter setter = getObjectAndGetSetter(expression, object, RESOLVE_CLASS);
		if (setter == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target classs of: " + object);
		}
		return setter.getField();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Getter method for the property expression or null if such getter doesn't exist (only
	 *         field)
	 */
	public final static Method getPropertyGetter(String expression, Object object)
	{
		ObjectAndGetSetter setter = getObjectAndGetSetter(expression, object, RESOLVE_CLASS);
		if (setter == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target classs of: " + object);
		}
		return setter.getGetter();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Setter method for the property expression or null if such setter doesn't exist (only
	 *         field)
	 */
	public final static Method getPropertySetter(String expression, Object object)
	{
		ObjectAndGetSetter setter = getObjectAndGetSetter(expression, object, RESOLVE_CLASS);
		if (setter == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target classs of: " + object);
		}
		return setter.getSetter();
	}

	private static ObjectAndGetSetter getObjectAndGetSetter(final String expression,
		final Object object, int tryToCreateNull)
	{
		final String expressionBracketsSeperated = Strings.replaceAll(expression, "[", ".[")
			.toString();
		int index = getNextDotIndex(expressionBracketsSeperated, 0);
		int lastIndex = 0;
		Object value = object;
		Class<?> clz = value.getClass();
		String exp = expressionBracketsSeperated;
		while (index != -1)
		{
			exp = expressionBracketsSeperated.substring(lastIndex, index);
			if (exp.length() == 0)
			{
				exp = expressionBracketsSeperated.substring(index + 1);
				break;
			}

			IGetAndSet getAndSetter = null;
			try
			{
				getAndSetter = getGetAndSetter(exp, clz);
			}
			catch (WicketRuntimeException ex)
			{

				// expression by it self can't be found. try to find a
				// setPropertyByIndex(int,value) method
				index = getNextDotIndex(expressionBracketsSeperated, index + 1);
				if (index != -1)
				{
					String indexExpression = expressionBracketsSeperated.substring(lastIndex, index);
					getAndSetter = getGetAndSetter(indexExpression, clz);
				}
				else
				{
					exp = expressionBracketsSeperated.substring(lastIndex);
					break;
				}
			}
			Object newValue = null;
			if (value != null)
			{
				newValue = getAndSetter.getValue(value);
			}
			if (newValue == null)
			{
				if (tryToCreateNull == CREATE_NEW_VALUE)
				{
					newValue = getAndSetter.newValue(value);
					if (newValue == null)
					{
						return null;
					}
				}
				else if (tryToCreateNull == RESOLVE_CLASS)
				{
					clz = getAndSetter.getTargetClass();
				}
				else
				{
					return null;
				}
			}
			value = newValue;
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
		IGetAndSet getAndSetter = getGetAndSetter(exp, clz);
		return new ObjectAndGetSetter(getAndSetter, value);
	}


	private static int getNextDotIndex(String expression, int start)
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

	private final static IGetAndSet getGetAndSetter(String exp, Class<?> clz)
	{
		IClassCache classesToGetAndSetters = getClassesToGetAndSetters();
		Map<String, IGetAndSet> getAndSetters = classesToGetAndSetters.get(clz);
		if (getAndSetters == null)
		{
			getAndSetters = new ConcurrentHashMap<String, IGetAndSet>(8);
			classesToGetAndSetters.put(clz, getAndSetters);
		}

		IGetAndSet getAndSetter = getAndSetters.get(exp);
		if (getAndSetter == null)
		{
			Method method = null;
			Field field = null;
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
				method = findMethod(clz, exp);
			}
			else
			{
				method = findGetter(clz, exp);
			}
			if (method == null)
			{
				if (List.class.isAssignableFrom(clz))
				{
					try
					{
						int index = Integer.parseInt(exp);
						getAndSetter = new ListGetSet(index);
					}
					catch (NumberFormatException ex)
					{
						// can't parse the exp as an index, maybe the exp was a
						// method.
						method = findMethod(clz, exp);
						if (method != null)
						{
							getAndSetter = new MethodGetAndSet(method, MethodGetAndSet.findSetter(
								method, clz), null);
						}
						else
						{
							field = findField(clz, exp);
							if (field != null)
							{
								getAndSetter = new FieldGetAndSetter(field);
							}
							else
							{
								throw new WicketRuntimeException(
									"The expression '" +
										exp +
										"' is neither an index nor is it a method or field for the list " +
										clz);
							}
						}
					}
				}
				else if (Map.class.isAssignableFrom(clz))
				{
					getAndSetter = new MapGetSet(exp);
				}
				else if (clz.isArray())
				{
					try
					{
						int index = Integer.parseInt(exp);
						getAndSetter = new ArrayGetSet(clz.getComponentType(), index);
					}
					catch (NumberFormatException ex)
					{
						if (exp.equals("length") || exp.equals("size"))
						{
							getAndSetter = new ArrayLengthGetSet();
						}
						else
						{
							throw new WicketRuntimeException("can't parse the exp " + exp +
								" as an index for an array lookup");
						}
					}
				}
				else
				{
					field = findField(clz, exp);
					if (field == null)
					{
						method = findMethod(clz, exp);
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
									// if so then it could be a
									// getPropertyIndex(int)
									// and setPropertyIndex(int, object)
									String name = Character.toUpperCase(propertyName.charAt(0)) +
										propertyName.substring(1);
									method = clz.getMethod("get" + name, new Class[] { int.class });
									getAndSetter = new ArrayPropertyGetSet(method, parsedIndex);

								}
								catch (Exception e)
								{
									throw new WicketRuntimeException(
										"no get method defined for class: " + clz +
											" expression: " + propertyName);
								}
							}
							else
							{
								// We do not look for a public FIELD because
								// that is
								// not good
								// programming with beans patterns
								throw new WicketRuntimeException(
									"No get method defined for class: " + clz + " expression: " +
										exp);
							}
						}
						else
						{
							getAndSetter = new MethodGetAndSet(method, MethodGetAndSet.findSetter(
								method, clz), field);
						}
					}
					else
					{
						getAndSetter = new FieldGetAndSetter(field);
					}
				}
			}
			else
			{
				field = findField(clz, exp);
				getAndSetter = new MethodGetAndSet(method, MethodGetAndSet.findSetter(method, clz),
					field);
			}
			getAndSetters.put(exp, getAndSetter);
		}
		return getAndSetter;
	}


	/**
	 * @param clz
	 * @param expression
	 * @return introspected field
	 */
	private static Field findField(Class<?> clz, String expression)
	{
		Field field = null;
		try
		{
			field = clz.getField(expression);
		}
		catch (Exception e)
		{
			Class<?> tmp = clz;
			while (tmp != null && tmp != Object.class)
			{
				Field[] fields = tmp.getDeclaredFields();
				for (int i = 0; i < fields.length; i++)
				{
					if (fields[i].getName().equals(expression))
					{
						fields[i].setAccessible(true);
						return fields[i];
					}
				}
				tmp = tmp.getSuperclass();
			}
			log.debug("Cannot find field " + clz + "." + expression);
		}
		return field;
	}

	/**
	 * @param clz
	 * @param expression
	 * @return The method for the expression null if not found
	 */
	private final static Method findGetter(Class<?> clz, String expression)
	{
		String name = Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
		Method method = null;
		try
		{
			method = clz.getMethod("get" + name, (Class[])null);
		}
		catch (Exception e)
		{
		}
		if (method == null)
		{
			try
			{
				method = clz.getMethod("is" + name, (Class[])null);
			}
			catch (Exception e)
			{
				log.debug("Cannot find getter " + clz + "." + expression);
			}
		}
		return method;
	}

	private final static Method findMethod(Class<?> clz, String expression)
	{
		if (expression.endsWith("()"))
		{
			expression = expression.substring(0, expression.length() - 2);
		}
		Method method = null;
		try
		{
			method = clz.getMethod(expression, (Class[])null);
		}
		catch (Exception e)
		{
			log.debug("Cannot find method " + clz + "." + expression);
		}
		return method;
	}

	/**
	 * Utility class: instantiation not allowed.
	 */
	private PropertyResolver()
	{
	}

	/**
	 * @author jcompagner
	 * 
	 */
	private final static class ObjectAndGetSetter
	{

		private final IGetAndSet getAndSetter;
		private final Object value;

		/**
		 * @param getAndSetter
		 * @param value
		 */
		public ObjectAndGetSetter(IGetAndSet getAndSetter, Object value)
		{
			this.getAndSetter = getAndSetter;
			this.value = value;
		}

		/**
		 * @param value
		 * @param converter
		 */
		public void setValue(Object value, PropertyResolverConverter converter)
		{
			getAndSetter.setValue(this.value, value, converter);
		}

		/**
		 * @return The value
		 */
		public Object getValue()
		{
			return getAndSetter.getValue(value);
		}

		/**
		 * @return class of property value
		 */
		public Class<?> getTargetClass()
		{
			return getAndSetter.getTargetClass();
		}

		/**
		 * @return Field or null if no field exists for expression
		 */
		public Field getField()
		{
			return getAndSetter.getField();
		}

		/**
		 * @return Getter method or null if no getter exists for expression
		 */
		public Method getGetter()
		{
			return getAndSetter.getGetter();
		}

		/**
		 * @return Setter method or null if no setter exists for expression
		 */
		public Method getSetter()
		{
			return getAndSetter.getSetter();
		}

	}


	/**
	 * @author jcompagner
	 */
	public static interface IGetAndSet
	{
		/**
		 * @param object
		 *            The object where the value must be taken from.
		 * 
		 * @return The value of this property
		 */
		public Object getValue(final Object object);

		/**
		 * @return The target class of the object that as to be set.
		 */
		public Class<?> getTargetClass();

		/**
		 * @param object
		 *            The object where the new value must be set on.
		 * 
		 * @return The new value for the property that is set back on that object.
		 */
		public Object newValue(Object object);

		/**
		 * @param object
		 * @param value
		 * @param converter
		 */
		public void setValue(final Object object, final Object value,
			PropertyResolverConverter converter);

		/**
		 * @return Field or null if there is no field
		 */
		public Field getField();

		/**
		 * @return Getter method or null if there is no getter
		 */
		public Method getGetter();

		/**
		 * @return Setter of null if there is no setter
		 */
		public Method getSetter();
	}

	private static abstract class AbstractGetAndSet implements IGetAndSet
	{
		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getField()
		 */
		public Field getField()
		{
			return null;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getGetter()
		 */
		public Method getGetter()
		{
			return null;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getSetter()
		 */
		public Method getSetter()
		{
			return null;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getTargetClass()
		 */
		public Class<?> getTargetClass()
		{
			return null;
		}
	}

	private static final class MapGetSet extends AbstractGetAndSet
	{
		final private String key;

		MapGetSet(String key)
		{
			this.key = key;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return ((Map<?, ?>)object).get(key);
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#setValue(Object, Object,
		 *      PropertyResolverConverter)
		 */
		@SuppressWarnings("unchecked")
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			((Map<String, Object>)object).put(key, value);
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(Object)
		 */
		public Object newValue(Object object)
		{
			// Map can't make a newValue or should it look what is more in the
			// map and try to make one of the class if finds?
			return null;
		}
	}

	private static final class ListGetSet extends AbstractGetAndSet
	{
		final private int index;

		ListGetSet(int index)
		{
			this.index = index;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			if (((List<?>)object).size() <= index)
				return null;
			return ((List<?>)object).get(index);
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#setValue(Object, Object,
		 *      PropertyResolverConverter)
		 */
		@SuppressWarnings("unchecked")
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			List<Object> lst = (List<Object>)object;

			if (lst.size() > index)
			{
				lst.set(index, value);
			}
			else if (lst.size() == index)
			{
				lst.add(value);
			}
			else
			{
				while (lst.size() < index)
				{
					lst.add(null);
				}
				lst.add(value);
			}
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(Object)
		 */
		public Object newValue(Object object)
		{
			// List can't make a newValue or should it look what is more in the
			// list and try to make one of the class if finds?
			return null;
		}
	}

	private static final class ArrayGetSet extends AbstractGetAndSet
	{
		private final int index;
		private final Class<?> clzComponentType;

		ArrayGetSet(Class<?> clzComponentType, int index)
		{
			this.clzComponentType = clzComponentType;
			this.index = index;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			if (Array.getLength(object) > index)
			{
				return Array.get(object, index);
			}
			return null;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#setValue(Object, Object,
		 *      PropertyResolverConverter)
		 */
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			value = converter.convert(value, clzComponentType);
			Array.set(object, index, value);
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			Object value = null;
			try
			{
				value = clzComponentType.newInstance();
				Array.set(object, index, value);
			}
			catch (Exception e)
			{
				log.warn("Cannot set new value " + value + " at index " + index +
					" for array holding elements of class " + clzComponentType, e);
			}
			return value;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getTargetClass()
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return clzComponentType;
		}
	}

	private static final class ArrayLengthGetSet extends AbstractGetAndSet
	{
		ArrayLengthGetSet()
		{
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return new Integer(Array.getLength(object));
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#setValue(Object, Object,
		 *      PropertyResolverConverter)
		 */
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			throw new WicketRuntimeException("Cant set the length on an array");
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			throw new WicketRuntimeException("Cant get a new value from a length of an array");
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getTargetClass()
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return int.class;
		}
	}

	private static final class ArrayPropertyGetSet extends AbstractGetAndSet
	{
		final private Integer index;
		final private Method getMethod;
		private Method setMethod;

		ArrayPropertyGetSet(Method method, int index)
		{
			this.index = new Integer(index);
			getMethod = method;
			getMethod.setAccessible(true);
		}

		private final static Method findSetter(Method getMethod, Class<?> clz)
		{
			String name = getMethod.getName();
			name = "set" + name.substring(3);
			try
			{
				return clz.getMethod(name, new Class[] { int.class, getMethod.getReturnType() });
			}
			catch (Exception e)
			{
				log.debug("Cannot find setter method corresponding to " + getMethod);
			}
			return null;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			Object ret = null;
			try
			{
				ret = getMethod.invoke(object, new Object[] { index });
			}
			catch (InvocationTargetException ex)
			{
				throw new WicketRuntimeException("Error calling index property method: " +
					getMethod + " on object: " + object, ex.getCause());
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error calling index property method: " +
					getMethod + " on object: " + object, ex);
			}
			return ret;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#setValue(Object, Object,
		 *      PropertyResolverConverter)
		 */
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			if (setMethod == null)
			{
				setMethod = findSetter(getMethod, object.getClass());
			}
			if (setMethod != null)
			{
				setMethod.setAccessible(true);
				Object converted = converter.convert(value, getMethod.getReturnType());
				if (converted == null && value != null)
				{
					throw new ConversionException("Can't convert value: " + value + " to class: " +
						getMethod.getReturnType() + " for setting it on " + object);
				}
				try
				{
					setMethod.invoke(object, new Object[] { index, converted });
				}
				catch (InvocationTargetException ex)
				{
					throw new WicketRuntimeException("Error index property calling method: " +
						setMethod + " on object: " + object, ex.getCause());
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error index property calling method: " +
						setMethod + " on object: " + object, ex);
				}
			}
			else
			{
				throw new WicketRuntimeException("no set method defined for value: " + value +
					" on object: " + object);
			}
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getTargetClass()
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return getMethod.getReturnType();
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			if (setMethod == null)
			{
				setMethod = findSetter(getMethod, object.getClass());
			}

			if (setMethod == null)
			{
				log.warn("Null setMethod");
				return null;
			}

			Class<?> clz = getMethod.getReturnType();
			Object value = null;
			try
			{
				value = clz.newInstance();
				setMethod.invoke(object, new Object[] { index, value });
			}
			catch (Exception e)
			{
				log.warn("Cannot set new value " + value + " at index " + index, e);
			}
			return value;
		}
	}

	private static final class MethodGetAndSet extends AbstractGetAndSet
	{
		private final Method getMethod;
		private final Method setMethod;
		private final Field field;

		MethodGetAndSet(Method getMethod, Method setMethod, Field field)
		{
			this.getMethod = getMethod;
			this.getMethod.setAccessible(true);
			this.field = field;
			this.setMethod = setMethod;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public final Object getValue(final Object object)
		{
			Object ret = null;
			try
			{
				ret = getMethod.invoke(object, (Object[])null);
			}
			catch (InvocationTargetException ex)
			{
				throw new WicketRuntimeException("Error calling method: " + getMethod +
					" on object: " + object, ex.getCause());
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error calling method: " + getMethod +
					" on object: " + object, ex);
			}
			return ret;
		}

		/**
		 * @param object
		 * @param value
		 * @param converter
		 */
		public final void setValue(final Object object, final Object value,
			PropertyResolverConverter converter)
		{
			Class type = null;
			if (setMethod != null)
			{
				type = getMethod.getReturnType();
			}
			else if (field != null)
			{
				type = field.getType();
			}

			Object converted = null;
			if (type != null)
			{
				converted = converter.convert(value, getMethod.getReturnType());
				if (converted == null)
				{
					if (value != null)
					{
						throw new ConversionException("Can't convert value: " + value +
							" to class: " + getMethod.getReturnType() + " for setting it on " +
							object);
					}
					else if (getMethod.getReturnType().isPrimitive())
					{
						throw new ConversionException(
							"Can't convert null value to a primitive class: " +
								getMethod.getReturnType() + " for setting it on " + object);
					}
				}
			}

			if (setMethod != null)
			{
				try
				{
					setMethod.invoke(object, new Object[] { converted });
				}
				catch (InvocationTargetException ex)
				{
					throw new WicketRuntimeException("Error calling method: " + setMethod +
						" on object: " + object, ex.getCause());
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error calling method: " + setMethod +
						" on object: " + object, ex);
				}
			}
			else if (field != null)
			{
				try
				{
					field.set(object, converted);
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error setting field: " + field +
						" on object: " + object, ex);
				}
			}
			else
			{
				throw new WicketRuntimeException("no set method defined for value: " + value +
					" on object: " + object + " while respective getMethod being " +
					getMethod.getName());
			}
		}

		private final static Method findSetter(Method getMethod, Class<?> clz)
		{
			String name = getMethod.getName();
			if (name.startsWith("get"))
			{
				name = "set" + name.substring(3);
			}
			else
			{
				name = "set" + name.substring(2);
			}
			try
			{
				Method method = clz.getMethod(name, new Class[] { getMethod.getReturnType() });
				if (method != null)
				{
					method.setAccessible(true);
				}
				return method;
			}
			catch (NoSuchMethodException e)
			{
				Method[] methods = clz.getMethods();
				for (int i = 0; i < methods.length; i++)
				{
					if (methods[i].getName().equals(name))
					{
						Class<?>[] parameterTypes = methods[i].getParameterTypes();
						if (parameterTypes.length == 1)
						{
							if (parameterTypes[0].isAssignableFrom(getMethod.getReturnType()))
							{
								return methods[i];
							}
						}
					}
				}
				log.debug("Cannot find setter corresponding to " + getMethod);
			}
			catch (Exception e)
			{
				log.debug("Cannot find setter corresponding to " + getMethod);
			}
			return null;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			if (setMethod == null)
			{
				log.warn("Null setMethod");
				return null;
			}

			Class<?> clz = getMethod.getReturnType();
			Object value = null;
			try
			{
				value = clz.newInstance();
				setMethod.invoke(object, new Object[] { value });
			}
			catch (Exception e)
			{
				log.warn("Cannot set new value " + value, e);
			}
			return value;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getTargetClass()
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return getMethod.getReturnType();
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.AbstractGetAndSet#getGetter()
		 */
		@Override
		public Method getGetter()
		{
			return getMethod;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.AbstractGetAndSet#getSetter()
		 */
		@Override
		public Method getSetter()
		{
			return setMethod;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.AbstractGetAndSet#getField()
		 */
		@Override
		public Field getField()
		{
			return field;
		}
	}

	/**
	 * @author jcompagner
	 */
	private static class FieldGetAndSetter extends AbstractGetAndSet
	{

		private final Field field;

		/**
		 * Construct.
		 * 
		 * @param field
		 */
		public FieldGetAndSetter(Field field)
		{
			super();
			this.field = field;
			this.field.setAccessible(true);
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			try
			{
				return field.get(object);
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error getting field value of field " + field +
					" from object " + object, ex);
			}
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			Class<?> clz = field.getType();
			Object value = null;
			try
			{
				value = clz.newInstance();
				field.set(object, value);
			}
			catch (Exception e)
			{
				log.warn("Cannot set field " + field + " to " + value, e);
			}
			return value;
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#setValue(Object, Object,
		 *      PropertyResolverConverter)
		 */
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			value = converter.convert(value, field.getType());
			try
			{
				field.set(object, value);
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error setting field value of field " + field +
					" on object " + object + ", value " + value, ex);
			}
		}

		/**
		 * @see org.apache.wicket.util.lang.PropertyResolver.IGetAndSet#getTargetClass()
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return field.getType();
		}

		@Override
		public Field getField()
		{
			return field;
		}
	}

	private static IClassCache getClassesToGetAndSetters()
	{
		Object key = null;
		if (Application.exists())
		{
			key = Application.get();
		}
		else
		{
			key = PropertyResolver.class;
		}
		IClassCache result = applicationToClassesToGetAndSetters.get(key);
		if (result == null)
		{
			applicationToClassesToGetAndSetters.put(key, result = new DefaultClassCache());
		}
		return result;
	}

	/**
	 * Clean up cache for this app.
	 * 
	 * @param application
	 */
	public static void destroy(Application application)
	{
		applicationToClassesToGetAndSetters.remove(application);
	}


	/**
	 * Sets the {@link IClassCache} for the given application.
	 * 
	 * If the Application is null then it will be the default if no application is found. So if you
	 * want to be sure that your {@link IClassCache} is handled in all situations then call this
	 * method twice with your implementations. One time for the application and the second time with
	 * null.
	 * 
	 * @param application
	 *            to use or null if the default must be set.
	 * @param classCache
	 */
	public static void setClassCache(Application application, IClassCache classCache)
	{
		if (application != null)
		{
			applicationToClassesToGetAndSetters.put(application, classCache);
		}
		else
		{
			applicationToClassesToGetAndSetters.put(PropertyResolver.class, classCache);
		}
	}


	/**
	 * An implementation of the class can be set on the
	 * {@link PropertyResolver#setClassCacheCreator(Application, IClassCache)} method for a specific
	 * application. This class cache can then be a special map with an eviction policy or do nothing
	 * if nothing should be cached for the given class.
	 * 
	 * For example if you have proxy classes that are constantly created you could opt for not
	 * caching those at all or have a special Map implementation that will evict that class at a
	 * certain point.
	 * 
	 * @author jcompagner
	 */
	public static interface IClassCache
	{
		/**
		 * Put the class into the cache, or if that class shouldn't be cached do nothing.
		 * 
		 * @param clz
		 * @param values
		 */
		void put(Class<?> clz, Map<String, IGetAndSet> values);

		/**
		 * Returns the class map from the cache.
		 * 
		 * @param clz
		 * @return the map of the given class
		 */
		Map<String, IGetAndSet> get(Class<?> clz);
	}

	private static class DefaultClassCache implements IClassCache
	{
		private final ConcurrentHashMap<Class<?>, Map<String, IGetAndSet>> map = Generics.newConcurrentHashMap(16);

		public Map<String, IGetAndSet> get(Class<?> clz)
		{
			return map.get(clz);
		}

		public void put(Class<?> clz, Map<String, IGetAndSet> values)
		{
			map.put(clz, values);
		}
	}
}
