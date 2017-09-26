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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;

/**
 * Works on a {@link java.time.ZonedDateTime} object. Displays a date field and a DatePicker, a field
 * for hours and a field for minutes, and an AM/PM field. The format (12h/24h) of the hours field
 * depends on the time format of this {@link DateTimeField}'s {@link Locale}, as does the visibility
 * of the AM/PM field (see {@link DateTimeField#use12HourFormat}).
 * <p>
 * <strong>Ajaxifying the DateTimeField</strong>: If you want to update a DateTimeField with an
 * {@link AjaxFormComponentUpdatingBehavior}, you have to attach it to the contained
 * {@link DateField} by overriding {@link #newDateTextField(String, IModel)} and calling
 * {@link #processInput()}:
 * 
 * <pre>{@code
 *  DateTimeField dateTimeField = new DateTimeField(...) {
 *    protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel)
 *    {
 *      DateTextField dateField = super.newDateTextField(id, dateFieldModel);     
 *      dateField.add(new AjaxFormComponentUpdatingBehavior("change") {
 *        protected void onUpdate(AjaxRequestTarget target) {
 *          processInput(); // let DateTimeField process input too
 *
 *          ...
 *        }
 *      });
 *      return recorder;
 *    }
 *  }
 * }</pre>
 * 
 * @author eelcohillenius
 * @see DateField for a variant with just the date field and date picker
 */
public class DateTimeField extends FormComponentPanel<ZonedDateTime>
{
	private static final long serialVersionUID = 1L;

	// Component-IDs
	protected static final String DATE = "date";
	protected static final String TIME = "time";

	// The date TextField and it's associated model object
	// Note that any time information in date will be ignored
	private DateField dateField;
	private TimeField timeField;
	private ZonedDateTime dateTime = ZonedDateTime.now();

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
	public DateTimeField(final String id, final IModel<ZonedDateTime> model)
	{
		super(id, model);

		// Sets the type that will be used when updating the model for this component.
		setType(ZonedDateTime.class);

		// Create and add the date TextField
		add(dateField = newDateField(DATE, new DateModel()));
		add(timeField = newTimeField(TIME, new TimeModel()));
	}

	/**
	 * 
	 * @return The date TextField
	 */
	protected final DateField getDateField()
	{
		return dateField;
	}

	/**
	 * 
	 * @return The date TextField
	 */
	protected final TimeField getTimeField()
	{
		return timeField;
	}

	@Override
	public String getInput()
	{
		// since we override convertInput, we can let this method return a value
		// that is just suitable for error reporting
		return String.format("%s, %s", dateField.getInput(), timeField.getInput());
	}

	/**
	 * Gets the client's time zone.
	 * 
	 * @return The client's time zone or null
	 */
	protected ZoneId getClientTimeZone()
	{
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo)
		{
			TimeZone timeZone = ((WebClientInfo) info).getProperties().getTimeZone();
			return timeZone != null ? timeZone.toZoneId() : null;
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
	public void convertInput()
	{
		try
		{
			// Get the converted input values
			LocalDate localDate = dateField.getConvertedInput();

			if (localDate == null)
			{
				return;
			}

			// Use the input to create a date object with proper timezone
			LocalTime localTime = timeField.getConvertedInput();
			ZonedDateTime date = ZonedDateTime.of(localDate, localTime, getClientTimeZone());

			// The date will be in the server's timezone
			setConvertedInput(date);
		}
		catch (RuntimeException e)
		{
			DateTimeField.this.error(e.getMessage());
			invalid();
		}
	}

	/**
	 * create a new {@link DateField} instance to be added to this panel.
	 * 
	 * @param id
	 *            the component id
	 * @param dateFieldModel
	 *            model that should be used by the {@link DateField}
	 * @return a new date text field instance
	 */
	protected DateField newDateField(String id, IModel<LocalDate> dateFieldModel)
	{
		return DateField.forShortStyle(id, dateFieldModel);
	}

	/**
	 * create a new {@link TimeField} instance to be added to this panel.
	 * 
	 * @param id
	 *            the component id
	 * @param timeFieldModel
	 *            model that should be used by the {@link TimeField}
	 * @return a new time text field instance
	 */
	protected TimeField newTimeField(String id, IModel<LocalTime> timeFieldModel)
	{
		return TimeField.forShortStyle(id, timeFieldModel);
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		dateField.setRequired(isRequired());
		timeField.setRequired(isRequired());

		ZonedDateTime modelObject = getModelObject();
		if (modelObject == null)
		{
			dateTime = null;
		}
		else
		{
			// convert date to the client's time zone if we have that info
			ZoneId zone = getClientTimeZone();
			if (zone != null)
			{
				modelObject = modelObject.withZoneSameInstant(zone);
			}
		}

		super.onBeforeRender();
	}

	protected class DateModel implements IModel<LocalDate>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public LocalDate getObject()
		{
			return dateTime.toLocalDate();
		}

		@Override
		public void setObject(LocalDate date)
		{
			dateTime = dateTime.with(ChronoField.YEAR, date.getYear());
			dateTime = dateTime.with(ChronoField.MONTH_OF_YEAR, date.getMonthValue());
			dateTime = dateTime.with(ChronoField.DAY_OF_YEAR, date.getDayOfMonth());
		}

		@Override
		public void detach()
		{
		}
	}

	protected class TimeModel implements IModel<LocalTime>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public LocalTime getObject()
		{
			return dateTime.toLocalTime();
		}

		@Override
		public void setObject(LocalTime time)
		{
			dateTime = dateTime.with(ChronoField.HOUR_OF_DAY, time.getHour());
			dateTime = dateTime.with(ChronoField.MINUTE_OF_HOUR, time.getMinute());
		}

		@Override
		public void detach()
		{
		}
	}
}
