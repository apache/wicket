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
 * Validator for dealing with string lengths. Usually this validator is used
 * through the static factory methods, but it and its inner classes can also be
 * subclassed directly.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class StringValidator extends AbstractValidator
{

	/**
	 * Gets a String range validator to check if a string length is between min
	 * and max.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "StringValidator.range" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: the minimum length</li>
	 * <li>${maximum}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum length of the string.
	 * @param maximum
	 *            The maximum length of the string.
	 * 
	 * @return The StringValidator
	 */
	public static StringValidator lengthBetween(int minimum, int maximum)
	{
		return new LengthBetweenValidator(minimum, maximum);
	}

	/**
	 * Gets a String minimum validator to check if a string length is greater
	 * then the given minimum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "StringValidator.minimum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: the minimum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum length of the string.
	 * 
	 * @return The StringValidator
	 */
	public static StringValidator minimumLength(int minimum)
	{
		return new MinimumLengthValidator(minimum);
	}

	/**
	 * Gets a String maximum validator to check if a string length is smaller
	 * then the given maximum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "StringValidator.maximum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${maximum}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param maximum
	 *            The maximum length of the string.
	 * 
	 * @return The StringValidator
	 */
	public static StringValidator maximumLength(int maximum)
	{
		return new MaximumLengthValidator(maximum);
	}

	/**
	 * Gets a String exact length validator to check if a string length is
	 * exactly the same as the given value
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "StringValidator.exact" and the messages keys that can be used are:
	 * <ul>
	 * <li>${exact}: the maximum length</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param length
	 *            The required length of the string.
	 * 
	 * @return The StringValidator
	 */
	public static StringValidator exactLength(int length)
	{
		return new ExactLengthValidator(length);
	}

	/**
	 * Validator to check if the length of the string is within some range
	 */
	public static class LengthBetweenValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int minimum;
		private final int maximum;

		/**
		 * Construct.
		 * 
		 * @param minimum
		 * @param maximum
		 */
		public LengthBetweenValidator(int minimum, int maximum)
		{
			this.minimum = minimum;
			this.maximum = maximum;

		}

		/**
		 * @see org.apache.wicket.validation.validator.AbstractValidator#variablesMap(org.apache.wicket.validation.IValidatable)
		 */
		protected Map variablesMap(IValidatable validatable)
		{
			final Map map = super.variablesMap(validatable);
			map.put("minimum", new Integer(minimum));
			map.put("maximum", new Integer(maximum));
			map.put("length", new Integer(((String)validatable.getValue()).length()));
			return map;
		}

		/**
		 * @see org.apache.wicket.markup.html.form.validation.AbstractValidator#resourceKey(org.apache.wicket.markup.html.form.FormComponent)
		 */
		protected String resourceKey()
		{
			return "StringValidator.range";
		}

		protected void onValidate(IValidatable validatable)
		{
			final String value = (String)validatable.getValue();
			if (value.length() < minimum || value.length() > maximum)
			{
				error(validatable);
			}

		}

	}

	/**
	 * Validator to check if the length of the string meets a minumum
	 * requirement
	 */
	public static class MinimumLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int minimum;

		/**
		 * Construct.
		 * 
		 * @param minimum
		 */
		public MinimumLengthValidator(int minimum)
		{
			this.minimum = minimum;
		}

		/**
		 * @see org.apache.wicket.validation.validator.AbstractValidator#variablesMap(org.apache.wicket.validation.IValidatable)
		 */
		protected Map variablesMap(IValidatable validatable)
		{
			final Map map = super.variablesMap(validatable);
			map.put("minimum", new Integer(minimum));
			map.put("length", new Integer(((String)validatable.getValue()).length()));
			return map;
		}

		/**
		 * @see org.apache.wicket.markup.html.form.validation.AbstractValidator#resourceKey(org.apache.wicket.markup.html.form.FormComponent)
		 */
		protected String resourceKey()
		{
			return "StringValidator.minimum";
		}

		protected void onValidate(IValidatable validatable)
		{
			if (((String)validatable.getValue()).length() < minimum)
			{
				error(validatable);
			}
		}

	}

	/**
	 * Validator to check if the length of the string is exactly the specified
	 * length
	 */
	public static class ExactLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int length;

		/**
		 * Construct.
		 * 
		 * @param length
		 */
		public ExactLengthValidator(int length)
		{
			this.length = length;
		}

		protected Map variablesMap(IValidatable validatable)
		{
			final Map map = super.variablesMap(validatable);
			map.put("length", new Integer(((String)validatable.getValue()).length()));
			map.put("exact", new Integer(this.length));
			return map;
		}

		/**
		 * @see org.apache.wicket.markup.html.form.validation.AbstractValidator#resourceKey(org.apache.wicket.markup.html.form.FormComponent)
		 */
		protected String resourceKey()
		{
			return "StringValidator.exact";
		}

		protected void onValidate(IValidatable validatable)
		{
			if (((String)validatable.getValue()).length() != length)
			{
				error(validatable);
			}
		}

	}

	/**
	 * Validator to check if the length of the string meets a maximum
	 * requirement
	 */
	public static class MaximumLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int maximum;

		/**
		 * Construct.
		 * 
		 * @param maximum
		 */
		public MaximumLengthValidator(int maximum)
		{
			this.maximum = maximum;
		}

		/**
		 * @see org.apache.wicket.validation.validator.AbstractValidator#variablesMap(org.apache.wicket.validation.IValidatable)
		 */
		protected Map variablesMap(IValidatable validatable)
		{
			final Map map = super.variablesMap(validatable);
			map.put("maximum", new Integer(maximum));
			map.put("length", new Integer(((String)validatable.getValue()).length()));
			return map;
		}

		/**
		 * @see org.apache.wicket.markup.html.form.validation.AbstractValidator#resourceKey(org.apache.wicket.markup.html.form.FormComponent)
		 */
		protected String resourceKey()
		{
			return "StringValidator.maximum";
		}

		protected void onValidate(IValidatable validatable)
		{
			if (((String)validatable.getValue()).length() > maximum)
			{
				error(validatable);
			}
		}
	}
}
