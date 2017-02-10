package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.wicket.core.util.lang.DefaultPropertyLocator;

public class ReflectionUtility
{

	/**
	 * @param clz
	 * @param expression
	 * @return introspected field
	 */
	public static Field findField(final Class<?> clz, final String expression)
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
			DefaultPropertyLocator.log.debug("Cannot find field " + clz + "." + expression);
		}
		return field;
	}

	/**
	 * @param clz
	 * @param expression
	 * @return The method for the expression null if not found
	 */
	public static Method findGetter(final Class<?> clz, final String expression)
	{
		String name = Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
		Method method = null;
		try
		{
			method = clz.getMethod(MethodGetAndSet.GET + name, (Class[])null);
		}
		catch (Exception ignored)
		{
		}
		if (method == null)
		{
			try
			{
				method = clz.getMethod(MethodGetAndSet.IS + name, (Class[])null);
			}
			catch (Exception e)
			{
				DefaultPropertyLocator.log.debug("Cannot find getter " + clz + "." + expression);
			}
		}
		return method;
	}

	public static Method findMethod(final Class<?> clz, String expression)
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
			DefaultPropertyLocator.log.debug("Cannot find method " + clz + "." + expression);
		}
		return method;
	}

}
