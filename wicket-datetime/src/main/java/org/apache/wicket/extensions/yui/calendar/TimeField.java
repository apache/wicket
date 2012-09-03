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
import java.util.Locale;

import org.apache.wicket.model.IModel;

/**
 * Works on a {@link java.util.Date} object. Displays a field for hours and a field for minutes, and
 * an AM/PM field. The format (12h/24h) of the hours field depends on the time format of this
 * {@link TimeField}'s {@link Locale}, as does the visibility of the AM/PM field (see
 * {@link TimeField#use12HourFormat}).
 * 
 * @author eelcohillenius
 * @see DateField for a variant with just the date field and date picker
 */
public class TimeField extends DateTimeField
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *      the component id
	 */
	public TimeField(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *      the component id
	 * @param model
	 *      the component's model
	 */
	public TimeField(String id, IModel<Date> model)
	{
		super(id, model);

		getDateTextField().setVisibilityAllowed(false);
	}

	@Override
	protected void convertInput()
	{
		Date modelObject = (Date)getDefaultModelObject();
		getDateTextField().setConvertedInput(modelObject != null ? modelObject : newDateInstance());
		super.convertInput();
	}

}