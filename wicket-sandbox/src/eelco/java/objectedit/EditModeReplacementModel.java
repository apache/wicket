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
 */
public class EditModeReplacementModel extends Model
{
	/** edit mode. */
	private final EditMode editMode;

	/** property descriptor. */
	private PropertyDescriptor descriptor;

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
		if(editMode.getMode() == EditMode.MODE_READ_ONLY
				|| descriptor.getWriteMethod() == null)
		{
			return "disabled";
		}
		return null;
	}

}
