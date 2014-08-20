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
package org.apache.wicket.bean.validation.customconstraint;

import java.util.regex.Matcher;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.wicket.util.parse.metapattern.MetaPattern;
import org.apache.wicket.util.string.Strings;

public class PasswordValidator implements ConstraintValidator<PasswordConstraintAnnotation, String>
{

	@Override
	public void initialize(PasswordConstraintAnnotation constraintAnnotation)
	{
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		// password must be at least 8 chars long.
		if (Strings.isEmpty(value) || value.length() < 8)
		{
			return false;
		}

		Matcher matcher = MetaPattern.NON_WORD.matcher(value);
		
		// password must not contain non-word characters.
		if (matcher.find())
		{
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("{" +
				PasswordConstraintAnnotation.CUSTOM_BUNDLE_KEY + "}").addConstraintViolation();
			return false;
		}

		return true;
	}

}
