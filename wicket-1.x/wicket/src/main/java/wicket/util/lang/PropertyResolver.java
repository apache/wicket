/*
 * $Id: PropertyResolver.java 4710 2006-03-02 00:46:15 -0800 (Thu, 02 Mar 2006)
 * eelco12 $ $Revision$ $Date: 2006-03-02 00:46:15 -0800 (Thu, 02 Mar
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.util.concurrent.ConcurrentHashMap;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.string.Strings;

/**
 * This class parses expressions to lookup or set a value on the object that is
 * given. <br/> The supported expressions are:
 * <p>
 * "property": This can can then be a bean property with get and set method. Or
 * if a map is given as an object it will be lookup with the property as a key
 * when there is not get method for that property. <p/>
 * <p>
 * "property1.property2": Both properties are lookup as written above. If
 * property1 evaluates to null then if there is a setMethod (or if it is a map)
 * and the Class of the property has a default constructor then the object will
 * be constructed and set on the object. <p/>
 * <p>
 * "property.index": If the property is a List or Array then the second property
 * can be a index on that list like: 'mylist.0' this expression will also map on
 * a getProperty(index) or setProperty(index,value) methods. If the object is a
 * List then the list will grow automaticaly if the index is greater then the
 * size <p/>
 * <p>
 * Index or map properties can also be written as: "property[index]" or
 * "property[key]" <p/>
 * 
 * @author jcompagner
 */
public final class PropertyResolver
{
	private final static Map classesToGetAndSetters = new ConcurrentHashMap(64);

	/** Log. */
	private static final Log log = LogFactory.getLog(PropertyResolver.class);

	/**
	 * Looksup the value from the object with the given expression. If the
	 * expresion, the object itself or one property evalutes to null then a null
	 * will be returned.
	 * 
	 * @param expression
	 *            The expression string with the property to be lookup.
	 * @param object
	 *            The object which is evaluated.
	 * @return The value that is evaluted. Null something in the expression
	 *         evaluted to null.
	 */
	public final static Object getValue(final String expression, final Object object)
	{
		if (expression == null || expression.equals("") || object == null)
		{
			return object;
		}

		ObjectAndGetSetter getter = getObjectAndGetSetter(expression, object, false);
		if (getter == null)
		{
			return null;
		}
		return getter.getValue();

	}

	/**
	 * Set the value on the object with the given expression. If the expression
	 * can't be evaluated then a WicketRuntimeException will be thrown. If a
	 * null object is encounted then it will try to generate it by calling the
	 * default constructor and set it on the object.
	 * 
	 * The value will be tried to convert to the right type with the given
	 * converter.
	 * 
	 * @param expression
	 *            The expression string with the property to be set.
	 * @param object
	 *            The object which is evaluated to set the value on.
	 * @param value
	 *            The value to set.
	 * @param converter
	 *            The convertor to convert the value if needed to the right
	 *            type.
	 */
	public final static void setValue(final String expression, final Object object, Object value,
			IConverter converter)
	{
		if (expression == null || expression.equals(""))
		{
			throw new WicketRuntimeException("Empty expression setting value: " + value
					+ " on object: " + object);
		}
		if (object == null)
		{
			throw new WicketRuntimeException("Null object setting value: " + value
					+ " with expression: " + expression);
		}

		ObjectAndGetSetter setter = getObjectAndGetSetter(expression, object, true);
		if (setter == null)
		{
			throw new WicketRuntimeException("Null object returned for expression: " + expression
					+ " for setting value: " + value + " on: " + object);
		}
		setter.setValue(value, converter == null ? Session.get().getConverter() : converter);
	}

