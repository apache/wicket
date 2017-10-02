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
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

/**
 * Works on a {@link java.time.ZonedDateTime} object. Displays a date field and a DatePicker, a field
 * for hours and a field for minutes, and an AM/PM field. The format (12h/24h) of the hours field
 * depends on the time format of this {@link AbstractDateTimeField}'s {@link Locale}, as does the visibility
 * of the AM/PM field (see {@link AbstractDateTimeField#use12HourFormat}).
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
abstract class AbstractDateTimeField<T extends Temporal> extends FormComponentPanel<T>
{
	private static final long serialVersionUID = 1L;

	// Component-IDs
	protected static final String DATE = "date";
	protected static final String TIME = "time";

	// The date TextField and it's associated model object
	// Note that any time information in date will be ignored
	private DateField dateField;
	private TimeField timeField;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AbstractDateTimeField(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public AbstractDateTimeField(final String id, final IModel<T> model)
	{
		super(id, model);

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

			// The date will be in the server's timezone
			setConvertedInput(performConvert(localDate, localTime));
		}
		catch (RuntimeException e)
		{
			AbstractDateTimeField.this.error(e.getMessage());
			invalid();
		}
	}

	abstract T performConvert(LocalDate date, LocalTime time);

	abstract void prepareObject();

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

		prepareObject();

		super.onBeforeRender();
	}

	abstract LocalDate getLocalDate();
	abstract void setLocalDate(LocalDate date);
	abstract LocalTime getLocalTime();
	abstract void setLocalTime(LocalTime time);

	protected class DateModel implements IModel<LocalDate>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public LocalDate getObject()
		{
			return getLocalDate();
		}

		@Override
		public void setObject(LocalDate date)
		{
			setLocalDate(date);
		}
	}

	protected class TimeModel implements IModel<LocalTime>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public LocalTime getObject()
		{
			return getLocalTime();
		}

		@Override
		public void setObject(LocalTime time)
		{
			setLocalTime(time);
		}
	}
}
