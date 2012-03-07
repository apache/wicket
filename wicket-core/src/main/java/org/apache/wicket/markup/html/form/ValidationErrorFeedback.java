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
package org.apache.wicket.markup.html.form;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.validation.IValidationError;


/**
 * This class is the parameter to {@link Component#error(Serializable)} instead of the generated
 * error string itself (when {@link FormComponent#error(IValidationError)} is called). The advantage
 * is that a custom feedback panel would still have access to the underlying
 * {@link IValidationError} that generated the error message - providing much more context.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ValidationErrorFeedback implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** error object */
	private final IValidationError error;

	/** error message */
	private final String message;

	/**
	 * Construct.
	 * 
	 * @param error
	 * @param message
	 */
	public ValidationErrorFeedback(final IValidationError error, final String message)
	{
		if (error == null)
		{
			throw new IllegalArgumentException("Argument [[error]] cannot be null");
		}
		this.error = error;
		this.message = message;
	}

	/**
	 * Gets serialVersionUID.
	 * 
	 * @return serialVersionUID
	 */
	public static long getSerialVersionUID()
	{
		return serialVersionUID;
	}

	/**
	 * Gets error.
	 * 
	 * @return error
	 */
	public IValidationError getError()
	{
		return error;
	}

	/**
	 * Gets message.
	 * 
	 * @return message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return message;
	}


}
