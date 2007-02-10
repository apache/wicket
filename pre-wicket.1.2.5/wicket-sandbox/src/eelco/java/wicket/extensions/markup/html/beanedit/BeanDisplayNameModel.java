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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;

import wicket.Component;
import wicket.model.Model;

/**
 * Model for displaying the name of a JavaBean.
 * 
 * @author Eelco Hillenius
 */
public final class BeanDisplayNameModel extends Model
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
	public BeanDisplayNameModel(BeanModel beanModel)
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
			BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
			String displayName;

			if (beanDescriptor != null)
			{
				displayName = beanDescriptor.getDisplayName();
			}
			else
			{
				Class clazz = beanModel.getBean().getClass();
				displayName = (clazz != null) ? clazz.getName() : null;
			}
			return displayName;
		}

		return null;
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
}
