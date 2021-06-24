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
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.util.Date;

import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.ConversionException;

/**
 * Works on a {@link java.time.temporal.Temporal} object, aggregating a {@link LocalDateTextField} and a {@link TimeField}.
 * <p>
 * <strong>Ajaxifying an AbstractDateTimeField</strong>:
 * If you want to update this component with an {@link AjaxFormComponentUpdatingBehavior}, you have to attach it
 * to the contained components by overriding {@link #newDateField(String, IModel)}:
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
 */
abstract class AbstractDateTimeField<T extends Temporal> extends FormComponentPanel<T>
{
	private static final long serialVersionUID = 1L;

	public static final String DATE_CSS_CLASS_KEY = CssUtils.key(AbstractDateTimeField.class, "date");

	public static final String TIME_CSS_CLASS_KEY = CssUtils.key(AbstractDateTimeField.class, "time");

	private LocalDateTextField localDateField;
	
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

		add(new Label("timeSeparator", new ResourceModel("AbstractDateTimeField.timeSeparator"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();

				timeField.configure();
				
				setVisible(timeField.isVisible());
			}
		});
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		add(localDateField = newDateField("date", new DateModel()));
		add(timeField = newTimeField("time", new TimeModel()));
	}

	/**
	 * 
	 * @return The date TextField
	 */
	protected final LocalDateTextField getDateField()
	{
		return localDateField;
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
		return String.format("%s, %s", localDateField.getInput(), timeField.getInput());
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
		// Get the converted input values
		LocalDate date = localDateField.getConvertedInput();
		LocalTime time = timeField.getConvertedInput();

		T temporal;
		if (date == null && time == null)
		{
			temporal = null;
		}
		else
		{
			if (date == null)
			{
				error(newValidationError(new ConversionException("Cannot create temporal without date").setTargetType(getType())));
				return;
			}
			if (time == null)
			{
				time = getDefaultTime();
				if (time == null) {
					error(newValidationError(new ConversionException("Cannot create temporal without time").setTargetType(getType())));
					return;
				}
			}
			
			// Use the input to create proper date-time
			temporal = createTemporal(date, time);
		}
		
		setConvertedInput(temporal);
	}

	/**
	 * Get a default time if none was entered.
	 * 
	 * @return {@code null} by default
	 */
	protected LocalTime getDefaultTime()
	{
		return null;
	}

	/**
	 * create a new {@link LocalDateTextField} instance to be added to this panel.
	 * 
	 * @param id
	 *            the component id
	 * @param dateFieldModel
	 *            model that should be used by the {@link LocalDateTextField}
	 * @return a new date text field instance
	 */
	protected LocalDateTextField newDateField(String id, IModel<LocalDate> dateFieldModel)
	{
		return new LocalDateTextField(id, dateFieldModel, FormatStyle.SHORT) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				
				tag.append("class", getString(DATE_CSS_CLASS_KEY), " ");
			}
		};
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
		return new TimeField(id, timeFieldModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				
				tag.append("class", getString(TIME_CSS_CLASS_KEY), " ");
			}
		};
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		localDateField.setRequired(isRequired());
		timeField.setRequired(isRequired());

		super.onBeforeRender();
	}

	/**
	 * Get the local date from the given temporal.
	 * 
	 * @param temporal
	 * @return local date
	 */
	protected abstract LocalDate getLocalDate(T temporal);

	/**
	 * Get the time from the given temporal.
	 * 
	 * @param temporal
	 * @return time
	 */
	protected abstract LocalTime getLocalTime(T temporal);

	/**
	 * Create the temporal object from date and time. 
	 * 
	 * @param date
	 * @param time
	 * @return
	 */
	protected abstract T createTemporal(LocalDate date, LocalTime time);

	private class DateModel implements IModel<LocalDate>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public LocalDate getObject()
		{
			T temporal = getModelObject();
			if (temporal == null) {
				return null;
			}
			
			return getLocalDate(temporal);
		}

		@Override
		public void setObject(LocalDate date)
		{
			// ignored
		}
	}

	private class TimeModel implements IModel<LocalTime>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public LocalTime getObject()
		{
			T temporal = getModelObject();
			if (temporal == null) {
				return null;
			}
			
			return getLocalTime(temporal);
		}

		@Override
		public void setObject(LocalTime time)
		{
			// ignored
		}
	}
}
