package org.apache.wicket.validation.validator;

import java.io.Serializable;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking that the value is not smaller then a specified minimum value
 * 
 * @param <Z>
 *            type of validatable
 */
public class MinimumValidator<Z extends Comparable<Z> & Serializable> implements IValidator<Z>
{
	private static final long serialVersionUID = 1L;
	private final Z minimum;

	/**
	 * Constructor
	 * 
	 * @param minimum
	 *            the minimum value
	 */
	public MinimumValidator(Z minimum)
	{
		this.minimum = minimum;
	}

	public void validate(IValidatable<Z> validatable)
	{
		Z value = validatable.getValue();
		if (value.compareTo(minimum) < 0)
		{
			ValidationError error = new ValidationError();
			error.addMessageKey("MinimumValidator");
			error.setVariable("minimum", minimum);
			validatable.error(error);
		}
	}

	/**
	 * Gets the minimum value.
	 * 
	 * @return minimum value
	 */
	public Z getMinimum()
	{
		return minimum;
	}
}