package org.apache.wicket.core.util.reflection;

import java.util.Map;

import org.apache.wicket.core.util.lang.PropertyResolverConverter;

public final class MapGetAndSet extends AbstractGetAndSet
{
	private final String key;

	public MapGetAndSet(String key)
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