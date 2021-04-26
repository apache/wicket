package org.apache.wicket.bean.validation;

import java.util.function.Supplier;

import jakarta.validation.Configuration;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;


/**
 * This is the default validator provider. It creates a validator instance with the default message
 * interpolator wrapped inside a {@link SessionLocaleInterpolator} so it is aware of Wicket's
 * locale. Only one instance of the {@link Validator} is created.
 * 
 * @author igor
 * 
 */
public class DefaultValidatorProvider implements Supplier<Validator>
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
