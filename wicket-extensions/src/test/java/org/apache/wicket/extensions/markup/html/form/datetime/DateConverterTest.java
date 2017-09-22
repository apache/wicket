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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.extensions.markup.html.form.datetime.PatternDateConverter;
import org.apache.wicket.extensions.markup.html.form.datetime.StyleDateConverter;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.CalendarConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link DateConverter} and subclasses.
 * 
 * @author akiraly
 */
public class DateConverterTest
{
	/**
	 * WICKET-3598
	 */
	@Test
	public void testLocaleUsed()
	{
		Locale locale = Locale.GERMAN;

		StyleDateConverter styleDateConverter = new StyleDateConverter("F-", false);
		DateTimeFormatter styleFormatter = styleDateConverter.getFormat(locale);

		Assert.assertEquals(locale, styleFormatter.getLocale());

		PatternDateConverter patternDateConverter = new PatternDateConverter(
			styleDateConverter.getDatePattern(locale), false);
		DateTimeFormatter patternFormatter = patternDateConverter.getFormat(locale);

		Assert.assertEquals(locale, patternFormatter.getLocale());

		Calendar now = Calendar.getInstance();

		ZonedDateTime zNow = ZonedDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
		String actual = styleDateConverter.convertToString(zNow, locale);
		String expected = patternDateConverter.convertToString(zNow, locale);

		Assert.assertEquals(expected, actual);
	}

	/**
	 * WICKET-3658
	 */
	@Test
	public void testCalendarConverterWithDelegate()
	{
		Locale locale = Locale.GERMAN;

		Calendar input = Calendar.getInstance(locale);
		input.clear();
		input.set(2011, Calendar.MAY, 7);

		final StyleDateConverter styleDateConverter = new StyleDateConverter("F-", false);

		CalendarConverter calendarConverter = new CalendarConverter(new IConverter<Date>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Date convertToObject(String value, Locale locale) throws ConversionException {
				ZonedDateTime zd = styleDateConverter.convertToObject(value, locale);
				return zd == null ? null : Date.from(zd.toInstant());
			}

			@Override
			public String convertToString(Date value, Locale locale) {
				return styleDateConverter.convertToString(ZonedDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), locale);
			}
			
		});

		String expected = styleDateConverter.convertToString(ZonedDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault()), locale);
		String actual = calendarConverter.convertToString(input, locale);

		Assert.assertEquals(expected, actual);

		Calendar revert = calendarConverter.convertToObject(actual, locale);

		Assert.assertEquals(input, revert);
	}
}
