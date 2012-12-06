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

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking if length of a string falls within [min,max] range.
 * 
 * If either min or max are {@code null} they are not checked.
 * 
 * <p>
 * If the component is attached to an {@code input} tag, a {@code maxlen} attribute will be added if
 * the maximum is set.
 * 
 * *
 * <p>
 * Resource keys:
 * <ul>
 * <li>{@code StringValidator.exact} if min==max ({@link #exactLength(int)})</li>
 * <li>{@code StringValidator.range} if both min and max are not {@code null}</li>
 * <li>{@code StringValidator.minimum} if max is {@code null} ({@link #minimumLength(int)})</li>
 * <li>{@code StringValidator.maximum} if min is {@code null} ({@link #maximumLength(int)})</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Error Message Variables:
 * <ul>
 * <li>{@code name}: the id of {@code Component} that failed</li>
 * <li>{@code label}: the label of the {@code Component} (either comes from
 * {@code FormComponent.labelModel} or resource key {@code <form-id>.<form-component-id>}</li>
 * <li>{@code input}: the input value</li>
 * <li>{@code length}: the length of the entered</li>
 * <li>{@code minimum}: the minimum alloed length</li>
 * <li>{@code maximum}: the maximum allowed length</li>
 * </ul>
 * </p>
 * 
 * @author igor
 */
public class StringValidator extends AbstractRangeValidator<Integer, String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that sets the minimum and maximum length values.
	 * 
	 * @param minimum
	 *            the minimum lenghh
	 * @param maximum
	 *            the maximum length
	 */
	public StringValidator(Integer minimum, Integer maximum)
	{
		setRange(minimum, maximum);
	}

	/**
	 * Constructor used for subclasses who want to set the range using
	 * {@link #setRange(Comparable, Comparable)}
	 */
	protected StringValidator()
	{
	}

	@Override
	protected Integer getValue(IValidatable<String> validatable)
	{
		return validatable.getValue().length();
	}

	@Override
	protected ValidationError decorate(ValidationError error, IValidatable<String> validatable)
	{
		error = super.decorate(error, validatable);
		error.setVariable("length", validatable.getValue().length());
		return error;
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);
		if (getMaximum() != null && "input".equalsIgnoreCase(tag.getName()))
		{
			tag.put("maxlength", getMaximum());
		}
	}

	/**
	 * @param length
	 * @return a {@link StringValidator} that generates an error if a string is not of an exact
	 *         length
	 */
	public static StringValidator exactLength(int length)
	{
		return new StringValidator(length, length);
	}

	/**
	 * @param length
	 * @return a {@link StringValidator} that generates an error if a string exceeds a maximum
	 *         length
	 */
	public static StringValidator maximumLength(int length)
	{
		return new StringValidator(null, length);
	}

	/**
	 * @param length
	 * @return a {@link StringValidator} that generates an error if a string is not of a minimum
	 *         length
	 */
	public static StringValidator minimumLength(int length)
	{
		return new StringValidator(length, null);
	}

	/**
	 * @param minimum
	 * @param maximum
	 * @return a {@link StringValidator} that generates an error if the length of a string is not
	 *         between (inclusive) minimum and maximum
	 */
	public static StringValidator lengthBetween(int minimum, int maximum)
	{
		return new StringValidator(minimum, maximum);
	}

}