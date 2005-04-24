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

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wicket.Component;
import wicket.model.IModel;

/**
 * 
 */
public final class PropertyDescriptorListModel extends ReadOnlyBeanModel
{
	/**
	 * Construct.
	 * @param nestedModel model that provides the java bean
	 */
	public PropertyDescriptorListModel(IModel nestedModel)
	{
		super(nestedModel);
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		BeanInfo beanInfo = getBeanInfo(component);
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		if(descriptors != null)
		{
			List all = new ArrayList();
			int len = descriptors.length;
			for(int i = 0; i < len; i++)
			{
				if(shouldAdd(descriptors[i]))
				{
					all.add(descriptors[i]);
				}
			}
			return all;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * Whether this descriptor should be added to the list.
	 * @param descriptor
	 * @return whether this descriptor should be added to the list
	 */
	private boolean shouldAdd(PropertyDescriptor descriptor)
	{
		if("class".equals(descriptor.getName()))
		{
			return false;
		}
		return true;
	}
}