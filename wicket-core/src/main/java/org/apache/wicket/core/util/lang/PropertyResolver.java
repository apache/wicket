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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses expressions to lookup or set a value on the object that is given. <br/>
 * The supported expressions are:
 * <dl>
 * <dt>"property"</dt>
 * <dd>
 * This could be a bean property with getter and setter. Or if a map is given as
 * an object it will be lookup with the expression as a key when there is not getter for that
 * property.
 * </dd>
 * <dt>"property1.property2"</dt>
 * <dd>
 * Both properties are looked up as described above. If property1 evaluates to
 * null then if there is a setter (or if it is a map) and the Class of the property has a default
 * constructor then the object will be constructed and set on the object.
 * </dd>
 * <dt>"method()"</dt>
 * <dd>
 * The corresponding method is invoked.
 * </dd>
 * <dt>"property.index" or "property[index]"</dt>
 * <dd>
 * If the property is a List or Array then the following expression can be a index on
 * that list like: 'mylist.0'. The list will grow automatically if the index is greater than the size.<p>
 * This expression will also map on indexed properties, i.e. {@code getProperty(index)} and {@code setProperty(index,value)}
 * methods.
 * </dd>
 * <dt>"property.key" or "property[key]"</dt>
 * <dd>
 * If the property is a Map then the following expression can be a key in that map like: 'myMap.key'.
 * </dd>
 * </dl>
 * <strong>Note that the {@link DefaultPropertyLocator} by default provides access to private members
 * and methods. If guaranteeing encapsulation of the target objects is a big concern, you should consider
 * using an alternative implementation.</strong>
 * <p>
 * <strong>Note: If a property evaluates to an instance of {@link org.apache.wicket.model.IModel} then
 * the expression should use '.object' to work with its value.</strong>
 *
 * @author jcompagner
 * @author svenmeier
 */
