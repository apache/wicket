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
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * Works on a {@link java.util.Date} object. Displays a field for hours and a field for minutes, and
 * an AM/PM field. The format (12h/24h) of the hours field depends on the time format of this
 * {@link TimeField}'s {@link Locale}, as does the visibility of the AM/PM field (see
 * {@link TimeField#use12HourFormat}).
 * 
 * @author eelcohillenius
 * @see TimeField for a variant with just the date field and date picker
 */
public class TimeField extends FormComponentPanel<LocalTime> implements ITextFormatProvider
{
	private static final long serialVersionUID = 1L;

	/**
	 * Enumerated type for different ways of handling the render part of requests.
	 */
	public enum AM_PM {
		/** */
		AM("AM"),

		/** */
		PM("PM");

		private  final String value;

		AM_PM(final String name)
		{
			value = name;
		}

		@Override
		public String toString()
		{
			return value;
		}
	}
	protected static final String HOURS = "hours";
	protected static final String MINUTES = "minutes";
	protected static final String AM_OR_PM_CHOICE = "amOrPmChoice";

	private static final IConverter<Integer> MINUTES_CONVERTER = new IntegerConverter() {
		private static final long serialVersionUID = 1L;

		protected NumberFormat newNumberFormat(Locale locale) {
			return new DecimalFormat("00");
		}
	};

	// The TextField for "hours" and it's associated model object
	private TextField<Integer> hoursField;

	// The TextField for "minutes" and it's associated model object
	private TextField<Integer> minutesField;

	// The dropdown list for AM/PM and it's associated model object
	private DropDownChoice<AM_PM> amOrPmChoice;
	private LocalTime time = LocalTime.now();

	/**
	 * Creates a new TimeField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param timePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @return TimeField
	 */
	public static TimeField forTimePattern(String id, IModel<LocalTime> model, String timePattern)
	{
		return new TimeField(id, model, new PatternTimeConverter(timePattern));
	}

	/**
	 * Creates a new TimeField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param timePattern
	 *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available
	 *            patterns.
	 * @return TimeField
	 */
	public static TimeField forTimePattern(String id, String timePattern)
	{
		return forTimePattern(id, null, timePattern);
	}

	/**
	 * Creates a new TimeField using the provided date style.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param timeStyle
	 *            Date style to use. The first character is the date style, and the second character
	 *            is the time style. Specify a character of 'S' for short style, 'M' for medium, 'L'
	 *            for long, and 'F' for full. A date or time may be ommitted by specifying a style
	 *            character '-'. See {@link org.joda.time.DateTimeFormat#forStyle(String)}.
	 * @return TimeField
	 */
	public static TimeField forTimeStyle(String id, IModel<LocalTime> model, String timeStyle)
	{
		return new TimeField(id, model, new StyleTimeConverter(timeStyle));
	}

	/**
	 * Creates a new TimeField using the provided date style.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param timeStyle
	 *            Date style to use. The first character is the date style, and the second character
	 *            is the time style. Specify a character of 'S' for short style, 'M' for medium, 'L'
	 *            for long, and 'F' for full. A date or time may be ommitted by specifying a style
	 *            character '-'. See {@link org.joda.time.DateTimeFormat#forStyle(String)}.
	 * @return TimeField
	 */
	public static TimeField forTimeStyle(String id, String timeStyle)
	{
		return forTimeStyle(id, null, timeStyle);
	}

	/**
	 * Creates a new TimeField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @return TimeField
	 */
	public static TimeField forShortStyle(String id)
	{
		return forShortStyle(id, null);
	}

	/**
	 * Creates a new TimeField defaulting to using a short date pattern
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @return TimeField
	 */
	public static TimeField forShortStyle(String id, IModel<LocalTime> model)
	{
		return new TimeField(id, model, new StyleTimeConverter());
	}

	/**
	 * Creates a new TimeField using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param converter
	 *            the date converter
	 * @return TimeField
	 */
	public static TimeField withConverter(String id, IDateConverter<LocalTime> converter)
	{
		return withConverter(id, null, converter);
	}

	/**
	 * Creates a new TimeField using the provided converter.
	 * 
	 * @param id
	 *            The id of the text field
	 * @param model
	 *            The model
	 * @param converter
	 *            the date converter
	 * @return TimeField
	 */
	public static TimeField withConverter(String id, IModel<LocalTime> model, IDateConverter<LocalTime> converter)
	{
		return new TimeField(id, model, converter);
	}

	/**
	 * The converter for the TextField
	 */
	private final IDateConverter<LocalTime> converter;

	/**
	 * Construct.
	 * 
	 * @param id
	 *      the component id
	 */
	public TimeField(String id, IDateConverter<LocalTime> converter)
	{
		this(id, null, converter);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *      the component id
	 * @param model
	 *      the component's model
	 */
	public TimeField(String id, IModel<LocalTime> model, IDateConverter<LocalTime> converter)
	{
		super(id, model);

		Args.notNull(converter, "converter");
		this.converter = converter;

		// Sets the type that will be used when updating the model for this component.
		setType(LocalTime.class);


		// Create and add the "hours" TextField
		add(hoursField = newHoursTextField(HOURS, new HoursModel(), Integer.class));

		// Create and add the "minutes" TextField
		add(minutesField = newMinutesTextField(MINUTES, new MinutesModel(), Integer.class));

		// Create and add the "AM/PM" Listbox
		add(amOrPmChoice = new DropDownChoice<>(AM_OR_PM_CHOICE, new AmPmModel(), Arrays.asList(AM_PM.values())));

		add(new WebMarkupContainer("hoursSeparator")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible()
			{
				return minutesField.determineVisibility();
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
	protected TextField<Integer> newHoursTextField(final String id, IModel<Integer> model, Class<Integer> type) {
		TextField<Integer> hoursTextField = new TextField<>(id, model, type);
		hoursTextField.add(getMaximumHours() == 24 ? RangeValidator.range(0, 23) : RangeValidator
			.range(1, 12));
		hoursTextField.setLabel(new Model<>(HOURS));
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
		};
		minutesField.add(new RangeValidator<>(0, 59));
		minutesField.setLabel(new Model<>(MINUTES));
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
		Integer hoursInput = hoursField.getConvertedInput();
		Integer minutesInput = minutesField.getConvertedInput();
		AM_PM amOrPmInput = amOrPmChoice.getConvertedInput();

		// Use the input to create a date object with proper timezone
		LocalTime localTime = LocalTime.of(hoursInput, minutesInput);

		// Adjust for halfday if needed
		if (use12HourFormat())
		{
			int halfday = (amOrPmInput == AM_PM.PM ? 1 : 0);
			localTime = localTime.with(ChronoField.AMPM_OF_DAY, halfday);
		}
		super.convertInput();
	}

	@Override
	protected void onBeforeRender() {
		hoursField.setRequired(isRequired());
		minutesField.setRequired(isRequired());

		boolean use12HourFormat = use12HourFormat();
		amOrPmChoice.setVisible(use12HourFormat);
		super.onBeforeRender();
	}

	/**
	 * Checks whether the current {@link Locale} uses the 12h or 24h time format. This method can be
	 * overridden to e.g. always use 24h format.
	 * 
	 * @return true, if the current {@link Locale} uses the 12h format.<br/>
	 *         false, otherwise
	 */
	protected boolean use12HourFormat()
	{
		String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(null, FormatStyle.SHORT, IsoChronology.INSTANCE, getLocale());
		return pattern.indexOf('a') != -1 || pattern.indexOf('h') != -1 || pattern.indexOf('K') != -1;
	}

	/**
	 * @return either 12 or 24, depending on the hour format of the current {@link Locale}
	 */
	private int getMaximumHours()
	{
		return getMaximumHours(use12HourFormat());
	}

	/**
	 * Convenience method (mainly for optimization purposes), in case {@link #use12HourFormat()} has
	 * already been stored in a local variable and thus doesn't need to be computed again.
	 * 
	 * @param use12HourFormat
	 *            the hour format to use
	 * @return either 12 or 24, depending on the parameter <code>use12HourFormat</code>
	 */
	private int getMaximumHours(boolean use12HourFormat)
	{
		return use12HourFormat ? 12 : 24;
	}


	protected class HoursModel implements IModel<Integer>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Integer getObject()
		{
			return time.getHour();
		}

		@Override
		public void setObject(Integer hour)
		{
			time = time.with(ChronoField.HOUR_OF_DAY, hour);
		}

		@Override
		public void detach()
		{
		}
	}

	protected class MinutesModel implements IModel<Integer>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Integer getObject()
		{
			return time.getMinute();
		}

		@Override
		public void setObject(Integer minute)
		{
			time = time.with(ChronoField.MINUTE_OF_HOUR, minute);
		}

		@Override
		public void detach()
		{
		}
	}

	protected class AmPmModel implements IModel<AM_PM>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public AM_PM getObject()
		{
			int i = time.get(ChronoField.AMPM_OF_DAY);
			return i == 0 ? AM_PM.AM : AM_PM.PM;
		}

		@Override
		public void setObject(AM_PM amPm)
		{
			int i = AM_PM.AM == amPm ? 0 : 1;
			time = time.with(ChronoField.AMPM_OF_DAY, i);
		}

		@Override
		public void detach()
		{
		}
	}

	/**
	 * @return The specialized converter.
	 * @see org.apache.wicket.Component#createConverter(java.lang.Class)
	 */
	@Override
	protected IConverter<?> createConverter(Class<?> clazz)
	{
		if (LocalTime.class.isAssignableFrom(clazz))
		{
			return converter;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
	 */
	@Override
	public final String getTextFormat()
	{
		return converter.getPattern(getLocale());
	}
}
