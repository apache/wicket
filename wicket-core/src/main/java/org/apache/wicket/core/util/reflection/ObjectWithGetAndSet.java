package org.apache.wicket.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.wicket.core.util.lang.PropertyResolverConverter;

/**
 * @author jcompagner
 *
 */
public class ObjectWithGetAndSet
{
	private final IGetAndSet getAndSet;
	private final Object value;

	/**
	 * @param getAndSet
	 * @param value
	 */
	public ObjectWithGetAndSet(IGetAndSet getAndSet, Object value)
	{
		this.getAndSet = getAndSet;
		this.value = value;
	}

	/**
	 * @param value
	 * @param converter
	 */
	public void setValue(Object value, PropertyResolverConverter converter)
	{
		getAndSet.setValue(this.value, value, converter);
	}

	/**
	 * @param createIfNull if the value should be created in case the property is null
	 * @return The value
	 */
	public Object getValue(boolean createIfNull)
	{
		Object propertyValue = getAndSet.getValue(value);
		if(propertyValue == null && createIfNull)
			propertyValue = getAndSet.newValue(value);
		return propertyValue;
	}

	/**
	 * @return class of property value
	 */
	public Class<?> getTargetClass()
	{
		return getAndSet.getTargetClass();
	}

	/**
	 * @return Field or null if no field exists for expression
	 */
	public Field getField()
	{
		return getAndSet.getField();
	}

	/**
	 * @return Getter method or null if no getter exists for expression
	 */
	public Method getGetter()
	{
		return getAndSet.getGetter();
	}

	/**
	 * @return Setter method or null if no setter exists for expression
	 */
	public Method getSetter()
	{
		return getAndSet.getSetter();
	}
}