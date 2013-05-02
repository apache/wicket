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

import java.io.Serializable;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking if a given value falls within [min,max] range.
 * 
 * If either min or max are {@code null} they are not checked.
 * 
 * <p>
 * Resource keys:
 * <ul>
 * <li>{@code RangeValidator.exact} if min==max</li>
 * <li>{@code RangeValidator.range} if both min and max are not {@code null}</li>
 * <li>{@code RangeValidator.minimum} if max is {@code null}</li>
 * <li>{@code RangeValidator.maximum} if min is {@code null}</li>
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
 * <li>{@code minimum}: the minimum allowed value</li>
 * <li>{@code maximum}: the maximum allowed value</li>
 * </ul>
 * </p>
 * 
 * @param <Z>
 *            type of validatable
 * 
 * @author igor
 */
public class RangeValidator<Z extends Comparable<Z> & Serializable> extends
	AbstractRangeValidator<Z, Z>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param minimum
	 * @param maximum
	 * @return a {@link RangeValidator} that validates if a value falls within a range
	 */
	public static <T extends Comparable<T> & Serializable> RangeValidator<T> range(T minimum,
		T maximum)
	{
		return new RangeValidator<T>(minimum, maximum);
	}

	/**
	 * @param minimum
	 * @return a {@link RangeValidator} that validates if a value is a least {@code minimum}
	 */
	public static <T extends Comparable<T> & Serializable> RangeValidator<T> minimum(T minimum)
	{
		return new RangeValidator<T>(minimum, null);
	}

	/**
	 * @param maximum
	 * @return a {@link RangeValidator} that validates if a value is a most {@code maximum}
	 */
	public static <T extends Comparable<T> & Serializable> RangeValidator<T> maximum(T maximum)
	{
		return new RangeValidator<T>(null, maximum);
	}

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
		setRange(minimum, maximum);
	}

	/**
	 * Constructor used for subclasses who want to set the range using
	 * {@link #setRange(Comparable, Comparable)}
	 */
	protected RangeValidator()
	{
	}

	@Override
	protected Z getValue(IValidatable<Z> validatable)
	{
		return validatable.getValue();
	}

	@Override
	protected IValidationError decorate(IValidationError error, IValidatable<Z> validatable)
	{
		// TODO wicket 7: remove deprecated keys
		error = super.decorate(error, validatable);

		if (error instanceof ValidationError)
		{
			ValidationError ve = (ValidationError) error;
			switch (getMode())
			{
				case MINIMUM :
					ve.addKey("MinimumValidator");
					break;
				case MAXIMUM :
					ve.addKey("MaximumValidator");
					break;
			}
		}
		return error;
	}
}