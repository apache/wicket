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
package org.apache.wicket.extensions.yui.calendar;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.ClientInfo;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.ZeroPaddingIntegerConverter;
import org.apache.wicket.validation.validator.RangeValidator;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Works on a {@link java.util.Date} object. Displays a date field and a {@link DatePicker}, a field
 * for hours and a field for minutes, and an AM/PM field. The format (12h/24h) of the hours field
 * depends on the time format of this {@link DateTimeField}'s {@link Locale}, as does the visibility
 * of the AM/PM field (see {@link DateTimeField#use12HourFormat}).
 * 
 * @author eelcohillenius
 * @see DateField for a variant with just the date field and date picker
 */
public class DateTimeField extends FormComponentPanel<Date>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Enumerated type for different ways of handling the render part of requests.
	 */
	private static enum AM_PM {
		AM("AM"), PM("PM");

		/** */
		private String value;

		AM_PM(final String name)
		{
			value = name;
		}

		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString()
		{
			return value;
		}
	}

	private static final IConverter MINUTES_CONVERTER = new ZeroPaddingIntegerConverter(2);

	private AM_PM amOrPm = AM_PM.AM;

	private DropDownChoice<AM_PM> amOrPmChoice;

	private MutableDateTime date;

	private DateTextField dateField;

	private Integer hours;

	private TextField<Integer> hoursField;

	private Integer minutes;

	private TextField<Integer> minutesField;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public DateTimeField(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public DateTimeField(final String id, final IModel<Date> model)
	{
		super(id, model);

		setType(Date.class);
		PropertyModel<Date> dateFieldModel = new PropertyModel<Date>(this, "date");
		add(dateField = newDateTextField("date", dateFieldModel));
		dateField.add(newDatePicker());
		add(hoursField = new TextField<Integer>("hours", new PropertyModel<Integer>(this, "hours"),
				Integer.class));
		hoursField.add(new HoursValidator());
		hoursField.setLabel(new Model<String>("hours"));
		add(minutesField = new TextField<Integer>("minutes", new PropertyModel<Integer>(this,
				"minutes"), Integer.class)
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public IConverter getConverter(Class type)
			{
				return MINUTES_CONVERTER;
			}
		});
		minutesField.add(new RangeValidator<Integer>(0, 59));
		minutesField.setLabel(new Model<String>("minutes"));
		add(amOrPmChoice = new DropDownChoice<AM_PM>("amOrPmChoice", new PropertyModel<AM_PM>(this,
				"amOrPm"), Arrays.asList(AM_PM.values())));
	}

	/**
	 * Gets amOrPm.
	 * 
	 * @return amOrPm
	 */
	public AM_PM getAmOrPm()
	{
		return amOrPm;
	}

	/**
	 * Gets date.
	 * 
	 * @return date
	 */
	public Date getDate()
	{
		return (date != null) ? date.toDate() : null;
	}

	/**
	 * Gets hours.
	 * 
	 * @return hours
	 */
	public Integer getHours()
	{
		return hours;
	}

	/**
	 * TODO comment
	 * 
	 * @param widgetProperties
	 */
	protected void configure(Map< ? , ? > widgetProperties)
	{
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#getInput()
	 */
	@Override
	public String getInput()
	{
		// since we override convertInput, we can let this method return a value
		// that is just suitable for error reporting
		return dateField.getInput() + ", " + hoursField.getInput() + ":" + minutesField.getInput();
	}

	/**
	 * Gets minutes.
	 * 
	 * @return minutes
	 */
	public Integer getMinutes()
	{
		return minutes;
	}

	/**
	 * Sets amOrPm.
	 * 
	 * @param amOrPm
	 *            amOrPm
	 */
	public void setAmOrPm(final AM_PM amOrPm)
	{
		this.amOrPm = amOrPm;
	}

	/**
	 * Sets date.
	 * 
	 * @param date
	 *            date
	 */
	public void setDate(final Date date)
	{
		if (date == null)
		{
			this.date = null;
			setDefaultModelObject(null);
			setHours(null);
			setMinutes(null);
			return;
		}

		this.date = new MutableDateTime(date);

		Integer hours = getHours();
		if (hours != null)
		{
			boolean use12HourFormat = use12HourFormat();
			this.date.set(DateTimeFieldType.hourOfDay(), hours.intValue() %
					(use12HourFormat ? 12 : 24));

			Integer minutes = getMinutes();
			this.date.setMinuteOfHour((minutes != null) ? minutes.intValue() : 0);
		}

		setDefaultModelObject(this.date.toDate());
	}

	/**
	 * Sets hours.
	 * 
	 * @param hours
	 *            hours
	 */
	public void setHours(final Integer hours)
	{
		this.hours = hours;
	}

	/**
	 * Sets minutes.
	 * 
	 * @param minutes
	 *            minutes
	 */
	public void setMinutes(final Integer minutes)
	{
		this.minutes = minutes;
	}

	/**
	 * Gets the client's time zone.
	 * 
	 * @return The client's time zone or null
	 */
	protected TimeZone getClientTimeZone()
	{
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo)
		{
			return ((WebClientInfo)info).getProperties().getTimeZone();
		}
		return null;
	}

	/**
	 * Sets the converted input, which is an instance of {@link Date}, possibly null. It combines
	 * the inputs of the nested date, hours, minutes and am/pm fields and constructs a date from it.
	 * <p>
	 * Note that overriding this method is a better option than overriding {@link #updateModel()}
	 * like the first versions of this class did. The reason for that is that this method can be
	 * used by form validators without having to depend on the actual model being updated, and this
	 * method is called by the default implementation of {@link #updateModel()} anyway (so we don't
	 * have to override that anymore).
	 * </p>
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertInput()
	 */
	@Override
	protected void convertInput()
	{
		Object dateFieldInput = dateField.getConvertedInput();
		if (dateFieldInput != null)
		{
			MutableDateTime date = new MutableDateTime(dateFieldInput);
			Integer hours = hoursField.getConvertedInput();
			Integer minutes = minutesField.getConvertedInput();
			AM_PM amOrPm = amOrPmChoice.getConvertedInput();

			try
			{
				boolean use12HourFormat = use12HourFormat();
				if (hours != null)
				{
					date.set(DateTimeFieldType.hourOfDay(), hours.intValue() %
							getMaximumHours(use12HourFormat));
					date.setMinuteOfHour((minutes != null) ? minutes.intValue() : 0);
				}
				if (use12HourFormat)
				{
					date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
				}

				TimeZone zone = getClientTimeZone();
				if (zone != null)
				{
					date.setMillis(getMillis(zone, TimeZone.getDefault(), date.getMillis()));
				}

				// the date will be in the server's timezone
				setConvertedInput(date.toDate());
			}
			catch (RuntimeException e)
			{
				DateTimeField.this.error(e.getMessage());
				invalid();
			}
		}
		else
		{
			setConvertedInput(null);
		}
	}

	/**
	 * 
	 * @param to
	 * @param from
	 * @param instant
	 * @return millis
	 */
	private long getMillis(TimeZone to, TimeZone from, long instant)
	{
		return DateTimeZone.forTimeZone(from).getMillisKeepLocal(DateTimeZone.forTimeZone(to),
				instant);
	}

	/**
	 * create a new {@link DateTextField} instance to be added to this panel.
	 * 
	 * @param dateFieldModel
	 *            model that should be used by the {@link DateTextField}
	 * @return a new date text field instance
	 */
	protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel)
	{
		return new DateTextField(id, dateFieldModel, new StyleDateConverter(false));
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		dateField.setRequired(isRequired());
		hoursField.setRequired(isRequired());
		minutesField.setRequired(isRequired());

		boolean use12HourFormat = use12HourFormat();
		amOrPmChoice.setVisible(use12HourFormat);

		Date d = (Date)getDefaultModelObject();
		if (d != null)
		{
			date = new MutableDateTime(d);
		}
		else
		{
			date = null;
			hours = null;
			minutes = null;
		}

		if (date != null)
		{
			// convert date to the client's time zone if we have that info
			TimeZone zone = getClientTimeZone();
			// instantiate with the previously set date
			if (zone != null)
			{
				date.setMillis(getMillis(TimeZone.getDefault(), zone, date.getMillis()));
			}

			if (use12HourFormat)
			{
				int hourOfHalfDay = date.get(DateTimeFieldType.hourOfHalfday());
				hours = new Integer(hourOfHalfDay == 0 ? 12 : hourOfHalfDay);
			}
			else
			{
				hours = new Integer(date.get(DateTimeFieldType.hourOfDay()));
			}
			amOrPm = (date.get(DateTimeFieldType.halfdayOfDay()) == 0) ? AM_PM.AM : AM_PM.PM;
			minutes = new Integer(date.getMinuteOfHour());

			// we don't really have to reset the date field to the server's
			// timezone, as it's the same milliseconds from EPOCH anyway, and
			// toDate will always get the Date object initialized for the time zone
			// of the server
		}

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
		String pattern = DateTimeFormat.patternForStyle("-S", getLocale());
		return pattern.indexOf('a') != -1 || pattern.indexOf('h') != -1 ||
				pattern.indexOf('K') != -1;
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

	/**
	 * Validator for the {@link DateTimeField}'s hours field. Behaves like
	 * <code>RangeValidator</code>, setting appropriate range according to
	 * {@link DateTimeField#getMaximumHours()}
	 * 
	 * @see DateTimeField#getMaximumHours()
	 * @author Gerolf Seitz
	 */
	private class HoursValidator extends RangeValidator<Integer>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 */
		public HoursValidator()
		{
			if (getMaximumHours() == 24)
			{
				setRange(0, 23);
			}
			else
			{
				setRange(1, 12);
			}
		}
	}

	/**
	 * The DatePicker that gets added to the DateTimeField component. Users may override this method
	 * with a DatePicker of their choice.
	 */
	protected DatePicker newDatePicker()
	{
		return new DatePicker()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void configure(Map<String, Object> widgetProperties)
			{
				super.configure(widgetProperties);
				DateTimeField.this.configure(widgetProperties);
			}
		};
	}
}
