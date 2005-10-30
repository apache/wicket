/*
 * $Id$
 * $Revision$ $Date$
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

/**
 * 
 */
package wicket.util.object;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import wicket.WicketRuntimeException;
import wicket.util.concurrent.ConcurrentHashMap;
import wicket.util.convert.ConversionException;
import wicket.util.convert.Converter;
import wicket.util.convert.IConverter;

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
public class Objects
{
	private final static Map classesToGetAndSetters = new ConcurrentHashMap(64);


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
			return object;

		ObjectAndGetSetter getter = getObjectAndGetSetter(expression, object, false);
		if (getter == null)
			return null;
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
		setter.setValue(value, converter == null ? new Converter() : converter);
	}

	private static ObjectAndGetSetter getObjectAndGetSetter(final String expression,
			final Object object, boolean tryToCreateNull)
	{
		String exp = expression.replace('[', '.');
		if (exp != expression)
		{
			exp = exp.replaceAll("].", ".");
		}
		final String expressionWithoutBrackets = exp;
		int index = exp.indexOf('.');
		int lastIndex = 0;
		Object value = object;
		Class clz = value.getClass();
		while (index != -1)
		{
			exp = expressionWithoutBrackets.substring(lastIndex, index);
			IGetAndSet getAndSetter = null;
			try
			{
				getAndSetter = getGetAndSetter(exp, clz);
			}
			catch (WicketRuntimeException ex)
			{

				// expression by it self can't be found. try to find a
				// setPropertyByIndex(int,value) method
				index = expressionWithoutBrackets.indexOf('.', index + 1);
				if (index != -1)
				{
					String indexExpression = expressionWithoutBrackets.substring(lastIndex, index);
					getAndSetter = getGetAndSetter(indexExpression, clz);
				}
				else
				{
					exp = expressionWithoutBrackets.substring(lastIndex);
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
			index = expressionWithoutBrackets.indexOf('.', lastIndex);
			clz = value.getClass();
			if (index == -1)
			{
				exp = expressionWithoutBrackets.substring(lastIndex);
				break;
			}
		}
		IGetAndSet getAndSetter = getGetAndSetter(exp, clz);
		return new ObjectAndGetSetter(getAndSetter, value);
	}


	private final static IGetAndSet findArrayProperty(String exp, Class clz, int index)
	{
		String name = "get" + Character.toUpperCase(exp.charAt(0)) + exp.substring(1);
		try
		{
			Method method = clz.getMethod(name, new Class[] { int.class });
			IGetAndSet getAndSet = new ArrayPropertyGetSet(method, index);
			Map getAndSetters = (Map)classesToGetAndSetters.get(clz);
			if (getAndSetters == null)
			{
				getAndSetters = new ConcurrentHashMap(8);
				classesToGetAndSetters.put(clz, getAndSetters);
			}
		}
		catch (Exception ex)
		{
			// TODO log
		}
		return null;
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
			Method method = findGetter(clz, exp);
			if (method == null)
			{
				if (List.class.isAssignableFrom(clz))
				{
					int index = Integer.parseInt(exp);
					getAndSetter = new ListGetSet(index);
				}
				else if (Map.class.isAssignableFrom(clz))
				{
					getAndSetter = new MapGetSet(exp);
				}
				else if (clz.isArray())
				{
					int index = Integer.parseInt(exp);
					getAndSetter = new ArrayGetSet(index);
				}
				else
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
							method = clz.getMethod("get" + name, new Class[] { int.class });
							getAndSetter = new ArrayPropertyGetSet(method, parsedIndex);

						}
						catch (Exception e)
						{
							throw new WicketRuntimeException("no get method defined for class: "
									+ clz + " expression: " + propertyName);
						}
					}
					else
					{
						throw new WicketRuntimeException("no get method defined for class: " + clz
								+ " expression: " + exp);
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
		catch (Exception ex)
		{
		}
		if (method == null)
		{
			try
			{
				method = clz.getMethod("is" + name, null);
			}
			catch (Exception ex)
			{
			}
		}
		return method;
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
		 * @see wicket.util.object.Objects.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return ((Map)object).get(key);
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			((Map)object).put(key, value);
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#newValue(Object)
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
		 * @see wicket.util.object.Objects.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return ((List)object).get(index);
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#setValue(java.lang.Object,
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
		 * @see wicket.util.object.Objects.IGetAndSet#newValue(Object)
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
		 * @see wicket.util.object.Objects.IGetAndSet#getValue(java.lang.Object)
		 */
		public Object getValue(Object object)
		{
			return Array.get(object, index);
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#setValue(java.lang.Object,
		 *      java.lang.Object, wicket.util.convert.IConverter)
		 */
		public void setValue(Object object, Object value, IConverter converter)
		{
			value = converter.convert(value, object.getClass().getComponentType());
			Array.set(object, index, value);
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#newValue(java.lang.Object)
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
			catch (Exception ex)
			{
				// TODO LOG
			}
			return value;
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
			catch (Exception ex)
			{
				// TODO LOG
			}
			return null;
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#getValue(java.lang.Object)
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
		 * @see wicket.util.object.Objects.IGetAndSet#setValue(java.lang.Object,
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
		 * @see wicket.util.object.Objects.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			if (setMethod == null)
			{
				setMethod = findSetter(getMethod, object.getClass());
			}
			// TODO LOG
			if (setMethod == null)
				return null;

			Class clz = getMethod.getReturnType();
			Object value = null;
			try
			{
				value = clz.newInstance();
				setMethod.invoke(object, new Object[] { index, value });
			}
			catch (Exception ex)
			{
				// TODO LOG
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
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#getValue(java.lang.Object)
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
				if (converted == null && value != null)
				{
					throw new ConversionException("Can't convert value: " + value + " to class: "
							+ getMethod.getReturnType() + " for setting it on " + object);
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
				return clz.getMethod(name, new Class[] { getMethod.getReturnType() });
			}
			catch (Exception ex)
			{
				// TODO LOG
			}
			return null;
		}

		/**
		 * @see wicket.util.object.Objects.IGetAndSet#newValue(java.lang.Object)
		 */
		public Object newValue(Object object)
		{
			if (setMethod == null)
			{
				setMethod = findSetter(getMethod, object.getClass());
			}
			// TODO LOG
			if (setMethod == null)
				return null;

			Class clz = getMethod.getReturnType();
			Object value = null;
			try
			{
				value = clz.newInstance();
				setMethod.invoke(object, new Object[] { value });
			}
			catch (Exception ex)
			{
				// TODO LOG
			}
			return value;
		}

	}
}
