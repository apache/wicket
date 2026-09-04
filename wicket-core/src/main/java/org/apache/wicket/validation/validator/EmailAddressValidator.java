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

import java.util.regex.Pattern;

/**
 * Validator for checking the form/pattern of email addresses.
 * 
 * NOTICE: This validator only checks the most commonly used email address patterns. For a validator
 * that can check the entire range of rfc compliant email addresses see
 * <code>org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator</code>
 *
 * <p>Also see org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator</p>
 *
 * @author Chris Turner
 * @author Jonathan Locke
 * @author Martijn Dashorst
 * @author Al Maw
 *
 *
 * @since 1.3
 */
public class EmailAddressValidator extends PatternValidator
{
	private static final long serialVersionUID = 1L;

	/** singleton instance */
	private static final EmailAddressValidator INSTANCE = new EmailAddressValidator();

	/**
	 * Retrieves the singleton instance of <code>EmailAddressValidator</code>.
	 * 
	 * @return the singleton instance of <code>EmailAddressValidator</code>
	 */
	public static EmailAddressValidator getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Protected constructor to force use of static singleton accessor. Override this constructor to
	 * implement resourceKey(Component).
	 */
	protected EmailAddressValidator()
	{
		super(
			"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)",
			Pattern.CASE_INSENSITIVE);
	}
}
