package wicket.extensions.markup.html.form.select;

import java.io.Serializable;

import wicket.model.IModel;

/**
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IOptionRenderer extends Serializable
{
	/**
	 * Get the value for displaying to the user.
	 * 
	 * @param object
	 *            SelectOption model object
	 * @return the value for displaying to the user.
	 */
	public String getDisplayValue(Object object);

	/**
	 * Gets the model that will be used to represent the value object.
	 * 
	 * This is a good place to wrap the value object with a detachable model one
	 * is desired
	 * 
	 * @param value
	 * @return model that will contain the value object
	 */
	public IModel getModel(Object value);
}
