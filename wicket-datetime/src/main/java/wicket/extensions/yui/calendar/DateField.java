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

import java.util.Date;

import org.joda.time.MutableDateTime;

import wicket.MarkupContainer;
import wicket.datetime.markup.html.form.DateTextField;
import wicket.markup.html.form.FormComponentPanel;
import wicket.model.IModel;
import wicket.model.PropertyModel;

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
	 * @param parent
	 *            The parent
	 * @param id
	 */
	public DateField(MarkupContainer parent, String id) {
		super(parent, id);
		init();
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 * @param model
	 */
	public DateField(MarkupContainer parent, String id, IModel model) {
		super(parent, id, model);
		init();
	}

	/**
	 * Gets date.
	 * 
	 * @param parent
	 *            The parent
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
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel() {

		dateField.updateModel();

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
		dateField = DateTextField.forShortStyle(this, "date",
				new PropertyModel(this, "date"));
		dateField.add(new DatePicker());
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

		super.onAttach();
	}
}
