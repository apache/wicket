package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Field;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jcompagner
 */
public class FieldGetAndSet extends AbstractGetAndSet
{
	private static final Logger log = LoggerFactory.getLogger(FieldGetAndSet.class);
	private final Field field;

	/**
	 * Construct.
	 *
	 * @param field
	 */
	public FieldGetAndSet(final Field field)
	{
		super();
		this.field = field;
		this.field.setAccessible(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(final Object object)
	{
		try
		{
			return field.get(object);
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException(
				"Error getting field value of field " + field + " from object " + object, ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object newValue(final Object object)
	{
		Class<?> clz = field.getType();
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
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(final Object object, Object value,
		final PropertyResolverConverter converter)
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getTargetClass()
	{
		return field.getType();
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