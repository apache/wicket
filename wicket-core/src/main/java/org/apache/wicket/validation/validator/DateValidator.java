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

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking if a given date falls within [min,max] range.
 * 
 * If either min or max are {@code null} they are not checked.
 * 
 * <p>
 * Resource keys:
 * <ul>
 * <li>{@code DateValidator.exact} if min==max</li>
 * <li>{@code DateValidator.range} if both min and max are not {@code null}</li>
 * <li>{@code DateValidator.minimum} if max is {@code null}</li>
 * <li>{@code DateValidator.maximum} if min is {@code null}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Error Message Variables:
 * <ul>
 * <li>{@code name}: the id of {@code Component} that failed</li>
 * <li>{@code label}: the label of the {@code Component} (either comes from
 * {@code FormComponent.labelModel} or resource key {@code <form-id>.<form-component-id>}</li>
 * <li>{@code input}: the input value</li>
 * <li>{@code inputdate}: the formatted input value</li>
 * <li>{@code minimum}: the minimum allowed value</li>
 * <li>{@code maximum}: the maximum allowed value</li>
 * </ul>
 * </p>
 * 
 * @author igor
 */
public class DateValidator extends RangeValidator<Date>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * @return a {@link DateValidator} that validates if a date is between (inclusive) a minimum and
	 *         maximum
	 */
	public static DateValidator range(Date minimum, Date maximum)
	{
		return new DateValidator(minimum, maximum);
	}

	/**
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 * 
	 * @return a {@link DateValidator} that validates if a date is between (inclusive) a minimum and
	 *         maximum
	 */
	public static DateValidator range(Date minimum, Date maximum, String format)
	{
		return new DateValidator(minimum, maximum, format);
	}

	/**
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * 
	 * @return a {@link DateValidator} that validates if a date is after or equal to a minimum date
	 */
	public static DateValidator minimum(Date minimum)
	{
		return new DateValidator(minimum, null);
	}

	/**
	 * @param minimum
	 *            the minimum <code>Date</code>
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 * 
	 * @return a {@link DateValidator} that validates if a date is after or equal to a minimum date
	 */
	public static DateValidator minimum(Date minimum, String format)
	{
		return new DateValidator(minimum, null, format);
	}

	/**
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * 
	 * @return a {@link DateValidator} that validates if a date is before or equal to a maximum date
	 */
	public static DateValidator maximum(Date maximum)
	{
		return new DateValidator(null, maximum);
	}

	/**
	 * @param maximum
	 *            the maximum <code>Date</code>
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 * 
	 * @return a {@link DateValidator} that validates if a date is before or equal to a maximum date
	 */
	public static DateValidator maximum(Date maximum, String format)
	{
		return new DateValidator(null, maximum, format);
	}

	private String format;

	/**
	 * Constructor that sets the minimum and maximum date values and a custom date formating.
	 * 
	 * @param minimum
	 *            the minimum date
	 * @param maximum
	 *            the maximum date
	 * @param format
	 *            The format string used to format the date with SimpleDateFormat
	 */
	public DateValidator(Date minimum, Date maximum, String format)
	{
		super(minimum, maximum);
		this.format = format;
	}

	/**
	 * Constructor that sets the minimum and maximum date values.
	 * 
	 * @param minimum
	 *            the minimum date
	 * @param maximum
	 *            the maximum date
	 */
	public DateValidator(Date minimum, Date maximum)
	{
		this(minimum, maximum, null);
	}

	/**
	 * Constructor used for subclasses who want to set the range using
	 * {@link #setRange(Comparable, Comparable)}
	 */
	protected DateValidator()
	{
	}

	@Override
	protected IValidationError decorate(IValidationError error, IValidatable<Date> validatable)
	{
		error = super.decorate(error, validatable);

		if (error instanceof ValidationError)
		{
			ValidationError ve = (ValidationError) error;
			ve.setVariable("inputdate", validatable.getValue());

			// format variables if format has been specified
			if (format != null)
			{
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				if (getMinimum() != null)
				{
					ve.setVariable("minimum", sdf.format(getMinimum()));
				}
				if (getMaximum() != null)
				{
					ve.setVariable("maximum", sdf.format(getMaximum()));
				}
				ve.setVariable("inputdate", sdf.format(validatable.getValue()));
			}
		}

		return error;
	}
}
