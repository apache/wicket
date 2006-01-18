package wicket.markup.html.form.validation;

import wicket.markup.html.form.FormComponent;

/**
 * Factory for creating resource keys for validator error messages
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IValidatorResourceKeyFactory
{
	/**
	 * Creates a resource key for validator's error message
	 * 
	 * @param validator
	 *            The validator that needs the resource key
	 * @param formComponent
	 *            The form component that is in error
	 * @return resource key string for the validator
	 */
	String newKey(IValidator validator, FormComponent formComponent);
}
