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

import junit.framework.TestCase;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.Validatable;

/**
 * Tests a few valid and invalid credit card numbers.
 * 
 * @author Joachim F. Rohde
 */
public class CreditCardValidatorTest extends TestCase
{
	/**
	 * Constructor.
	 */
	public CreditCardValidatorTest()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 */
	public CreditCardValidatorTest(String name)
	{
		super(name);
	}

	/**
	 * Tests a couple of credit card numbers that shouldn't be valid.
	 */
	public void testInvalidCreditCardNumbers()
	{
		// null value
		CreditCardValidator test = new CreditCardValidator();
		IValidatable<String> validatable = new Validatable(null);
		test.onValidate(validatable);
		assertEquals(false, validatable.isValid());

		// too short
		validatable = new Validatable("9845");
		test.onValidate(validatable);
		assertEquals(false, validatable.isValid());

		// too long
		validatable = new Validatable("1234678910111213141516");
		test.onValidate(validatable);
		assertEquals(false, validatable.isValid());

		// contains a char
		validatable = new Validatable("3782822X6310005");
		test.onValidate(validatable);
		assertEquals(false, validatable.isValid());

		// invalid number
		validatable = new Validatable("840898920205250");
		test.onValidate(validatable);
		assertEquals(false, validatable.isValid());
	}

	/**
	 * Tests a couple of credit card numbers that should be valid. Those numbers has been taken from
	 * https://www.paypal.com/en_US/vhelp/paypalmanager_help/credit_card_numbers.htm
	 */
	public void testValidCreditCardNumbers()
	{
		// American Express
		CreditCardValidator test = new CreditCardValidator();
		IValidatable<String> validatable = new Validatable("378282246310005");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// American Express
		validatable = new Validatable("371449635398431");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// American Express Corporate
		validatable = new Validatable("378734493671000");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// American Express Corporate with dashes and spaces (should be filtered
		// and are therefor legal)
		validatable = new Validatable("378 - 7344-9367 1000");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Diners Club
		validatable = new Validatable("30569309025904");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Discover
		validatable = new Validatable("6011111111111117");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Discover
		validatable = new Validatable("6011000990139424");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// JCB
		validatable = new Validatable("3530111333300000");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// JCB
		validatable = new Validatable("3566002020360505");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Mastercard
		validatable = new Validatable("5555555555554444");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Mastercard
		validatable = new Validatable("5105105105105100");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Visa
		validatable = new Validatable("4111111111111111");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Visa
		validatable = new Validatable("4012888888881881");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Visa
		validatable = new Validatable("4222222222222");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());

		// Switch / Solo
		validatable = new Validatable("6331101999990016");
		test.onValidate(validatable);
		assertEquals(true, validatable.isValid());
	}
}
