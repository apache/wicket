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

import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * A {@link TextField} for HTML5 &lt;input&gt; with type <em>number</em>.
 * <p>
 * The {@code <input>}'s value will be rendered in floating-point representation, as required by
 * the <a href="https://www.w3.org/TR/html-markup/input.number.html">HTML specification</a>. Use a simple
 * {@code TextField} to use a locale specific conversion of numbers.
 * <p>
 * Automatically validates the input against the configured {@link #setMinimum(N) min} and
 * {@link #setMaximum(N) max} attributes. If any of them is <code>null</code> then respective
 * MIN_VALUE or MAX_VALUE for the number type is used. If the number type has no minimum and/or
 * maximum value then {@link Double#MIN_VALUE} and {@link Double#MAX_VALUE} are used respectfully.
 * 
 * @param <N>
 *            the type of the number
 */
public class NumberTextField<N extends Number & Comparable<N>> extends TextField<N>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Use this as a marker of step attribute value "any"
	 * Because the w3c spec requires step to be a non-negative digit
	 * greater than zero we use zero as delegate for "any" keyword.
	 */
	public static final Double ANY = Double.valueOf(0d);

	private RangeValidator<N> validator;

	private IModel<N> minimum;

	private IModel<N> maximum;

	private IModel<N> step;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 */
	public NumberTextField(String id)
	{
		this(id, null, null);
	}


	/**
	 * Construct.
	 *
	 * @param id
	 *            The component id
	 * @param type
	 *            The type to use when updating the model for this text field
	 */
	public NumberTextField(String id, Class<N> type)
	{
		this(id, null, type);
	}


	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The input value
	 */
	public NumberTextField(String id, IModel<N> model)
	{
		this(id, model, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The input value
	 * @param type
	 *            The type to use when updating the model for this text field
	 */
	public NumberTextField(String id, IModel<N> model, Class<N> type)
	{
		super(id, model, type);

		validator = null;
		minimum = Model.of((N)null);
		maximum = Model.of((N)null);
		step = Model.of((N)null);
	}
	
	/**
	 * Sets the minimum allowed value
	 *
	 * @param minimum
	 *            the minimum allowed value
	 * @return this instance
	 */
	public NumberTextField<N> setMinimum(final N minimum)
	{
		this.minimum = Model.of(minimum);
		return this;
	}

	/**
	 * Sets the maximum allowed value
	 *
	 * @param maximum
	 *            the maximum allowed value
	 * @return this instance
	 */
	public NumberTextField<N> setMaximum(final N maximum)
	{
		this.maximum = Model.of(maximum);
		return this;
	}

	/**
	 * Sets the step attribute
	 *
	 * @param step
	 *            the step attribute
	 * @return this instance
	 */
	public NumberTextField<N> setStep(final N step)
	{
		this.step = Model.of(step);
		return this;
	}

	/**
	 * Sets the minimum allowed value
	 * 
	 * @param minimum
	 *            the minimum allowed value
	 * @return this instance
	 */
	public NumberTextField<N> setMinimum(final IModel<N> minimum)
	{
		this.minimum = minimum;
		return this;
	}

	/**
	 * Sets the maximum allowed value
	 *
	 * @param maximum
	 *            the maximum allowed value
	 * @return this instance
	 */
	public NumberTextField<N> setMaximum(final IModel<N> maximum)
	{
		this.maximum = maximum;
		return this;
	}

	/**
	 * Sets the step attribute
	 *
	 * @param step
	 *            the step attribute
	 * @return this instance
	 */
	public NumberTextField<N> setStep(final IModel<N> step)
	{
		this.step = step;
		return this;
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();

		if (validator != null)
		{
			remove(validator);
			validator = null;
		}

		final N min = minimum.getObject();
		final N max = maximum.getObject();
		if (min != null || max != null)
		{
			validator = RangeValidator.range(min, max);
			add(validator);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<N> getNumberType()
	{
		Class<N> numberType = getType();
		if (numberType == null && getModelObject() != null)
		{
			numberType = (Class<N>)getModelObject().getClass();
		}
		return numberType;
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		IValueMap attributes = tag.getAttributes();

		final N min = minimum.getObject();
		if (min != null)
		{
			attributes.put("min", Objects.stringValue(min));
		}
		else
		{
			attributes.remove("min");
		}

		final N max = maximum.getObject();
		if (max != null)
		{
			attributes.put("max", Objects.stringValue(max));
		}
		else
		{
			attributes.remove("max");
		}

		final N _step = step.getObject();
		if (_step != null)
		{
			if (_step.doubleValue() == ANY)
			{
				attributes.put("step", "any");
			}
			else
			{
				attributes.put("step", Objects.stringValue(_step));
			}
		}
		else
		{
			attributes.remove("step");
		}
	}

	@Override
	protected String[] getInputTypes()
	{
		return new String[] {"number"};
	}

	/**
	 * The formatting for {@link Locale#ENGLISH} might not be compatible with HTML (e.g. group
	 * digits), thus use {@link Objects#stringValue(Object)} instead.
	 * 
	 * @return value
	 */
	@Override
	protected String getModelValue()
	{
		N value = getModelObject();
		if (value == null)
		{
			return "";
		}
		else
		{
			return Objects.stringValue(value);
		}
	}

	/**
	 * Always use {@link Locale#ENGLISH} to parse the input.
	 */
	@Override
	public void convertInput()
	{
		IConverter<N> converter = getConverter(getNumberType());

		try
		{
			setConvertedInput(converter.convertToObject(getInput(), Locale.ENGLISH));
		}
		catch (ConversionException e)
		{
			error(newValidationError(e));
		}
	}
}
