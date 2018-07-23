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

import java.time.LocalDate;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link LocalDateConverter}
 */
public class LocalDateConverterTest extends Assert
{
	@Test
	public void convertToString() {
		LocalDateConverter converter = new LocalDateConverter();
		String date = converter.convertToString(LocalDate.of(2016, 7, 11), Locale.ENGLISH);
		assertThat(date, is(equalTo("7/11/16")));
	}

	@Test
	public void convertToObject() {
		LocalDateConverter converter = new LocalDateConverter();
		LocalDate date = converter.convertToObject("7/11/16", Locale.ENGLISH);
		assertThat(date, is(equalTo(LocalDate.of(2016, 7, 11))));
	}
	
	@Test
	public void convertFails() {
		LocalDateConverter converter = new LocalDateConverter();

		try {
			converter.convertToObject("aaa", Locale.ENGLISH);
		} catch (ConversionException expected) {
		}
	}
}
