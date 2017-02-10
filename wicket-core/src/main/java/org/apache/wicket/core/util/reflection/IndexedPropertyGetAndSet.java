package org.apache.wicket.core.util.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.convert.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IndexedPropertyGetAndSet extends AbstractGetAndSet
{
	private static final Logger log = LoggerFactory.getLogger(IndexedPropertyGetAndSet.class);
	final private Integer index;
	final private Method getMethod;
	private Method setMethod;

	public IndexedPropertyGetAndSet(final Method method, final int index)
	{
		this.index = index;
		getMethod = method;
		getMethod.setAccessible(true);
	}

	private static Method findSetter(final Method getMethod, final Class<?> clz)
	{
		String name = getMethod.getName();
		name = MethodGetAndSet.SET + name.substring(3);
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
			throw new WicketRuntimeException(
				"Error calling index property method: " + getMethod + " on object: " + object,
				ex.getCause());
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException(
				"Error calling index property method: " + getMethod + " on object: " + object, ex);
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
				throw new ConversionException("Can't convert value: " + value + " to class: "
					+ getMethod.getReturnType() + " for setting it on " + object);
			}
			try
			{
				setMethod.invoke(object, index, converted);
			}
			catch (InvocationTargetException ex)
			{
				throw new WicketRuntimeException(
					"Error index property calling method: " + setMethod + " on object: " + object,
					ex.getCause());
			}
			catch (Exception ex)
			{
				throw new WicketRuntimeException(
					"Error index property calling method: " + setMethod + " on object: " + object,
					ex);
			}
		}
		else
		{
			throw new WicketRuntimeException(
				"No set method defined for value: " + value + " on object: " + object);
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