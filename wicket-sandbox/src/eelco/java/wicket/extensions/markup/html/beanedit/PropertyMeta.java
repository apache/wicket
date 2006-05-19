/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import java.beans.PropertyDescriptor;
import java.io.Serializable;

/**
 * Wraps meta data about a property.
 * 
 * @author Eelco Hillenius
 */
public class PropertyMeta implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** model with the bean that owns the property. */
	private final BeanModel beanModel;

	/** the beans property descriptor. */
	private final PropertyDescriptor propertyDescriptor;

	/** the edit mode; defaults to EditMode.READ_WRITE. */
	private EditMode editMode = EditMode.READ_WRITE;

	/**
	 * Construct.
	 * 
	 * @param beanModel
	 *            the bean model
	 * @param propertyDescriptor
	 *            the beans property descriptor
	 */
	public PropertyMeta(final BeanModel beanModel, final PropertyDescriptor propertyDescriptor)
	{
		if (propertyDescriptor == null)
		{
			throw new IllegalArgumentException("Argument propertyDescriptor may not be null");
		}

		if (beanModel == null)
		{
			throw new IllegalArgumentException("Argument beanModel may not be null");
		}

		this.beanModel = beanModel;
		this.propertyDescriptor = propertyDescriptor;
	}

	/**
	 * Gets the model with the bean that owns the property.
	 * 
	 * @return the model with the bean that owns the property
	 */
	public final BeanModel getBeanModel()
	{
		return beanModel;
	}

	/**
	 * Gets the beans property descriptor.
	 * 
	 * @return the beans property descriptor
	 */
	public final PropertyDescriptor getPropertyDescriptor()
	{
		return propertyDescriptor;
	}

	/**
	 * Gets the type of the property.
	 * 
	 * @return the type of the property
	 */
	public Class getPropertyType()
	{
		return propertyDescriptor.getPropertyType();
	}

	/**
	 * Gets the display name of the property.
	 * 
	 * @return the display name of the property
	 */
	public String getDisplayName()
	{
		return propertyDescriptor.getDisplayName();
	}

	/**
	 * Gets the edit mode.
	 * 
	 * @return the edit mode
	 */
	public EditMode getEditMode()
	{
		return editMode;
	}

	/**
	 * Sets the edit mode.
	 * 
	 * @param editMode
	 *            the edit mode
	 */
	public void setEditMode(EditMode editMode)
	{
		this.editMode = editMode;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "meta for property " + propertyDescriptor.getDisplayName() + " of bean "
				+ beanModel.getBean();
	}
}
