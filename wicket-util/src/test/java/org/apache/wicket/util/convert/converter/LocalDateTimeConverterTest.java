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

import java.time.LocalDateTime;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link LocalDateTimeConverter}
 */
public class LocalDateTimeConverterTest extends Assert
{
	@Test
	public void convertToString() {
		LocalDateTimeConverter converter = new LocalDateTimeConverter();
		String date = converter.convertToString(LocalDateTime.of(2016, 7, 11, 1, 2, 3), Locale.ENGLISH);
		assertThat(date, is(equalTo("Jul 11, 2016, 1:02:03 AM")));
	}

	@Test
	public void convertToObject() {
		LocalDateTimeConverter converter = new LocalDateTimeConverter();
		LocalDateTime date = converter.convertToObject("Jul 11, 2016, 1:02:03 AM", Locale.ENGLISH);
		assertThat(date, is(equalTo(LocalDateTime.of(2016, 7, 11, 1, 2, 3))));
	}
	
	@Test
	public void convertFails() {
		LocalDateTimeConverter converter = new LocalDateTimeConverter();

		try {
			converter.convertToObject("aaa", Locale.ENGLISH);
		} catch (ConversionException expected) {
		}
	}
}
