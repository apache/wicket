/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.wizard.framework.beanedit;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wicket.Component;
import wicket.model.IModel;

/**
 * Models that extracts the property descriptors of a JavaBean as a list.
 *
 * @author Eelco Hillenius
 */
public final class BeanPropertiesListModel extends ReadOnlyModel
{
	/**
	 * Construct.
	 * @param nestedModel model that provides the java bean
	 */
	public BeanPropertiesListModel(IModel nestedModel)
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