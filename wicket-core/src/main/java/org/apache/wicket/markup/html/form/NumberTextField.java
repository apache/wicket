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

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractDecimalConverter;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * A {@link TextField} for HTML5 &lt;input&gt; with type <em>number</em>.
 * 
 * <p>
 * Automatically validates the input against the configured {@link #setMinimum(Number) min} and
 * {@link #setMaximum(Number) max} attributes. If any of them is <code>null</code> then
 * {@link Double#MIN_VALUE} and {@link Double#MAX_VALUE} are used respectfully.
 * 
 * Note: {@link #setType(Class)} must be called explicitly!
 * 
 * @param <N>
 *            the number type
 */
public class NumberTextField<N extends Number & Comparable<N>> extends TextField<N>
{
	private static final long serialVersionUID = 1L;

	/**
	 * A special locale which is used to render decimal number formats that are HTML5 compliant.
	 *
	 * See <a href="http://dev.w3.org/html5/markup/datatypes.html#common.data.float">HTML5 number format</a>
	 */
	private static final Locale HTML5_LOCALE = new Locale("en", "", "wicket-html5");

	private RangeValidator<N> validator;

	private N minimum;

	private N maximum;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 */
	public NumberTextField(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            the input value
	 */
	public NumberTextField(String id, IModel<N> model)
	{
		this(id, model, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            the input value
	 * @param type
	 *            The type to use when updating the model for this text field
	 */
	public NumberTextField(String id, IModel<N> model, Class<N> type)
	{
		super(id, model, type);

		validator = null;
		minimum = null;
		maximum = null;
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
	public NumberTextField<N> setMaximum(final N maximum)
	{
		this.maximum = maximum;
		return this;
	}

	@Override
	public void onConfigure()
	{
		super.onConfigure();

		if (validator != null)
		{
			remove(validator);
		}

		validator = new RangeValidator<N>(minimum, maximum);
		add(validator);
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		IValueMap attributes = tag.getAttributes();

		if (minimum != null)
		{
			attributes.put("min", minimum);
		}
		else
		{
			attributes.remove("min");
		}

		if (maximum != null)
		{
			attributes.put("max", maximum);
		}
		else
		{
			attributes.remove("max");
		}
	}

	@Override
	protected String getInputType()
	{
		return "number";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * WICKET-3591 Browsers support only formatting in English
	 */
	@Override
	public Locale getLocale()
	{
		return HTML5_LOCALE;
	}

	/**
	 * {@inheritDoc}
	 *
	 * WICKET-4501 NumberTextField&lt;BigDecimal> renders its value in unsupported number format
	 */
	@Override
	public <C> IConverter<C> getConverter(Class<C> type)
	{
		IConverter<C> converter = super.getConverter(type);
		if (converter instanceof AbstractDecimalConverter<?>)
		{
			AbstractDecimalConverter<?> adc = (AbstractDecimalConverter<?>)converter;
			NumberFormat numberFormat = adc.getNumberFormat(HTML5_LOCALE);
			// do not use grouping for HTML5 number/range fields because
			// it is not supported by browsers
			numberFormat.setGroupingUsed(false);
			adc.setNumberFormat(HTML5_LOCALE, numberFormat);
		}
		return converter;
	}
}
