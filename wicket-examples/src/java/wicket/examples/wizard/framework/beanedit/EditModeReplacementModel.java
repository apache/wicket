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

import java.beans.PropertyDescriptor;

import wicket.Component;
import wicket.model.Model;

/**
 * <p>
 * Replacement model that returns 'disabled' when the editMode == EditMode.MODE_READ_ONLY
 * or the property descriptor has no write method. Use this model with AttributeModifier
 * 'disabled'.
 * </p>
 * <p>
 * Be sure NOT to have the 'disabled' attribute in your markup, and create
 * the AttributeModifier with constructor parameter 'addAttributeIfNotPresent' true.
 * </p>
 *
 * @author Eelco Hillenius
 */
public final class EditModeReplacementModel extends Model
{
	/** edit mode. */
	private final EditMode editMode;

	/** property descriptor. */
	private final PropertyDescriptor descriptor;

	/**
	 * Construct.
	 * @param editMode edit mode
	 */
	public EditModeReplacementModel(EditMode editMode)
	{
		super();
		this.editMode = editMode;
		this.descriptor = null;
	}

	/**
	 * Construct.
	 * @param descriptor property descriptor
	 */
	public EditModeReplacementModel(PropertyDescriptor descriptor)
	{
		super();
		this.descriptor = descriptor;
		this.editMode = null;
	}

	/**
	 * Construct.
	 * @param editMode edit mode
	 * @param descriptor property descriptor
	 */
	public EditModeReplacementModel(EditMode editMode, PropertyDescriptor descriptor)
	{
		super();
		this.editMode = editMode;
		this.descriptor = descriptor;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		if(editMode != null)
		{
			return (editMode == EditMode.READ_ONLY) ? "disabled" : null;
		}
		if(descriptor != null)
		{
			return (descriptor.getWriteMethod() == null) ? "disabled" : null;
		}
		return null;
	}
}