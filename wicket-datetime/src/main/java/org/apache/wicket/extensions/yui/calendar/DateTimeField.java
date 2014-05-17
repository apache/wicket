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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.core.request.ClientInfo;
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
	/**
	 * Enumerated type for different ways of handling the render part of requests.
	 */
	public static enum AM_PM {
		/** */
		AM("AM"),

		/** */
		PM("PM");

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

	private static final long serialVersionUID = 1L;

	// Component-IDs
	protected static final String DATE = "date";
	protected static final String HOURS = "hours";
	protected static final String MINUTES = "minutes";
	protected static final String AM_OR_PM_CHOICE = "amOrPmChoice";

	// PropertyModel string to access getAmOrPm
	private static final String AM_OR_PM = "amOrPm";

	private static final IConverter<Integer> MINUTES_CONVERTER = new ZeroPaddingIntegerConverter(2);

	// The dropdown list for AM/PM and it's associated model object
	private DropDownChoice<AM_PM> amOrPmChoice;
	private AM_PM amOrPm = AM_PM.AM;

	// The date TextField and it's associated model object
	// Note that any time information in date will be ignored
	private DateTextField dateField;
	private Date date;

	// The TextField for "hours" and it's associated model object
	private TextField<Integer> hoursField;
	private Integer hours;

	// The TextField for "minutes" and it's associated model object
	private TextField<Integer> minutesField;
	private Integer minutes;

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

		// Sets the type that will be used when updating the model for this component.
		setType(Date.class);

		// Create and add the date TextField
		PropertyModel<Date> dateFieldModel = new PropertyModel<Date>(this, DATE);
		add(dateField = newDateTextField(DATE, dateFieldModel));

		// Add a date picker to the date TextField
		dateField.add(newDatePicker());

		// Create and add the "hours" TextField
		add(hoursField = new TextField<Integer>(HOURS, new PropertyModel<Integer>(this, HOURS),
			Integer.class));
		hoursField.add(getMaximumHours() == 24 ? RangeValidator.range(0, 23) : RangeValidator
			.range(1, 12));
		hoursField.setLabel(new Model<String>(HOURS));

		// Create and add the "minutes" TextField
		add(minutesField = new TextField<Integer>(MINUTES,
			new PropertyModel<Integer>(this, MINUTES), Integer.class)
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public <C> IConverter<C> getConverter(Class<C> type)
			{
				if (Integer.class.isAssignableFrom(type))
				{
					return (IConverter<C>)MINUTES_CONVERTER;
				}
				else
				{
					return super.getConverter(type);
				}
			}
		});
		minutesField.add(new RangeValidator<Integer>(0, 59));
		minutesField.setLabel(new Model<String>(MINUTES));

		// Create and add the "AM/PM" Listbox
		add(amOrPmChoice = new DropDownChoice<AM_PM>(AM_OR_PM_CHOICE, new PropertyModel<AM_PM>(
			this, AM_OR_PM), Arrays.asList(AM_PM.values())));

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
	 * 
	 * @return The date TextField
	 */
	protected final DateTextField getDateTextField()
	{
		return dateField;
	}

	/**
	 * Gets the amOrPm model object of the drop down choice.
	 * 
	 * @return amOrPm
	 */
	public final AM_PM getAmOrPm()
	{
		return amOrPm;
	}

	/**
	 * Gets the date model object for the date TextField. Any associated time information will be
	 * ignored.
	 * 
	 * @return date
	 */
	public final Date getDate()
	{
		return date;
	}

	/**
	 * Gets the hours model object for the TextField
	 * 
	 * @return hours
	 */
	public final Integer getHours()
	{
		return hours;
	}

	/**
	 * Gets the minutes model object for the TextField
	 * 
	 * @return minutes
	 */
	public final Integer getMinutes()
	{
		return minutes;
	}

	/**
	 * Gives overriding classes the option of adding (or even changing/ removing) configuration
	 * properties for the javascript widget. See <a
	 * href="http://developer.yahoo.com/yui/calendar/">the widget's documentation</a> for the
	 * available options. If you want to override/ remove properties, you should call
	 * super.configure(properties) first. If you don't call that, be aware that you will have to
	 * call {@link #configure(java.util.Map)} manually if you like localized strings to be added.
	 * 
	 * @param widgetProperties
	 *            the current widget properties
	 */
	protected void configure(Map<String, Object> widgetProperties)
	{
	}

	@Override
	public String getInput()
	{
		// since we override convertInput, we can let this method return a value
		// that is just suitable for error reporting
		return dateField.getInput() + ", " + hoursField.getInput() + ":" + minutesField.getInput();
	}

	/**
	 * Sets the amOrPm model object associated with the drop down choice.
	 * 
	 * @param amOrPm
	 *            amOrPm
	 */
	public final void setAmOrPm(final AM_PM amOrPm)
	{
		this.amOrPm = amOrPm;
	}

	/**
	 * Sets the date model object associated with the date TextField. It does not affect hours or
	 * minutes.
	 * 
	 * @param date
	 *            date
	 */
	public final void setDate(final Date date)
	{
		this.date = date;
	}

	/**
	 * Sets hours.
	 * 
	 * @param hours
	 *            hours
	 */
	public final void setHours(final Integer hours)
	{
		this.hours = hours;
	}

	/**
	 * Sets minutes.
	 * 
	 * @param minutes
	 *            minutes
	 */
	public final void setMinutes(final Integer minutes)
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
	 */
	@Override
	protected void convertInput()
	{
		try
		{
			// Get the converted input values
			Date dateFieldInput = dateField.getConvertedInput();
			Integer hoursInput = hoursField.getConvertedInput();
			Integer minutesInput = minutesField.getConvertedInput();
			AM_PM amOrPmInput = amOrPmChoice.getConvertedInput();

			if (dateFieldInput == null)
			{
				return;
			}

			// Get year, month and day ignoring any timezone of the Date object
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateFieldInput);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int hours = (hoursInput == null ? 0 : hoursInput % 24);
			int minutes = (minutesInput == null ? 0 : minutesInput);

			// Use the input to create a date object with proper timezone
			MutableDateTime date = new MutableDateTime(year, month, day, hours, minutes, 0, 0,
				DateTimeZone.forTimeZone(getClientTimeZone()));

			// Adjust for halfday if needed
			if (use12HourFormat())
			{
				int halfday = (amOrPmInput == AM_PM.PM ? 1 : 0);
				date.set(DateTimeFieldType.halfdayOfDay(), halfday);
				date.set(DateTimeFieldType.hourOfHalfday(), hours % 12);
			}

			// The date will be in the server's timezone
			setConvertedInput(newDateInstance(date.getMillis()));
		}
		catch (RuntimeException e)
		{
			DateTimeField.this.error(e.getMessage());
			invalid();
		}
	}

	/**
	 * A factory method for the DateTextField's model object.
	 *
	 * @return any specialization of java.util.Date
	 */
	protected Date newDateInstance()
	{
		return new Date();
	}

	/**
	 * A factory method for the DateTextField's model object.
	 *
	 * @param time
	 *      the time in milliseconds
	 * @return any specialization of java.util.Date
	 */
	protected Date newDateInstance(long time)
	{
		return new Date(time);
	}

	/**
	 * create a new {@link DateTextField} instance to be added to this panel.
	 * 
	 * @param id
	 *            the component id
	 * @param dateFieldModel
	 *            model that should be used by the {@link DateTextField}
	 * @return a new date text field instance
	 */
	protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel)
	{
		return DateTextField.forShortStyle(id, dateFieldModel, false);
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

		Date modelObject = (Date)getDefaultModelObject();
		if (modelObject == null)
		{
			date = null;
			hours = null;
			minutes = null;
		}
		else
		{
			MutableDateTime mDate = new MutableDateTime(modelObject);
			// convert date to the client's time zone if we have that info
			TimeZone zone = getClientTimeZone();
			if (zone != null)
			{
				mDate.setZone(DateTimeZone.forTimeZone(zone));
			}

			date = mDate.toDateTime().toLocalDate().toDate();

			if (use12HourFormat)
			{
				int hourOfHalfDay = mDate.get(DateTimeFieldType.hourOfHalfday());
				hours = hourOfHalfDay == 0 ? 12 : hourOfHalfDay;
			}
			else
			{
				hours = mDate.get(DateTimeFieldType.hourOfDay());
			}

			amOrPm = (mDate.get(DateTimeFieldType.halfdayOfDay()) == 0) ? AM_PM.AM : AM_PM.PM;
			minutes = mDate.getMinuteOfHour();
		}

		super.onBeforeRender();
	}

	/**
	 * Change a date in another timezone
	 * 
	 * @param date
	 *            The input date.
	 * @param zone
	 *            The target timezone.
	 * @return A new converted date.
	 */
	public static Date changeTimeZone(Date date, TimeZone zone)
	{
		Calendar first = Calendar.getInstance(zone);
		first.setTimeInMillis(date.getTime());

		Calendar output = Calendar.getInstance();
		output.set(Calendar.YEAR, first.get(Calendar.YEAR));
		output.set(Calendar.MONTH, first.get(Calendar.MONTH));
		output.set(Calendar.DAY_OF_MONTH, first.get(Calendar.DAY_OF_MONTH));
		output.set(Calendar.HOUR_OF_DAY, first.get(Calendar.HOUR_OF_DAY));
		output.set(Calendar.MINUTE, first.get(Calendar.MINUTE));
		output.set(Calendar.SECOND, first.get(Calendar.SECOND));
		output.set(Calendar.MILLISECOND, first.get(Calendar.MILLISECOND));

		return output.getTime();
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
	 * The DatePicker that gets added to the DateTimeField component. Users may override this method
	 * with a DatePicker of their choice.
	 * 
	 * @return a new {@link DatePicker} instance
	 */
	protected DatePicker newDatePicker()
	{
		return new DatePicker()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void configure(final Map<String, Object> widgetProperties,
				final IHeaderResponse response, final Map<String, Object> initVariables)
			{
				super.configure(widgetProperties, response, initVariables);

				DateTimeField.this.configure(widgetProperties);
			}
		};
	}
}
