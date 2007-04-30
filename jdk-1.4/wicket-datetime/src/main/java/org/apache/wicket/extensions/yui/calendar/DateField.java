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

import java.util.Date;

import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.joda.time.MutableDateTime;


/**
 * Works on a {@link java.util.Date} object. Displays a date field and a
 * {@link CalendarPopup calendar popup}.
 * 
 * @author eelcohillenius
 */
public class DateField extends FormComponentPanel {

	private static final long serialVersionUID = 1L;

	private MutableDateTime date;

	private DateTextField dateField;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public DateField(String id) {
		super(id);
		init();
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public DateField(String id, IModel model) {
		super(id, model);
		init();
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
	 * Sets date.
	 * 
	 * @param date
	 *            date
	 */
	public void setDate(Date date) {
		this.date = (date != null) ? new MutableDateTime(date) : null;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel() {

		if (date != null) {
			Date d = date.toDate();
			setModelObject(d);
		} else {
			setModelObject(null);
		}
	}

	/**
	 * Initialize.
	 */
	private void init() {

		setType(Date.class);
		add(dateField = DateTextField.forShortStyle("date", new PropertyModel(
				this, "date")));
		dateField.add(new DatePicker());
	}

	/**
	 * @see org.apache.wicket.Component#onAttach()
	 */
	protected void onAttach() {

		Date d = (Date) getModelObject();
		if (d != null) {
			date = new MutableDateTime(d);
		} else {
			date = null;
		}

		super.onAttach();
	}
}
