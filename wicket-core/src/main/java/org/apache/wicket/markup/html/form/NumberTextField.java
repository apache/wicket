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
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Numbers;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * A {@link TextField} for HTML5 &lt;input&gt; with type <em>number</em>.
 * 
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
	 * A special locale which is used to parse decimal number formats that are HTML5 compliant.
	 * 
	 * See <a href="http://dev.w3.org/html5/markup/datatypes.html#common.data.float">HTML5 number
	 * format</a>
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

		validator = new RangeValidator<N>(getMinValue(), getMaxValue());
		add(validator);
	}

	private N getMinValue()
	{
		N result;
		if (minimum != null)
		{
			result = minimum;
		}
		else
		{
			Class<N> numberType = getNumberType();
			result = (N)Numbers.getMinValue(numberType);
		}
		return result;
	}

	private N getMaxValue()
	{
		N result;
		if (maximum != null)
		{
			result = maximum;
		}
		else
		{
			Class<N> numberType = getNumberType();
			result = (N)Numbers.getMaxValue(numberType);
		}
		return result;
	}

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

		if (minimum != null)
		{
			attributes.put("min", Objects.stringValue(minimum));
		}
		else
		{
			attributes.remove("min");
		}

		if (maximum != null)
		{
			attributes.put("max", Objects.stringValue(maximum));
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
	protected void convertInput()
	{
		IConverter<N> converter = getConverter(getNumberType());

		try
		{
			setConvertedInput(converter.convertToObject(getInput(), HTML5_LOCALE));
		}
		catch (ConversionException e)
		{
			ValidationError error = new ValidationError();
			if (e.getResourceKey() != null)
			{
				error.addMessageKey(e.getResourceKey());
			}
			if (e.getTargetType() != null)
			{
				error.addMessageKey("ConversionError." + Classes.simpleName(e.getTargetType()));
			}
			error.addMessageKey("ConversionError");

			final Locale locale = e.getLocale();
			if (locale != null)
			{
				error.setVariable("locale", locale);
			}
			error.setVariable("exception", e);

			Map<String, Object> variables = e.getVariables();
			if (variables != null)
			{
				error.getVariables().putAll(variables);
			}

			error(error);
		}
	}
}
