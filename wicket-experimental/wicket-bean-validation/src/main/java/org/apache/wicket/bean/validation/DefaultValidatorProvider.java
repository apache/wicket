package org.apache.wicket.bean.validation;

import javax.validation.Configuration;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.wicket.util.IProvider;

/**
 * This is the default validator provider. It creates a validator instance with the default message
 * interpolator wrapped inside a {@link SessionLocaleInterpolator} so it is aware of Wicket's
 * locale. Only one instance of the {@link Validator} is created.
 * 
 * @author igor
 * 
 */
public class DefaultValidatorProvider implements IProvider<Validator>
{

	private Validator validator;

	@Override
	public Validator get()
	{
		if (validator == null)
		{
			Configuration<?> config = Validation.byDefaultProvider().configure();

			MessageInterpolator interpolator = config.getDefaultMessageInterpolator();
			interpolator = new SessionLocaleInterpolator(interpolator);

			ValidatorFactory factory = config.messageInterpolator(interpolator)
				.buildValidatorFactory();

			validator = factory.getValidator();
		}
		return validator;
	}
}
