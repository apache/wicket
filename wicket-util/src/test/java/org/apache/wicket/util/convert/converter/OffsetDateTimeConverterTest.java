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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link OffsetDateTimeConverter}
 */
public class OffsetDateTimeConverterTest extends Assert
{
	@Test
	public void convertToString() {
		OffsetDateTimeConverter converter = new OffsetDateTimeConverter();
		OffsetDateTime offsetDateTime = OffsetDateTime.of(2016, 7, 11, 1, 2, 3, 0, ZoneOffset.ofHours(2));
		String dateTime = converter.convertToString(offsetDateTime, Locale.ENGLISH);
		assertThat(dateTime, is(equalTo("2016-07-10T23:02:03Z")));
	}

	@Test
	public void convertToObject() {
		OffsetDateTimeConverter converter = new OffsetDateTimeConverter();
		OffsetDateTime dateTime = converter.convertToObject("2016-07-10T23:02:03+02:00", Locale.ENGLISH);
		OffsetDateTime offsetDateTime = OffsetDateTime.of(2016, 7, 11, 1, 2, 3, 0, ZoneOffset.ofHours(2));
		assertThat(dateTime, is(equalTo(offsetDateTime)));
	}
}
