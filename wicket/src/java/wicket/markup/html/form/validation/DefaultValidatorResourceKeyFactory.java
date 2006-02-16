package wicket.markup.html.form.validation;

import wicket.markup.html.form.FormComponent;
import wicket.util.lang.Classes;

/**
 * Default implementation of IValidatorResourceKeyFactory. Generates keys of
 * form <form-id>.<component-name>.<validator-class>
 * 
 * @author Igor Vaynberg (ivaynberg)
 *  
 */
public class DefaultValidatorResourceKeyFactory implements IValidatorResourceKeyFactory
{
	/**
	 * @see IValidatorResourceKeyFactory#newKey(IValidator, FormComponent)
	 */
	public String newKey(IValidator validator, FormComponent formComponent)
	{
		return formComponent.getId() + "." + Classes.name(validator.getClass());
	}
}
