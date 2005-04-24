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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;

import wicket.Component;
import wicket.model.IModel;

/**
 * 
 */
public final class DisplayNameModel extends ReadOnlyBeanModel
{
	/**
	 * Construct.
	 * @param nestedModel model that provides the java bean
	 */
	public DisplayNameModel(IModel nestedModel)
	{
		super(nestedModel);
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		BeanInfo beanInfo = getBeanInfo(component);
		if(beanInfo != null)
		{
			BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
			String displayName;
			if(beanDescriptor != null)
			{
				displayName = beanDescriptor.getDisplayName();
			}
			else
			{
				Class clazz = getBeanClass(component);
				displayName = (clazz != null) ? clazz.getName() : null;
			}
			return displayName;
		}
		return null;
	}
}
