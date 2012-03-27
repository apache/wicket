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

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Base class for validators that check if a given value falls within [min,max] range.
 * 
 * If either min or max are {@code null} they are not checked.
 * 
 * <p>
 * Resource keys:
 * <ul>
 * <li>{@code <class.simpleName>.exact} if min==max</li>
 * <li>{@code <class.simpleName>.range} if both min and max are not {@code null}</li>
 * <li>{@code <class.simpleName>.minimum} if max is {@code null}</li>
 * <li>{@code <class.simpleName>.maximum} if min is {@code null}</li>
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
 * @param <R>
 *            type of range value
 * @param <V>
 *            type of validatable
 * 
 * @author igor
 */
public abstract class AbstractRangeValidator<R extends Comparable<R> & Serializable, V extends Serializable>
	extends Behavior implements IValidator<V>
{
	private static final long serialVersionUID = 1L;
	private R minimum;
	private R maximum;

	/**
	 * Constructor that sets the minimum and maximum values.
	 * 
	 * @param minimum
	 *            the minimum value
	 * @param maximum
	 *            the maximum value
	 */
	public AbstractRangeValidator(R minimum, R maximum)
	{
		setRange(minimum, maximum);
	}

	/**
	 * Constructor used for subclasses who want to set the range using
	 * {@link #setRange(Comparable, Comparable)}
	 */
	protected AbstractRangeValidator()
	{
	}

	/**
	 * Sets validator range
	 * 
	 * @param minimum
	 * @param maximum
	 */
	protected final void setRange(R minimum, R maximum)
	{
		if (minimum == null && maximum == null)
		{
			throw new IllegalArgumentException("Both minimum and maximum values cannot be null");
		}
		this.minimum = minimum;
		this.maximum = maximum;
	}

	@Override
	public void validate(IValidatable<V> validatable)
	{
		R value = getValue(validatable);
		final R min = getMinimum();
		final R max = getMaximum();
		if ((min != null && value.compareTo(min) < 0) || (max != null && value.compareTo(max) > 0))
		{
			ValidationError error = new ValidationError(this, getMode().getVariation());
			error.setVariable("minimum", min);
			error.setVariable("maximum", max);
			validatable.error(decorate(error, validatable));
		}
	}

	/**
	 * Gets the value that should be validated against the range
	 * 
	 * @param validatable
	 * @return value to validate
	 */
	protected abstract R getValue(IValidatable<V> validatable);

	/**
	 * Gets the minimum value.
	 * 
	 * @return minimum value
	 */
	public R getMinimum()
	{
		return minimum;
	}

	/**
	 * Gets the maximum value.
	 * 
	 * @return maximum value
	 */
	public R getMaximum()
	{
		return maximum;
	}

	/**
	 * Allows subclasses to decorate reported errors
	 * 
	 * @param error
	 * @param validatable
	 * @return decorated error
	 */
	protected ValidationError decorate(ValidationError error, IValidatable<V> validatable)
	{
		return error;
	}

	/**
	 * Gets validation mode which is determined by whether min, max, or both values are provided
	 * 
	 * @return validation mode
	 */
	public final Mode getMode()
	{
		final R min = getMinimum();
		final R max = getMaximum();

		if (min == null && max != null)
		{
			return Mode.MAXIMUM;
		}
		else if (max == null && min != null)
		{
			return Mode.MINIMUM;
		}
		else if ((min == null && max == null) || max.equals(min))
		{
			return Mode.EXACT;
		}
		else
		{
			return Mode.RANGE;
		}
	}

	/**
	 * Validator mode
	 * 
	 * @author igor
	 */
	public static enum Mode {
		MINIMUM, MAXIMUM, RANGE, EXACT;

		public String getVariation()
		{
			return name().toLowerCase();
		}
	}

}