/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.beanedit;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wicket.Component;
import wicket.model.Model;

/**
 * Models that extracts the property descriptors of a JavaBean as a list.
 * 
 * @author Eelco Hillenius
 */
public final class BeanPropertiesListModel extends Model
{
	private static final long serialVersionUID = 1L;

	/** the bean model. */
	private final BeanModel beanModel;

	/**
	 * Construct.
	 * 
	 * @param beanModel
	 *            model that provides the java bean
	 */
	public BeanPropertiesListModel(BeanModel beanModel)
	{
		this.beanModel = beanModel;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		BeanInfo beanInfo = beanModel.getBeanInfo(component);
		if (beanInfo != null)
		{
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			if (descriptors != null)
			{
				List all = new ArrayList();
				int len = descriptors.length;
				for (int i = 0; i < len; i++)
				{
					if (shouldAdd(descriptors[i]))
					{
						PropertyMeta meta = new PropertyMeta(beanModel, descriptors[i]);
						all.add(meta);
					}
				}
				int defaultPropertyIndex = beanInfo.getDefaultPropertyIndex();
				if (defaultPropertyIndex != -1)
				{

				}
				return all;
			}
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * As this is a read-only model, this method allways throws an
	 * {@link UnsupportedOperationException}.
	 * 
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		throw new UnsupportedOperationException("this model is read only");
	}

	/**
	 * Whether this descriptor should be added to the list.
	 * 
	 * @param descriptor
	 * @return whether this descriptor should be added to the list
	 */
	private boolean shouldAdd(PropertyDescriptor descriptor)
	{
		if ("class".equals(descriptor.getName()))
		{
			return false;
		}
		return true;
	}
}