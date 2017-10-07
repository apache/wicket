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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;

/**
 * Converts to {@link java.time.ZonedDateTime}.
 */
public class ZonedDateTimeConverter extends AbstractJavaTimeConverter<ZonedDateTime>
{
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

	@Override
	protected Class<ZonedDateTime> getTargetType()
	{
		return ZonedDateTime.class;
	}

	@Override
	protected ZonedDateTime createTemporal(TemporalAccessor temporalAccessor)
	{
		return ZonedDateTime.from(temporalAccessor);
	}

	@Override
	protected DateTimeFormatter getDateTimeFormatter() {
		return DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault());
	}
}
