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
	private String format;

	public DateValidator(Date minimum, Date maximum, String format)
	{
		super(minimum, maximum);
		this.format = format;
	}

	public DateValidator(Date minimum, Date maximum)
	{
		this(minimum, maximum, null);
	}

	public DateValidator()
	{
	}

	@Override
	protected ValidationError decorate(ValidationError error, IValidatable<Date> validatable)
	{
		error = super.decorate(error, validatable);

		error.setVariable("inputdate", validatable.getValue());

		// format variables if format has been specified
		if (format != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			if (getMinimum() != null)
			{
				error.setVariable("minimum", sdf.format(getMinimum()));
			}
			if (getMaximum() != null)
			{
				error.setVariable("maximum", sdf.format(getMaximum()));
			}
			error.setVariable("inputdate", sdf.format(validatable.getValue()));
		}

		return error;
	}
}
