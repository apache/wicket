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
 * Validator for checking that the value is not greater then a specified maximum value
 * 
 * @param <Z>
 *            type of validatable
 */
public class MaximumValidator<Z extends Comparable<Z> & Serializable> implements IValidator<Z>
{
	private static final long serialVersionUID = 1L;
	private final Z maximum;

	/**
	 * Constructor
	 * 
	 * @param maximum
	 *            the maximum value
	 */
	public MaximumValidator(Z maximum)
	{
		this.maximum = maximum;

	}

	/**
	 * {@inheritDoc}
	 */
	public void validate(IValidatable<Z> validatable)
	{
		Z value = validatable.getValue();
		if (value.compareTo(maximum) > 0)
		{
			ValidationError error = new ValidationError();
			error.addMessageKey("MaximumValidator");
			error.setVariable("maximum", maximum);
			validatable.error(error);
		}
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