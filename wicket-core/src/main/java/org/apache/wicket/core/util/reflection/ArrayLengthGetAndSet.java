package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Array;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;

public final class ArrayLengthGetAndSet extends AbstractGetAndSet
{
	public ArrayLengthGetAndSet()
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
		throw new WicketRuntimeException(
			"Can't get a new value from a length of an array: " + object);
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