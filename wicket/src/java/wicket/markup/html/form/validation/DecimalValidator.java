/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.form.validation;

import wicket.markup.html.form.FormComponent;

/**
 * Ensures that the form component has a numeric value in a given range. The range static
 * factory method constructs a DecimalValidator with minimum and maximum values specified
 * as Java longs. Convenience fields exist for INT, POSITIVE_INT, LONG and POSITIVE_LONG
 * which match the appropriate ranges of numbers.
 * @author Jonathan Locke
 */
public class DecimalValidator extends AbstractValidator
{
	/**
	 * Validator that ensures int value.
	 */
	public static final DecimalValidator INT = new DecimalValidator(
			Integer.MIN_VALUE, Integer.MAX_VALUE);

	/**
	 * Validator that ensures positive int value.
	 */
	public static final DecimalValidator POSITIVE_INT = new DecimalValidator(0, Integer.MAX_VALUE);

	/**
	 * Validator that ensures long value.
	 */
	public static final DecimalValidator LONG = new DecimalValidator(Long.MIN_VALUE, Long.MAX_VALUE);

	/**
	 * Validator that ensures positive long value.
	 */
	public static final DecimalValidator POSITIVE_LONG = new DecimalValidator(0, Long.MAX_VALUE);

	/** Lower bound on valid decimal number. */
	private final long min;

	/** Upper bound on valid decimal number. */
	private final long max;

	/**
	 * Private constructor forces use of static factory method and static instances.
	 * @param min Lower bound on valid decimal number
	 * @param max Upper bound on valid decimal number
	 */
	private DecimalValidator(final long min, final long max)
	{
		this.min = min;
		this.max = max;
	}

	/**
	 * Gets a decimal validator with a given range.
	 * @param min Lower bound on valid decimal number
	 * @param max Upper bound on valid decimal number
	 * @return Validator object
	 */
	public final static DecimalValidator range(final long min, final long max)
	{
		return new DecimalValidator(min, max);
	}

	/**
	 * Validates the given form component. Ensures that the form component has a numeric
	 * value. If min and max arguments are given, this validator also ensures the value is
	 * in bounds.
	 * @param component The component to validate
	 * @return Error for component or NO_ERROR if none
	 */
	public final ValidationErrorMessage validate(final FormComponent component)
	{
		// Get component value
		final String value = component.getRequestString();

		// Don't test emtpy/null values that should required validator do.
		if (value != null && !"".equals(value))
		{
			try
			{
				// Get long value
				final long longValue = Long.parseLong(value);

				// Check range
				if (longValue < min || longValue > max)
				{
					return errorMessage(value, component);
				}
			}
			catch (NumberFormatException e)
			{
				return errorMessage(value, component);
			}
		}
		return NO_ERROR;
	}
}
