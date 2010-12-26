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

import java.util.Map;

import org.apache.wicket.validation.IValidatable;


/**
 * Validator for checking <code>String</code> lengths. Usually this validator is used through the
 * static factory methods, but it and its inner classes can also be subclassed directly.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public abstract class StringValidator extends AbstractValidator<String>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Validator for checking if the length of a <code>String</code> is exactly the specified
	 * length.
	 */
	public static class ExactLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int length;

		/**
		 * Constructor.
		 * 
		 * @param length
		 *            the length value
		 */
		public ExactLengthValidator(int length)
		{
			this.length = length;
		}

		/**
		 * Retrieves the length value.
		 * 
		 * @return the length value
		 */
		public final int getLength()
		{
			return length;
		}

		/**
		 * see AbstractValidator#onValidate(IValidatable)
		 */
		@Override
		protected void onValidate(IValidatable<String> validatable)
		{
			if ((validatable.getValue()).length() != length)
			{
				error(validatable);
			}
		}

		/**
		 * @see AbstractValidator#resourceKey()
		 */
		@Override
		protected String resourceKey()
		{
			return "StringValidator.exact";
		}

		/**
		 * @see AbstractValidator#variablesMap(IValidatable)
		 */
		@Override
		protected Map<String, Object> variablesMap(IValidatable<String> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("length", (validatable.getValue() != null) ? (validatable.getValue()).length() : 0);
			map.put("exact", length);
			return map;
		}

	}

	/**
	 * Validator for checking if the length of a <code>String</code> is within the specified range.
	 */
	public static class LengthBetweenValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int maximum;
		private final int minimum;

		/**
		 * Constructor that sets the minimum and maximum values.
		 * 
		 * @param minimum
		 *            the minimum value
		 * @param maximum
		 *            the maximum value
		 */
		public LengthBetweenValidator(int minimum, int maximum)
		{
			this.minimum = minimum;
			this.maximum = maximum;

		}

		/**
		 * Retrieves the maximum value.
		 * 
		 * @return the maximum value
		 */
		public final int getMaximum()
		{
			return maximum;
		}

		/**
		 * Retrieves the minimum value.
		 * 
		 * @return the minimum value
		 */
		public final int getMinimum()
		{
			return minimum;
		}

		/**
		 * see AbstractValidator#onValidate(IValidatable)
		 */
		@Override
		protected void onValidate(IValidatable<String> validatable)
		{
			final String value = validatable.getValue();
			if (value.length() < minimum || value.length() > maximum)
			{
				error(validatable);
			}

		}

		/**
		 * @see AbstractValidator#resourceKey()
		 */
		@Override
		protected String resourceKey()
		{
			return "StringValidator.range";
		}

		/**
		 * @see AbstractValidator#variablesMap(IValidatable)
		 */
		@Override
		protected Map<String, Object> variablesMap(IValidatable<String> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("minimum", minimum);
			map.put("maximum", maximum);
			map.put("length", (validatable.getValue()).length());
			return map;
		}

	}

	/**
	 * Validator for checking if the length of a <code>String</code> meets the maximum length
	 * requirement.
	 */
	public static class MaximumLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int maximum;

		/**
		 * Constructor that sets a maximum length value.
		 * 
		 * @param maximum
		 *            the maximum length value
		 */
		public MaximumLengthValidator(int maximum)
		{
			this.maximum = maximum;
		}

		/**
		 * Retrieves the maximum length value.
		 * 
		 * @return the maximum length value
		 */
		public final int getMaximum()
		{
			return maximum;
		}

		/**
		 * see AbstractValidator#onValidate(IValidatable)
		 */
		@Override
		protected void onValidate(IValidatable<String> validatable)
		{
			if ((validatable.getValue()).length() > maximum)
			{
				error(validatable);
			}
		}

		/**
		 * @see AbstractValidator#resourceKey()
		 */
		@Override
		protected String resourceKey()
		{
			return "StringValidator.maximum";
		}

		/**
		 * @see AbstractValidator#variablesMap(IValidatable)
		 */
		@Override
		protected Map<String, Object> variablesMap(IValidatable<String> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("maximum", maximum);
			map.put("length", (validatable.getValue()).length());
			return map;
		}
	}

	/**
	 * Validator for checking if the length of a <code>String</code> meets the minimum length
	 * requirement.
	 */
	public static class MinimumLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int minimum;

		/**
		 * Constructor that sets a minimum length value.
		 * 
		 * @param minimum
		 *            the minimum length value
		 */
		public MinimumLengthValidator(int minimum)
		{
			this.minimum = minimum;
		}

		/**
		 * Retrieves the minimum length value.
		 * 
		 * @return the minimum length value
		 */
		public final int getMinimum()
		{
			return minimum;
		}

		/**
		 * see AbstractValidator#onValidate(IValidatable)
		 */
		@Override
		protected void onValidate(IValidatable<String> validatable)
		{
			if ((validatable.getValue()).length() < minimum)
			{
				error(validatable);
			}
		}

		/**
		 * @see AbstractValidator#resourceKey()
		 */
		@Override
		protected String resourceKey()
		{
			return "StringValidator.minimum";
		}

		/**
		 * @see AbstractValidator#variablesMap(IValidatable)
		 */
		@Override
		protected Map<String, Object> variablesMap(IValidatable<String> validatable)
		{
			final Map<String, Object> map = super.variablesMap(validatable);
			map.put("minimum", minimum);
			map.put("length", (validatable.getValue()).length());
			return map;
		}

	}

	/**
	 * Gets a <code>String</code> exact length validator for checking if a string length is exactly
	 * the same as the given length value. If that is not the case, then an error message will be
	 * generated with the key "StringValidator.exact". The message keys that can be used are:
	 * <p>
	 * <ul>
	 * <li>${exact}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param length
	 *            the required length of the string
	 * 
	 * @return the requested <code>StringValidator</code>
	 */
	public static StringValidator exactLength(int length)
	{
		return new ExactLengthValidator(length);
	}

	/**
	 * Gets a <code>String</code> range validator for checking if a string length falls between the
	 * minimum and and maximum lengths. If that is not the case, then an error message will be
	 * generated with the key "StringValidator.range". The message keys that can be used are:
	 * <p>
	 * <ul>
	 * <li>${minimum}: the minimum length</li>
	 * <li>${maximum}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            the minimum length of the string
	 * @param maximum
	 *            the maximum length of the string
	 * 
	 * @return the requested <code>StringValidator</code>
	 */
	public static StringValidator lengthBetween(int minimum, int maximum)
	{
		return new LengthBetweenValidator(minimum, maximum);
	}

	/**
	 * Gets a <code>String</code> maximum validator for checking if a string length is smaller than
	 * the given maximum value. If that is not the case, then an error message will be generated
	 * with the key "StringValidator.maximum". The message keys that can be used are:
	 * <p>
	 * <ul>
	 * <li>${maximum}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param maximum
	 *            the maximum length of the string
	 * 
	 * @return the requested <code>StringValidator</code>
	 */
	public static StringValidator maximumLength(int maximum)
	{
		return new MaximumLengthValidator(maximum);
	}

	/**
	 * Gets a <code>String</code> minimum validator for checking if a string length is greater than
	 * the given minimum value. If that is not the case, then an error message will be generated
	 * with the key "StringValidator.minimum". The message keys that can be used are:
	 * <p>
	 * <ul>
	 * <li>${minimum}: the minimum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user gave</li>
	 * <li>${name}: the name of the <code>Component</code> that failed</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            the minimum length of the string
	 * 
	 * @return the requested <code>StringValidator</code>
	 */
	public static StringValidator minimumLength(int minimum)
	{
		return new MinimumLengthValidator(minimum);
	}
}
