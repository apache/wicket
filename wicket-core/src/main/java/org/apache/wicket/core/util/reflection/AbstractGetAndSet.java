package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AbstractGetAndSet implements IGetAndSet
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