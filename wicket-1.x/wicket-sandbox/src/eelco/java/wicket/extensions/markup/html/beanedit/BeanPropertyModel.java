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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.model.IModel;

/**
 * Model for one JavaBean property.
 * 
 * @author Eelco Hillenius
 */
public class BeanPropertyModel implements IModel
{
	private static final long serialVersionUID = 1L;

	/** property descriptor. */
	private final PropertyMeta propertyMeta;

	/**
	 * Construct.
	 * 
	 * @param propertyMeta
	 */
	public BeanPropertyModel(PropertyMeta propertyMeta)
	{
		this.propertyMeta = propertyMeta;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		Method method = propertyMeta.getPropertyDescriptor().getReadMethod();
		if (method != null)
		{
			Object bean = propertyMeta.getBeanModel().getBean();
			try
			{
				Object value = method.invoke(bean, (Object[])null);
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
		Method method = propertyMeta.getPropertyDescriptor().getWriteMethod();
		if (method != null)
		{
			Object bean = propertyMeta.getBeanModel().getBean();
			try
			{
				method.invoke(bean, new Object[] { object });
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

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}
}
