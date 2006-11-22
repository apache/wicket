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
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.Serializable;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.model.IModel;

/**
 * Model for JavaBeans.
 * 
 * @author Eelco Hillenius
 */
public class BeanModel implements IModel
{
	private static final long serialVersionUID = 1L;

	/** the java bean to edit. */
	private final Serializable bean;

	/**
	 * Construct.
	 * 
	 * @param bean
	 *            the javabean to edit
	 */
	public BeanModel(Serializable bean)
	{
		if (bean == null)
		{
			throw new IllegalArgumentException("bean must be not null");
		}

		this.bean = bean;
	}

	/**
	 * Gets the {@link BeanInfo} object of the model object.
	 * 
	 * @param component
	 * @return BeanInfo object for the model object, or null if the model object
	 *         is null
	 */
	protected final BeanInfo getBeanInfo(Component component)
	{
		Class objectClass = bean.getClass();
		try
		{
			return Introspector.getBeanInfo(objectClass);
		}
		catch (IntrospectionException e)
		{
			throw new WicketRuntimeException(e);
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

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		return bean;
	}

	/**
	 * Throws an {@link UnsupportedOperationException} as changing the bean is
	 * not permitted.
	 * 
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		throw new UnsupportedOperationException("BeanModel is read-only");
	}

	/**
	 * Convenience method.
	 * 
	 * @return the bean
	 */
	public Serializable getBean()
	{
		return bean;
	}
}
