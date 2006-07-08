/*
 * $Id: StringValidator.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 22:39:45 +0000 (Thu, 25 May
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.validation;

import java.io.Serializable;
import java.util.Map;

import wicket.markup.html.form.FormComponent;
import wicket.util.string.Strings;

/**
 * A validator for strings that can be used for subclassing or use one of the
 * static factory methods to get the default string validators as range, maximum
 * or minimum.
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
	 * Gets a String exact length validator to check if a string length is exactly the same as the given value
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
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public void validate(final FormComponent formComponent)
	{
		onValidate(formComponent, (String)formComponent.getConvertedInput());
	}

	/**
	 * Subclasses should override this method to validate the string value for a
	 * component.
	 * 
	 * @param formComponent
	 *            form component
	 * @param value
	 *            The string value to validate
	 */
	public abstract void onValidate(FormComponent formComponent, String value);

	private static class LengthBetweenValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int minimum;
		private final int maximum;

		private LengthBetweenValidator(int minimum, int maximum)
		{
			this.minimum = minimum;
			this.maximum = maximum;

		}

		/**
		 * @see wicket.markup.html.form.validation.StringValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      java.lang.String)
		 */
		@Override
		public void onValidate(FormComponent formComponent, String value)
		{
			if (!Strings.isEmpty(value))
			{
				if (value.length() < minimum || value.length() > maximum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("minimum", new Integer(minimum));
			map.put("maximum", new Integer(maximum));
			map.put("length", new Integer(((String)formComponent.getConvertedInput()).length()));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "StringValidator.range";
		}

	}

	private static class MinimumLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int minimum;

		private MinimumLengthValidator(int minimum)
		{
			this.minimum = minimum;
		}

		/**
		 * @see wicket.markup.html.form.validation.StringValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      java.lang.String)
		 */
		@Override
		public void onValidate(FormComponent formComponent, String value)
		{
			if (!Strings.isEmpty(value))
			{
				if (value.length() < minimum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("minimum", new Integer(minimum));
			map.put("length", new Integer(((String)formComponent.getConvertedInput()).length()));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "StringValidator.minimum";
		}

	}

	private static class ExactLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int length;

		private ExactLengthValidator(int length)
		{
			this.length = length;
		}

		/**
		 * @see wicket.markup.html.form.validation.StringValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      java.lang.String)
		 */
		@Override
		public void onValidate(FormComponent formComponent, String value)
		{
			if (!Strings.isEmpty(value))
			{
				if (value.length() != length)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("length", new Integer(((String)formComponent.getConvertedInput()).length()));
			map.put("exact", this.length);
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "StringValidator.exact";
		}

	}
	
	private static class MaximumLengthValidator extends StringValidator
	{
		private static final long serialVersionUID = 1L;
		private final int maximum;

		private MaximumLengthValidator(int maximum)
		{
			this.maximum = maximum;
		}

		/**
		 * @see wicket.markup.html.form.validation.StringValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      java.lang.String)
		 */
		@Override
		public void onValidate(FormComponent formComponent, String value)
		{
			if (!Strings.isEmpty(value))
			{
				if (value.length() > maximum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("maximum", new Integer(maximum));
			map.put("length", new Integer(((String)formComponent.getConvertedInput()).length()));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "StringValidator.maximum";
		}
	}
}