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
package org.apache.wicket.validation;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.lang.Args;

/**
 * Adapts {@link IValidator} to Behavior
 * 
 * @author igor
 * @param <T>
 *            type of validator's validatable
 */
public class ValidatorAdapter<T> extends Behavior implements IValidator<T>
{
	private static final long serialVersionUID = 1L;

	private final IValidator<T> validator;

	/**
	 * Constructor
	 * 
	 * @param validator
	 *            validator to be adapted
	 */
	public ValidatorAdapter(IValidator<T> validator)
	{
		Args.notNull(validator, "validator");

		this.validator = validator;
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IValidatable<T> validatable)
	{
		validator.validate(validatable);
	}

	/**
	 * Gets adapted validator
	 * 
	 * @return validator
	 */
	public IValidator<T> getValidator()
	{
		return validator;
	}
}
