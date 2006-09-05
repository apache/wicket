package wicket.extensions.model;

import wicket.Component;
import wicket.model.AbstractDetachableAssignmentAwareModel;

/**
 * Model adapter that makes working with models for checkboxes easier.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractCheckBoxModel extends AbstractDetachableAssignmentAwareModel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Returns model's value
	 * 
	 * @param component
	 * @return true to indicate the checkbox should be selected, false otherwise
	 */
	public abstract boolean isSelected(Component component);

	/**
	 * Callback for setting the model's value to true or false
	 * 
	 * @param component
	 * @param sel
	 *            true if the checkbox is selected, false otherwise
	 */
	public abstract void setSelected(Component component, boolean sel);

	/**
	 * @see wicket.model.AbstractDetachableAssignmentAwareModel#onAttach()
	 */
	@Override
	protected void onAttach()
	{
	}

	/**
	 * @see wicket.model.AbstractDetachableAssignmentAwareModel#onDetach()
	 */
	@Override
	protected void onDetach()
	{
	}

	/**
	 * @see wicket.model.AbstractDetachableAssignmentAwareModel#onGetObject(wicket.Component)
	 */
	@Override
	protected Object onGetObject(Component component)
	{
		return isSelected(component) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * @see wicket.model.AbstractDetachableAssignmentAwareModel#onSetObject(wicket.Component,
	 *      java.lang.Object)
	 */
	@Override
	protected void onSetObject(Component component, Object object)
	{
		boolean sel = Boolean.TRUE.equals(object);
		setSelected(component, sel);
	}

}
