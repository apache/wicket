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

import org.apache.wicket.validation.Validatable;

/**
 * @author jcompagner
 */
public class NumberValidatorTest extends TestCase
{
	/**
	 * @throws Exception
	 */
	public void testNegative() throws Exception
	{
		Validatable validatable = new Validatable(new Integer(-1));
		NumberValidator.NEGATIVE.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(0));
		NumberValidator.NEGATIVE.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Integer(1));
		NumberValidator.NEGATIVE.validate(validatable);
		assertEquals(1, validatable.getErrors().size());
	}

	/**
	 * @throws Exception
	 */
	public void testPositive() throws Exception
	{
		Validatable validatable = new Validatable(new Integer(-1));
		NumberValidator.POSITIVE.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Integer(0));
		NumberValidator.POSITIVE.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Integer(1));
		NumberValidator.POSITIVE.validate(validatable);
		assertEquals(0, validatable.getErrors().size());
	}

	/**
	 * @throws Exception
	 */
	public void testDoubleRange() throws Exception
	{
		NumberValidator range = NumberValidator.range(1.1, 1.8);

		Validatable validatable = new Validatable(new Double(1));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Double(1.1));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(1.5));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(1.8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(2));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());
	}


	/**
	 * @throws Exception
	 */
	public void testDoubleMaximum() throws Exception
	{
		NumberValidator range = NumberValidator.maximum(1.8);

		Validatable validatable = new Validatable(new Double(-100.8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(1));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(1.8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(2));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());
	}

	/**
	 * @throws Exception
	 */
	public void testDoubleMinimum() throws Exception
	{
		NumberValidator range = NumberValidator.minimum(1.8);

		Validatable validatable = new Validatable(new Double(-100.8));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Double(1));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Double(1.8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Double(2));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());
	}

	/**
	 * @throws Exception
	 */
	public void testIntegerMaximum() throws Exception
	{
		NumberValidator range = NumberValidator.maximum(8);

		Validatable validatable = new Validatable(new Integer(-100));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(1));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(9));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());
	}

	/**
	 * @throws Exception
	 */
	public void testIntegerMinimum() throws Exception
	{
		NumberValidator range = NumberValidator.minimum(8);

		Validatable validatable = new Validatable(new Integer(-100));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Integer(1));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Integer(8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(9));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());
	}

	/**
	 * @throws Exception
	 */
	public void testIntegerRange() throws Exception
	{
		NumberValidator range = NumberValidator.range(1, 8);

		Validatable validatable = new Validatable(new Integer(0));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable(new Integer(1));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(5));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(8));
		range.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable(new Integer(9));
		range.validate(validatable);
		assertEquals(1, validatable.getErrors().size());
	}
}
