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

import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Base class for custom property editors.
 */
public abstract class PropertyEditor extends Panel
{
	/**
	 * Construct.
	 * @param id component id
	 * @param beanModel model with the target bean
	 * @param descriptor property descriptor
	 */
	public PropertyEditor(String id, IModel beanModel, final PropertyDescriptor descriptor)
	{
		super(id);
	}
}
