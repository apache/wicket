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