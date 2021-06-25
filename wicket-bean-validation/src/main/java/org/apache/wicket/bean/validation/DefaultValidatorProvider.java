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
package org.apache.wicket.bean.validation;

import java.util.function.Supplier;

import jakarta.validation.Configuration;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;


/**
 * This is the default validator provider. It creates a validator instance with the default message
 * interpolator wrapped inside a {@link SessionLocaleInterpolator} so it is aware of Wicket's
 * locale. Only one instance of the {@link Validator} is created.
 * 
 * @author igor
 * 
 */
public class DefaultValidatorProvider implements Supplier<Validator>
{

	private Validator validator;

	@Override
	public Validator get()
	{
		if (validator == null)
		{
			Configuration<?> config = Validation.byDefaultProvider().configure();

			MessageInterpolator interpolator = config.getDefaultMessageInterpolator();
			interpolator = new SessionLocaleInterpolator(interpolator);

			ValidatorFactory factory = config.messageInterpolator(interpolator)
				.buildValidatorFactory();

			validator = factory.getValidator();
		}
		return validator;
	}
}