public final class PropertyResolver
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PropertyResolver.class);

	private final static int RETURN_NULL = 0;
	private final static int CREATE_NEW_VALUE = 1;
	private final static int RESOLVE_CLASS = 2;

	private final static ConcurrentHashMap<Object, IPropertyLocator> applicationToLocators = Generics.newConcurrentHashMap(2);

	private static final String GET = "get";
	private static final String IS = "is";
	private static final String SET = "set";

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
	public static Object getValue(final String expression, final Object object)
	{
		if (expression == null || expression.equals("") || object == null)
		{
			return object;
		}

		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object, RETURN_NULL);
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
	public static void setValue(final String expression, final Object object,
		final Object value, final PropertyResolverConverter converter)
	{
		if (Strings.isEmpty(expression))
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

		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object, CREATE_NEW_VALUE);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for setting value: " + value + " on: " + object);
		}
		objectWithGetAndSet.setValue(value, converter == null ? new PropertyResolverConverter(Application.get()
			.getConverterLocator(), Session.get().getLocale()) : converter);
	}

	/**
	 * @param expression
	 * @param object
	 * @return class of the target property object
	 * @throws WicketRuntimeException if the cannot be resolved
	 */
	public static Class<?> getPropertyClass(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object, RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getTargetClass();
	}

	/**
	 * @param <T>
	 * @param expression
	 * @param clz
	 * @return class of the target Class property expression
	 * @throws WicketRuntimeException if class cannot be resolved
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getPropertyClass(final String expression, final Class<?> clz)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, null, RESOLVE_CLASS, clz);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("No Class returned for expression: " + expression +
				" for getting the target class of: " + clz);
		}
		return (Class<T>)objectWithGetAndSet.getTargetClass();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Field for the property expression
	 * @throws WicketRuntimeException if there is no such field
	 */
	public static Field getPropertyField(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object, RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getField();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Getter method for the property expression
	 * @throws WicketRuntimeException if there is no getter method
	 */
	public static Method getPropertyGetter(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object, RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getGetter();
	}

	/**
	 * @param expression
	 * @param object
	 * @return Setter method for the property expression
	 * @throws WicketRuntimeException if there is no setter method
	 */
	public static Method getPropertySetter(final String expression, final Object object)
	{
		ObjectWithGetAndSet objectWithGetAndSet = getObjectWithGetAndSet(expression, object, RESOLVE_CLASS);
		if (objectWithGetAndSet == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression +
				" for getting the target class of: " + object);
		}
		return objectWithGetAndSet.getSetter();
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
	private static ObjectWithGetAndSet getObjectWithGetAndSet(final String expression,
		final Object object, int tryToCreateNull)
	{
		return getObjectWithGetAndSet(expression, object, tryToCreateNull, object.getClass());
	}

	/**
	 * <b>Temporary</b> tokenizer that can fallback to old tokenization strategy and that should be
	 * removed in benefit of the abstract syntax tree generated by the property expression parser
	 * once it is introduced
	 */
	private static class PropertyResolverTokenizer
	{
		public static List<String> tokenize(String expression){
			PropertyExpression astNode = new PropertyExpressionParser().parse(expression);
			List<String> tokens = new ArrayList<String>();
			do
			{
				if (astNode.beanProperty != null)
				{
					tokens.add(astNode.beanProperty.propertyName);
					if (astNode.beanProperty.index != null)
					{
						tokens.add("[" + astNode.beanProperty.index + "]");
					}
				}
				else if (astNode.javaProperty != null)
				{
					if (astNode.javaProperty.index != null)
					{
						tokens.add(astNode.javaProperty.javaIdentifier);
						tokens.add("[" + astNode.javaProperty.index + "]");
					}
					else if (astNode.javaProperty.hasMethodSign)
					{
						tokens.add(astNode.javaProperty.javaIdentifier + "()");
					}
					else
					{
						tokens.add(astNode.javaProperty.javaIdentifier);
					}
				}
				else
				{
					tokens.add("[" + astNode.index + "]");
				}
			}
			while ((astNode = astNode.next) != null);
			return tokens;
		}

		public static List<String> legacyTokenize(String expression)
		{
			List<String> tokens = new ArrayList<String>();
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
			String exp = expressionBracketsSeperated;
			while (index != -1)
			{
				exp = expressionBracketsSeperated.substring(lastIndex, index);
				tokens.add(exp);
				lastIndex = index + 1;
				index = getNextDotIndex(expressionBracketsSeperated, lastIndex);
				if (index == -1)
				{
					exp = expressionBracketsSeperated.substring(lastIndex);
					break;
				}
			}
			tokens.add(exp);
			return tokens;

		}
	}

	/**
	 * Receives the class parameter also, since this method can resolve the type for some
	 * expression, only knowing the target class.
	 *
	 * @param expression property expression
	 * @param object root object
	 * @param tryToCreateNull how should null values be handled
	 * @param clz owning clazz
	 * @return final getAndSet and the target to apply it on, or {@code null} if expression results in an intermediate null
	 */
	private static ObjectWithGetAndSet getObjectWithGetAndSet(final String expression, final Object object, final int tryToCreateNull, Class<?> clz)
	{
		List<String> tokens = null;
		String userOldTokenizer = System.getProperty("wicket.use_old_tokenizer");
		if ("S".equals(userOldTokenizer))
		{
			tokens = PropertyResolverTokenizer.legacyTokenize(expression);
		}
		else
		{
			tokens = PropertyResolverTokenizer.tokenize(expression);
		}

		Object value = object;
		String exp = null;
		Iterator<String> iterator = tokens.iterator();
		while (iterator.hasNext())
		{
			exp = iterator.next();
			if (!iterator.hasNext())
			{
				break;
			}
				
			if (exp.length() == 0)
			{
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
				if (iterator.hasNext())
				{
					exp = exp + "." + iterator.next();
					if (!iterator.hasNext())
					{
						break;
					}
					else
					{
						getAndSet = getGetAndSet(exp, clz);
					}
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

	private static IGetAndSet getGetAndSet(String exp, final Class<?> clz)
	{
		IPropertyLocator locator = getLocator();
		
		IGetAndSet getAndSet = locator.get(clz, exp);
		if (getAndSet == null) {
			throw new WicketRuntimeException(
					"Property could not be resolved for class: " + clz + " expression: " + exp);
		}
		
		return getAndSet;
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
	private final static class ObjectWithGetAndSet
	{
		private final IGetAndSet getAndSet;
		private final Object value;

		/**
		 * @param getAndSet
		 * @param value
		 */
		public ObjectWithGetAndSet(IGetAndSet getAndSet, Object value)
		{
			this.getAndSet = getAndSet;
			this.value = value;
		}

		/**
		 * @param value
		 * @param converter
		 */
		public void setValue(Object value, PropertyResolverConverter converter)
		{
			getAndSet.setValue(this.value, value, converter);
		}

		/**
		 * @return The value
		 */
		public Object getValue()
		{
			return getAndSet.getValue(value);
		}

		/**
		 * @return class of property value
		 */
		public Class<?> getTargetClass()
		{
			return getAndSet.getTargetClass();
		}

		/**
		 * @return Field or null if no field exists for expression
		 */
		public Field getField()
		{
			return getAndSet.getField();
		}

		/**
		 * @return Getter method or null if no getter exists for expression
		 */
		public Method getGetter()
		{
			return getAndSet.getGetter();
		}

		/**
		 * @return Setter method or null if no setter exists for expression
		 */
		public Method getSetter()
		{
			return getAndSet.getSetter();
		}
	}

	/**
	 * A property to get and set.
	 * 
	 * @author jcompagner
	 */
	public interface IGetAndSet
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

	public static abstract class AbstractGetAndSet implements IGetAndSet
	{
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Field getField()
		{
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Method getGetter()
		{
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Method getSetter()
		{
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return null;
		}
	}

	private static final class MapGetAndSet extends AbstractGetAndSet
	{
		private final String key;

		MapGetAndSet(String key)
		{
			this.key = key;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(final Object object)
		{
			return ((Map<?, ?>)object).get(key);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void setValue(final Object object, final Object value,
			final PropertyResolverConverter converter)
		{
			((Map<String, Object>)object).put(key, value);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object newValue(final Object object)
		{
			// Map can't make a newValue or should it look what is more in the
			// map and try to make one of the class if finds?
			return null;
		}
	}

	private static final class ListGetAndSet extends AbstractGetAndSet
	{
		final private int index;

		ListGetAndSet(int index)
		{
			this.index = index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(final Object object)
		{
			if (((List<?>)object).size() <= index)
			{
				return null;
			}
			return ((List<?>)object).get(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void setValue(final Object object, final Object value,
			final PropertyResolverConverter converter)
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
		 * {@inheritDoc}
		 */
		@Override
		public Object newValue(Object object)
		{
			// List can't make a newValue or should it look what is more in the
			// list and try to make one of the class if finds?
			return null;
		}
	}

	private static final class ArrayGetAndSet extends AbstractGetAndSet
	{
		private final int index;
		private final Class<?> clzComponentType;

		ArrayGetAndSet(Class<?> clzComponentType, int index)
		{
			this.clzComponentType = clzComponentType;
			this.index = index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(Object object)
		{
			if (Array.getLength(object) > index)
			{
				return Array.get(object, index);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
			value = converter.convert(value, clzComponentType);
			Array.set(object, index, value);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
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
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return clzComponentType;
		}
	}

	private static final class ArrayLengthGetAndSet extends AbstractGetAndSet
	{
		ArrayLengthGetAndSet()
		{
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(final Object object)
		{
			return Array.getLength(object);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setValue(final Object object, final Object value,
			final PropertyResolverConverter converter)
		{
			throw new WicketRuntimeException("You can't set the length on an array:" + object);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object newValue(final Object object)
		{
			throw new WicketRuntimeException("Can't get a new value from a length of an array: " +
				object);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return int.class;
		}
	}

	private static final class IndexedPropertyGetAndSet extends AbstractGetAndSet
	{
		final private Integer index;
		final private Method getMethod;
		private Method setMethod;

		IndexedPropertyGetAndSet(final Method method, final int index)
		{
			this.index = index;
			getMethod = method;
			getMethod.setAccessible(true);
		}

		private static Method findSetter(final Method getMethod, final Class<?> clz)
		{
			String name = getMethod.getName();
			name = SET + name.substring(3);
			try
			{
				return clz.getMethod(name, new Class[] { int.class, getMethod.getReturnType() });
			}
			catch (Exception e)
			{
				log.debug("Can't find setter method corresponding to " + getMethod);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(Object object)
		{
			Object ret;
			try
			{
				ret = getMethod.invoke(object, index);
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
		 * {@inheritDoc}
		 */
		@Override
		public void setValue(final Object object, final Object value,
			final PropertyResolverConverter converter)
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
					setMethod.invoke(object, index, converted);
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
				throw new WicketRuntimeException("No set method defined for value: " + value +
					" on object: " + object);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return getMethod.getReturnType();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
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
				setMethod.invoke(object, index, value);
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
		 * {@inheritDoc}
		 */
		@Override
		public final Object getValue(final Object object)
		{
			Object ret;
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
		@Override
		public final void setValue(final Object object, final Object value,
			PropertyResolverConverter converter)
		{
			Class<?> type = null;
			if (setMethod != null)
			{
				// getMethod is always there and if the value will be set through a setMethod then
				// the getMethod return type will be its type. Else we have to look at the
				// parameters if the setter but getting the return type is quicker
				type = getMethod.getReturnType();
			}
			else if (field != null)
			{
				type = field.getType();
			}

			Object converted = null;
			if (type != null)
			{
				converted = converter.convert(value, type);
				if (converted == null)
				{
					if (value != null)
					{
						throw new ConversionException("Method [" + getMethod +
							"]. Can't convert value: " + value + " to class: " +
							getMethod.getReturnType() + " for setting it on " + object);
					}
					else if (getMethod.getReturnType().isPrimitive())
					{
						throw new ConversionException("Method [" + getMethod +
							"]. Can't convert null value to a primitive class: " +
							getMethod.getReturnType() + " for setting it on " + object);
					}
				}
			}

			if (setMethod != null)
			{
				try
				{
					setMethod.invoke(object, converted);
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

		private static Method findSetter(Method getMethod, Class<?> clz)
		{
			String name = getMethod.getName();
			if (name.startsWith(GET))
			{
				name = SET + name.substring(3);
			}
			else
			{
				name = SET + name.substring(2);
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
				for (Method method : methods)
				{
					if (method.getName().equals(name))
					{
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length == 1)
						{
							if (parameterTypes[0].isAssignableFrom(getMethod.getReturnType()))
							{
								return method;
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
		 * {@inheritDoc}
		 */
		@Override
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
				setMethod.invoke(object, value);
			}
			catch (Exception e)
			{
				log.warn("Cannot set new value " + value, e);
			}
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return getMethod.getReturnType();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Method getGetter()
		{
			return getMethod;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Method getSetter()
		{
			return setMethod;
		}

		/**
		 * {@inheritDoc}
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
	private static class FieldGetAndSet extends AbstractGetAndSet
	{
		private final Field field;

		/**
		 * Construct.
		 *
		 * @param field
		 */
		public FieldGetAndSet(final Field field)
		{
			super();
			this.field = field;
			this.field.setAccessible(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(final Object object)
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
		 * {@inheritDoc}
		 */
		@Override
		public Object newValue(final Object object)
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
		 * {@inheritDoc}
		 */
		@Override
		public void setValue(final Object object, Object value,
			final PropertyResolverConverter converter)
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
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getTargetClass()
		{
			return field.getType();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Field getField()
		{
			return field;
		}
	}

	/**
	 * Clean up cache for this app.
	 *
	 * @param application
	 */
	public static void destroy(Application application)
	{
		applicationToLocators.remove(application);
	}

	/**
	 * Get the current {@link IPropertyLocator}.
	 * 
	 * @return locator for the current {@link Application} or a general one if no current application is present
	 * @see Application#get()
	 */
	public static IPropertyLocator getLocator()
	{
		Object key;
		if (Application.exists())
		{
			key = Application.get();
		}
		else
		{
			key = PropertyResolver.class;
		}
		IPropertyLocator result = applicationToLocators.get(key);
		if (result == null)
		{
			IPropertyLocator tmpResult = applicationToLocators.putIfAbsent(key, result = new CachingPropertyLocator(new DefaultPropertyLocator()));
			if (tmpResult != null)
			{
				result = tmpResult;
			}
		}
		return result;
	}

	/**
	 * Set a locator for the given application.
	 * 
	 * @param application application, may be {@code null}
	 * @param locator locator
	 */
	public static void setLocator(final Application application, final IPropertyLocator locator)
	{
		if (application == null)
		{
			applicationToLocators.put(PropertyResolver.class, locator);
		}
		else
		{
			applicationToLocators.put(application, locator);
		}
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
	 * A wrapper for another {@link IPropertyLocator} that caches results of {@link #get(Class, String)}.
	 */
	public static class CachingPropertyLocator implements IPropertyLocator
	{
		private final ConcurrentHashMap<String, IGetAndSet> map = Generics.newConcurrentHashMap(16);
		
		/**
		 * Special token to put into the cache representing no located {@link IGetAndSet}. 
		 */
		private IGetAndSet NONE = new AbstractGetAndSet() {

			@Override
			public Object getValue(Object object) {
				return null;
			}

			@Override
			public Object newValue(Object object) {
				return null;
			}

			@Override
			public void setValue(Object object, Object value, PropertyResolverConverter converter) {
			}
		};

		private IPropertyLocator locator;

		public CachingPropertyLocator(IPropertyLocator locator) {
			this.locator = locator;
		}

		@Override
		public IGetAndSet get(Class<?> clz, String exp) {
			String key = clz.getName() + "#" + exp;
			
			IGetAndSet located = map.get(key);
			if (located == null) {
				located = locator.get(clz, exp);
				if (located == null) {
					located = NONE;
				}
				map.put(key, located);
			}
			
			if (located == NONE) {
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
		@Override
		public IGetAndSet get(Class<?> clz, String exp) {
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
						getAndSet = new ListGetAndSet(index);
					}
					catch (NumberFormatException ex)
					{
						// can't parse the exp as an index, maybe the exp was a
						// method.
						method = findMethod(clz, exp);
						if (method != null)
						{
							getAndSet = new MethodGetAndSet(method, MethodGetAndSet.findSetter(
								method, clz), null);
						}
						else
						{
							field = findField(clz, exp);
							if (field != null)
							{
								getAndSet = new FieldGetAndSet(field);
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
							throw new WicketRuntimeException("Can't parse the expression '" + exp +
								"' as an index for an array lookup");
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
									// if so then it could be a getPropertyIndex(int)
									// and setPropertyIndex(int, object)
									String name = Character.toUpperCase(propertyName.charAt(0)) +
										propertyName.substring(1);
									method = clz.getMethod(GET + name, new Class[] { int.class });
									getAndSet = new IndexedPropertyGetAndSet(method, parsedIndex);
								}
								catch (Exception e)
								{
									throw new WicketRuntimeException(
										"No get method defined for class: " + clz +
											" expression: " + propertyName);
								}
							}
						}
						else
						{
							getAndSet = new MethodGetAndSet(method, MethodGetAndSet.findSetter(
								method, clz), null);
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
				field = findField(clz, exp);
				getAndSet = new MethodGetAndSet(method, MethodGetAndSet.findSetter(method, clz),
					field);
			}
			
			return getAndSet;
		}
		
		/**
		 * @param clz
		 * @param expression
		 * @return introspected field
		 */
		private Field findField(final Class<?> clz, final String expression)
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
					for (Field aField : fields)
					{
						if (aField.getName().equals(expression))
						{
							aField.setAccessible(true);
							return aField;
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
		private Method findGetter(final Class<?> clz, final String expression)
		{
			String name = Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
			Method method = null;
			try
			{
				method = clz.getMethod(GET + name, (Class[])null);
			}
			catch (Exception ignored)
			{
			}
			if (method == null)
			{
				try
				{
					method = clz.getMethod(IS + name, (Class[])null);
				}
				catch (Exception e)
				{
					log.debug("Cannot find getter " + clz + "." + expression);
				}
			}
			return method;
		}

		private Method findMethod(final Class<?> clz, String expression)
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
	}
}