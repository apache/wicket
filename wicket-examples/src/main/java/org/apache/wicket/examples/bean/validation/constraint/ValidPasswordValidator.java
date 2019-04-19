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
package org.apache.wicket.examples.bean.validation.constraint;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String>
{
	private static final Pattern CONTENT = Pattern.compile("[0-9a-zA-Z]*");
	private static final Pattern DIGITS = Pattern.compile("(.*\\d.*){2}");

	@Override
	public void initialize(ValidPassword constraintAnnotation)
	{

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		boolean validationResult = true;

		if (value == null)
		{
			validationResult = false; 
		}
		else if (!CONTENT.matcher(value).matches())
		{
			validationResult = false;
		}
		else if (!DIGITS.matcher(value).matches())
		{
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("{password.needDigits}")
				.addConstraintViolation();

			validationResult = false;
		}

		return validationResult;
	}

}
