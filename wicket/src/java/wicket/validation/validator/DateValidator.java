/*
 * $Id$ $Revision$ $Date$
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
package wicket.validation.validator;

import java.util.Date;
import java.util.Map;

import wicket.validation.IValidatable;

/**
 * A validator for dates that can be used for subclassing or use one of the
 * static factory methods to get the default date validators as range, maximum
 * or minimum.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class DateValidator extends AbstractValidator<Date>
{

	/**
	 * Gets a Date range validator to check if the date is between the minimum
	 * and maximum dates.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "DateValidator.range" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: The minimum date</li>
	 * <li>${maximum}: The maximum date</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum date.
	 * @param maximum
	 *            The maximum date.
	 * 
	 * @return The DateValidator
	 */
	public static DateValidator range(Date minimum, Date maximum)
	{
		return new RangeValidator(minimum, maximum);
	}

	/**
	 * Gets a Date minimum validator to check if a date is greater then the
	 * given minimum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "DateValidator.minimum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: The minimal date</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum length of the string.
	 * 
	 * @return The DateValidator
	 */
	public static DateValidator minimum(Date minimum)
	{
		return new MinimumValidator(minimum);
	}

	/**
	 * Gets a Date maximum validator to check if a date is smaller then the
	 * given maximum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "DateValidator.maximum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${maximum}: The maximum date</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param maximum
	 *            The maximum date.
	 * 
	 * @return The DateValidator
	 */
	public static DateValidator maximum(Date maximum)
	{
		return new MaximumValidator(maximum);
	}


	private static class RangeValidator extends DateValidator
	{
		private static final long serialVersionUID = 1L;
		private final Date minimum;
		private final Date maximum;

		private RangeValidator(Date minimum, Date maximum)
		{
			this.minimum = minimum;
			this.maximum = maximum;

		}

		@Override
		protected Map<String, Object> variablesMap(IValidatable<Date> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("minimum", minimum);
			map.put("maximum", maximum);
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey()
		{
			return "DateValidator.range";
		}

		@Override
		protected void onValidate(IValidatable<Date> validatable)
		{
			Date value = validatable.getValue();
			if (value.before(minimum) || value.after(maximum))
			{
				error(validatable);
			}

		}

	}

	private static class MinimumValidator extends DateValidator
	{
		private static final long serialVersionUID = 1L;
		private final Date minimum;

		private MinimumValidator(Date minimum)
		{
			this.minimum = minimum;
		}


		@Override
		protected Map<String, Object> variablesMap(IValidatable<Date> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("minimum", minimum);
			return map;
		}

		@Override
		protected String resourceKey()
		{
			return "DateValidator.minimum";
		}


		@Override
		protected void onValidate(IValidatable<Date> validatable)
		{
			Date value = validatable.getValue();
			if (value.before(minimum))
			{
				error(validatable);
			}

		}

	}

	private static class MaximumValidator extends DateValidator
	{
		private static final long serialVersionUID = 1L;
		private final Date maximum;

		private MaximumValidator(Date maximum)
		{
			this.maximum = maximum;
		}


		@Override
		protected Map<String, Object> variablesMap(IValidatable<Date> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("maximum", maximum);
			return map;
		}

		@Override
		protected String resourceKey()
		{
			return "DateValidator.maximum";
		}


		@Override
		protected void onValidate(IValidatable<Date> validatable)
		{
			Date value = validatable.getValue();
			if (value.after(maximum))
			{
				error(validatable);
			}

		}

	}
}