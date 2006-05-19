package wicket.extensions.model;

import wicket.Component;
import wicket.model.IModel;

/**
 * Model adapter that makes working with models for checkboxes easier.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractCheckBoxModel implements IModel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		return isSelected(component) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		boolean sel = Boolean.TRUE.equals(object);
		setSelected(component, sel);

	}


	/**
	 * Callback for setting the model's value to true or false
	 * 
	 * @param component
	 * @param sel
	 *            true if the checkbox is selected, false otherwise
	 */
	public abstract void setSelected(Component component, boolean sel);

	/**
	 * Returns model's value
	 * 
	 * @param component
	 * @return true to indicate the checkbox should be selected, false otherwise
	 */
	public abstract boolean isSelected(Component component);


	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}

}
