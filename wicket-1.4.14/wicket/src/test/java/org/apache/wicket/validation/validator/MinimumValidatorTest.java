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

import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;

/**
 * @author jcompagner
 */
public class MinimumValidatorTest extends TestCase
{


	/**
	 * @throws Exception
	 */
	public void testDoubleMinimum() throws Exception
	{
		IValidator<Double> validator = new MinimumValidator<Double>(1.8);

		Validatable<Double> validatable = new Validatable<Double>(new Double(-100.8));
		validator.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable<Double>(new Double(1));
		validator.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable<Double>(new Double(1.8));
		validator.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable<Double>(new Double(2));
		validator.validate(validatable);
		assertEquals(0, validatable.getErrors().size());
	}


	/**
	 * @throws Exception
	 */
	public void testIntegerMinimum() throws Exception
	{
		IValidator<Integer> validator = new MinimumValidator<Integer>(8);

		Validatable<Integer> validatable = new Validatable<Integer>(new Integer(-100));
		validator.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable<Integer>(new Integer(1));
		validator.validate(validatable);
		assertEquals(1, validatable.getErrors().size());

		validatable = new Validatable<Integer>(new Integer(8));
		validator.validate(validatable);
		assertEquals(0, validatable.getErrors().size());

		validatable = new Validatable<Integer>(new Integer(9));
		validator.validate(validatable);
		assertEquals(0, validatable.getErrors().size());
	}


}
