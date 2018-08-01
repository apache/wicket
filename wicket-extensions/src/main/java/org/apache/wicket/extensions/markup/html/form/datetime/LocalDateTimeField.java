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
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.wicket.model.IModel;

/**
 * Works on a {@link java.time.LocalDateTime} object. See {@link AbstractDateTimeField} for
 * further details.
 *  
 * @author eelcohillenius
 */
public class LocalDateTimeField extends AbstractDateTimeField<LocalDateTime>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public LocalDateTimeField(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public LocalDateTimeField(final String id, final IModel<LocalDateTime> model)
	{
		super(id, model);

		// Sets the type that will be used when updating the model for this component.
		setType(LocalDateTime.class);
	}

	@Override
	protected LocalDateTime createTemporal(LocalDate date, LocalTime time) {
		return LocalDateTime.of(date, time);
	}

	@Override
	protected LocalDate getLocalDate(LocalDateTime temporal)
	{
		return temporal.toLocalDate();
	}

	@Override
	protected LocalTime getLocalTime(LocalDateTime temporal)
	{
		return temporal.toLocalTime();
	}
}
