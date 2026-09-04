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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests a couple of valid and invalid email patterns.
 * 
 * @author Maurice Marrink
 */
class EmailValidatorTest
{
	/**
	 * Tests a couple of emails that should be valid.
	 */
	@Test
	void testValidEmails()
	{
		EmailAddressValidator test = new EmailAddressValidator();
		String[] emails = new String[] { "b.blaat@topicus.nl", "blaat@hotmail.com",
				"1.2.3.4@5.6.7.nl", "m@m.nl", "M@M.NL" };
		for (String email : emails)
		{
			assertTrue(test.getPattern().matcher(email).matches(), email + " should be valid");
		}
	}

	/**
	 * Tests a couple of emails that should not be valid.
	 */
	@Test
	void testInvalidEmails()
	{
		EmailAddressValidator test = new EmailAddressValidator();
		String[] emails = new String[] { ".blaat@topicus.nl", "blaat.@hotmail.com", "blaat@nl",
				"blaat@.nl" };
		for (String email : emails)
		{
			assertFalse(test.getPattern().matcher(email).matches(), email + " should not be valid");
		}
	}
}
