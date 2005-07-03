/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package wicket.examples.beanedit;

import java.beans.PropertyDescriptor;

import wicket.extensions.markup.html.beanedit.BeanModel;
import wicket.extensions.markup.html.beanedit.BeanPropertyEditor;
import wicket.extensions.markup.html.beanedit.EditMode;

/**
 * 
 */
public class AddressEditor extends BeanPropertyEditor
{

	/**
	 * Construct.
	 * @param id component id
	 * @param beanModel model with the target bean
	 * @param descriptor property descriptor
	 */
	public AddressEditor(String id, BeanModel beanModel, final PropertyDescriptor descriptor)
	{
		super(id, beanModel, descriptor, EditMode.READ_WRITE);
	}

}
