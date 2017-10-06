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
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;

/**
 * Works on a {@link java.time.ZonedDateTimeTime} object. Displays a date field and a DatePicker, a field
 * for hours and a field for minutes, and an AM/PM field. The format (12h/24h) of the hours field
 * depends on the time format of this {@link ZonedDateTimeField}'s {@link Locale}, as does the visibility
 * of the AM/PM field (see {@link ZonedDateTimeField#use12HourFormat}).
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
public class ZonedDateTimeField extends AbstractDateTimeField<ZonedDateTime>
{
	private static final long serialVersionUID = 1L;

	private ZonedDateTime dateTime = ZonedDateTime.now();

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public ZonedDateTimeField(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public ZonedDateTimeField(final String id, final IModel<ZonedDateTime> model)
	{
		super(id, model);

		// Sets the type that will be used when updating the model for this component.
		setType(ZonedDateTime.class);
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

	ZonedDateTime performConvert(LocalDate date, LocalTime time) {
		return ZonedDateTime.of(date, time, getClientTimeZone());
	}

	@Override
	void prepareObject() {
		ZonedDateTime modelObject = getModelObject();
		if (modelObject != null)
		{
			// convert date to the client's time zone if we have that info
			ZoneId zone = getClientTimeZone();
			if (zone != null)
			{
				modelObject = modelObject.withZoneSameInstant(zone);
			}
		}
	}

	LocalDate getLocalDate()
	{
		return getModelObject() == null ? null : dateTime.toLocalDate();
	}

	void setLocalDate(LocalDate date)
	{
		dateTime = dateTime.with(ChronoField.YEAR, date.getYear())
				.with(ChronoField.MONTH_OF_YEAR, date.getMonthValue())
				.with(ChronoField.DAY_OF_YEAR, date.getDayOfMonth());
	}

	LocalTime getLocalTime()
	{
		return getModelObject() == null ? null : dateTime.toLocalTime();
	}

	void setLocalTime(LocalTime time)
	{
		dateTime = dateTime.with(ChronoField.HOUR_OF_DAY, time.getHour())
				.with(ChronoField.MINUTE_OF_HOUR, time.getMinute());
	}
}
