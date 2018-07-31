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

import org.apache.wicket.util.convert.ConversionException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link LocalTimeConverter}
 */
public class LocalTimeConverterTest
{
	@Test
	public void convertToString() {
		LocalTimeConverter converter = new LocalTimeConverter();
		String time = converter.convertToString(LocalTime.of(1, 2, 3), Locale.ENGLISH);
		assertEquals(time, "01:02:03");
	}

	@Test
	public void convertToObject() {
		LocalTimeConverter converter = new LocalTimeConverter();
		LocalTime time = converter.convertToObject("01:02:03", Locale.ENGLISH);
		assertEquals(time, LocalTime.of(1, 2, 3));
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
