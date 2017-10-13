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
package org.apache.wicket.extensions.markup.html.form.datetime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * Works on a {@link LocalTime} object. Displays a field for hours and a field for minutes, and an
 * AM/PM field. The format (12h/24h) of the hours field depends on the time format of this
 * {@link TimeField}'s {@link Locale}, as does the visibility of the AM/PM field (see
 * {@link TimeField#use12HourFormat}).
 * 
 * @author eelcohillenius
 */
public class TimeField extends FormComponentPanel<LocalTime>
{
	private static final long serialVersionUID = 1L;

	public static final String HOURS_CSS_CLASS_KEY = CssUtils.key(TimeField.class, "hours");

	public static final String MINUTES_CSS_CLASS_KEY = CssUtils.key(TimeField.class, "minutes");

	/**
	 * Enumerated type for different ways of handling the render part of requests.
	 */
	public enum AM_PM
	{
		AM, PM;
	}

	private static final IConverter<Integer> MINUTES_CONVERTER = new IntegerConverter()
	{
		private static final long serialVersionUID = 1L;

		protected NumberFormat newNumberFormat(Locale locale)
		{
			return new DecimalFormat("00");
		}
	};

	// The TextField for "hours" and it's associated model object
	private TextField<Integer> hoursField;

	// The TextField for "minutes" and it's associated model object
	private TextField<Integer> minutesField;

	// The dropdown list for AM/PM and it's associated model object
	private DropDownChoice<AM_PM> amOrPmChoice;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public TimeField(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the component's model
	 */
	public TimeField(String id, IModel<LocalTime> model)
	{
		super(id, model);

		// Sets the type that will be used when updating the model for this component.
		setType(LocalTime.class);

		add(new Label("hoursSeparator", new ResourceModel("TimeField.hoursSeparator"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();

				minutesField.configure();

				setVisible(minutesField.isVisible());
			}
		});
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// Create and add the "hours" TextField
		add(hoursField = newHoursTextField("hours", new HoursModel(), Integer.class));

		// Create and add the "minutes" TextField
		add(minutesField = newMinutesTextField("minutes", new MinutesModel(), Integer.class));

		// Create and add the "AM/PM" choice
		add(amOrPmChoice = new DropDownChoice<AM_PM>("amOrPmChoice", new AmPmModel(),
			Arrays.asList(AM_PM.values())) {
			@Override
			protected boolean localizeDisplayValues()
			{
				return true;
			}
		});
	}

	/**
	 * create a new {@link TextField} instance for hours to be added to this panel.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            model that should be used by the {@link TextField}
	 * @param type
	 *            the type of the text field
	 * @return a new text field instance
	 */
	protected TextField<Integer> newHoursTextField(final String id, IModel<Integer> model,
		Class<Integer> type)
	{
		TextField<Integer> hoursTextField = new TextField<Integer>(id, model, type)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String[] getInputTypes()
			{
				return new String[] { "number" };
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);

				tag.append("class", getString(HOURS_CSS_CLASS_KEY), " ");

				tag.put("min", use12HourFormat() ? 1 : 0);
				tag.put("max", use12HourFormat() ? 12 : 23);
			}
		};
		hoursTextField
			.add(use12HourFormat() ? RangeValidator.range(1, 12) : RangeValidator.range(0, 23));
		return hoursTextField;
	}

	/**
	 * create a new {@link TextField} instance for minutes to be added to this panel.
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            model that should be used by the {@link TextField}
	 * @param type
	 *            the type of the text field
	 * @return a new text field instance
	 */
	protected TextField<Integer> newMinutesTextField(final String id, IModel<Integer> model,
		Class<Integer> type)
	{
		TextField<Integer> minutesField = new TextField<Integer>(id, model, type)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected IConverter<?> createConverter(Class<?> type)
			{
				if (Integer.class.isAssignableFrom(type))
				{
					return MINUTES_CONVERTER;
				}
				return null;
			}

			@Override
			protected String[] getInputTypes()
			{
				return new String[] { "number" };
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);

				tag.append("class", getString(MINUTES_CSS_CLASS_KEY), " ");

				tag.put("min", 0);
				tag.put("max", 59);
			}
		};
		minutesField.add(new RangeValidator<>(0, 59));
		return minutesField;
	}

	@Override
	public String getInput()
	{
		// since we override convertInput, we can let this method return a value
		// that is just suitable for error reporting
		return String.format("%s:%s", hoursField.getInput(), minutesField.getInput());
	}

	@Override
	public void convertInput()
	{
		Integer hours = hoursField.getConvertedInput();
		Integer minutes = minutesField.getConvertedInput();
		AM_PM amOrPmInput = amOrPmChoice.getConvertedInput();

		LocalTime localTime;
		if (hours == null && minutes == null)
		{
			localTime = null;
		}
		else if (hours != null && minutes != null)
		{
			// Use the input to create a LocalTime object
			localTime = LocalTime.of(hours, minutes);

			// Adjust for halfday if needed
			if (use12HourFormat())
			{
				int halfday = (amOrPmInput == AM_PM.PM ? 1 : 0);
				localTime = localTime.with(ChronoField.AMPM_OF_DAY, halfday);
			}
		}
		else
		{
			error(newValidationError(new ConversionException("Cannot parse time").setTargetType(getType())));
			return;
		}

		setConvertedInput(localTime);
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();

		hoursField.setRequired(isRequired());
		minutesField.setRequired(isRequired());

		amOrPmChoice.setVisible(use12HourFormat());
	}

	/**
	 * Checks whether the current {@link Locale} uses the 12h or 24h time format. This method can be
	 * overridden to e.g. always use 24h format.
	 * 
	 * @return {@value true}, if the current {@link Locale} uses the 12h format.<br/>
	 *         {@value false}, otherwise
	 */
	protected boolean use12HourFormat()
	{
		String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(null,
			FormatStyle.SHORT, IsoChronology.INSTANCE, getLocale());
		return pattern.indexOf('a') != -1 || pattern.indexOf('h') != -1
			|| pattern.indexOf('K') != -1;
	}

	protected class HoursModel implements IModel<Integer>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Integer getObject()
		{
			LocalTime t = TimeField.this.getModelObject();
			if (t == null)
			{
				return null;
			}
			return use12HourFormat() ? t.get(ChronoField.CLOCK_HOUR_OF_AMPM) : t.getHour();
		}

		@Override
		public void setObject(Integer hour)
		{
			// ignored
		}
	}

	protected class MinutesModel implements IModel<Integer>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Integer getObject()
		{
			LocalTime t = TimeField.this.getModelObject();
			return t == null ? null : t.getMinute();
		}

		@Override
		public void setObject(Integer minute)
		{
			// ignored
		}
	}

	protected class AmPmModel implements IModel<AM_PM>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public AM_PM getObject()
		{
			LocalTime t = TimeField.this.getModelObject();
			int i = t == null ? 0 : t.get(ChronoField.AMPM_OF_DAY);
			return i == 0 ? AM_PM.AM : AM_PM.PM;
		}

		@Override
		public void setObject(AM_PM amPm)
		{
			// ignored
		}
	}
}