package org.apache.wicket.validation.validator;

import java.io.Serializable;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking if a given value falls within [min,max] range.
 * 
 * @param <Z>
 *            type of validatable
 */
public class RangeValidator<Z extends Comparable<Z> & Serializable> implements IValidator<Z>
{
	private static final long serialVersionUID = 1L;
	private final Z minimum;
	private final Z maximum;

	/**
	 * Constructor that sets the minimum and maximum values.
	 * 
	 * @param minimum
	 *            the minimum value
	 * @param maximum
	 *            the maximum value
	 */
	public RangeValidator(Z minimum, Z maximum)
	{
		this.minimum = minimum;
		this.maximum = maximum;

	}

	public void validate(IValidatable<Z> validatable)
	{
		Z value = validatable.getValue();
		if (value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0)
		{
			ValidationError error = new ValidationError();
			error.addMessageKey("RangeValidator");
			error.setVariable("minimum", minimum);
			error.setVariable("maximum", maximum);
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