	private static ObjectAndGetSetter getObjectAndGetSetter(final String expression,
			final Object object, boolean tryToCreateNull)
	{
		final String expressionBracketsSeperated = Strings.replaceAll(expression, "[", ".[").toString();
		int index = expressionBracketsSeperated.indexOf('.');
		int lastIndex = 0;
		Object value = object;
		Class clz = value.getClass();
		String exp = expressionBracketsSeperated;
		while (index != -1)
		{
			exp = expressionBracketsSeperated.substring(lastIndex, index);
			IGetAndSet getAndSetter = null;
			try
			{
				getAndSetter = getGetAndSetter(exp, clz);
			}
			catch (WicketRuntimeException ex)
			{

				// expression by it self can't be found. try to find a
				// setPropertyByIndex(int,value) method
				index = expressionBracketsSeperated.indexOf('.', index + 1);
				if (index != -1)
				{
					String indexExpression = expressionBracketsSeperated
							.substring(lastIndex, index);
					getAndSetter = getGetAndSetter(indexExpression, clz);
				}
				else
				{
					exp = expressionBracketsSeperated.substring(lastIndex);
					break;
				}
			}
			Object newValue = getAndSetter.getValue(value);
			if (newValue == null)
			{
				if (tryToCreateNull)
				{
					newValue = getAndSetter.newValue(value);
					if (newValue == null)
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			}
			value = newValue;
			lastIndex = index + 1;
			index = expressionBracketsSeperated.indexOf('.', lastIndex);
			clz = value.getClass();
			if (index == -1)
			{
				exp = expressionBracketsSeperated.substring(lastIndex);
				break;
			}
		}
		IGetAndSet getAndSetter = getGetAndSetter(exp, clz);
		return new ObjectAndGetSetter(getAndSetter, value);
	}


	private final static IGetAndSet getGetAndSetter(String exp, Class clz)
	{
		Map getAndSetters = (Map)classesToGetAndSetters.get(clz);
		if (getAndSetters == null)
		{
			getAndSetters = new ConcurrentHashMap(8);
			classesToGetAndSetters.put(clz, getAndSetters);
		}

		IGetAndSet getAndSetter = (IGetAndSet)getAndSetters.get(exp);
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
						// can't parse the exp als a index maybe the exp was a
						// method.
						method = findMethod(clz, exp);
						if (method != null)
						{
							getAndSetter = new MethodGetAndSet(method);
						}
						else
						{
							throw new WicketRuntimeException("The expression '" + exp
									+ "' is neither an index nor is it a method for the list " + clz);
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
						getAndSetter = new ArrayGetSet(index);
					}
					catch (NumberFormatException ex)
					{
						if (exp.equals("length") || exp.equals("size"))
						{
							getAndSetter = new ArrayLengthGetSet();
						}
						else
						{
							throw new WicketRuntimeException("can't parse the exp " + exp
									+ " as an index for an array lookup");
						}
					}
				}
				else
				{
					field = findField(clz, exp);
					if(field == null)
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
									String name = Character.toUpperCase(propertyName.charAt(0))
											+ propertyName.substring(1);
									method = clz.getMethod("get" + name, new Class[] { int.class });
									getAndSetter = new ArrayPropertyGetSet(method, parsedIndex);
	
								}
								catch (Exception e)
								{
									throw new WicketRuntimeException(
											"no get method defined for class: " + clz + " expression: "
													+ propertyName);
								}
							}
							else
							{
								// We do not look for a public FIELD because that is
								// not good
								// programming with beans patterns
								throw new WicketRuntimeException("No get method defined for class: "
										+ clz + " expression: " + exp);
							}
						}
						else
						{
							getAndSetter = new MethodGetAndSet(method);
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
				getAndSetter = new MethodGetAndSet(method);
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
	private static Field findField(Class clz, String expression)
	{
		Field field = null;
		try
		{
			field = clz.getField(expression);
		}
		catch (Exception e)
		{
			Class tmp = clz;
			while(tmp != null && tmp != Object.class)
			{
				Field[] fields = tmp.getDeclaredFields();
				for (int i = 0; i < fields.length; i++)
				{
					if(fields[i].getName().equals(expression))
					{
						fields[i].setAccessible(true);
						return fields[i];
					}
				}
				tmp = tmp.getSuperclass();
			}
			log.debug("Cannot find field " + clz + "." + expression, e);
		}
		return field;
	}

	/**
	 * @param clz
	 * @param expression
	 * @return The method for the expression null if not found
	 */
	private final static Method findGetter(Class clz, String expression)
	{
		String name = Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
		Method method = null;
		try
		{
			method = clz.getMethod("get" + name, null);
		}
		catch (Exception e)
		{
		}
		if (method == null)
		{
			try
			{
				method = clz.getMethod("is" + name, null);
			}
			catch (Exception e)
			{
				log.debug("Cannot find getter " + clz + "." + expression, e);
			}
		}
		return method;
	}

	private final static Method findMethod(Class clz, String expression)
	{
		if (expression.endsWith("()"))
		{
			expression = expression.substring(0, expression.length() - 2);
		}
		Method method = null;
		try
		{
			method = clz.getMethod(expression, null);
		}
		catch (Exception e)
		{
			log.debug("Cannot find method " + clz + "." + expression, e);
		}
		return method;
	}

	/**
	 * Utility class: instantiation not allowed.
	 */
	private PropertyResolver() {
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
		public void setValue(Object value, IConverter converter)
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

	}


	private static interface IGetAndSet
	{
		/**
		 * @param object
		 *            The object where the value must be taken from.
		 * 
		 * @return The value of this property
		 */
		public Object getValue(final Object object);

		/**
		 * @param object
		 *            The object where the new value must be set on.
		 * 
		 * @return The new value for the property that is set back on that
		 *         object.
		 */
		public Object newValue(Object object);

		/**
		 * @param object
		 * @param value
		 * @param converter
		 */
		public void setValue(final Object object, final Object value, IConverter converter);

	}

	private static final class MapGetSet implements IGetAndSet
	{
		final private String key;

		MapGetSet(String key)
		{
			this.key = key;
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return ((Map)object).get(key);
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			((Map)object).put(key, value);
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(Object)
		 */
		public Object newValue(Object object)
		{
			// Map can't make a newValue or should it look what is more in the
			// map and try to make one of the class if finds?
			return null;
		}
	}

	private static final class ListGetSet implements IGetAndSet
	{
		final private int index;

		ListGetSet(int index)
		{
			this.index = index;
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return ((List)object).get(index);
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			List lst = (List)object;

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
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(Object)
		 */
		public Object newValue(Object object)
		{
			// List can't make a newValue or should it look what is more in the
			// list and try to make one of the class if finds?
			return null;
		}
	}

	private static final class ArrayGetSet implements IGetAndSet
	{
		final private int index;

		ArrayGetSet(int index)
		{
			this.index = index;
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return Array.get(object, index);
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			value = converter.convert(value, object.getClass().getComponentType());
			Array.set(object, index, value);
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			Class clz = object.getClass().getComponentType();
			Object value = null;
			try
			{
				value = clz.newInstance();
				Array.set(object, index, value);
			}
			catch (Exception e)
			{
				log.warn("Cannot set new value " + value + " at index " + index
						+ " for array holding elements of class " + clz, e);
			}
			return value;
		}
	}

	private static final class ArrayLengthGetSet implements IGetAndSet
	{
		ArrayLengthGetSet()
		{
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return new Integer(Array.getLength(object));
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			throw new WicketRuntimeException("Cant set the length on an array");
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			throw new WicketRuntimeException("Cant get a new value from a length of an array");
		}
	}

	private static final class ArrayPropertyGetSet implements IGetAndSet
	{
		final private Integer index;
		final private Method getMethod;
		private Method setMethod;

		ArrayPropertyGetSet(Method method, int index)
		{
			this.index = new Integer(index);
			this.getMethod = method;
		}

		private final static Method findSetter(Method getMethod, Class clz)
		{
			String name = getMethod.getName();
			name = "set" + name.substring(3);
			try
			{
				return clz.getMethod(name, new Class[] { int.class, getMethod.getReturnType() });
			}
			catch (Exception e)
			{
				log.debug("Cannot find setter method corresponding to " + getMethod, e);
			}
			return null;
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
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
				throw new WicketRuntimeException("Error calling index property method: "
						+ getMethod + " on object: " + object, ex.getCause());
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error calling index property method: "
						+ getMethod + " on object: " + object, ex);
			}
			return ret;
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			if (setMethod == null)
			{
				setMethod = findSetter(getMethod, object.getClass());
			}
			if (setMethod != null)
			{
				Object converted = converter.convert(value, getMethod.getReturnType());
				if (converted == null && value != null)
				{
					throw new ConversionException("Can't convert value: " + value + " to class: "
							+ getMethod.getReturnType() + " for setting it on " + object);
				}
				try
				{
					setMethod.invoke(object, new Object[] { index, converted });
				}
				catch (InvocationTargetException ex)
				{
					throw new WicketRuntimeException("Error index property calling method: "
							+ setMethod + " on object: " + object, ex.getCause());
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error index property calling method: "
							+ setMethod + " on object: " + object, ex);
				}
			}
			else
			{
				throw new WicketRuntimeException("no set method defined for value: " + value
						+ " on object: " + object);
			}
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
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

			Class clz = getMethod.getReturnType();
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

	private static final class MethodGetAndSet implements IGetAndSet
	{
		private Method getMethod;
		private Method setMethod;

		MethodGetAndSet(Method getMethod)
		{
			this.getMethod = getMethod;
			this.getMethod.setAccessible(true);
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public final Object getValue(final Object object)
		{
			Object ret = null;
			try
			{
				ret = getMethod.invoke(object, null);
			}
			catch (InvocationTargetException ex)
			{
				throw new WicketRuntimeException("Error calling method: " + getMethod
						+ " on object: " + object, ex.getCause());
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error calling method: " + getMethod
						+ " on object: " + object, ex);
			}
			return ret;
		}

		/**
		 * @param object
		 * @param value
		 * @param converter
		 */
		public final void setValue(final Object object, final Object value, IConverter converter)
		{
			if (setMethod == null)
			{
				setMethod = findSetter(getMethod, object.getClass());
			}
			if (setMethod != null)
			{
				Object converted = converter.convert(value, getMethod.getReturnType());
				if ( converted == null)
				{
					if( value != null )
					{
						throw new ConversionException("Can't convert value: " + value + " to class: "
								+ getMethod.getReturnType() + " for setting it on " + object);
					}
					else if( getMethod.getReturnType().isPrimitive() )
					{
						throw new ConversionException("Can't convert null value to a primitive class: "
								+ getMethod.getReturnType() + " for setting it on " + object);
					}
				}
				try
				{
					setMethod.invoke(object, new Object[] { converted });
				}
				catch (InvocationTargetException ex)
				{
					throw new WicketRuntimeException("Error calling method: " + setMethod
							+ " on object: " + object, ex.getCause());
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error calling method: " + setMethod
							+ " on object: " + object, ex);
				}
			}
			else
			{
				throw new WicketRuntimeException("no set method defined for value: " + value
						+ " on object: " + object);
			}
		}

		private final static Method findSetter(Method getMethod, Class clz)
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
			catch (Exception e)
			{
				log.debug("Cannot find setter corresponding to " + getMethod, e);
			}
			return null;
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
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

			Class clz = getMethod.getReturnType();
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

	}

	/**
	 * @author jcompagner
	 */
	private static class FieldGetAndSetter implements IGetAndSet
	{

		private Field field;

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
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			try
			{
				return field.get(object);
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error getting field value of field " + field
						+ " from object " + object, ex);
			}
		}

		/**
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			Class clz = field.getType();
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
		 * @see wicket.util.lang.PropertyResolver.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			value = converter.convert(value, field.getType());
			try
			{
				field.set(object, value);
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException("Error setting field value of field " + field
						+ " on object " + object + ", value " + value, ex);
			}
		}
	}
}