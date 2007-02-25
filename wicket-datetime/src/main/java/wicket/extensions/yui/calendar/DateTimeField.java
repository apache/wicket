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
package wicket.extensions.yui.calendar;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import wicket.MarkupContainer;
import wicket.Session;
import wicket.datetime.markup.html.form.DateTextField;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.FormComponentPanel;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;
import wicket.model.PropertyModel;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;
import wicket.validation.validator.NumberValidator;

/**
 * Works on a {@link java.util.Date} object. Displays a date field and a date
 * picker, a field for hours and a field for minutes, and a AM/ PM field.
 * 
 * @author eelcohillenius
 * @see DateField for a variant with just the date field and date picker
 */
// TODO AM/PM should really be locale dependent; some locales have 12 hour
// systems with AM/PM, others have 24 hour systems
public class DateTimeField extends FormComponentPanel {

	/**
	 * Enumerated type for different ways of handling the render part of
	 * requests.
	 */
	private static enum AM_PM {

		AM, PM;
	}

	private static final long serialVersionUID = 1L;

	private AM_PM amOrPm = AM_PM.AM;

	private DropDownChoice amOrPmChoice;

	private MutableDateTime date;

	private DateTextField dateField;

	private Integer hours;

	private TextField hoursField;

	private Integer minutes;

	private TextField minutesField;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public DateTimeField(MarkupContainer parent, String id) {
		super(parent, id);
		init();
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public DateTimeField(MarkupContainer parent, String id, IModel model) {
		super(parent, id, model);
		init();
	}

	/**
	 * Gets amOrPm.
	 * 
	 * @return amOrPm
	 */
	public AM_PM getAmOrPm() {
		return amOrPm;
	}

	/**
	 * Gets date.
	 * 
	 * @return date
	 */
	public Date getDate() {
		return (date != null) ? date.toDate() : null;
	}

	/**
	 * Gets hours.
	 * 
	 * @return hours
	 */
	public Integer getHours() {
		return hours;
	}

	/**
	 * Gets minutes.
	 * 
	 * @return minutes
	 */
	public Integer getMinutes() {
		return minutes;
	}

	/**
	 * Sets amOrPm.
	 * 
	 * @param amOrPm
	 *            amOrPm
	 */
	public void setAmOrPm(AM_PM amOrPm) {
		this.amOrPm = amOrPm;
	}

	/**
	 * Sets date.
	 * 
	 * @param date
	 *            date
	 */
	public void setDate(Date date) {
		this.date = (date != null) ? new MutableDateTime(date) : null;
	}

	/**
	 * Sets hours.
	 * 
	 * @param hours
	 *            hours
	 */
	public void setHours(Integer hours) {
		this.hours = hours;
	}

	/**
	 * Sets minutes.
	 * 
	 * @param minutes
	 *            minutes
	 */
	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel() {

		dateField.updateModel();
		hoursField.updateModel();
		minutesField.updateModel();
		amOrPmChoice.updateModel();

		if (date != null) {

			try {
				TimeZone zone = getClientTimeZone();
				if (zone != null) {
					date.setZone(DateTimeZone.forTimeZone(zone));
				}

				if (hours != null) {
					date.set(DateTimeFieldType.hourOfHalfday(), hours
							.intValue());
					date.setMinuteOfHour((minutes != null) ? minutes.intValue()
							: 0);
				}
				if (amOrPm == AM_PM.PM) {
					date.set(DateTimeFieldType.halfdayOfDay(), 1);
				} else {
					date.set(DateTimeFieldType.halfdayOfDay(), 0);
				}

				// the date will be in the server's timezone
				Date d = date.toDate();
				setModelObject(d);

			} catch (RuntimeException e) {
				DateTimeField.this.error(e.getMessage());
				invalid();
			}

		} else {
			setModelObject(null);
		}
	}

	/**
	 * Gets the client's time zone.
	 * 
	 * @return The client's time zone or null
	 */
	private TimeZone getClientTimeZone() {
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo) {
			return ((WebClientInfo) info).getProperties().getTimeZone();
		}
		return null;
	}

	private void init() {

		setType(Date.class);
		dateField = DateTextField.forShortStyle(this, "date",
				new PropertyModel(this, "date"));
		dateField.add(new DatePicker());
		// add(new CalendarPopup("picker", dateField));
		hoursField = new TextField(this, "hours", new PropertyModel(this,
				"hours"), Integer.class);
		hoursField.add(NumberValidator.range(0, 12));
		minutesField = new TextField(this, "minutes", new PropertyModel(this,
				"minutes"), Integer.class);
		minutesField.add(NumberValidator.range(0, 60));
		amOrPmChoice = new DropDownChoice(this, "amOrPmChoice",
				new PropertyModel(this, "amOrPm"), Arrays
						.asList(AM_PM.values()));
	}

	/**
	 * @see wicket.Component#onAttach()
	 */
	protected void onAttach() {

		Date d = (Date) getModelObject();
		if (d != null) {
			date = new MutableDateTime(d);
		} else {
			date = null;
		}

		if (date != null) {

			// convert date to the client's time zone if we have that info
			TimeZone zone = getClientTimeZone();
			// instantiate with the previously set date
			if (zone != null) {
				date.setZone(DateTimeZone.forTimeZone(zone));
			}

			hours = new Integer(date.get(DateTimeFieldType.hourOfHalfday()));
			minutes = new Integer(date.getMinuteOfHour());
			amOrPm = (date.get(DateTimeFieldType.halfdayOfDay()) == 0) ? AM_PM.AM
					: AM_PM.PM;

			// we don't really have to reset the date field to the server's
			// timezone, as it's the same milis from EPOCH anyway, and toDate
			// will always get the Date object initialized for the time zone
			// of the server
		}

		super.onAttach();
	}
}
