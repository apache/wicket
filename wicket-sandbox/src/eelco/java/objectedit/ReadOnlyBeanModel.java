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

import wicket.Component;
import wicket.model.IModel;

/**
 * 
 */
public abstract class ReadOnlyBeanModel extends BeanModel
{

	/**
	 * Construct.
	 * @param nestedModel model that provides the java bean
	 */
	public ReadOnlyBeanModel(IModel nestedModel)
	{
		super(nestedModel);
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		throw new UnsupportedOperationException("this (" + this + ") model is read-only");
	}
}
