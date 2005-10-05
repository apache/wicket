package wicket;

import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.validation.IValidator;
import wicket.util.lang.Classes;

/**
 * Default implementation of IValidatorResourceKeyFactory. Generates keys of
 * form <form-id>.<component-name>.<validator-class>
 * 
 * @author Igor Vaynberg ivaynberg@privesec.com
 * 
 */
public class DefaultValidatorResourceKeyFactory implements IValidatorResourceKeyFactory
{
	/**
	 * @see IValidatorResourceKeyFactory#newKey(IValidator, FormComponent)
	 */
	public String newKey(IValidator validator, FormComponent formComponent)
	{
		return formComponent.getForm().getId() + "." + formComponent.getId() + "."
		+ Classes.name(validator.getClass());
	}
}
