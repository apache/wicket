package wicket.markup.html.form;

import wicket.markup.html.form.validation.IFormValidationStrategy;
import wicket.markup.html.form.validation.IFormValidator;

/**
 * The default form validation strategy.
 */
final class DefaultFormValidationStrategy implements IFormValidationStrategy
{
	/**
	 * Construct.
	 */
	DefaultFormValidationStrategy()
	{
	}

	/**
	 * Validates all children of this form and the form itself, recording all messages
	 * that are returned by the validators.
	 * @param form the form that the validation is applied to
	 */
	public void validate(final Form form)
	{
		// Visit all the form components and validate each
		form.visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// Validate form component
				formComponent.validate();

				// If component is not valid (has an error)
				if (!formComponent.isValid())
				{
					// tell component to deal with invalidity
					formComponent.invalid();
				}
				else
				{
					// tell component that it is valid now
					formComponent.valid();
				}
			}
		});

		if ((form.validator != IFormValidator.NULL) && (!form.hasError()))
		{
			// now, visit any validators of the form itself
			form.validator.validate(form);
		}
	}
}