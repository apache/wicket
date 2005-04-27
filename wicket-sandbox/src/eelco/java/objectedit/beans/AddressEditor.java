/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package objectedit.beans;

import java.beans.PropertyDescriptor;

import objectedit.PropertyEditor;
import wicket.model.IModel;

/**
 * 
 */
public class AddressEditor extends PropertyEditor
{

	/**
	 * Construct.
	 * @param id component id
	 * @param beanModel model with the target bean
	 * @param descriptor property descriptor
	 */
	public AddressEditor(String id, IModel beanModel, final PropertyDescriptor descriptor)
	{
		super(id, beanModel, descriptor);
	}

}
