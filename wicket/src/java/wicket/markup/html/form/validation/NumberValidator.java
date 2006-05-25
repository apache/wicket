/*
 * $Id$ $Revision$ $Date$
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

/**
 * A validator for numbers. See the static factory methods to make range/minimum
 * or maximum validators for doubles or longs
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 */
public abstract class NumberValidator extends AbstractValidator
{
	/**
	 * A validatior for testing if it is a positive number value
	 */
	public static final NumberValidator POSITIVE = minimum(0);

	/**
	 * A validatior for testing if it is a positive number value
	 */
	public static final NumberValidator NEGATIVE = maximum(0);


	/**
	 * Gets a Integer range validator to check if a number is between the
	 * mininum and maximum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "NumberValidator.range" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: the minimal value</li>
	 * <li>${maximum}: the maximum value</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum value.
	 * @param maximum
	 *            The maximum value.
	 * 
	 * @return The NumberValidator
	 */
	public static NumberValidator range(long minimum, long maximum)
	{
		return new RangeValidator(minimum, maximum);
	}

	/**
	 * Gets a Integer minimum validator to check if a integer value is greater
	 * then the given minimum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "NumberValidator.minimum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: the minimal value</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum value.
	 * 
	 * @return The NumberValidator
	 */
	public static NumberValidator minimum(long minimum)
	{
		return new MinimumValidator(minimum);
	}

	/**
	 * Gets a Integer range validator to check if an integer value is smaller
	 * then the given maximum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "StringValidator.maximum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${maximum}: the maximum value</li>
	 * <li>${length}: the length of the user input</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param maximum
	 *            The maximum value.
	 * 
	 * @return The NumberValidator
	 */
	public static NumberValidator maximum(long maximum)
	{
		return new MaximumValidator(maximum);
	}

	/**
	 * Gets a Double range validator to check if a number is between the mininum
	 * and maximum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "NumberValidator.range" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: the minimal value</li>
	 * <li>${maximum}: the maximum value</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum value.
	 * @param maximum
	 *            The maximum value.
	 * 
	 * @return The NumberValidator
	 */
	public static NumberValidator range(double minimum, double maximum)
	{
		return new DoubleRangeValidator(minimum, maximum);
	}

	/**
	 * Gets a Double minimum validator to check if a integer value is greater
	 * then the given minimum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "NumberValidator.minimum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${minimum}: the minimal value</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param minimum
	 *            The minimum value.
	 * 
	 * @return The NumberValidator
	 */
	public static NumberValidator minimum(double minimum)
	{
		return new DoubleMinimumValidator(minimum);
	}

	/**
	 * Gets a Double range validator to check if an integer value is smaller
	 * then the given maximum value.
	 * 
	 * If that is not the case then an error message will be generated with the
	 * key "StringValidator.maximum" and the messages keys that can be used are:
	 * <ul>
	 * <li>${maximum}: the maximum value</li>
	 * <li>${input}: the input the user did give</li>
	 * <li>${name}: the name of the component that failed</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param maximum
	 *            The maximum value.
	 * 
	 * @return The NumberValidator
	 */
	public static NumberValidator maximum(double maximum)
	{
		return new DoubleMaximumValidator(maximum);
	}

	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public void validate(final FormComponent formComponent)
	{
		onValidate(formComponent, (Number)formComponent.getConvertedInput());
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
	public abstract void onValidate(FormComponent formComponent, Number value);

	private static class RangeValidator extends NumberValidator
	{
		private static final long serialVersionUID = 1L;
		private final long minimum;
		private final long maximum;

		private RangeValidator(long minimum, long maximum)
		{
			this.minimum = minimum;
			this.maximum = maximum;

		}

		/**
		 * @see wicket.markup.html.form.validation.NumberValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      Number)
		 */
		@Override
		public void onValidate(FormComponent formComponent, Number value)
		{
			if (value != null)
			{
				if (value.longValue() < minimum || value.longValue() > maximum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("minimum", new Long(minimum));
			map.put("maximum", new Long(maximum));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "NumberValidator.range";
		}

	}

	private static class MinimumValidator extends NumberValidator
	{
		private static final long serialVersionUID = 1L;
		private final long minimum;

		private MinimumValidator(long minimum)
		{
			this.minimum = minimum;
		}

		/**
		 * @see wicket.markup.html.form.validation.NumberValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      Number)
		 */
		@Override
		public void onValidate(FormComponent formComponent, Number value)
		{
			if (value != null)
			{
				if (value.longValue() < minimum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("minimum", new Long(minimum));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "NumberValidator.minimum";
		}

	}

	private static class MaximumValidator extends NumberValidator
	{
		private static final long serialVersionUID = 1L;
		private final long maximum;

		private MaximumValidator(long maximum)
		{
			this.maximum = maximum;
		}

		/**
		 * @see wicket.markup.html.form.validation.NumberValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      Number)
		 */
		@Override
		public void onValidate(FormComponent formComponent, Number value)
		{
			if (value != null)
			{
				if (value.longValue() > maximum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("maximum", new Long(maximum));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "NumberValidator.maximum";
		}
	}

	private static class DoubleRangeValidator extends NumberValidator
	{
		private static final long serialVersionUID = 1L;
		private final double minimum;
		private final double maximum;

		private DoubleRangeValidator(double minimum, double maximum)
		{
			this.minimum = minimum;
			this.maximum = maximum;

		}

		/**
		 * @see wicket.markup.html.form.validation.NumberValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      Number)
		 */
		@Override
		public void onValidate(FormComponent formComponent, Number value)
		{
			if (value != null)
			{
				if (value.doubleValue() < minimum || value.doubleValue() > maximum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("minimum", new Double(minimum));
			map.put("maximum", new Double(maximum));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "NumberValidator.range";
		}

	}

	private static class DoubleMinimumValidator extends NumberValidator
	{
		private static final long serialVersionUID = 1L;
		private final double minimum;

		private DoubleMinimumValidator(double minimum)
		{
			this.minimum = minimum;
		}

		/**
		 * @see wicket.markup.html.form.validation.NumberValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      Number)
		 */
		@Override
		public void onValidate(FormComponent formComponent, Number value)
		{
			if (value != null)
			{
				if (value.doubleValue() < minimum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("minimum", new Double(minimum));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "NumberValidator.minimum";
		}

	}

	private static class DoubleMaximumValidator extends NumberValidator
	{
		private static final long serialVersionUID = 1L;
		private final double maximum;

		private DoubleMaximumValidator(double maximum)
		{
			this.maximum = maximum;
		}

		/**
		 * @see wicket.markup.html.form.validation.NumberValidator#onValidate(wicket.markup.html.form.FormComponent,
		 *      Number)
		 */
		@Override
		public void onValidate(FormComponent formComponent, Number value)
		{
			if (value != null)
			{
				if (value.doubleValue() > maximum)
				{
					error(formComponent);
				}
			}
		}

		@Override
		protected Map<String, Serializable> messageModel(FormComponent formComponent)
		{
			final Map<String, Serializable> map = super.messageModel(formComponent);
			map.put("maximum", new Double(maximum));
			return map;
		}

		/**
		 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
		 */
		@Override
		protected String resourceKey(FormComponent formComponent)
		{
			return "NumberValidator.maximum";
		}
	}

}