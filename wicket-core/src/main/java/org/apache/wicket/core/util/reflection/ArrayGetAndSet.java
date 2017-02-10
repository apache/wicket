package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Array;

import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayGetAndSet extends AbstractGetAndSet
{
	private static final Logger log = LoggerFactory.getLogger(ArrayGetAndSet.class);
	private final int index;
	private final Class<?> clzComponentType;

	public ArrayGetAndSet(Class<?> clzComponentType, int index)
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
			log.warn("Cannot set new value " + value + " at index " + index
				+ " for array holding elements of class " + clzComponentType, e);
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