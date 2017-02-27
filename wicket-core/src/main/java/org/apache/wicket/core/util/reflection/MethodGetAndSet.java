package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.convert.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MethodGetAndSet extends AbstractGetAndSet
{
	static final Logger log = LoggerFactory.getLogger(MethodGetAndSet.class);

	private final Method getMethod;
	private final Method setMethod;
	private final Field field;

	public MethodGetAndSet(Method getMethod, Method setMethod, Field field)
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
			throw new WicketRuntimeException(
				"Error calling method: " + getMethod + " on object: " + object, ex.getCause());
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException(
				"Error calling method: " + getMethod + " on object: " + object, ex);
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
					throw new ConversionException(
						"Method [" + getMethod + "]. Can't convert value: " + value + " to class: "
							+ getMethod.getReturnType() + " for setting it on " + object);
				}
				else if (getMethod.getReturnType().isPrimitive())
				{
					throw new ConversionException("Method [" + getMethod
						+ "]. Can't convert null value to a primitive class: "
						+ getMethod.getReturnType() + " for setting it on " + object);
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
				throw new WicketRuntimeException(
					"Error calling method: " + setMethod + " on object: " + object, ex.getCause());
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException(
					"Error calling method: " + setMethod + " on object: " + object, ex);
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
				throw new WicketRuntimeException(
					"Error setting field: " + field + " on object: " + object, ex);
			}
		}
		else
		{
			throw new WicketRuntimeException(
				"no set method defined for value: " + value + " on object: " + object
					+ " while respective getMethod being " + getMethod.getName());
		}
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