package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionUtility
{

	private static final Logger log = LoggerFactory.getLogger(ReflectionUtility.class);
	public static final String GET = "get";
	private static final String IS = "is";
	public static final String SET = "set";

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
			log.debug("Cannot find field " + clz + "." + expression);
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
			log.debug("Cannot find method " + clz + "." + expression);
		}
		return method;
	}

	public static Method findPositionGetter(Class<?> clz, String property)
	{
		String name = Character.toUpperCase(property.charAt(0)) + property.substring(1);
		try
		{
			return clz.getMethod(GET + name, new Class[] { int.class });
		}
		catch (Exception e)
		{
			log.debug("Cannot find method " + clz + "." + name + "(int)");
			return null;
		}
	}

	public static Method findSetter(Method getMethod, Class<?> clz)
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

}
