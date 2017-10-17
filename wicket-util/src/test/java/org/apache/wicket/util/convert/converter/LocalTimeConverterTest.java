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

import java.time.LocalTime;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link LocalTimeConverter}
 */
public class LocalTimeConverterTest extends Assert
{
	@Test
	public void convertToString() {
		LocalTimeConverter converter = new LocalTimeConverter();
		String time = converter.convertToString(LocalTime.of(1, 2, 3), Locale.ENGLISH);
		assertThat(time, is(equalTo("01:02:03")));
	}

	@Test
	public void convertToObject() {
		LocalTimeConverter converter = new LocalTimeConverter();
		LocalTime time = converter.convertToObject("01:02:03", Locale.ENGLISH);
		assertThat(time, is(equalTo(LocalTime.of(1, 2, 3))));
	}
	
	@Test
	public void convertFails() {
		LocalTimeConverter converter = new LocalTimeConverter();

		try {
			converter.convertToObject("aaa", Locale.ENGLISH);
		} catch (ConversionException expected) {
		}
	}
}
