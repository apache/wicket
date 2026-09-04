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

import org.apache.wicket.model.IModel;

/**
 * Works on a {@link java.time.ZonedDateTime} object. See {@link AbstractDateTimeField} for
 * further details.
 * 
 * @author eelcohillenius
 */
public class ZonedDateTimeField extends AbstractDateTimeField<ZonedDateTime>
{
	private static final long serialVersionUID = 1L;

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
	 * Creates a zoned date time in the systems default zone.
	 * 
	 * @see ZoneId#systemDefault()
	 */
	@Override
	protected ZonedDateTime createTemporal(LocalDate date, LocalTime time) {
		return ZonedDateTime.of(date, time, ZoneId.systemDefault());
	}

	@Override
	protected LocalDate getLocalDate(ZonedDateTime temporal)
	{
		return temporal.toLocalDate();
	}

	@Override
	protected LocalTime getLocalTime(ZonedDateTime temporal)
	{
		return temporal.toLocalTime();
	}
}
