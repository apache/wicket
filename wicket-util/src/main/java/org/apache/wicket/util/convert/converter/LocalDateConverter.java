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
package org.apache.wicket.util.convert.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;

/**
 * Converts from Object to {@link LocalDate}.
 */
public class LocalDateConverter extends AbstractJavaTimeConverter<LocalDate>
{
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter SHORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

	@Override
	protected Class<LocalDate> getTargetType()
	{
		return LocalDate.class;
	}

	@Override
	protected LocalDate createTemporal(TemporalAccessor temporalAccessor)
	{
		return LocalDate.from(temporalAccessor);
	}

	@Override
	protected DateTimeFormatter getDateTimeFormatter() {
		return SHORT_DATE_TIME_FORMATTER;
	}
}
