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

import org.apache.wicket.model.IModel;

/**
 * This implementation of {@link IValidatable} is meant to be used outside of Wicket. It allows
 * other parts of the application to utilize {@link IValidator}s for validation.
 * <p>
 * Example: <code><pre>
 * class WebService
 * {
 * 	public void addUser(String firstName, String lastName)
 * 	{
 * 		Validatable standin = new Validatable();
 * 		standin.setValue(firstName);
 * 		new FirstNameValidator().validate(standin);
 * 		standing.setValue(lastName);
 * 		new LastNameValidator().validate(standin);
 * 		if (!standin.isValid())
 * 		{
 * 			// roll your own ValidationException
 * 			throw new ValidationException(standin.getErrors());
 * 		}
 * 		else
 * 		{
 * 			// add user here
 * 		}
 * 	}
 * }
 * </pre></code>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of validatable
 * @since 1.2.6
 */
public class Validatable<T> implements IValidatable<T>
{
	/** the value object */
	private T value;

	/** the list of errors */
	private ArrayList<IValidationError> errors;

	private IModel<T> model;

	/**
	 * Constructor.
	 */
	public Validatable()
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 *            The value that will be tested
	 */
	public Validatable(T value)
	{
		this.value = value;
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public void setModel(IModel<T> model)
	{
		this.model = model;
	}

	/**
	 * Sets the value object that will be returned by {@link #getValue()}.
	 * 
	 * @param value
	 *            the value object
	 */
	public void setValue(T value)
	{
		this.value = value;
	}

	/**
	 * @see IValidatable#getValue()
	 */
	@Override
	public T getValue()
	{
		return value;
	}

	/**
	 * @see IValidatable#error(IValidationError)
	 */
	@Override
	public void error(IValidationError error)
	{
		if (errors == null)
		{
			errors = new ArrayList<IValidationError>();
		}
		errors.add(error);
	}

	/**
	 * Retrieves an unmodifiable list of any errors reported against this <code>IValidatable</code>
	 * instance.
	 * 
	 * @return an unmodifiable list of errors
	 */
	public List<IValidationError> getErrors()
	{
		if (errors == null)
		{
			return Collections.emptyList();
		}
		else
		{
			return Collections.unmodifiableList(errors);
		}
	}

	/**
	 * @see IValidatable#isValid()
	 */
	@Override
	public boolean isValid()
	{
		return errors == null;
	}

	@Override
	public IModel<T> getModel()
	{
		return model;
	}

}
