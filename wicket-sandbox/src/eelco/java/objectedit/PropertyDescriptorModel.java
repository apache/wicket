/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package objectedit;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.model.IModel;

/**
 * 
 */
public class PropertyDescriptorModel extends BeanModel
{
	/** property descriptor. */
	private final PropertyDescriptor descriptor;

	/**
	 * Construct.
	 * @param nestedModel
	 * @param descriptor
	 */
	public PropertyDescriptorModel(IModel nestedModel, PropertyDescriptor descriptor)
	{
		super(nestedModel);
		this.descriptor = descriptor;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		Method method = descriptor.getReadMethod();
		if (method != null)
		{
			Object bean = getBean(component);
			try
			{
				Object value = method.invoke(bean, null);
				return value;
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
		return null;
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		Method method = descriptor.getWriteMethod();
		if (method != null)
		{
			Object bean = getBean(component);
			try
			{
				method.invoke(bean, new Object[]{object});
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
	}
}
