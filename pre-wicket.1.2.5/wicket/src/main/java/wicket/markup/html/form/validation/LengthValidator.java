/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.validation;

import java.util.Map;

import wicket.markup.html.form.FormComponent;
import wicket.util.string.StringList;
import wicket.util.string.Strings;

/**
 * Validates that a form component's string value is of a certain min/max
 * length. Validators are constructed by calling the min, max and range static
 * factory methods. For example, LengthValidator.min(6) would return a validator
 * valid only when the input of the component it is attached to is at least 6
 * characters long. Likewise, LengthValidator.range(3, 5) would only validate a
 * component containing between 3 and 5 characters (inclusive).
 *
 * Depending on which factory is used to create the validator, one or more of the 
 * following parameters are added to the error message interpolation:
 * <ul>
 * <li>min</li>
 * <li>max</li>
 * <li>length - length of the user input, always present</li>
 * </ul>
 * 
 * @author Jonathan Locke
 * 
 * @deprecated see {@link StringValidator}
 */
public class LengthValidator extends AbstractValidator
{
	private static final long serialVersionUID = 1L;
	
	/** True if minimum bound should be checked. */
	private final boolean checkMin;

	/** True if maximum bound should be checked. */
	private final boolean checkMax;

	/** Lower bound on valid length. */
	private final int min;

	/** Upper bound on valid length. */
	private final int max;

	/**
	 * Private constructor forces use of static factory method and static
	 * instances. Or override it to implement resourceKey(Component)
	 * 
	 * @param checkMin
	 *            True if minimum bound should be checked
	 * @param min
	 *            Lower bound on valid length
	 * @param checkMax
	 *            True if maximum bound should be checked
	 * @param max
	 *            Upper bound on valid length
	 */
	protected LengthValidator(final boolean checkMin, final int min, final boolean checkMax,
			final int max)
	{
		this.min = min;
		this.max = max;
		this.checkMin = checkMin;
		this.checkMax = checkMax;
	}

	/**
	 * Gets a length validator object that requires a minimum number of
	 * characters.
	 * 
	 * @param min
	 *            Minimum number of characters
	 * @return Validator object
	 */
	public final static LengthValidator min(final int min)
	{
		return new LengthValidator(true, min, false, 0);
	}

	/**
	 * Gets a length validator object that requires a maximum number of
	 * characters.
	 * 
	 * @param max
	 *            Maximum number of characters
	 * @return Validator object
	 */
	public final static LengthValidator max(final int max)
	{
		return new LengthValidator(false, 0, true, max);
	}

	/**
	 * Gets a length validator object that requires a minimum and maximum number
	 * of characters.
	 * 
	 * @param min
	 *            Minimum number of characters
	 * @param max
	 *            Maximum number of characters
	 * @return Validator object
	 */
	public final static LengthValidator range(final int min, final int max)
	{
		return new LengthValidator(true, min, true, max);
	}


	/**
	 * Validates that a form component's value is of a certain minimum and/or
	 * maximum length.
	 * @see AbstractValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public final void validate(FormComponent formComponent)
	{
		String value = (String)formComponent.getConvertedInput();
		// If value is non-empty
		if (!Strings.isEmpty(value))
		{
			// Check length
			if ((checkMin && value.length() < min) || (checkMax && value.length() > max))
			{
				error(formComponent);
			}
		}
	}

	/**
	 * Gets whether the maximum bound should be checked.
	 * 
	 * @return whether the minimum bound should be checked
	 */
	public final boolean isCheckMax()
	{
		return checkMax;
	}

	/**
	 * Gets whether the minimum bound should be checked.
	 * 
	 * @return whether the minimum bound should be checked
	 */
	public final boolean isCheckMin()
	{
		return checkMin;
	}

	/**
	 * Gets the upper bound on valid length.
	 * 
	 * @return the upper bound on valid length
	 */
	public final int getMax()
	{
		return max;
	}

	/**
	 * Gets the lower bound on valid length.
	 * 
	 * @return the lower bound on valid length
	 */
	public final int getMin()
	{
		return min;
	}

	/**
	 * Gets the default variables for interpolation. These are:
	 * <ul>
	 * <li>${min}: the minimal length</li>
	 * <li>${max}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * </ul>
	 * they are only added when the corresponding enabling flag is set.
	 * @param formComponent form component
	 * @return a map with the variables for interpolation
	 */
	protected Map messageModel(FormComponent formComponent)
	{
		final Map map = super.messageModel(formComponent);
		if (checkMin) 
		{
			map.put("min", new Long(min));
		}
		if (checkMax) 
		{
			map.put("max", new Long(max));
		}
		int size = 0;
		if (formComponent.getInput() != null) 
		{
			size = formComponent.getInput().length();
		}
		map.put("length", new Integer(size));
        return map;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		final StringList list = new StringList();

		if (checkMin)
		{
			list.add("min = " + min);
		}

		if (checkMax)
		{
			list.add("max = " + max);
		}

		return "[LengthValidator " + list.toString() + "]";
	}
}
