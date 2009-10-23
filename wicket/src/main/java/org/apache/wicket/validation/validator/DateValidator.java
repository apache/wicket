/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.validation.validator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.validation.IValidatable;


/**
 * Validator for checking dates. This validator can be extended or can be used for one of its static
 * factory methods to get the default <code>DateValidator</code> as a range, maximum, or minimum
 * type.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public abstract class DateValidator extends AbstractValidator<Date>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Gets a <code>Date</code> range validator for checking if the <code>Date</code> value falls
	 * between the minimum and maximum <code>Date</code> values. If that is not the case, an error
	 * message will be generated with the key "DateValidator.range". The message keys that can be
	 * used are:
	 * <p>
	 * <ul>
	 * <li>${minimum}: the minimum date</li>
	 * <li>${maximum}: the maximum date</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * 
	 * @return the requested <code>DateValidator</code>
	 */
	public static DateValidator range(Date minimum, Date maximum)
	{
		return new RangeValidator(minimum, maximum, null);
	}

	/**
	 * @see #range(Date, Date)
	 * 
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 * 
	 * @return the requested <code>DateValidator</code>
	 */
	public static DateValidator range(Date minimum, Date maximum, String format)
	{
		return new RangeValidator(minimum, maximum, format);
	}

	/**
	 * Gets a <code>Date</code> minimum validator for checking if a <code>Date</code> value is
	 * greater than the given minimum <code>Date</code> value. If that is not the case, an error
	 * message will be generated with the key "DateValidator.minimum". The message keys that can be
	 * used are:
	 * <p>
	 * <ul>
	 * <li>${minimum}: the minimum date</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * 
	 * @return the requested <code>DateValidator</code>
	 */
	public static DateValidator minimum(Date minimum)
	{
		return new MinimumValidator(minimum, null);
	}

	/**
	 * @see #minimum(Date)
	 * 
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 * 
	 * @return the requested <code>DateValidator</code>
	 */
	public static DateValidator minimum(Date minimum, String format)
	{
		return new MinimumValidator(minimum, format);
	}

	/**
	 * Gets a <code>Date</code> maximum validator for checking if a <code>Date</code> value is
	 * smaller than the given maximum value. If that is not the case, an error message will be
	 * generated with the key "DateValidator.maximum". The message keys that can be used are:
	 * <p>
	 * <ul>
	 * <li>${maximum}: the maximum date</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * 
	 * @return the requested <code>DateValidator</code>
	 */
	public static DateValidator maximum(Date maximum)
	{
		return new MaximumValidator(maximum, null);
	}

	/**
	 * @see #maximum(Date)
	 * 
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 * 
	 * @return the requested <code>DateValidator</code>
	 */
	public static DateValidator maximum(Date maximum, String format)
	{
		return new MaximumValidator(maximum, format);
	}

	/**
	 * 
	 */
	private static class RangeValidator extends DateValidator
	{
		private static final long serialVersionUID = 1L;
		private final Date minimum;
		private final Date maximum;
		private final String format;

		private RangeValidator(Date minimum, Date maximum, String format)
		{
			this.minimum = minimum;
			this.maximum = maximum;
			this.format = format;
		}

		@Override
		protected Map<String, Object> variablesMap(IValidatable<Date> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			if (format == null)
			{
				map.put("minimum", minimum);
				map.put("maximum", maximum);
				map.put("inputdate", validatable.getValue());
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				map.put("minimum", sdf.format(minimum));
				map.put("maximum", sdf.format(maximum));
				map.put("inputdate", sdf.format(validatable.getValue()));
			}
			return map;
		}

		/**
		 * @see AbstractValidator#resourceKey(FormComponent)
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
		private final String format;

		private MinimumValidator(Date minimum, String format)
		{
			this.minimum = minimum;
			this.format = format;
		}

		@Override
		protected Map<String, Object> variablesMap(IValidatable<Date> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			if (format == null)
			{
				map.put("minimum", minimum);
				map.put("inputdate", validatable.getValue());
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				map.put("minimum", sdf.format(minimum));
				map.put("inputdate", sdf.format(validatable.getValue()));
			}
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
		private final String format;

		private MaximumValidator(Date maximum, String format)
		{
			this.maximum = maximum;
			this.format = format;
		}

		@Override
		protected Map<String, Object> variablesMap(IValidatable<Date> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			if (format == null)
			{
				map.put("maximum", maximum);
				map.put("inputdate", validatable.getValue());
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				map.put("maximum", sdf.format(maximum));
				map.put("inputdate", sdf.format(validatable.getValue()));
			}
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
