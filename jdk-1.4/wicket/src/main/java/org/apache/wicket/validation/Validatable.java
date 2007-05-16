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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This implementation of {@link IValidatable} is meant to be used outside of
 * wicket. It allows other parts of the application to utilize
 * {@link IValidator}s to validate values.
 * 
 * Example: <code>
 * class WebService {
 *   public void addUser(String firstName, String lastName) {
 *     Validatable standin=new Validatable();
 *     standin.setValue(firstName);
 *     new FirstNameValidator().validate(standin);
 *     standing.setValue(lastName);
 *     new LastNameValidator().validate(standin);
 *     if (!standin.isValid()) {
 *       // roll your own ValidationException
 *       throw new ValidationException(standin.getErrors());
 *     } else {
 *         // add user here
 *     }
 *   }
 * }
 * </code>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class Validatable implements IValidatable
{

	private Object value;
	private ArrayList errors;

	/**
	 * Constructor
	 */
	public Validatable()
	{
	}

	/**
	 * Sets the value object that will be returned by {@link #getValue()}
	 * 
	 * @param value
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}

	/**
	 * @see org.apache.wicket.validation.IValidatable#getValue()
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * @see org.apache.wicket.validation.IValidatable#error(org.apache.wicket.validation.IValidationError)
	 */
	public void error(IValidationError error)
	{
		if (errors == null)
		{
			errors = new ArrayList();
		}
		errors.add(error);
	}

	/**
	 * @return umodifiable list of any errors reported against this validatable
	 *         instance
	 */
	public List getErrors()
	{
		if (errors == null)
		{
			return Collections.EMPTY_LIST;
		}
		else
		{
			return Collections.unmodifiableList(errors);
		}
	}

	/**
	 * @see org.apache.wicket.validation.IValidatable#isValid()
	 */
	public boolean isValid()
	{
		return errors == null;
	}

}
