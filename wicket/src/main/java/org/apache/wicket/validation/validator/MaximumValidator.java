package org.apache.wicket.validation.validator;

import java.io.Serializable;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking that the value is not greater then a specified maximum value
 * 
 * @param <Z>
 *            type of validatable
 */
public class MaximumValidator<Z extends Comparable<Z> & Serializable> implements IValidator<Z>
{
	private static final long serialVersionUID = 1L;
	private final Z maximum;

	/**
	 * Constructor
	 * 
	 * @param maximum
	 *            the maximum value
	 */
	public MaximumValidator(Z maximum)
	{
		this.maximum = maximum;

	}

	/**
	 * {@inheritDoc}
	 */
	public void validate(IValidatable<Z> validatable)
	{
		Z value = validatable.getValue();
		if (value.compareTo(maximum) > 0)
		{
			ValidationError error = new ValidationError();
			error.addMessageKey("MaximumValidator");
			error.setVariable("maximum", maximum);
			validatable.error(error);
		}
	}

	/**
	 * Gets the maximum value.
	 * 
	 * @return maximum value
	 */
	public Z getMaximum()
	{
		return maximum;
	}


}